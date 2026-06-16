"""Inferencia de contexto del afectado a partir de texto libre en español.

Heurística determinista (sin LLM) para extraer lo evidente del título/descripción
y de los mensajes del chat. Lo ambiguo lo pregunta el asistente.
"""
import re
import unicodedata

# Tipos normalizados (deben coincidir con las claves de CATEGORIA_FALLBACK en productos.py)
TIPOS = {
    "incendio": ["incendio", "fuego", "quema", "llamas", "forestal"],
    "terremoto": ["terremoto", "sismo", "temblor", "replica"],
    "inundacion": ["inundacion", "aluvion", "desborde", "crecida", "anegado", "lluvia"],
    "necesidad_cronica": ["cronica", "pobreza", "vulnerable", "discapacidad"],
}

NECESIDADES = {
    "vivienda": ["casa", "hogar", "vivienda", "techo"],
    "ropa": ["ropa", "vestimenta", "abrigo", "frazada"],
    "alimento": ["alimento", "comida", "hambre", "mercaderia"],
    "agua": ["agua", "potable", "sed"],
    "higiene": ["higiene", "aseo", "jabon"],
    "salud": ["medicamento", "remedio", "salud", "enferm", "herida", "cortada", "corte",
              "sangr", "fractura", "quemadura", "lesion", "golpe", "botiquin", "curacion"],
}

CONDICIONES = {
    "bebes": ["bebe", "guagua", "lactante", "panal"],
    "adultos_mayores": ["adulto mayor", "anciano", "abuelo", "abuela", "tercera edad"],
    "enfermedades": ["enfermo", "enferm", "cronico", "diabet", "hipertens"],
    "heridas": ["herida", "cortada", "corte", "sangr", "fractura", "quemadura", "lesion", "golpe"],
    "mascotas": ["mascota", "perro", "gato", "animal"],
}


def _norm(texto: str) -> str:
    texto = texto.lower()
    return "".join(
        c for c in unicodedata.normalize("NFD", texto)
        if unicodedata.category(c) != "Mn"
    )


def inferir_contexto(titulo: str, descripcion: str) -> dict:
    texto = _norm(f"{titulo} {descripcion}")

    tipo = None
    for nombre, claves in TIPOS.items():
        if any(_norm(k) in texto for k in claves):
            tipo = nombre
            break

    necesidades = [n for n, claves in NECESIDADES.items() if any(_norm(k) in texto for k in claves)]
    condiciones = [c for c, claves in CONDICIONES.items() if any(_norm(k) in texto for k in claves)]
    personas = _detectar_personas(texto)

    return {
        "tipo_catastrofe": tipo,
        "personas_detectadas": personas,
        "necesidades_detectadas": necesidades,
        "condiciones_especiales": condiciones,
    }


def _detectar_personas(texto: str) -> dict | None:
    match = re.search(r"(?:somos|familia de|grupo de|hogar de)\s+(\d{1,2})", texto)
    if not match:
        match = re.search(r"(\d{1,2})\s+personas", texto)
    total = int(match.group(1)) if match else None

    tiene_ninos = bool(re.search(r"\bnin[oa]s?\b|hijos?|menores|guagua", texto))
    tiene_adultos_mayores = "adulto mayor" in texto or "anciano" in texto or "abuel" in texto

    if total is None and not tiene_ninos and not tiene_adultos_mayores:
        return None
    return {
        "total": total,
        "tiene_ninos": tiene_ninos,
        "tiene_adultos_mayores": tiene_adultos_mayores,
    }


def datos_faltantes(contexto: dict) -> list[str]:
    faltan: list[str] = []
    personas = contexto.get("personas_detectadas")
    if not personas or not personas.get("total"):
        faltan.append("cantidad_personas")
    if not contexto.get("necesidades_detectadas"):
        faltan.append("necesidades")
    return faltan


def fusionar(contexto: dict, nuevo: dict) -> dict:
    """Fusiona datos recién extraídos en el contexto acumulado, sin perder lo previo."""
    if nuevo.get("tipo_catastrofe") and not contexto.get("tipo_catastrofe"):
        contexto["tipo_catastrofe"] = nuevo["tipo_catastrofe"]

    if nuevo.get("personas_detectadas"):
        actual = contexto.get("personas_detectadas") or {}
        merged = {**actual}
        for k, v in nuevo["personas_detectadas"].items():
            # no sobreescribir un total ya conocido con None; OR para flags booleanos
            if v:
                merged[k] = v if k == "total" else (merged.get(k) or v)
        contexto["personas_detectadas"] = merged

    for clave in ("necesidades_detectadas", "condiciones_especiales"):
        existentes = set(contexto.get(clave) or [])
        existentes.update(nuevo.get(clave) or [])
        contexto[clave] = sorted(existentes)

    return contexto
