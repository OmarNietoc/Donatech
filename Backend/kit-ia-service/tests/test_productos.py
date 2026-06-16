import numpy as np

from app.core import db
from app.core import embeddings as emb_mod
from app.services import productos as productos_mod
from app.services.productos import ProductCache, _PRODUCTS_QUERY

# Vocabulario para embeddings deterministas en test (sin cargar el modelo real).
_VOCAB = ["incendio", "abrigo", "frazada", "agua", "alimento", "higiene", "carpa", "ropa"]


def _fake_encode(texts):
    vecs = []
    for t in texts:
        low = t.lower()
        v = np.array([1.0 if w in low else 0.0 for w in _VOCAB], dtype=float)
        if v.sum() == 0:
            v[0] = 1e-6  # evitar vector nulo
        v = v / np.linalg.norm(v)
        vecs.append(v)
    return np.array(vecs)


_PRODUCTS = [
    {"id": "P1", "nombre": "Frazada abrigo polar", "descripcion": "frazada", "precio": 8500,
     "stock": 10, "prioridad": "ALTO", "categoria": "abrigo"},
    {"id": "P2", "nombre": "Bidón de agua 5L", "descripcion": "agua potable", "precio": 2000,
     "stock": 50, "prioridad": "CRITICO", "categoria": "agua"},
    {"id": "P3", "nombre": "Caja de alimento no perecible", "descripcion": "alimento", "precio": 12000,
     "stock": 30, "prioridad": "ALTO", "categoria": "alimento"},
    {"id": "P4", "nombre": "Kit de higiene básico", "descripcion": "higiene aseo", "precio": 4000,
     "stock": 20, "prioridad": "MEDIO", "categoria": "higiene"},
    {"id": "P5", "nombre": "Carpa familiar", "descripcion": "carpa refugio", "precio": 35000,
     "stock": 5, "prioridad": "ALTO", "categoria": "carpa"},
    {"id": "P6", "nombre": "Set de ropa abrigada", "descripcion": "ropa abrigo", "precio": 9000,
     "stock": 15, "prioridad": "MEDIO", "categoria": "ropa"},
    {"id": "P7", "nombre": "Linterna LED", "descripcion": "iluminacion", "precio": 3000,
     "stock": 40, "prioridad": "BAJO", "categoria": "otros"},
    {"id": "P8", "nombre": "Botiquín primeros auxilios", "descripcion": "salud", "precio": 7000,
     "stock": 12, "prioridad": "ALTO", "categoria": "salud"},
]


def _cache(monkeypatch):
    monkeypatch.setattr(emb_mod.embeddings_engine, "encode", _fake_encode)
    monkeypatch.setattr(productos_mod.embeddings_engine, "encode", _fake_encode)
    monkeypatch.setattr(db, "fetch_all", lambda q, p=(): list(_PRODUCTS))
    cache = ProductCache()
    cache.precompute()
    return cache


def test_query_filtra_activo_y_stock():
    # La query nunca debe traer inactivos ni sin stock.
    assert "activo = 1" in _PRODUCTS_QUERY
    assert "stock > 0" in _PRODUCTS_QUERY


def test_filtrado_retorna_minimo_productos(monkeypatch):
    cache = _cache(monkeypatch)
    seleccion = cache.buscar_relevantes(
        "incendio abrigo frazada ropa", tipo_catastrofe="incendio", top_n=20
    )
    assert len(seleccion) >= 6


def test_ranking_incendio_prioriza_abrigo(monkeypatch):
    cache = _cache(monkeypatch)
    seleccion = cache.buscar_relevantes(
        "incendio abrigo frazada", tipo_catastrofe="incendio", top_n=20
    )
    top_ids = [p["id"] for p in seleccion[:2]]
    # P1 (frazada abrigo) debe estar entre los mejores rankeados
    assert "P1" in top_ids


def test_precompute_vacio_no_rompe(monkeypatch):
    monkeypatch.setattr(productos_mod.embeddings_engine, "encode", _fake_encode)
    monkeypatch.setattr(db, "fetch_all", lambda q, p=(): [])
    cache = ProductCache()
    cache.precompute()
    assert cache.buscar_relevantes("incendio", tipo_catastrofe="incendio") == []
