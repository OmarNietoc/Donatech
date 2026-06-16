from pydantic import BaseModel, ConfigDict, Field


class _ReqBase(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True)


# ─── Iniciar sesión ──────────────────────────────────────────────────────
class IniciarSesionRequest(_ReqBase):
    campana_id: int = Field(..., gt=0, description="ID de la campaña del afectado")
    nombre_afectado: str = Field(..., min_length=1, description="Nombre del afectado")
    titulo_campana: str = Field(..., min_length=1, description="Título de la campaña")
    descripcion_campana: str = Field(
        ..., min_length=20, description="Descripción (mínimo 20 caracteres para contexto útil)"
    )


class ContextoInferido(BaseModel):
    tipo_catastrofe: str | None = None
    personas_detectadas: dict | None = None
    necesidades_detectadas: list[str] = Field(default_factory=list)
    condiciones_especiales: list[str] = Field(default_factory=list)


class IniciarSesionResponse(BaseModel):
    sesion_id: str
    mensaje_asistente: str
    contexto_inferido: ContextoInferido
    datos_faltantes: list[str]
    turno_actual: int
    listo_para_generar: bool


# ─── Mensaje ──────────────────────────────────────────────────────────────
class MensajeRequest(_ReqBase):
    sesion_id: str = Field(..., min_length=1)
    mensaje_usuario: str = Field(..., min_length=1)


class MensajeResponse(BaseModel):
    mensaje_asistente: str
    contexto_actualizado: dict
    turno_actual: int
    listo_para_generar: bool


# ─── Generar kit ────────────────────────────────────────────────────────────
class GenerarRequest(_ReqBase):
    sesion_id: str = Field(..., min_length=1)


class ProductoKit(BaseModel):
    producto_id: str
    nombre: str
    cantidad: int
    precio_unitario: int
    subtotal: int


class GenerarResponse(BaseModel):
    nombre_kit: str
    descripcion_kit: str
    tipo: str = "USO_UNICO"
    campana_id: int
    productos: list[ProductoKit]
    monto_total: int
    moneda: str = "CLP"


# ─── Confirmar kit ──────────────────────────────────────────────────────────
class ProductoConfirmar(_ReqBase):
    producto_id: str = Field(..., min_length=1)
    cantidad: int = Field(..., gt=0)


class ConfirmarRequest(_ReqBase):
    sesion_id: str = Field(..., min_length=1)
    nombre_kit: str = Field(..., min_length=3, max_length=120)
    descripcion_kit: str | None = Field(default=None, max_length=1000)
    campana_id: int = Field(..., gt=0)
    cantidad_necesaria: int = Field(default=1, gt=0, description="Meta: cuántos kits necesita")
    productos: list[ProductoConfirmar] = Field(..., min_length=1)


class ConfirmarResponse(BaseModel):
    kit_id: int
    campana_id: int
    nombre_kit: str
    monto_total: int
    estado: str = "CREADO"
    tipo: str = "USO_UNICO"
