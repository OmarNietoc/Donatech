"""Motor de embeddings (sentence-transformers).

El modelo se carga UNA sola vez al arrancar la app (lifespan), no por request.
"""
import logging

import numpy as np
from sentence_transformers import SentenceTransformer

from app.config.settings import settings

logger = logging.getLogger(__name__)


class EmbeddingsEngine:
    def __init__(self) -> None:
        self._model: SentenceTransformer | None = None

    def load(self) -> None:
        if self._model is None:
            logger.info("Cargando modelo de embeddings: %s", settings.embeddings_model)
            self._model = SentenceTransformer(settings.embeddings_model)

    @property
    def cargado(self) -> bool:
        return self._model is not None

    def encode(self, texts: list[str]) -> np.ndarray:
        if self._model is None:
            raise RuntimeError("El modelo de embeddings no está cargado")
        return self._model.encode(
            texts, convert_to_numpy=True, normalize_embeddings=True
        )


embeddings_engine = EmbeddingsEngine()
