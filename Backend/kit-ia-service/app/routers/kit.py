import logging

from fastapi import APIRouter, HTTPException, Request

from app.clients.spring_client import spring_client
from app.clients.users_client import obtener_nombre
from app.config.settings import settings
from app.core.embeddings import embeddings_engine
from app.core.session_store import session_store
from app.services.productos import product_cache
from app.schemas.kit_schemas import (
    ConfirmarRequest,
    ConfirmarResponse,
    ContextoInferido,
    GenerarRequest,
    GenerarResponse,
    IniciarSesionRequest,
    IniciarSesionResponse,
    MensajeRequest,
    MensajeResponse,
)
from app.services import chat as chat_svc
from app.services import contexto as contexto_svc
from app.services.generador import generar_kit

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/kit-ia", tags=["kit-ia"])


@router.post("/sesion/iniciar", response_model=IniciarSesionResponse)
async def iniciar_sesion(req: IniciarSesionRequest, request: Request):
    # Nombre autoritativo desde ms-users (no el correo). Cae al del request si falla.
    nombre_afectado = await obtener_nombre(
        request.headers.get("X-User-Id"), request.headers.get("X-User-Email")
    ) or req.nombre_afectado

    contexto = contexto_svc.inferir_contexto(req.titulo_campana, req.descripcion_campana)
    sesion = await session_store.crear(
        campana_id=req.campana_id,
        nombre_afectado=nombre_afectado,
        titulo_campana=req.titulo_campana,
        descripcion_campana=req.descripcion_campana,
        contexto=contexto,
    )
    mensaje, listo = await chat_svc.iniciar(sesion)
    logger.info(
        "Sesión iniciada",
        extra={"sesion_id": sesion.sesion_id, "campana_id": req.campana_id},
    )
    return IniciarSesionResponse(
        sesion_id=sesion.sesion_id,
        mensaje_asistente=mensaje,
        contexto_inferido=ContextoInferido(**contexto),
        datos_faltantes=contexto_svc.datos_faltantes(contexto),
        turno_actual=sesion.turno,
        listo_para_generar=listo,
    )


@router.post("/sesion/mensaje", response_model=MensajeResponse)
async def procesar_mensaje(req: MensajeRequest):
    sesion = await session_store.obtener(req.sesion_id)  # KeyError -> 404
    texto, listo = await chat_svc.procesar(sesion, req.mensaje_usuario)
    return MensajeResponse(
        mensaje_asistente=texto,
        contexto_actualizado=sesion.contexto,
        turno_actual=sesion.turno,
        listo_para_generar=listo,
    )


@router.post("/kit/generar", response_model=GenerarResponse)
async def generar(req: GenerarRequest):
    sesion = await session_store.obtener(req.sesion_id)
    kit = await generar_kit(sesion)
    sesion.kit_generado = kit
    return GenerarResponse(**kit)


@router.post("/kit/confirmar", response_model=ConfirmarResponse)
async def confirmar(req: ConfirmarRequest, request: Request):
    sesion = await session_store.obtener(req.sesion_id)

    # No confiar en los precios del cliente: recalcular desde el catálogo.
    precios = _precios_por_id([p.producto_id for p in req.productos])
    faltan = [p.producto_id for p in req.productos if p.producto_id not in precios]
    if faltan:
        raise HTTPException(
            status_code=400,
            detail=f"Productos inexistentes o sin stock: {faltan}",
        )

    items = [{"producto_id": p.producto_id, "cantidad": p.cantidad} for p in req.productos]
    monto_total = sum(precios[p.producto_id] * p.cantidad for p in req.productos)

    # Propaga la identidad que el gateway inyectó, para que catalog autorice.
    identidad = {
        h: request.headers[h]
        for h in ("X-User-Email", "X-User-Id", "X-User-Roles")
        if h in request.headers
    }

    try:
        kit_id, nombre_final = await spring_client.crear_kit_y_vincular(
            campana_id=req.campana_id,
            nombre=req.nombre_kit,
            descripcion=req.descripcion_kit or "",
            precio_estimado=monto_total,
            items=items,
            cantidad_necesaria=req.cantidad_necesaria,
            headers=identidad,
        )
    except Exception:
        logger.error("Error al confirmar kit", extra={"sesion_id": req.sesion_id}, exc_info=True)
        raise HTTPException(
            status_code=503,
            detail="No se pudo crear el kit en el servicio de catálogo. Intenta nuevamente.",
        )

    await session_store.eliminar(req.sesion_id)
    return ConfirmarResponse(
        kit_id=kit_id,
        campana_id=req.campana_id,
        nombre_kit=nombre_final,
        monto_total=monto_total,
    )


@router.delete("/sesion/{sesion_id}")
async def cerrar_sesion(sesion_id: str):
    await session_store.eliminar(sesion_id)
    return {"mensaje": "Sesión eliminada"}


@router.get("/health")
async def health():
    return {
        "status": "UP" if embeddings_engine.cargado else "DEGRADED",
        "embeddings": "UP" if embeddings_engine.cargado else "DOWN",
        "llm_provider": settings.llm_provider,
    }


def _precios_por_id(ids: list[str]) -> dict[str, int]:
    """Precios autoritativos desde el catálogo cacheado (activos con stock).

    Misma fuente que el feed /api/products/active; no se confía en el cliente.
    """
    if not ids:
        return {}
    if not product_cache.products:
        product_cache.precompute()
    quiero = set(ids)
    return {
        p["id"]: p["precio"]
        for p in product_cache.products
        if p["id"] in quiero and p.get("precio") is not None
    }
