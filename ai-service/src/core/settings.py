import os
from pathlib import Path

AI_SERVICE_ROOT = Path(__file__).resolve().parents[2]

PATTERN_SERVICE_URL = os.getenv("PATTERN_SERVICE_URL", "http://localhost:8081")
DEFAULT_VIOLENCE_THRESHOLD = float(os.getenv("DEFAULT_VIOLENCE_THRESHOLD", "0.60"))

MOVINET_WEIGHTS_DIR = Path(
    os.getenv("MOVINET_WEIGHTS_DIR", str(AI_SERVICE_ROOT / "weights"))
).expanduser()
if not MOVINET_WEIGHTS_DIR.is_absolute():
    MOVINET_WEIGHTS_DIR = (AI_SERVICE_ROOT / MOVINET_WEIGHTS_DIR).resolve()

_DEFAULT_CHECKPOINT = AI_SERVICE_ROOT / "weights" / "movinet_a0_violence.pt"
MOVINET_CHECKPOINT_PATH = Path(
    os.getenv("MOVINET_CHECKPOINT_PATH", str(_DEFAULT_CHECKPOINT))
).expanduser()
if not MOVINET_CHECKPOINT_PATH.is_absolute():
    MOVINET_CHECKPOINT_PATH = (AI_SERVICE_ROOT / MOVINET_CHECKPOINT_PATH).resolve()

MOVINET_MODEL_NAME = os.getenv("MOVINET_MODEL_NAME", "A0")
MOVINET_IMAGE_SIZE = int(os.getenv("MOVINET_IMAGE_SIZE", "172"))
MOVINET_NUM_FRAMES = int(os.getenv("MOVINET_NUM_FRAMES", "32"))
MOVINET_DEVICE = os.getenv("MOVINET_DEVICE", "auto")
