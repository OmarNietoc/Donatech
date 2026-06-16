from app.services import contexto as ctx


def test_extrae_tipo_incendio():
    c = ctx.inferir_contexto(
        "Perdí mi casa en incendio forestal",
        "Un incendio consumió mi hogar y necesitamos ropa y frazadas urgente.",
    )
    assert c["tipo_catastrofe"] == "incendio"
    assert "ropa" in c["necesidades_detectadas"]
    assert "vivienda" in c["necesidades_detectadas"]


def test_extrae_tipo_inundacion_y_personas():
    c = ctx.inferir_contexto(
        "Aluvión destruyó nuestro barrio",
        "Somos 4 personas, dos adultos y dos niños pequeños, nos quedamos sin nada.",
    )
    assert c["tipo_catastrofe"] == "inundacion"
    assert c["personas_detectadas"]["total"] == 4
    assert c["personas_detectadas"]["tiene_ninos"] is True


def test_descripcion_corta_o_vacia():
    c = ctx.inferir_contexto("", "")
    assert c["tipo_catastrofe"] is None
    assert c["personas_detectadas"] is None
    assert c["necesidades_detectadas"] == []


def test_datos_faltantes_cuando_falta_personas():
    c = ctx.inferir_contexto("Incendio", "Incendio en mi casa, perdí todo y necesito ayuda.")
    faltan = ctx.datos_faltantes(c)
    assert "cantidad_personas" in faltan


def test_fusionar_actualiza_personas_y_necesidades():
    base = ctx.inferir_contexto("Incendio forestal", "Incendio arrasó mi casa, necesito ropa.")
    nuevo = ctx.inferir_contexto("", "Somos 5 personas con un adulto mayor, falta agua.")
    fusion = ctx.fusionar(base, nuevo)
    assert fusion["personas_detectadas"]["total"] == 5
    assert fusion["personas_detectadas"]["tiene_adultos_mayores"] is True
    assert "agua" in fusion["necesidades_detectadas"]
    # el tipo original se conserva
    assert fusion["tipo_catastrofe"] == "incendio"
