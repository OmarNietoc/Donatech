"""Memoria de sesiones de chat en RAM, protegida con asyncio.Lock.

Las sesiones son efímeras por diseño: no se persisten en BD. Un job de limpieza
elimina las inactivas por más de SESSION_TTL_MINUTES.
"""
import asyncio
import logging
import time
import uuid
from dataclasses import dataclass, field

from app.config.settings import settings

logger = logging.getLogger(__name__)


@dataclass
class Session:
    sesion_id: str
    campana_id: int
    nombre_afectado: str
    titulo_campana: str
    descripcion_campana: str
    contexto: dict
    historial: list = field(default_factory=list)
    turno: int = 1
    listo_para_generar: bool = False
    kit_generado: dict | None = None
    created_at: float = field(default_factory=time.time)
    last_activity: float = field(default_factory=time.time)

    def touch(self) -> None:
        self.last_activity = time.time()


class SessionStore:
    def __init__(self) -> None:
        self._sessions: dict[str, Session] = {}
        self._lock = asyncio.Lock()

    async def crear(self, **kwargs) -> Session:
        sesion_id = str(uuid.uuid4())
        sesion = Session(sesion_id=sesion_id, **kwargs)
        async with self._lock:
            self._sessions[sesion_id] = sesion
        return sesion

    async def obtener(self, sesion_id: str) -> Session:
        async with self._lock:
            if sesion_id not in self._sessions:
                raise KeyError(sesion_id)
            sesion = self._sessions[sesion_id]
            sesion.touch()
            return sesion

    async def eliminar(self, sesion_id: str) -> None:
        async with self._lock:
            self._sessions.pop(sesion_id, None)

    async def limpiar_expiradas(self) -> int:
        ttl = settings.session_ttl_minutes * 60
        ahora = time.time()
        async with self._lock:
            expiradas = [
                sid for sid, s in self._sessions.items()
                if ahora - s.last_activity > ttl
            ]
            for sid in expiradas:
                del self._sessions[sid]
        if expiradas:
            logger.info("Sesiones expiradas limpiadas: %d", len(expiradas))
        return len(expiradas)


session_store = SessionStore()
