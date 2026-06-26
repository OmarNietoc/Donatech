"""Excepciones de dominio del ms kit-ia."""


class CatalogoNoDisponibleError(Exception):
    """El ms catalog no respondió tras agotar los reintentos.

    Se mapea a HTTP 503 en los handlers globales.
    """
