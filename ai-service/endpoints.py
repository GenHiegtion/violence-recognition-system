from typing import Literal

from fastapi import APIRouter

from model_inference import ModelUnavailableError, get_engine, heuristic_score
from pattern_client import fetch_thresholds
from schemas import InferenceRequest, InferenceResponse
from settings import DEFAULT_VIOLENCE_THRESHOLD

router = APIRouter()


@router.get("/health")
def health() -> dict:
    return {"status": "ok", "service": "ai-inference-service"}


@router.get("/health/ai")
def health_ai() -> dict:
    return {"status": "ok", "service": "ai-inference-service"}


@router.post("/api/ai/infer", response_model=InferenceResponse)
async def infer(request: InferenceRequest) -> InferenceResponse:
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
            engine = get_engine()
            output = engine.predict_video(request.video_path, request.frames_count)
            score = output.score
            model_name = output.model_name
            score_source = "movinet"
        except (ModelUnavailableError, FileNotFoundError, ValueError) as exc:
            score = heuristic_score(request.frames_count)
            score_source = "heuristic"
            note = f"MoViNet fallback: {exc}"
    else:
        score = heuristic_score(request.frames_count)
        score_source = "heuristic"
        note = "No video_path provided."

    label: Literal["Violence", "Non-violence"] = "Violence" if score >= threshold else "Non-violence"

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
