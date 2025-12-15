import os
import base64
import json
import re
from typing import Dict, Any, List, Tuple
from dotenv import load_dotenv
from openai import OpenAI

# =========================
# 1) 读取 .env
# =========================
load_dotenv()

DASHSCOPE_API_KEY = (os.getenv("DASHSCOPE_API_KEY") or "").strip()
if not DASHSCOPE_API_KEY:
    raise RuntimeError("缺少 DASHSCOPE_API_KEY，请在 .env 中配置：DASHSCOPE_API_KEY=xxxx")

BASE_URL = (os.getenv("DASHSCOPE_BASE_URL") or "https://dashscope.aliyuncs.com/compatible-mode/v1").strip()
MODEL_NAME = (os.getenv("MODEL_NAME") or "qwen-vl-plus").strip()

IMAGE_DIR = (os.getenv("IMAGE_DIR") or "./images").strip()
OUT_JSON_PATH = (os.getenv("OUT_JSON_PATH") or "./outfits.json").strip()

# imageUrl 前缀：如果你把图片拷贝到 app/src/main/assets/outfits/
# 用 file:///android_asset/outfits/xxx.jpg
IMAGE_URL_PREFIX = (os.getenv("IMAGE_URL_PREFIX") or "file:///android_asset/outfits/").strip()

# =========================
# 2) OpenAI Compatible Client
# =========================
client = OpenAI(
    api_key=DASHSCOPE_API_KEY,
    base_url=BASE_URL,
)

PROMPT = """
你是一名服装图像标注助手。你必须严格输出 JSON，不允许 markdown、不允许解释。

输出结构如下：

{
  "category": "",
  "clothing_type": "",
  "gender": "",
  "colors": [],
  "style": [],
  "patterns": [],
  "materials": [],
  "sleeve_length": "",
  "length": "",
  "fit": "",
  "details": [],
  "occasion": [],
  "quality": ""
}

所有标签必须使用中文，并且只能从以下选项中选择：

【category / 大类】
上衣, 下装, 连衣裙, 外套, 鞋子, 包袋, 配饰, 其他

【clothing_type / 细分类型】
T恤, 衬衫, 女衬衫, 毛衣, 卫衣, 外套, 夹克, 裙子, 牛仔裤, 长裤, 短裤, 连衣裙, 西装, 其他

【gender / 性别】
女款, 男款, 中性

【colors / 颜色】
黑色, 白色, 蓝色, 红色, 绿色, 黄色, 粉色, 棕色, 灰色, 紫色, 米色, 彩色

【style / 风格】
休闲, 正式, 运动, 街头, 复古, 优雅, 性感, 可爱, 极简

【patterns / 图案】
纯色, 条纹, 格纹, 碎花, 图案印花, 波点, 动物纹, 棋盘格, 几何图案

【materials / 材质】
棉, 牛仔, 羊毛, 皮革, 真丝, 亚麻, 涤纶, 针织, 缎面

【sleeve_length / 袖长】
无袖, 短袖, 五分袖, 长袖, 加长袖, 无

【length / 衣长/裙长】
短款, 腰部, 臀部, 过膝, 中长, 踝长, 拖地, 无

【fit / 版型】
紧身, 修身, 标准, 宽松, 超宽松

【details / 细节】
纽扣, 拉链, 帽子, 口袋, 腰带, 标志, 蕾丝, 荷叶边, 刺绣

【occasion / 场景】
日常, 工作, 派对, 运动, 户外, 正式场合

【quality / 图像质量】
低, 中, 高

仅输出 JSON，不要添加其他文字。
""".strip()


# =========================
# 3) 基础工具
# =========================
def load_image_as_data_url(path: str) -> str:
    ext = os.path.splitext(path)[1].lower()
    if ext in (".jpg", ".jpeg"):
        mime = "image/jpeg"
    elif ext == ".png":
        mime = "image/png"
    elif ext == ".webp":
        mime = "image/webp"
    else:
        mime = "image/jpeg"

    with open(path, "rb") as f:
        b64 = base64.b64encode(f.read()).decode("utf-8")
    return f"data:{mime};base64,{b64}"


def parse_json_content(raw: str) -> Dict[str, Any]:
    s = (raw or "").strip()

    # 容错：去掉 ```json ``` 包裹
    if s.startswith("```"):
        s = re.sub(r"^```[a-zA-Z]*\s*", "", s)
        s = re.sub(r"\s*```$", "", s).strip()

    # 容错：开头多了 "json"
    if s.lower().startswith("json"):
        s = s[4:].strip()

    return json.loads(s)


def label_image(image_path: str) -> Dict[str, Any]:
    data_url = load_image_as_data_url(image_path)
    completion = client.chat.completions.create(
        model=MODEL_NAME,
        messages=[
            {
                "role": "user",
                "content": [
                    {"type": "image_url", "image_url": {"url": data_url}},
                    {"type": "text", "text": PROMPT},
                ],
            }
        ],
    )
    raw = completion.choices[0].message.content
    return parse_json_content(raw)


def iter_images(directory: str):
    exts = {".png", ".jpg", ".jpeg", ".webp", ".bmp"}
    for name in sorted(os.listdir(directory)):
        path = os.path.join(directory, name)
        if os.path.isfile(path) and os.path.splitext(name)[1].lower() in exts:
            yield name, path


