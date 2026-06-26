import numpy as np

from app.clients import catalog_client as catalog_mod
from app.core import embeddings as emb_mod
from app.services import productos as productos_mod
from app.services.productos import ProductCache

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


# Shape del feed de catalog (ProductResponseDto): la categoría llega como
# 'categoriaNombre'; ProductCache la normaliza a 'categoria'.
_PRODUCTS = [
    {"id": "P1", "nombre": "Frazada abrigo polar", "descripcion": "frazada", "precio": 8500,
     "stock": 10, "prioridad": "ALTO", "categoriaNombre": "abrigo"},
    {"id": "P2", "nombre": "Bidón de agua 5L", "descripcion": "agua potable", "precio": 2000,
     "stock": 50, "prioridad": "CRITICO", "categoriaNombre": "agua"},
    {"id": "P3", "nombre": "Caja de alimento no perecible", "descripcion": "alimento", "precio": 12000,
     "stock": 30, "prioridad": "ALTO", "categoriaNombre": "alimento"},
    {"id": "P4", "nombre": "Kit de higiene básico", "descripcion": "higiene aseo", "precio": 4000,
     "stock": 20, "prioridad": "MEDIO", "categoriaNombre": "higiene"},
    {"id": "P5", "nombre": "Carpa familiar", "descripcion": "carpa refugio", "precio": 35000,
     "stock": 5, "prioridad": "ALTO", "categoriaNombre": "carpa"},
    {"id": "P6", "nombre": "Set de ropa abrigada", "descripcion": "ropa abrigo", "precio": 9000,
     "stock": 15, "prioridad": "MEDIO", "categoriaNombre": "ropa"},
    {"id": "P7", "nombre": "Linterna LED", "descripcion": "iluminacion", "precio": 3000,
     "stock": 40, "prioridad": "BAJO", "categoriaNombre": "otros"},
    {"id": "P8", "nombre": "Botiquín primeros auxilios", "descripcion": "salud", "precio": 7000,
     "stock": 12, "prioridad": "ALTO", "categoriaNombre": "salud"},
]


def _cache(monkeypatch):
    monkeypatch.setattr(emb_mod.embeddings_engine, "encode", _fake_encode)
    monkeypatch.setattr(productos_mod.embeddings_engine, "encode", _fake_encode)
    monkeypatch.setattr(
        catalog_mod.catalog_client, "obtener_productos_activos", lambda: list(_PRODUCTS)
    )
    cache = ProductCache()
    cache.precompute()
    return cache


def test_normaliza_categoria_del_feed(monkeypatch):
    # 'categoriaNombre' del feed catalog debe normalizarse a 'categoria'.
    cache = _cache(monkeypatch)
    assert all("categoria" in p for p in cache.products)
    assert cache.products[0]["categoria"] == "abrigo"


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
    monkeypatch.setattr(
        catalog_mod.catalog_client, "obtener_productos_activos", lambda: []
    )
    cache = ProductCache()
    cache.precompute()
    assert cache.buscar_relevantes("incendio", tipo_catastrofe="incendio") == []
