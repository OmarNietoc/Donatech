"""Generación del kit: selección de productos, cantidades, nombre y monto.

Usa el LLM para seleccionar/nombrar, con fallback determinista basado en reglas
si el LLM no está disponible o devuelve datos inválidos.
"""
import json
import logging
import re

from app.config.settings import settings
from app.core.llm import llm_client
from app.services.productos import product_cache, SALUD_KEYWORDS, PRIMEROS_AUXILIOS_KEYWORDS

logger = logging.getLogger(__name__)

CONSUMIBLES = ("agua", "alimento", "comida", "higiene", "aseo", "leche", "panal", "pañal")
ABRIGO = ("frazada", "saco", "abrigo", "colchon", "colchón", "cama", "carpa")


def _contexto_a_texto(ctx: dict) -> str:
    partes: list[str] = []
    if ctx.get("tipo_catastrofe"):
        partes.append(f"Catástrofe: {ctx['tipo_catastrofe']}")
    personas = ctx.get("personas_detectadas") or {}
    if personas.get("total"):
        partes.append(f"{personas['total']} personas")
    if personas.get("tiene_ninos"):
        partes.append("con niños")
    if personas.get("tiene_adultos_mayores"):
        partes.append("con adultos mayores")
    if ctx.get("necesidades_detectadas"):
        partes.append("necesidades: " + ", ".join(ctx["necesidades_detectadas"]))
    if ctx.get("condiciones_especiales"):
        partes.append("condiciones: " + ", ".join(ctx["condiciones_especiales"]))
    return ". ".join(partes) or "kit de emergencia básico para una familia"


