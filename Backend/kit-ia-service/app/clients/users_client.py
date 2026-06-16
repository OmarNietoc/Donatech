"""Cliente HTTP de solo lectura al ms users para obtener el nombre real del afectado.

El endpoint /api/users/internal/** es interno y abierto en la red (permitAll en users),
igual que lo consume el ms catalog.
"""
import logging

import httpx

from app.config.settings import settings

logger = logging.getLogger(__name__)


async def obtener_nombre(user_id: str | None, email: str | None) -> str | None:
    """Devuelve el nombre registrado del usuario (por id, o por email como respaldo)."""
    base = settings.users_url.rstrip("/")
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            if user_id and str(user_id).isdigit():
                resp = await client.get(f"{base}/api/users/internal/by-id/{user_id}")
            elif email:
                resp = await client.get(
                    f"{base}/api/users/internal/by-email", params={"email": email}
                )
            else:
                return None
            resp.raise_for_status()
            nombre = (resp.json() or {}).get("name")
            return nombre.strip() if isinstance(nombre, str) and nombre.strip() else None
    except Exception as exc:
        logger.warning("No se pudo obtener el nombre del usuario", extra={"reason": str(exc)})
        return None
