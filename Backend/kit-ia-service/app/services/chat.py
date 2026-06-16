"""Lógica conversacional del asistente.

Gestiona historial, límite de turnos/preguntas y detección del token de cierre.
"""
import logging
import re

from app.config.settings import settings
from app.core.llm import llm_client
from app.services import contexto as contexto_svc

logger = logging.getLogger(__name__)

TOKEN_LISTO = "[LISTO_PARA_GENERAR]"
# Detección tolerante: con o sin corchetes, mayúsculas/minúsculas, espacios.
_TOKEN_RE = re.compile(r"\[?\s*LISTO_PARA_GENERAR\s*\]?", re.IGNORECASE)

SYSTEM_PROMPT_TEMPLATE = """Eres un asistente empático de Donatech, una plataforma chilena de donaciones
para personas afectadas por catástrofes y situaciones de vulnerabilidad.

Tu único objetivo es recopilar la información necesaria para armar un kit de
ayuda personalizado que realmente se ajuste a la situación de la persona.

## Contexto ya disponible (NO volver a preguntar esto):
- Nombre: {nombre}
- Situación declarada: {titulo}
- Descripción completa: {descripcion}
- Tipo de catástrofe inferido: {tipo_catastrofe}
- Datos ya extraídos: {datos_extraidos}

## Información que necesitas recopilar (solo si no está en el contexto):
1. Cantidad exacta de personas afectadas
2. Composición del grupo: adultos, niños (edades aproximadas), adultos mayores
3. Necesidades más urgentes o específicas no mencionadas
4. Condiciones especiales: bebés, personas con enfermedades, mascotas

## Reglas estrictas:
- NUNCA preguntes algo que ya puedas inferir del contexto disponible
- Máximo {max_preguntas} preguntas por respuesta
- Sé breve, cálido y directo. Esta persona está pasando por un momento muy difícil
- Habla en español chileno natural, tutea siempre al usuario
- El kit contiene ÚNICAMENTE PRODUCTOS físicos (alimentos, agua, abrigo, ropa, higiene,
  primeros auxilios, pañales, leche, artículos para mascotas, etc.).
- NUNCA ofrezcas ni preguntes por SERVICIOS (mudanza, transporte, arriendo, ayuda
  psicológica, trámites, mano de obra). Si la persona menciona un servicio, reconduce
  con empatía hacia los productos que podrían ayudarle.
- Orienta tus preguntas a identificar QUÉ PRODUCTOS necesita y para cuántas personas.
- Cuando tengas suficiente información, responde EXACTAMENTE con el token, sin nada más
  ni antes ni después: [LISTO_PARA_GENERAR]
"""


def construir_system_prompt(sesion) -> str:
    ctx = sesion.contexto
    return SYSTEM_PROMPT_TEMPLATE.format(
        nombre=sesion.nombre_afectado,
        titulo=sesion.titulo_campana,
        descripcion=sesion.descripcion_campana,
        tipo_catastrofe=ctx.get("tipo_catastrofe") or "no identificado",
        datos_extraidos=_resumen(ctx),
        max_preguntas=settings.max_questions_per_turn,
    )


def _resumen(ctx: dict) -> str:
    partes: list[str] = []
    personas = ctx.get("personas_detectadas") or {}
    if personas.get("total"):
        partes.append(f"{personas['total']} personas")
    if personas.get("tiene_ninos"):
        partes.append("hay niños")
    if personas.get("tiene_adultos_mayores"):
        partes.append("hay adultos mayores")
    if ctx.get("necesidades_detectadas"):
        partes.append("necesidades: " + ", ".join(ctx["necesidades_detectadas"]))
    if ctx.get("condiciones_especiales"):
        partes.append("condiciones: " + ", ".join(ctx["condiciones_especiales"]))
    return "; ".join(partes) if partes else "ninguno aún"


def _detectar_listo(respuesta: str) -> bool:
    return bool(_TOKEN_RE.search(respuesta))


def _limpiar(respuesta: str) -> str:
    return _TOKEN_RE.sub("", respuesta).strip()


async def iniciar(sesion) -> tuple[str, bool]:
    system_prompt = construir_system_prompt(sesion)
    instruccion = (
        "Saluda con empatía usando el nombre y la situación de la persona. "
        "Si falta información esencial (cantidad de personas o necesidades), "
        "haz las preguntas mínimas necesarias. Si ya tienes lo necesario, "
        "responde solo con el token."
    )
    respuesta = await llm_client.completar(
        [{"role": "user", "content": instruccion}], system_prompt=system_prompt
    )
    listo = _detectar_listo(respuesta)
    texto = _limpiar(respuesta)
    sesion.historial.append({"role": "assistant", "content": texto})
    sesion.listo_para_generar = listo
    return texto, listo


async def procesar(sesion, mensaje_usuario: str) -> tuple[str, bool]:
    sesion.historial.append({"role": "user", "content": mensaje_usuario})
    contexto_svc.fusionar(sesion.contexto, contexto_svc.inferir_contexto("", mensaje_usuario))
    sesion.turno += 1

    # Tope de turnos: forzar generación sin más preguntas.
    if sesion.turno > settings.max_chat_turns:
        texto = "Ya tengo lo necesario. Te prepararé una propuesta de kit a tu medida."
        sesion.historial.append({"role": "assistant", "content": texto})
        sesion.listo_para_generar = True
        return texto, True

    system_prompt = construir_system_prompt(sesion)
    respuesta = await llm_client.completar(sesion.historial, system_prompt=system_prompt)
    listo = _detectar_listo(respuesta)
    texto = _limpiar(respuesta)
    # Si el modelo solo emitió el token, da un cierre cálido en vez de texto vacío.
    if listo and not texto:
        texto = "Perfecto, con esto puedo armar tu kit. Generándolo…"
    sesion.historial.append({"role": "assistant", "content": texto})
    sesion.listo_para_generar = listo
    return texto, listo
