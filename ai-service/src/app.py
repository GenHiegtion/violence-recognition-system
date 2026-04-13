import logging
import time

from fastapi import FastAPI
from fastapi import Request

from src.api.routes.inference import router

app = FastAPI(title="Violence Recognition AI Service", version="0.1.0")

logger = logging.getLogger("uvicorn.error")


@app.on_event("startup")
async def on_startup() -> None:
    logger.info("[SERVICE-LIFECYCLE] ai-service started")


@app.on_event("shutdown")
async def on_shutdown() -> None:
    logger.info("[SERVICE-LIFECYCLE] ai-service stopping")


@app.middleware("http")
async def log_inbound_requests(request: Request, call_next):
    started_at = time.perf_counter()
    method = request.method
    path = request.url.path
    logger.info("[INBOUND] ai-service received %s %s", method, path)

    try:
        response = await call_next(request)
    except Exception:
        duration_ms = int((time.perf_counter() - started_at) * 1000)
        logger.exception(
            "[INBOUND] ai-service failed %s %s durationMs=%s",
            method,
            path,
            duration_ms,
        )
        raise

    duration_ms = int((time.perf_counter() - started_at) * 1000)
    logger.info(
        "[INBOUND] ai-service completed %s %s status=%s durationMs=%s",
        method,
        path,
        response.status_code,
        duration_ms,
    )
    return response


app.include_router(router)
