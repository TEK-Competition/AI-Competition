import base64
import yaml

from openai import OpenAI


with open('config.yaml', 'r', encoding='utf-8') as file:
    config = yaml.safe_load(file)  # 安全读取 YAML 数据

Template = '''
1. 提取配料表信息
“请提取以下图片中的食品配料表所有成分，若不是中文，请对应翻译成中文，并按顺序列出。”

“请列出配料表中可能的高风险成分或添加剂。”

“图片中是否有可能导致过敏的成分？有哪些？”

“请列出配料表中主要的营养强化成分（如维生素、矿物质等）。”

2. 针对不同人群的分析
“这些成分中有哪些是老人需要注意的？”

“配料表中哪些成分对孕妇可能有风险？”

“是否含有不适合幼儿食用的成分？”

“请分析该产品对老人、孕妇、幼儿的健康影响。”

3. 报告撰写
“请根据以上配料表，写一段针对老人、孕妇、幼儿的简短风险和建议报告。输出为中文。”

“结合配料表，简要说明该食品对老人、孕妇、幼儿的适宜性。”

“如果有潜在风险，请简要说明理由，并提出建议。”
'''
# Function to encode the image
def image_bytes_to_data_url(image_bytes, file_extension):
    """
    把图片二进制（bytes或BytesIO）转为data URL
    """
    if file_extension.lower() == "webp":
        mime_type='image/webp'
    elif file_extension.lower() == "jpeg" or file_extension.lower() == "jpg":
        mime_type='image/jpeg'
    elif file_extension.lower() == "png":
        mime_type='image/png'
    else:
        raise Exception("Unsupported Image Type. Please upload file with ext jpg/jpeg/png/webp.")
    if hasattr(image_bytes, 'getvalue'):  # 兼容BytesIO对象
        image_bytes = image_bytes.getvalue()
    base64_encoded_data = base64.b64encode(image_bytes).decode('utf-8')
    return f"data:{mime_type};base64,{base64_encoded_data}"

def url_input(model,img_url):
    client = model(model)

    response = client.responses.create(
        model=model,
        input=[{
            "role": "user",
            "content": [
                {"type": "input_text", "text": Template},
                {
                    "type": "input_image",
                    "image_url": img_url,
                },
            ],
        }],
    )

    return response.output_text

def data_url_input(model,image_data_url):
    client = build_client(model)

    response = client.responses.create(
    model=model,
        input=[
            {
                "role": "user",
                "content": [
                    { "type": "input_text", "text": Template },
                    {
                        "type": "input_image",
                        "image_url": image_data_url,
                    },
                ],
            }
        ],
    )
    return response.output_text 

def build_client(model):
    if model.startswith("gpt-4.1") or model.startswith("gpt-4o"):
        return OpenAI(api_key=config["openai_api_key"])
    return None


def classify_product_category(model,ingredients):
    prompt = f"""
你是食品与健康产品分类专家。请只根据下方配料表内容，判断该产品最贴切的类别，只输出一个类别，不要解释理由。  
可选类别为：保健品、营养补充剂、普通食品、饮料、药品、零食、特殊医学用途配方食品、其他（如果内容无关或无法判断请选择“其他”）等。

配料表：{ingredients}
    """.strip()
    
    client = build_client(model)
    
    response = client.chat.completions.create(
        model=model,
        messages=[{"role": "user", "content": prompt}],
        max_tokens=10,
        temperature=0,
    )
    return response.choices[0].message.content.strip()