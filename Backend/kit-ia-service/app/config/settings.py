from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Configuración centralizada leída de variables de entorno / .env.

    Sin valores sensibles por defecto: las credenciales reales llegan por env.
    """

    # ─── LLM ──────────────────────────────────────────────────────────────
    # provider: "cloud" (DeepSeek/Groq/OpenAI u otro compatible) o "ollama".
    # Las vars LLM_* apuntan al proveedor cloud sea cual sea (todos hablan
    # el formato OpenAI; solo cambia base_url + model + key).
    llm_provider: str = "ollama"
    llm_api_key: str = ""
    llm_base_url: str = "https://api.deepseek.com"
    llm_model: str = "deepseek-chat"
    ollama_base_url: str = "http://ollama:11434/v1"
    ollama_model: str = "llama3.2:3b"

    # ─── Embeddings ───────────────────────────────────────────────────────
    embeddings_model: str = "paraphrase-multilingual-MiniLM-L12-v2"

    # ─── Spring ms catalog ────────────────────────────────────────────────
    # Lectura del catálogo (productos) y escritura (crear kits) por HTTP.
    # kit-ia NO accede a la BD de catalog directamente (db-per-service).
    ms_kits_url: str = "http://localhost:8081"

    # ─── Spring ms users (nombre real del afectado) ───────────────────────
    users_url: str = "http://localhost:8083"

    # ─── Service discovery (Eureka) ───────────────────────────────────────
    eureka_server_url: str = ""
    app_name: str = "kit-ia-service"
    app_host: str = "localhost"
    app_port: int = 8001

    # ─── App ──────────────────────────────────────────────────────────────
    log_level: str = "INFO"

    # ─── Umbrales y reglas de negocio ─────────────────────────────────────
    similarity_threshold_high: float = 0.75
    similarity_threshold_low: float = 0.50
    max_chat_turns: int = 3
    max_questions_per_turn: int = 2
    min_products_per_kit: int = 6
    session_ttl_minutes: int = 30

    model_config = SettingsConfigDict(
        env_file=".env",
        case_sensitive=False,
        extra="ignore",
    )


settings = Settings()
