"""Cliente HTTP de SOLO LECTURA hacia el ms catalog (Spring Boot).

Reemplaza el acceso directo a la BD de catalog: kit-ia consume el feed de
productos activos por HTTP, respetando db-per-service. Resiliencia con
reintentos; si catalog no responde tras 5 intentos se eleva una excepción
de dominio que el handler global traduce a HTTP 503.
"""
import logging

import httpx
from tenacity import (
    retry,
    retry_if_exception_type,
    stop_after_attempt,
    wait_exponential,
)

from app.config.settings import settings
from app.exceptions.errors import CatalogoNoDisponibleError

logger = logging.getLogger(__name__)

# Reintentar ante timeout, fallo de conexión o 5xx del catálogo.
_RETRYABLE = (httpx.TimeoutException, httpx.ConnectError, httpx.HTTPStatusError)

_retry = retry(
    stop=stop_after_attempt(5),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type(_RETRYABLE),
    reraise=True,
)


class CatalogClient:
    def __init__(self) -> None:
        self._base = settings.ms_kits_url.rstrip("/")

    @_retry
    def _get_active(self) -> list[dict]:
        with httpx.Client(timeout=10.0) as client:
            resp = client.get(f"{self._base}/api/products/active")
            resp.raise_for_status()  # 5xx -> HTTPStatusError -> reintento
            return resp.json()

    def obtener_productos_activos(self) -> list[dict]:
        """Productos activos con stock. Tras 5 intentos fallidos -> 503 de dominio."""
        try:
            return self._get_active()
        except httpx.HTTPError as exc:
            logger.error(
                "No se pudo obtener el catálogo tras 5 intentos: %s", exc
            )
            raise CatalogoNoDisponibleError(
                "No se pudo conectar con el servicio de catálogo. Intenta nuevamente."
            ) from exc


catalog_client = CatalogClient()
