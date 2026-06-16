"""Consulta del catálogo y filtrado de productos por relevancia semántica.

Solo SELECT sobre el schema catalog. Los embeddings de productos se precalculan
al arrancar y se cachean en memoria.
"""
import logging

import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

from app.config.settings import settings
from app.core import db
from app.core.embeddings import embeddings_engine

logger = logging.getLogger(__name__)

# Productos activos con stock disponible + nombre de categoría.
_PRODUCTS_QUERY = """
    SELECT p.id, p.nombre, p.descripcion, p.precio, p.stock, p.prioridad,
           COALESCE(c.name, '') AS categoria
    FROM products p
    LEFT JOIN categories c ON c.id = p.categoria_id
    WHERE p.activo = 1 AND p.stock > 0
"""

# Insumos específicos de curación de heridas (primeros auxilios).
PRIMEROS_AUXILIOS_KEYWORDS = [
    "botiquin", "primeros auxilios", "venda", "gasa", "curita", "aposito",
    "apósito", "antiseptico", "alcohol", "parche",
]

# Insumos de salud en general (superset; incluye primeros auxilios).
SALUD_KEYWORDS = PRIMEROS_AUXILIOS_KEYWORDS + [
    "desinfect", "analges", "medic", "salud", "suero", "termometro",
]

# Reglas base por tipo de catástrofe (fallback si la similitud es baja).
CATEGORIA_FALLBACK = {
    "incendio": ["abrigo", "ropa", "frazada", "agua", "alimento", "higiene", "calzado"],
    "terremoto": ["agua", "alimento", "carpa", "linterna", "frazada", "higiene", "botiquin"],
    "inundacion": ["agua", "bota", "frazada", "higiene", "alimento", "desinfectante"],
    "necesidad_cronica": ["alimento", "higiene", "agua", "abrigo"],
    "otro": ["agua", "alimento", "higiene", "abrigo", "frazada"],
}


class ProductCache:
    def __init__(self) -> None:
        self.products: list[dict] = []
        self.matrix: np.ndarray | None = None

    def precompute(self) -> None:
        rows = db.fetch_all(_PRODUCTS_QUERY)
        self.products = rows
        if not rows:
            logger.warning("No hay productos activos con stock para cachear")
            self.matrix = None
            return
        textos = [self._texto(r) for r in rows]
        self.matrix = embeddings_engine.encode(textos)
        logger.info("Embeddings de %d productos precalculados", len(rows))

    @staticmethod
    def _texto(row: dict) -> str:
        return (
            f"{row['nombre']}. {row.get('descripcion') or ''}. "
            f"Categoría: {row.get('categoria') or ''}"
        )

    def buscar_relevantes(self, contexto_texto: str, tipo_catastrofe: str | None = None,
                          top_n: int = 20) -> list[dict]:
        if not self.products or self.matrix is None:
            self.precompute()
        if not self.products or self.matrix is None:
            return []

        ctx_vec = embeddings_engine.encode([contexto_texto])
        scores = cosine_similarity(ctx_vec, self.matrix)[0]
        ranked = sorted(zip(self.products, scores), key=lambda x: x[1], reverse=True)

        seleccion = [
            dict(p, _score=float(s)) for p, s in ranked
            if s >= settings.similarity_threshold_low
        ]

        # Fallback por tipo de catástrofe si no se alcanza el mínimo.
        if len(seleccion) < settings.min_products_per_kit:
            claves = CATEGORIA_FALLBACK.get(
                tipo_catastrofe or "otro", CATEGORIA_FALLBACK["otro"]
            )
            ya_incluidos = {p["id"] for p in seleccion}
            for p, s in ranked:
                if p["id"] in ya_incluidos:
                    continue
                texto = self._texto(p).lower()
                if any(k in texto for k in claves):
                    seleccion.append(dict(p, _score=float(s)))
                    ya_incluidos.add(p["id"])
                if len(seleccion) >= top_n:
                    break

        return seleccion[:top_n]

    def buscar_por_palabras(self, claves: list[str]) -> list[dict]:
        """Productos cuyo NOMBRE o CATEGORÍA contiene alguna palabra clave.

        Se ignora la descripción a propósito: evita falsos positivos (p. ej. un
        analgésico cuya descripción dice "no mezclar con alcohol").
        """
        if not self.products:
            self.precompute()
        claves = [k.lower() for k in claves]
        return [
            p for p in self.products
            if any(k in f"{p['nombre']} {p.get('categoria', '')}".lower() for k in claves)
        ]


product_cache = ProductCache()
