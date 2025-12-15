import base64
import json
import os
import mimetypes
import time
from typing import Dict, Any, Optional
from PIL import Image
import io

from dotenv import load_dotenv
from fastapi import Request
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import PlainTextResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

import dashscope
from dashscope import MultiModalConversation

load_dotenv()

# ========== DashScope 配置 ==========
dashscope.base_http_api_url = "https://dashscope.aliyuncs.com/api/v1"
API_KEY = os.getenv("DASHSCOPE_API_KEY", "").strip()
if not API_KEY:
    raise RuntimeError("Missing DASHSCOPE_API_KEY in .env")

dashscope.api_key = API_KEY
MODEL = "qwen-image-edit-plus"

print("DASHSCOPE_API_KEY loaded:", "YES" if API_KEY else "NO")

# ========== FastAPI ==========
app = FastAPI(title="DressCode Dressing Backend")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
OUTFITS_JSON_PATH = os.path.join(BASE_DIR, "outfits.json")
OUTFITS_DIR = os.path.join(BASE_DIR, "outfits")
UPLOADS_DIR = os.path.join(BASE_DIR, "uploads")
os.makedirs(UPLOADS_DIR, exist_ok=True)

# ✅ 静态服务：GET /static/outfits/022.jpg
# 实际访问路径：/static/outfits/<filename>
app.mount("/static", StaticFiles(directory=BASE_DIR), name="static")


def load_outfits_map() -> Dict[int, Dict[str, Any]]:
    with open(OUTFITS_JSON_PATH, "r", encoding="utf-8") as f:
        arr = json.load(f)
    m: Dict[int, Dict[str, Any]] = {}
    for item in arr:
        try:
            m[int(item["id"])] = item
        except Exception:
            continue
    return m


def guess_mime(filename: str, fallback: str = "image/jpeg") -> str:
    mt, _ = mimetypes.guess_type(filename)
    return mt or fallback


def bytes_to_data_url(data: bytes, mime: str) -> str:
    b64 = base64.b64encode(data).decode("utf-8")
    return f"data:{mime};base64,{b64}"


def read_local_outfit_as_data_url(rel_path: str) -> str:
    rel_path = (rel_path or "").strip().lstrip("/")

    if not rel_path.startswith("outfits/"):
        raise ValueError(f"imageUrl 必须以 'outfits/' 开头，当前: {rel_path}")

    abs_path = os.path.join(BASE_DIR, rel_path)
    if not os.path.isfile(abs_path):
        raise FileNotFoundError(f"找不到 outfit 图片文件: {abs_path}")

    with open(abs_path, "rb") as f:
        raw_outfit_bytes = f.read()

    focused_outfit_bytes = crop_center_focus(raw_outfit_bytes, focus_ratio=0.6)

    mime = guess_mime(abs_path, fallback="image/jpeg")
    outfit_data_url = bytes_to_data_url(focused_outfit_bytes, mime)

    return outfit_data_url

def parse_result_image_url(resp: Any) -> Optional[str]:
    try:
        output = resp.get("output") or {}
        choices = output.get("choices") or []
        if choices:
            msg = choices[0].get("message") or {}
            content = msg.get("content") or []
            for block in content:
                if isinstance(block, dict) and "image" in block:
                    return block["image"]
                if isinstance(block, dict) and "image_url" in block:
                    return block["image_url"]
    except Exception:
        pass
    return None


