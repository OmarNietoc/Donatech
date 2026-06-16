"""Acceso de SOLO LECTURA a PostgreSQL (schema catalog).

Nunca ejecuta CREATE/ALTER/INSERT: el ms IA solo consulta el catálogo.
La escritura (crear kits) se delega al ms catalog vía HTTP.
"""
import logging
from contextlib import contextmanager

import psycopg2
from psycopg2.extras import RealDictCursor
from psycopg2.pool import ThreadedConnectionPool

from app.config.settings import settings

logger = logging.getLogger(__name__)

_pool: ThreadedConnectionPool | None = None


def init_pool() -> None:
    global _pool
    if _pool is not None:
        return
    _pool = ThreadedConnectionPool(
        minconn=1,
        maxconn=5,
        host=settings.db_host,
        port=settings.db_port,
        dbname=settings.db_name,
        user=settings.db_user,
        password=settings.db_password,
        options=f"-c search_path={settings.db_schema}",
    )
    logger.info("Pool PostgreSQL inicializado (schema=%s)", settings.db_schema)


def close_pool() -> None:
    global _pool
    if _pool is not None:
        _pool.closeall()
        _pool = None
        logger.info("Pool PostgreSQL cerrado")


@contextmanager
def _connection():
    if _pool is None:
        raise RuntimeError("El pool de PostgreSQL no está inicializado")
    conn = _pool.getconn()
    try:
        yield conn
    finally:
        _pool.putconn(conn)


def fetch_all(query: str, params: tuple = ()) -> list[dict]:
    with _connection() as conn:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(query, params)
            return [dict(row) for row in cur.fetchall()]


def ping() -> bool:
    try:
        fetch_all("SELECT 1")
        return True
    except Exception:
        logger.exception("Ping a PostgreSQL falló")
        return False
