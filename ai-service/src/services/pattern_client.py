import logging
from typing import Dict

import httpx

from src.core.settings import PATTERN_SERVICE_URL

logger = logging.getLogger("uvicorn.error")


async def fetch_thresholds() -> Dict[str, float]:
    try:
        logger.info("[OUTBOUND] ai-service calling pattern-service GET /api/patterns/thresholds")
        async with httpx.AsyncClient(timeout=2.5) as client:
            response = await client.get(f"{PATTERN_SERVICE_URL}/api/patterns/thresholds")
            response.raise_for_status()
            payload = response.json()
            if isinstance(payload, dict):
                logger.info("[OUTBOUND] ai-service received %s thresholds from pattern-service", len(payload))
                return {
                    str(key).lower(): max(0.0, min(1.0, float(value)))
                    for key, value in payload.items()
                }
    except Exception as exc:
        logger.warning("[OUTBOUND] ai-service failed fetching thresholds, using defaults: %s", exc)
        return {}
    return {}
