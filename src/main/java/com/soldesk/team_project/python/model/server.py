# server.py
import os
import json
import torch
import torch.nn as nn
from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
from transformers import AutoModel
from kobert_tokenizer import KoBERTTokenizer
from fastapi.staticfiles import StaticFiles

# ── 기본 설정
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
# 스크립트 파일이 있는 디렉토리를 모델 디렉토리로 설정
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_DIR = os.getenv("MODEL_DIR", SCRIPT_DIR)   # best_kobert_model.pt / label_classes.json 위치
MAX_LEN = int(os.getenv("MAX_LEN", "80")) # 학습 때 사용한 max_len 값

# ── 학습 때와 동일한 모델 구조 (Mean Pooling + LayerNorm + GELU)
class KoBertClassifier(nn.Module):
    def __init__(self, num_classes, dr_rate: float = 0.3, bert=None):
        super().__init__()
        # 요청하신 대로 safetensors 옵션 없이 그대로 사용합니다.
        self.bert = AutoModel.from_pretrained("skt/kobert-base-v1")
        hidden = self.bert.config.hidden_size
        self.layernorm = nn.LayerNorm(hidden)
        self.classifier = nn.Sequential(
            nn.Dropout(p=dr_rate),
            nn.Linear(hidden, 256),
            nn.GELU(),
            nn.Dropout(p=dr_rate),
            nn.Linear(256, num_classes),
        )

    def forward(self, input_ids, token_type_ids, attention_mask):
        out = self.bert(
            input_ids=input_ids,
            token_type_ids=token_type_ids,
            attention_mask=attention_mask,
        )
        last_hidden = out.last_hidden_state                # (B, L, H)
        mask = attention_mask.unsqueeze(-1).float()        # (B, L, 1)
        pooled = (last_hidden * mask).sum(1) / mask.sum(1).clamp(min=1.0)  # mean pooling
        pooled = self.layernorm(pooled)
        return self.classifier(pooled)

# ── 라벨/토크나이저/모델 로드
with open(os.path.join(MODEL_DIR, "label_classes.json"), "r", encoding="utf-8") as f:
    LABEL_CLASSES = json.load(f)
NUM_LABELS = len(LABEL_CLASSES)

tokenizer = KoBERTTokenizer.from_pretrained("skt/kobert-base-v1")
model = KoBertClassifier(num_classes=NUM_LABELS, dr_rate=0.3)
state = torch.load(os.path.join(MODEL_DIR, "best_kobert_model.pt"), map_location=DEVICE)
model.load_state_dict(state)
model.to(DEVICE).eval()
torch.set_grad_enabled(False)

# ── FastAPI
app = FastAPI(title="KoBERT Category Classifier", version="1.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], allow_credentials=True, allow_methods=["*"], allow_headers=["*"],
)

class PredictIn(BaseModel):
    title: str
    top_k: int = 5

class PredItem(BaseModel):
    label: str
    score: float

class PredOut(BaseModel):
    predictions: list[PredItem]

@app.post("/predict")
def predict(inp: PredictIn):
    title = (inp.title or "").strip()
    if not title:
        return {"predictions": []}

    enc = tokenizer(
        title,
        padding="max_length",
        truncation=True,
        max_length=MAX_LEN,
        return_tensors="pt",
    )
    input_ids = enc["input_ids"].to(DEVICE)
    attention_mask = enc["attention_mask"].to(DEVICE)
    token_type_ids = enc.get("token_type_ids", torch.zeros_like(input_ids)).to(DEVICE)

    # ✅ 추론 모드로 감싸기 (grad off)
    with torch.inference_mode():
        logits = model(input_ids, token_type_ids, attention_mask)
        probs  = torch.softmax(logits, dim=-1)

    # ✅ detach 후 numpy()
    probs = probs.detach().cpu().numpy()[0]

    k = max(1, min(int(inp.top_k), len(LABEL_CLASSES)))
    top_idx = probs.argsort()[-k:][::-1]
    preds = [{"label": LABEL_CLASSES[i], "score": float(probs[i])} for i in top_idx]
    return {"predictions": preds}

# StaticFiles는 필요시에만 활성화
# app.mount("/", StaticFiles(directory="static", html=True), name="static")