@app.post("/dressing/generate", response_class=PlainTextResponse)
async def dressing_generate(
    request: Request,
    photo: UploadFile = File(...),
    outfitId: str = Form(...),
    prompt: str = Form("只对图1做局部编辑：保持图1的人物身份、脸部五官、发型、肤色、表情、姿势、手部、背景、光照与构图完全不变。禁止改变任何非服装区域。将图2的服装真实地穿到图1人物身上（仅替换衣服区域），服装的颜色、材质、纹理、剪裁、领口袖口细节尽量与图2一致，并与图1光照阴影自然融合，边缘干净无涂抹、无漂浮、无错位。最终输出必须是“图1+换装后的衣服”，不得输出图2或接近图2的整张照片。"),
):
    ts = int(time.time() * 1000)
    os.makedirs(UPLOADS_DIR, exist_ok=True)

    print("client:", request.client, "ua:", request.headers.get("user-agent"))

    t_start = time.time()

    # 1) 读用户图（只读一次）
    user_bytes = await photo.read()
    if not user_bytes:
        return PlainTextResponse("EMPTY_PHOTO", status_code=400)

    user_path = os.path.join(UPLOADS_DIR, f"user_{ts}.jpg")
    with open(user_path, "wb") as f:
        f.write(user_bytes)

    user_mime = photo.content_type or guess_mime(photo.filename or "user.jpg")
    user_data_url = bytes_to_data_url(user_bytes, user_mime)
    print("user image bytes:", len(user_bytes))

    # 2) 读 outfit 图
    outfits_map = load_outfits_map()
    try:
        oid = int(outfitId)
    except Exception:
        return PlainTextResponse("INVALID_OUTFIT_ID", status_code=400)

    outfit = outfits_map.get(oid)
    if not outfit:
        return PlainTextResponse("OUTFIT_NOT_FOUND", status_code=404)

    rel = (outfit.get("imageUrl") or "").strip()
    if not rel:
        return PlainTextResponse("OUTFIT_IMAGE_EMPTY", status_code=400)

    abs_outfit = os.path.join(BASE_DIR, rel)
    if not os.path.isfile(abs_outfit):
        return PlainTextResponse(f"OUTFIT_FILE_NOT_FOUND: {abs_outfit}", status_code=400)

    outfit_path = os.path.join(UPLOADS_DIR, f"outfit_{ts}.jpg")
    with open(abs_outfit, "rb") as fin, open(outfit_path, "wb") as fout:
        fout.write(fin.read())

    print("debug urls:",
          f"http://127.0.0.1:8000/static/uploads/user_{ts}.jpg",
          f"http://127.0.0.1:8000/static/uploads/outfit_{ts}.jpg")

    try:
        outfit_data_url = read_local_outfit_as_data_url(rel)
    except Exception as e:
        return PlainTextResponse(f"OUTFIT_IMAGE_LOAD_FAILED: {e}", status_code=400)

    # 3) 调用 qwen-image-edit-plus（唯一一次）
    t_call = time.time()

    messages = [{
        "role": "user",
        "content": [
            {"image": user_data_url},  # 图1：基底（人像）
            {"image": user_data_url},
            {"image": outfit_data_url},  # 图2：衣服参考
            {"text": (
                "执行换装编辑任务。规则如下：\n"
                "1. 输出必须基于第一张图的人物身份、脸、发型、姿势、背景不变。\n"
                "2. 第二张图是你自己的记忆强化：确认这是同一个人，不能改变。\n"
                "3. 第三张图仅提供服装外观参考（颜色、材质、款式、细节），不得复制其构图、背景或人物。\n"
                "4. 将第三张图的服装真实地穿到第一张图人物身上，替换原有上衣/外套等，其他区域严禁修改。\n"
                "5. 输出必须看起来像第一张图经过局部编辑后的照片，禁止生成新姿势、新人物、新背景。\n"
                "6. 若无法实现，请留空服装区域，也不要脱离第一张图去模仿第三张图的整体内容。"
            )}
        ]
    }]

    resp = MultiModalConversation.call(
        api_key=API_KEY,
        model=MODEL,
        messages=messages,
        result_format="message",
        stream=False,
        n=1,
        watermark=False,
        prompt_extend=True,
        negative_prompt=(
            "保持原服装不变；不换装；微调颜色；直接输出图2；复制图2；"
            "verbatim reproduction of the second image；"
            "identical to the reference clothing photo；"
            "copying background, pose, face, hair from image 2；"
            "generating new person, changing identity, changing face, redrawing whole body；"
            "cartoon, drawing, sketch, watermark, text, logo, floating limbs, distorted hands；"
            "overexposed, blurry, low quality, mismatched lighting"
        ),
    )

    if isinstance(resp, dict) and resp.get("status_code") and resp.get("status_code") != 200:
        # 把真实错误回传给 App，方便你前端 Toast 显示
        return JSONResponse(resp, status_code=resp.get("status_code", 500))

    print("dashscope raw resp:", json.dumps(resp, ensure_ascii=False)[:2000])
    print("dashscope call seconds:", time.time() - t_call)

    # 4) 解析结果
    result_image = parse_result_image_url(resp)
    if not result_image:
        print("NO_RESULT_IMAGE resp keys:", resp.keys() if hasattr(resp, "keys") else type(resp))
        return JSONResponse({"error": "NO_RESULT_IMAGE", "raw": resp}, status_code=500)

    if result_image and looks_like_same_data_url(result_image, outfit_data_url):
        print("WARN: result seems identical to outfit input, retrying with 3-image trick...")
        # 走下面的“3图强制基底”重试逻辑

    print("total request seconds:", time.time() - t_start)
    print("result_image head:", result_image[:80])
    return result_image

@app.get("/ping", response_class=PlainTextResponse)
def ping():
    return "ok"

def looks_like_same_data_url(a: str, b: str) -> bool:
    if not a or not b: return False
    if a.startswith("data:") and b.startswith("data:"):
        return a[:200] == b[:200]  # 先用简单快速判断
    return False

def crop_center_focus(img_bytes: bytes, focus_ratio=0.5) -> bytes:
    im = Image.open(io.BytesIO(img_bytes)).convert("RGB")
    w, h = im.size
    cw, ch = int(w * focus_ratio), int(h * focus_ratio)
    left = (w - cw) // 2
    top = (h - ch) // 2
    crop = im.crop((left, top, left + cw, top + ch))
    out = io.BytesIO()
    crop.save(out, format="JPEG", quality=92)
    return out.getvalue()
