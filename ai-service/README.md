# AI Inference Service (PyTorch MoViNet)

Python microservice cho nhận diện `Violence` / `Non-violence`.

## Features

- Endpoint tách riêng trong `endpoints.py` (khong dat trong `main.py`).
- `POST /api/ai/infer` ho tro 3 che do tinh diem:
	- Nhan `violence_score` tu request.
	- Chay MoViNet PyTorch neu co `video_path`.
	- Fallback heuristic neu model/chieu video khong san sang.
- Lay threshold tu `pattern-service` (`/api/patterns/thresholds`).
- Notebook fine-tune MoViNet: `movinet_finetuning_pytorch.ipynb`.

## Files Chinh

- `main.py`: khoi tao FastAPI app va include router.
- `endpoints.py`: dinh nghia API endpoints.
- `model_inference.py`: loader + infer MoViNet PyTorch.
- `pattern_client.py`: goi threshold tu pattern-service.
- `schemas.py`: request/response models.
- `settings.py`: doc bien moi truong.

## Run Locally

```bash
uv sync
uv run main.py
```

Hoac:

```bash
uv sync
uv run uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

## Request Example

```bash
curl -X POST http://localhost:8000/api/ai/infer \
	-H "Content-Type: application/json" \
	-d '{
		"source_id": "cam-01",
		"pattern_code": "violence",
		"frames_count": 32,
		"video_path": "real-life-violence-dataset/Violence/V_1.mp4"
	}'
```

## Environment Variables

- `PATTERN_SERVICE_URL` (default: `http://localhost:8081`)
- `DEFAULT_VIOLENCE_THRESHOLD` (default: `0.60`)
- `MOVINET_CHECKPOINT_PATH` (default: `ai-service/movinet_a0_violence.pt`)
- `MOVINET_MODEL_NAME` (default: `A0`)
- `MOVINET_IMAGE_SIZE` (default: `172`)
- `MOVINET_NUM_FRAMES` (default: `32`)
- `MOVINET_DEVICE` (default: `auto`, tu chon `cpu`/`cuda`/`mps`)

## Fine-tuning Notebook

Notebook: `movinet_finetuning_pytorch.ipynb`

Notebook se:

- Doc dataset tu `real-life-violence-dataset/Violence` va `real-life-violence-dataset/NonViolence`.
- Fine-tune MoViNet A0 bang PyTorch 2.6.0.
- Luu checkpoint sang `movinet_a0_violence.pt` de AI service dung lai khi infer.