# =========================
# 4) 映射到 OutfitEntity 字段
# =========================
def stable_id_from_filename(filename: str) -> int:
    # 稳定 id：同一文件名永远同一 id（避免反复重跑导致 id 漂移）
    # 用简单 hash（跨平台稳定）
    h = 0
    for ch in filename:
        h = (h * 131 + ord(ch)) % 1000000007
    return 100000 + (h % 900000)  # 100000~999999


def map_gender_to_int(g: str) -> int:
    if g == "男款":
        return 1
    if g == "女款":
        return 2
    return 0  # 中性/未知


def pick_first_str(arr: Any, default: str = "") -> str:
    if isinstance(arr, list) and arr:
        return str(arr[0])
    return default


def map_occasion_to_scene(occasion: List[str]) -> str:
    s = set(occasion or [])
    if "工作" in s:
        return "上班"
    if "运动" in s:
        return "健身房"
    if "户外" in s:
        return "旅行"
    if "派对" in s:
        return "聚会"
    if "正式场合" in s:
        return "面试"
    if "日常" in s:
        return "日常"
    return "日常"


def infer_season(tags: Dict[str, Any]) -> str:
    sleeve = tags.get("sleeve_length", "")
    materials = set(tags.get("materials", []) or [])
    clothing_type = tags.get("clothing_type", "")
    category = tags.get("category", "")

    # 冬：羊毛/针织/外套/长袖倾向
    if "羊毛" in materials or "针织" in materials:
        return "冬"
    if category == "外套" or clothing_type in ("外套", "夹克", "西装"):
        return "冬"
    if sleeve in ("长袖", "加长袖") and clothing_type in ("毛衣", "卫衣", "西装"):
        return "秋"

    # 夏：无袖/短袖/短裤/连衣裙
    if sleeve in ("无袖", "短袖"):
        return "夏"
    if clothing_type in ("短裤", "连衣裙", "裙子", "T恤"):
        return "夏"

    # 秋：牛仔/长裤/夹克
    if "牛仔" in materials or clothing_type in ("长裤", "牛仔裤", "夹克"):
        return "秋"

    return "春"


def infer_weather(tags: Dict[str, Any], scene: str) -> str:
    # 你 App 里以前用：炎热/适中/寒冷（也可以扩展雨天）
    season = infer_season(tags)
    if season == "夏":
        return "炎热"
    if season == "冬":
        return "寒冷"
    # 场景户外更可能遇到雨天？这里给一个可控规则（默认不输出雨天）
    return "适中"


def build_keyword(tags: Dict[str, Any], scene: str, season: str, weather: str) -> str:
    parts: List[str] = []

    # 单值
    for k in ("category", "clothing_type", "gender", "sleeve_length", "length", "fit"):
        v = tags.get(k, "")
        if v:
            parts.append(str(v))

    # 数组
    for k in ("colors", "style", "patterns", "materials", "details", "occasion"):
        arr = tags.get(k, [])
        if isinstance(arr, list):
            parts.extend([str(x) for x in arr if x])

    # 加上你的筛选字段，保证可搜到
    if scene:
        parts.append(scene)
    if season:
        parts.append(season)
    if weather:
        parts.append(weather)

    # 去重保序
    seen = set()
    uniq = []
    for p in parts:
        if p not in seen:
            uniq.append(p)
            seen.add(p)

    return " ".join(uniq).strip()


def tags_to_outfit_entity(filename: str, tags: Dict[str, Any]) -> Dict[str, Any]:
    scene = map_occasion_to_scene(tags.get("occasion", []) or [])
    season = infer_season(tags)
    weather = infer_weather(tags, scene)

    outfit = {
        # ======= Room OutfitEntity 字段 =======
        "id": stable_id_from_filename(filename),
        "imageUrl": f"{IMAGE_URL_PREFIX}{filename}",
        "gender": map_gender_to_int(tags.get("gender", "")),
        "style": pick_first_str(tags.get("style", []), default="休闲"),
        "season": season,
        "scene": scene,
        "weather": weather,
        "keyword": build_keyword(tags, scene, season, weather),
    }
    return outfit


# =========================
# 5) 主流程：输出 outfits.json
# =========================
def main():
    if not os.path.isdir(IMAGE_DIR):
        raise RuntimeError(f"图片目录不存在: {IMAGE_DIR}")

    outfits: List[Dict[str, Any]] = []
    raw_backup: List[Dict[str, Any]] = []

    for filename, path in iter_images(IMAGE_DIR):
        print(f"处理图片: {filename} ...")
        try:
            tags = label_image(path)
        except Exception as e:
            print(f"  [ERROR] 标注失败: {e}")
            continue

        outfits.append(tags_to_outfit_entity(filename, tags))
        raw_backup.append({"filename": filename, "tags": tags})

    with open(OUT_JSON_PATH, "w", encoding="utf-8") as f:
        json.dump(outfits, f, ensure_ascii=False, indent=2)

    raw_path = os.path.splitext(OUT_JSON_PATH)[0] + "_raw.json"
    with open(raw_path, "w", encoding="utf-8") as f:
        json.dump(raw_backup, f, ensure_ascii=False, indent=2)

    print(f"完成！OutfitEntity JSON 已输出: {OUT_JSON_PATH}")
    print(f"原始标签备份输出: {raw_path}")


if __name__ == "__main__":
    main()
