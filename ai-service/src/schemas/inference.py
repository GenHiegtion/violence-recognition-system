from typing import Literal

from pydantic import BaseModel
from pydantic import Field


class InferenceRequest(BaseModel):
    source_id: str | None = Field(default=None, description="Ingestion job id or camera id")
    pattern_code: str = Field(default="violence", min_length=2, max_length=100)
    frames_count: int = Field(default=32, ge=1, le=10000)
    violence_score: float | None = Field(default=None, ge=0.0, le=1.0)
    video_path: str | None = Field(
        default=None,
        description="Absolute/relative path to a local video file for MoViNet inference.",
    )
    model_pt: str | None = Field(
        default=None,
        description="Optional weight filename or path resolved by AI service (for example movinet_a0_violence.pt).",
    )
    model_name: str | None = Field(
        default=None,
        description="Optional MoViNet variant hint (for example A0).",
    )


class InferenceResponse(BaseModel):
    source_id: str | None
    pattern_code: str
    threshold: float
    score: float
    label: Literal["Violence", "Non-violence"]
    score_source: Literal["request", "movinet", "heuristic"]
    model_name: str | None = None
    note: str | None = None
