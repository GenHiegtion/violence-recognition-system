import logging
from typing import Literal

from fastapi import APIRouter

from src.core.settings import DEFAULT_VIOLENCE_THRESHOLD
from src.schemas.inference import InferenceRequest
from src.schemas.inference import InferenceResponse
from src.services.model_inference import ModelUnavailableError
from src.services.model_inference import get_engine
from src.services.model_inference import heuristic_score
from src.services.pattern_client import fetch_thresholds

router = APIRouter()
logger = logging.getLogger("uvicorn.error")


@router.get("/health")
def health() -> dict:
    return {"status": "ok", "service": "ai-inference-service"}


@router.get("/health/ai")
def health_ai() -> dict:
    return {"status": "ok", "service": "ai-inference-service"}


@router.post("/api/ai/infer", response_model=InferenceResponse)
async def infer(request: InferenceRequest) -> InferenceResponse:
    logger.info(
        "[OUTBOUND] ai-service infer started source_id=%s pattern_code=%s frames=%s model_pt=%s model_name=%s",
        request.source_id,
        request.pattern_code,
        request.frames_count,
        request.model_pt,
        request.model_name,
    )

    thresholds = await fetch_thresholds()
    threshold = thresholds.get(request.pattern_code.lower(), DEFAULT_VIOLENCE_THRESHOLD)

    score_source: Literal["request", "movinet", "heuristic"]
    model_name: str | None = None
    note: str | None = None

    if request.violence_score is not None:
        score = request.violence_score
        score_source = "request"
    elif request.video_path:
        try:
            engine = get_engine(checkpoint_path=request.model_pt, model_name=request.model_name)
            output = engine.predict_video(request.video_path, request.frames_count)
            score = output.score
            model_name = output.model_name
            score_source = "movinet"
        except (ModelUnavailableError, FileNotFoundError, ValueError) as exc:
            score = heuristic_score(request.frames_count)
            score_source = "heuristic"
            note = f"MoViNet fallback: {exc}"
            logger.warning("[OUTBOUND] ai-service MoViNet fallback source_id=%s reason=%s", request.source_id, exc)
    else:
        score = heuristic_score(request.frames_count)
        score_source = "heuristic"
        note = "No video_path provided."

    label: Literal["Violence", "Non-violence"] = "Violence" if score >= threshold else "Non-violence"

    logger.info(
        "[OUTBOUND] ai-service infer completed source_id=%s label=%s score=%.6f threshold=%.6f source=%s",
        request.source_id,
        label,
        score,
        threshold,
        score_source,
    )

    return InferenceResponse(
        source_id=request.source_id,
        pattern_code=request.pattern_code,
        threshold=threshold,
        score=score,
        label=label,
        score_source=score_source,
        model_name=model_name,
        note=note,
    )
