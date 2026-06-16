import asyncio

import pytest

from app.core.session_store import SessionStore, Session
from app.services import chat as chat_svc


def _sesion(**over):
    base = dict(
        sesion_id="s1",
        campana_id=1,
        nombre_afectado="María",
        titulo_campana="Incendio",
        descripcion_campana="Incendio consumió mi hogar, necesito ayuda urgente para mi familia.",
        contexto={
            "tipo_catastrofe": "incendio",
            "personas_detectadas": {"total": 4, "tiene_ninos": True,
                                    "tiene_adultos_mayores": False},
            "necesidades_detectadas": ["ropa", "vivienda"],
            "condiciones_especiales": [],
        },
    )
    base.update(over)
    return Session(**base)


def test_detecta_token_listo(monkeypatch):
    async def fake_completar(messages, system_prompt=None, temperature=0.6):
        return "Listo para ayudarte. [LISTO_PARA_GENERAR]"

    monkeypatch.setattr(chat_svc.llm_client, "completar", fake_completar)
    sesion = _sesion()
    texto, listo = asyncio.run(chat_svc.procesar(sesion, "Somos 4"))
    assert listo is True
    assert "[LISTO_PARA_GENERAR]" not in texto


def test_supera_max_turnos_fuerza_generacion(monkeypatch):
    async def fake_completar(*a, **k):
        return "¿Algo más?"  # no debería llegar a llamarse

    monkeypatch.setattr(chat_svc.llm_client, "completar", fake_completar)
    sesion = _sesion(turno=3)  # ya en el último turno permitido
    texto, listo = asyncio.run(chat_svc.procesar(sesion, "ok"))
    assert listo is True


def test_sesion_no_encontrada_lanza_keyerror():
    store = SessionStore()
    with pytest.raises(KeyError):
        asyncio.run(store.obtener("inexistente"))


def test_sesion_expirada_es_limpiada():
    store = SessionStore()
    sesion = asyncio.run(store.crear(
        campana_id=1, nombre_afectado="X", titulo_campana="t",
        descripcion_campana="d" * 25, contexto={},
    ))
    sesion.last_activity = 0  # muy en el pasado
    eliminadas = asyncio.run(store.limpiar_expiradas())
    assert eliminadas == 1
    with pytest.raises(KeyError):
        asyncio.run(store.obtener(sesion.sesion_id))
