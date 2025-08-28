import logging
from mcp.server.fastmcp import FastMCP
import os
# from config import get_config

# 创建 MCP
mcp = FastMCP("EmployeePerformance")
log = logging.getLogger(__name__)

# 读取配置里的文件夹路径
# file_dir = get_config().get("file_system_dir")
file_dir = "./data/HR/"

def parse_md_file(filepath: str) -> dict:
    """
    解析单个 md 文件，把表格转成 dict
    """
    result = {}
    with open(filepath, "r", encoding="utf-8") as f:
        lines = f.readlines()
        for line in lines:
            line = line.strip()
            if "|" in line and not line.startswith("| ----"):
                parts = [x.strip() for x in line.strip("|").split("|")]
                if len(parts) == 2 and parts[0] != "维度":
                    result[parts[0]] = parts[1]
    return result


@mcp.tool(
        description="根据员工姓名查询绩效信息，读取对应 Markdown 文件并解析表格，返回各项评分。"
)
async def queryEmployeeScore(name: str) -> dict:
    """
    查询员工绩效，按名字读 md 文件
    """
    log.info(f"Querying candidate: {name}")

    filename = f"{name} - 绩效评分表.md"
    filepath = os.path.join(file_dir, filename)

    if not os.path.exists(filepath):
        log.warning(f"File not found: {filepath}")
        return {"error": "File not found", "file": filepath}

    try:
        scores = parse_md_file(filepath)
        log.info(f"Scores for {name}: {scores}")
        return {"name": name, "scores": scores}
    except Exception as e:
        log.exception(f"Failed to read file: {filepath}")
        return {"error": "Read failed", "details": str(e)}


@mcp.tool(
    description="根据员工的姓名读取其的原始 Markdown 文件内容，按姓名查找文件并返回纯文本内容。"
)
async def readFile(name: str) -> str:
    """
    返回整个 md 文件的原文
    """
    filepath = os.path.join(file_dir, f"{name} - 绩效评分表.md")
    log.info(f"Reading file: {filepath}")

    try:
        with open(filepath, "r", encoding="utf-8") as file:
            return file.read()
    except FileNotFoundError as e:
        log.error(f"File not found: {filepath}")
        return {"error": "File not found", "details": str(e)}
    except Exception as e:
        log.exception(f"Error reading file: {filepath}")
        return {"error": "Unexpected error", "details": str(e)}


if __name__ == "__main__":
    mcp.run(transport="stdio")
