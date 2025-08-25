import re

def parse_top_level_sections(md_text):
    # 1. 找出文档中所有标题的 # 数量
    header_hashes = re.findall(r'^(#{1,6})\s', md_text, flags=re.MULTILINE)
    if not header_hashes:
        return []
    min_level = min(len(h) for h in header_hashes)  # 最小的 # 数，就是顶级标题的等级

    # 2. 构造正则匹配顶级标题块，匹配以 min_level 个 # 开头的标题和后面内容
    pattern = rf'(^{"#"*min_level} .+?)(?=^{"#"*min_level} |\Z)'

    matches = re.findall(pattern, md_text, flags=re.MULTILINE | re.DOTALL)

    sections = []
    for block in matches:
        lines = block.strip().split('\n')
        header = lines[0].strip()
        content = '\n'.join(lines[1:]).strip()
        sections.append({
            'header': header,
            'content': content
        })
    return sections


def clean_title(text):
    return re.sub(r'^#+\s*', '', text)
