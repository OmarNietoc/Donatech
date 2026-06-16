"""Registro en Eureka (Netflix) para descubrimiento por nombre.

Si EUREKA_SERVER_URL está vacío, el registro se omite (modo standalone).
El fallo de registro no es fatal: se loggea y el servicio sigue operando.
"""
import logging

from app.config.settings import settings

logger = logging.getLogger(__name__)


async def registrar_en_eureka() -> bool:
    if not settings.eureka_server_url:
        logger.info("EUREKA_SERVER_URL vacío; registro en Eureka omitido")
        return False
    try:
        import py_eureka_client.eureka_client as eureka_client

        await eureka_client.init_async(
            eureka_server=settings.eureka_server_url,
            app_name=settings.app_name,
            instance_host=settings.app_host,
            instance_port=settings.app_port,
            health_check_url=(
                f"http://{settings.app_host}:{settings.app_port}/api/kit-ia/health"
            ),
        )
        logger.info("Registrado en Eureka como %s", settings.app_name)
        return True
    except Exception as exc:
        logger.warning("No se pudo registrar en Eureka", extra={"reason": str(exc)})
        return False


async def desregistrar_de_eureka() -> None:
    if not settings.eureka_server_url:
        return
    try:
        import py_eureka_client.eureka_client as eureka_client

        await eureka_client.stop_async()
        logger.info("Desregistrado de Eureka")
    except Exception as exc:
        logger.warning("Error al desregistrar de Eureka", extra={"reason": str(exc)})
