"""Cliente LLM agnóstico al proveedor.

El resto del código solo usa `llm_client.completar(...)`; no sabe si detrás
está DeepSeek u Ollama. Ambos hablan el formato OpenAI, cambia la base_url.
"""
import logging

from openai import AsyncOpenAI
from tenacity import retry, stop_after_attempt, wait_exponential

from app.config.settings import settings

logger = logging.getLogger(__name__)


class LLMClient:
    def __init__(self) -> None:
        # api_key no puede ser vacío para el SDK; se usa placeholder en Ollama.
        self._cloud = AsyncOpenAI(
            api_key=settings.llm_api_key or "not-set",
            base_url=settings.llm_base_url,
        )
        self._ollama = AsyncOpenAI(
            api_key="ollama",
            base_url=settings.ollama_base_url,
        )

    async def completar(self, messages: list[dict], system_prompt: str | None = None,
                        temperature: float = 0.6) -> str:
        msgs: list[dict] = []
        if system_prompt:
            msgs.append({"role": "system", "content": system_prompt})
        msgs.extend(messages)

        provider = settings.llm_provider.lower()
        try:
            if provider == "ollama":
                return await self._call(self._ollama, settings.ollama_model, msgs, temperature)
            return await self._call(self._cloud, settings.llm_model, msgs, temperature)
        except Exception as exc:
            # Fallback automático a Ollama si el proveedor cloud falla.
            if provider != "ollama":
                logger.warning("Fallback a Ollama activado", extra={"reason": str(exc)})
                return await self._call(self._ollama, settings.ollama_model, msgs, temperature)
            raise

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        reraise=True,
    )
    async def _call(self, client: AsyncOpenAI, model: str, msgs: list[dict],
                    temperature: float) -> str:
        resp = await client.chat.completions.create(
            model=model,
            messages=msgs,
            temperature=temperature,
        )
        return resp.choices[0].message.content or ""


llm_client = LLMClient()
