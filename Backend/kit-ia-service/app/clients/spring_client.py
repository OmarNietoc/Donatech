"""Cliente HTTP hacia el ms catalog (Spring Boot) para crear el kit.

Único punto que escribe en el dominio: crea el kit y lo vincula a la campaña.
Patrón saga con compensación: si el vínculo falla, se borra el kit creado.
"""
import logging

import httpx
from tenacity import retry, retry_if_exception_type, stop_after_attempt, wait_exponential

from app.config.settings import settings

logger = logging.getLogger(__name__)

_retry = retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type((httpx.TimeoutException, httpx.ConnectError)),
    reraise=True,
)


class SpringKitsClient:
    def __init__(self) -> None:
        self._base = settings.ms_kits_url.rstrip("/")

    @_retry
    async def _post(self, client: httpx.AsyncClient, path: str, json: dict) -> httpx.Response:
        resp = await client.post(f"{self._base}{path}", json=json)
        resp.raise_for_status()
        return resp

    @_retry
    async def _delete(self, client: httpx.AsyncClient, path: str) -> httpx.Response:
        resp = await client.delete(f"{self._base}{path}")
        resp.raise_for_status()
        return resp

    _NOMBRE_BASE = "Kit Personalizado"

    async def _siguiente_nombre(self, client: httpx.AsyncClient, campana_id: int) -> str:
        """'Kit Personalizado', luego 'Kit Personalizado 2', 3, ... por campaña."""
        try:
            resp = await client.get(f"{self._base}/api/campaigns/{campana_id}")
            resp.raise_for_status()
            kits = resp.json().get("kits") or []
            existentes = sum(
                1 for k in kits
                if (k.get("kitNombre") or "").strip().startswith(self._NOMBRE_BASE)
            )
        except Exception:
            existentes = 0
        return self._NOMBRE_BASE if existentes == 0 else f"{self._NOMBRE_BASE} {existentes + 1}"

    async def crear_kit_y_vincular(self, campana_id: int, nombre: str, descripcion: str,
                                   precio_estimado: int, items: list[dict],
                                   cantidad_necesaria: int = 1,
                                   headers: dict | None = None) -> tuple[int, str]:
        # El nombre de los kits personalizados se estandariza y numera por campaña,
        # ignorando el que venga del cliente.
        payload_kit = {
            "descripcion": descripcion,
            "activo": 1,
            "tipo": "USO_UNICO",
            "precioEstimado": precio_estimado,
            "items": [
                {"productId": it["producto_id"], "cantidadRequerida": it["cantidad"]}
                for it in items
            ],
        }
        # Se reenvía la identidad del usuario (inyectada por el gateway) para que
        # catalog autorice la creación del kit personalizado y el vínculo a campaña.
        async with httpx.AsyncClient(timeout=10.0, headers=headers or {}) as client:
            nombre_final = await self._siguiente_nombre(client, campana_id)
            payload_kit["nombre"] = nombre_final
            resp = await self._post(client, "/api/kits/personalized", payload_kit)
            kit_id = resp.json().get("id")
            if kit_id is None:
                raise RuntimeError("ms-catalog no devolvió el id del kit creado")

            try:
                await self._post(
                    client,
                    f"/api/campaigns/{campana_id}/kits",
                    {"kitId": kit_id, "cantidadNecesaria": cantidad_necesaria},
                )
            except Exception:
                logger.error(
                    "Fallo al vincular kit %s a campaña %s; compensando con DELETE",
                    kit_id, campana_id, exc_info=True,
                )
                try:
                    await self._delete(client, f"/api/kits/{kit_id}")
                except Exception:
                    logger.error("Compensación falló: el kit %s quedó huérfano", kit_id)
                raise

            logger.info("Kit %s ('%s') creado y vinculado a campaña %s",
                        kit_id, nombre_final, campana_id)
            return kit_id, nombre_final


spring_client = SpringKitsClient()
