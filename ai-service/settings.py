import os
from pathlib import Path

PATTERN_SERVICE_URL = os.getenv("PATTERN_SERVICE_URL", "http://localhost:8081")
DEFAULT_VIOLENCE_THRESHOLD = float(os.getenv("DEFAULT_VIOLENCE_THRESHOLD", "0.60"))

_DEFAULT_CHECKPOINT = Path(__file__).resolve().parent / "weights" / "movinet_a0_violence.pt"
MOVINET_CHECKPOINT_PATH = Path(
    os.getenv("MOVINET_CHECKPOINT_PATH", str(_DEFAULT_CHECKPOINT))
).expanduser()
if not MOVINET_CHECKPOINT_PATH.is_absolute():
    MOVINET_CHECKPOINT_PATH = (Path(__file__).resolve().parent / MOVINET_CHECKPOINT_PATH).resolve()

MOVINET_MODEL_NAME = os.getenv("MOVINET_MODEL_NAME", "A0")
MOVINET_IMAGE_SIZE = int(os.getenv("MOVINET_IMAGE_SIZE", "172"))
MOVINET_NUM_FRAMES = int(os.getenv("MOVINET_NUM_FRAMES", "32"))
MOVINET_DEVICE = os.getenv("MOVINET_DEVICE", "auto")
