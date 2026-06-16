"""Manejo global de excepciones con formato de error estándar."""
import logging
from datetime import datetime, timezone

import httpx
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.config.settings import settings

logger = logging.getLogger(__name__)


def _payload(error: str, mensaje: str, detalle: str | None = None) -> dict:
    body = {
        "error": error,
        "mensaje": mensaje,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }
    # El detalle técnico solo se expone en desarrollo.
    if settings.log_level.upper() == "DEBUG" and detalle:
        body["detalle"] = detalle
    return body


def registrar_handlers(app: FastAPI) -> None:
    @app.exception_handler(ValueError)
    async def _value(request: Request, exc: ValueError):
        return JSONResponse(
            status_code=400,
            content=_payload("BAD_REQUEST", str(exc), str(exc)),
        )

    @app.exception_handler(KeyError)
    async def _key(request: Request, exc: KeyError):
        return JSONResponse(
            status_code=404,
            content=_payload("SESION_NO_ENCONTRADA",
                             "La sesión no existe o expiró.", str(exc)),
        )

    @app.exception_handler(httpx.TimeoutException)
    async def _timeout(request: Request, exc: httpx.TimeoutException):
        return JSONResponse(
            status_code=503,
            content=_payload("SERVICIO_NO_DISPONIBLE",
                             "Un servicio dependiente no respondió a tiempo.", str(exc)),
        )

    @app.exception_handler(httpx.HTTPError)
    async def _httpx(request: Request, exc: httpx.HTTPError):
        return JSONResponse(
            status_code=503,
            content=_payload("SERVICIO_NO_DISPONIBLE",
                             "Error comunicándose con un servicio dependiente.", str(exc)),
        )

    @app.exception_handler(Exception)
    async def _generic(request: Request, exc: Exception):
        logger.error("Error no controlado", exc_info=True)
        return JSONResponse(
            status_code=500,
            content=_payload("ERROR_INTERNO", "Ocurrió un error inesperado.", str(exc)),
        )
