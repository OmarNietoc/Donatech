import asyncio
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.config.settings import settings
from app.core.embeddings import embeddings_engine
from app.core.eureka import desregistrar_de_eureka, registrar_en_eureka
from app.core.session_store import session_store
from app.exceptions.handlers import registrar_handlers
from app.routers import kit

logging.basicConfig(
    level=settings.log_level.upper(),
    format="%(asctime)s %(levelname)s %(name)s %(message)s",
)
logger = logging.getLogger(__name__)

_CLEANUP_INTERVAL_SECONDS = 300


async def _job_limpieza_sesiones() -> None:
    while True:
        await asyncio.sleep(_CLEANUP_INTERVAL_SECONDS)
        try:
            await session_store.limpiar_expiradas()
        except Exception:
            logger.exception("Error en el job de limpieza de sesiones")


@asynccontextmanager
async def lifespan(app: FastAPI):
    embeddings_engine.load()
    try:
        from app.services.productos import product_cache
        product_cache.precompute()
    except Exception:
        logger.exception("No se pudieron precalcular embeddings de productos al iniciar")

    tarea_limpieza = asyncio.create_task(_job_limpieza_sesiones())
    await registrar_en_eureka()

    yield

    tarea_limpieza.cancel()
    await desregistrar_de_eureka()


app = FastAPI(
    title="Donatech · Kit IA Service",
    description="Microservicio de IA para generar kits de ayuda personalizados.",
    version="1.0.0",
    lifespan=lifespan,
)

registrar_handlers(app)
app.include_router(kit.router)