def _composicion_grupo(ctx: dict) -> tuple[int, int]:
    personas = ctx.get("personas_detectadas") or {}
    total = personas.get("total") or 1
    ninos = max(1, total // 2) if personas.get("tiene_ninos") else 0
    adultos = max(1, total - ninos)
    return adultos, ninos


def _cantidad(producto: dict, adultos: int, ninos: int) -> int:
    texto = f"{producto['nombre']} {producto.get('categoria', '')}".lower()
    total = adultos + ninos
    if any(k in texto for k in CONSUMIBLES):
        return max(1, total)
    if any(k in texto for k in ABRIGO):
        return adultos + (ninos + 1) // 2
    return 1


def _parse_json_array(raw: str) -> list:
    raw = raw.strip()
    match = re.search(r"\[.*\]", raw, re.DOTALL)
    if match:
        raw = match.group(0)
    return json.loads(raw)


async def _seleccionar_con_llm(ctx: dict, candidatos: list[dict],
                               adultos: int, ninos: int) -> list[dict]:
    if not candidatos:
        return []
    catalogo = "\n".join(
        f"- id={c['id']} | {c['nombre']} | cat={c.get('categoria')} | precio={c['precio']}"
        for c in candidatos
    )
    total = adultos + ninos
    instruccion = (
        f"Contexto: {_contexto_a_texto(ctx)}. "
        f"Grupo familiar: {adultos} adultos, {ninos} niños (total {total}). "
        f"Selecciona entre {settings.min_products_per_kit} y 12 productos del catálogo "
        "que mejor cubran las necesidades.\n"
        "REGLA DE CANTIDAD: 'cantidad' es el NÚMERO DE UNIDADES o PAQUETES del producto "
        "a incluir, acorde al tamaño del grupo. El tamaño/medida ya viene en el nombre "
        "(ej: 'Arroz 5kg', 'Papel 12 unidades') — NO uses ese número como cantidad. "
        "Para consumibles usa 1-2 unidades por persona; para artículos durables 1 por persona o por hogar.\n"
        'Devuelve SOLO un JSON array: [{"producto_id": "...", "cantidad": N}]. '
        "Sin texto adicional.\n\n"
        f"Catálogo disponible:\n{catalogo}"
    )
    try:
        raw = await llm_client.completar(
            [{"role": "user", "content": instruccion}], temperature=0.3
        )
        data = _parse_json_array(raw)
    except Exception as exc:
        logger.warning("Selección por LLM falló; se usará fallback", extra={"reason": str(exc)})
        return []

    techo = max(2 * total, 10)  # cota anti-extremos por si el LLM malinterpreta
    por_id = {c["id"]: c for c in candidatos}
    seleccion: list[dict] = []
    for item in data:
        pid = str(item.get("producto_id"))
        if pid in por_id:
            cantidad = item.get("cantidad") or 1
            try:
                cantidad = int(cantidad)
            except (TypeError, ValueError):
                cantidad = 1
            cantidad = max(1, min(cantidad, techo))
            seleccion.append(dict(por_id[pid], cantidad=cantidad))
    return seleccion


async def _nombrar_kit(ctx: dict, adultos: int, ninos: int) -> tuple[str, str]:
    tipo = ctx.get("tipo_catastrofe") or "emergencia"
    total = adultos + ninos
    prompt = (
        "Genera un nombre corto (máx 60 caracteres) y una descripción (1-2 frases, cálida) "
        f"para un kit de ayuda. Catástrofe: {tipo}. Grupo: {total} personas "
        f"({adultos} adultos, {ninos} niños). "
        'Devuelve SOLO JSON {"nombre": "...", "descripcion": "..."}.'
    )
    try:
        raw = await llm_client.completar([{"role": "user", "content": prompt}], temperature=0.5)
        match = re.search(r"\{.*\}", raw, re.DOTALL)
        data = json.loads(match.group(0)) if match else {}
        nombre = (data.get("nombre") or "").strip()
        descripcion = (data.get("descripcion") or "").strip()
        if nombre and descripcion:
            return nombre[:120], descripcion[:1000]
    except Exception as exc:
        logger.warning("Nombrado por LLM falló; se usará fallback", extra={"reason": str(exc)})

    nombre = f"Kit {tipo.capitalize()} - {total} personas"
    descripcion = (
        f"Kit de emergencia diseñado para {total} personas afectadas por {tipo}."
    )
    return nombre[:120], descripcion


async def generar_kit(sesion) -> dict:
    ctx = sesion.contexto
    contexto_texto = _contexto_a_texto(ctx)
    candidatos = product_cache.buscar_relevantes(
        contexto_texto, tipo_catastrofe=ctx.get("tipo_catastrofe"), top_n=20
    )
    if not candidatos:
        raise ValueError("No hay productos disponibles para generar un kit")

    adultos, ninos = _composicion_grupo(ctx)
    seleccion = await _seleccionar_con_llm(ctx, candidatos, adultos, ninos)

    # Completar hasta el mínimo con reglas deterministas.
    if len(seleccion) < settings.min_products_per_kit:
        ya_incluidos = {s["id"] for s in seleccion}
        for c in candidatos:
            if c["id"] in ya_incluidos:
                continue
            seleccion.append(dict(c, cantidad=_cantidad(c, adultos, ninos)))
            if len(seleccion) >= settings.min_products_per_kit:
                break

    # Si hay heridas exigir primeros auxilios específicos; si solo "salud", basta insumo de salud.
    condiciones = ctx.get("condiciones_especiales") or []
    hay_heridas = any(c in ("heridas", "enfermedades") for c in condiciones)
    necesita_salud = hay_heridas or "salud" in (ctx.get("necesidades_detectadas") or [])
    if necesita_salud:
        claves = PRIMEROS_AUXILIOS_KEYWORDS if hay_heridas else SALUD_KEYWORDS
        ya_incluidos = {s["id"] for s in seleccion}
        ya_cubierto = any(
            any(k in f"{s['nombre']} {s.get('categoria', '')}".lower() for k in claves)
            for s in seleccion
        )
        if not ya_cubierto:
            for p in product_cache.buscar_por_palabras(claves):
                if p["id"] not in ya_incluidos:
                    seleccion.append(dict(p, cantidad=1))
                    logger.info("Insumo de salud/primeros auxilios añadido por contexto de heridas")
                    break

    productos: list[dict] = []
    monto_total = 0
    for item in seleccion:
        subtotal = item["precio"] * item["cantidad"]
        monto_total += subtotal
        productos.append({
            "producto_id": item["id"],
            "nombre": item["nombre"],
            "cantidad": item["cantidad"],
            "precio_unitario": item["precio"],
            "subtotal": subtotal,
        })

    nombre, descripcion = await _nombrar_kit(ctx, adultos, ninos)
    return {
        "nombre_kit": nombre,
        "descripcion_kit": descripcion,
        "tipo": "USO_UNICO",
        "campana_id": sesion.campana_id,
        "productos": productos,
        "monto_total": monto_total,
        "moneda": "CLP",
    }
