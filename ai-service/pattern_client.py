from typing import Dict

import httpx

from settings import PATTERN_SERVICE_URL


async def fetch_thresholds() -> Dict[str, float]:
    try:
        async with httpx.AsyncClient(timeout=2.5) as client:
            response = await client.get(f"{PATTERN_SERVICE_URL}/api/patterns/thresholds")
            response.raise_for_status()
            payload = response.json()
            if isinstance(payload, dict):
                return {
                    str(key).lower(): max(0.0, min(1.0, float(value)))
                    for key, value in payload.items()
                }
    except Exception:
        return {}
    return {}
