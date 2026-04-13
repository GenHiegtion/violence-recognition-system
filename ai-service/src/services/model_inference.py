from __future__ import annotations

from dataclasses import dataclass
from functools import lru_cache
from pathlib import Path

import cv2
import numpy as np
import torch

from src.core.settings import MOVINET_CHECKPOINT_PATH
from src.core.settings import MOVINET_DEVICE
from src.core.settings import MOVINET_IMAGE_SIZE
from src.core.settings import MOVINET_MODEL_NAME
from src.core.settings import MOVINET_NUM_FRAMES
from src.core.settings import MOVINET_WEIGHTS_DIR

_KINETICS_MEAN = np.array([0.43216, 0.394666, 0.37645], dtype=np.float32)
_KINETICS_STD = np.array([0.22803, 0.22145, 0.216989], dtype=np.float32)


class ModelUnavailableError(RuntimeError):
    """Raised when MoViNet model cannot be used."""


@dataclass
class VideoInferenceOutput:
    score: float
    model_name: str


def heuristic_score(frames_count: int) -> float:
    score = 0.25 + (frames_count / 120.0)
    return max(0.0, min(1.0, score))


class MoViNetBinaryInferenceEngine:
    def __init__(
        self,
        checkpoint_path: Path,
        model_name: str,
        image_size: int,
        default_num_frames: int,
        device_name: str,
    ) -> None:
        self.checkpoint_path = checkpoint_path
        self.model_name = model_name.upper()
        self.image_size = image_size
        self.default_num_frames = default_num_frames
        self.device = torch.device(self._resolve_device(device_name))

        self._model = None
        self._class_names = ["NonViolence", "Violence"]
        self._violence_index = 1
        self._load_error: Exception | None = None

        try:
            self._model = self._load_model()
        except Exception as exc:
            self._load_error = exc

    def _resolve_device(self, device_name: str) -> str:
        if device_name != "auto":
            return device_name
        if torch.cuda.is_available():
            return "cuda"
        if torch.backends.mps.is_available():
            return "mps"
        return "cpu"

    def _load_model(self) -> torch.nn.Module:
        if not self.checkpoint_path.exists():
            raise FileNotFoundError(
                f"Checkpoint not found at {self.checkpoint_path}. "
                "Please train/export from notebook first."
            )

        from movinets import MoViNet
        from movinets.config import _C

        cfg_map = {
            "A0": _C.MODEL.MoViNetA0,
            "A1": _C.MODEL.MoViNetA1,
            "A2": _C.MODEL.MoViNetA2,
            "A3": _C.MODEL.MoViNetA3,
            "A4": _C.MODEL.MoViNetA4,
            "A5": _C.MODEL.MoViNetA5,
        }
        cfg = cfg_map.get(self.model_name, _C.MODEL.MoViNetA0)

        checkpoint = torch.load(self.checkpoint_path, map_location="cpu")
        if isinstance(checkpoint, dict) and "state_dict" in checkpoint:
            state_dict = checkpoint["state_dict"]
            class_names = checkpoint.get("class_names")
            if isinstance(class_names, list) and len(class_names) >= 2:
                self._class_names = [str(name) for name in class_names]
                for idx, name in enumerate(self._class_names):
                    if name.lower() == "violence":
                        self._violence_index = idx
                        break
            num_classes = int(checkpoint.get("num_classes", len(self._class_names)))
        else:
            state_dict = checkpoint
            num_classes = len(self._class_names)

        model = MoViNet(
            cfg,
            causal=False,
            pretrained=False,
            num_classes=num_classes,
            conv_type="3d",
            tf_like=True,
        )
        model.load_state_dict(state_dict)
        model.to(self.device)
        model.eval()
        return model

    @property
    def ready(self) -> bool:
        return self._model is not None

    @property
    def load_error(self) -> Exception | None:
        return self._load_error

    def predict_video(self, video_path: str | Path, num_frames: int | None = None) -> VideoInferenceOutput:
        if self._model is None:
            reason = str(self._load_error) if self._load_error else "unknown reason"
            raise ModelUnavailableError(f"MoViNet model is unavailable: {reason}")

        frames = num_frames if num_frames is not None else self.default_num_frames
        clip = self._prepare_clip(video_path=Path(video_path), num_frames=frames)
        clip = clip.to(self.device)

        with torch.inference_mode():
            logits = self._model(clip)
            probs = torch.softmax(logits, dim=1)[0].detach().cpu().numpy()

        score = float(np.clip(probs[self._violence_index], 0.0, 1.0))
        return VideoInferenceOutput(score=score, model_name=f"MoViNet-{self.model_name}")

    def _prepare_clip(self, video_path: Path, num_frames: int) -> torch.Tensor:
        if not video_path.exists():
            raise FileNotFoundError(f"Video path not found: {video_path}")

        cap = cv2.VideoCapture(str(video_path))
        if not cap.isOpened():
            raise ValueError(f"Cannot open video: {video_path}")

        frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
        sampled_frames = []

        if frame_count > 0:
            indices = np.linspace(0, frame_count - 1, num=num_frames, dtype=np.int32)
            for idx in indices:
                cap.set(cv2.CAP_PROP_POS_FRAMES, int(idx))
                ok, frame = cap.read()
                if not ok or frame is None:
                    continue
                sampled_frames.append(frame)
        else:
            while len(sampled_frames) < num_frames:
                ok, frame = cap.read()
                if not ok or frame is None:
                    break
                sampled_frames.append(frame)

        cap.release()

        if not sampled_frames:
            raise ValueError(f"No readable frames in video: {video_path}")

        while len(sampled_frames) < num_frames:
            sampled_frames.append(sampled_frames[-1].copy())

        sampled_frames = sampled_frames[:num_frames]
        processed = []
        for frame in sampled_frames:
            frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            frame = cv2.resize(frame, (self.image_size, self.image_size), interpolation=cv2.INTER_AREA)
            frame = frame.astype(np.float32) / 255.0
            frame = (frame - _KINETICS_MEAN) / _KINETICS_STD
            processed.append(frame)

        arr = np.stack(processed, axis=0)
        arr = np.transpose(arr, (3, 0, 1, 2))
        tensor = torch.from_numpy(arr).float().unsqueeze(0)
        return tensor


def _resolve_checkpoint_path(checkpoint_path: str | Path | None) -> Path:
    if checkpoint_path is None:
        return MOVINET_CHECKPOINT_PATH

    path = Path(checkpoint_path).expanduser()
    if path.is_absolute():
        return path

    return (MOVINET_WEIGHTS_DIR / path).resolve()


@lru_cache(maxsize=8)
def _get_engine_cached(checkpoint_path: str, model_name: str) -> MoViNetBinaryInferenceEngine:
    return MoViNetBinaryInferenceEngine(
        checkpoint_path=Path(checkpoint_path),
        model_name=model_name,
        image_size=MOVINET_IMAGE_SIZE,
        default_num_frames=MOVINET_NUM_FRAMES,
        device_name=MOVINET_DEVICE,
    )


def get_engine(
    checkpoint_path: str | Path | None = None,
    model_name: str | None = None,
) -> MoViNetBinaryInferenceEngine:
    resolved_checkpoint = _resolve_checkpoint_path(checkpoint_path)
    resolved_model_name = (model_name or MOVINET_MODEL_NAME).upper()
    return _get_engine_cached(str(resolved_checkpoint), resolved_model_name)
