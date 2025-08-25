from datetime import datetime,date,time

import colorsys
import altair as alt
import pandas as pd
import streamlit as st

from userRecord import connectDB

coll = connectDB()
# 颜色映射（Tableau20 + Okabe-Ito，超出后自动补色）
def build_category_colors(categories):
    okabe_ito = ["#E69F00","#56B4E9","#009E73","#F0E442",
                 "#0072B2","#D55E00","#CC79A7","#999999"]
    tableau20 = ["#4E79A7","#F28E2B","#E15759","#76B7B2","#59A14F",
                 "#EDC948","#B07AA1","#FF9DA7","#9C755F","#BAB0AC",
                 "#1F77B4","#FF7F0E","#2CA02C","#D62728","#9467BD",
                 "#8C564B","#E377C2","#7F7F7F","#BCBD22","#17BECF"]
    base = tableau20 + okabe_ito  # 28色
    if len(categories) > len(base):
        need = len(categories) - len(base)
        extras = []
        for i in range(need):
            h = i / need
            l, s = 0.55, 0.65
            r,g,b = colorsys.hls_to_rgb(h, l, s)
            extras.append('#%02x%02x%02x' % (int(r*255), int(g*255), int(b*255)))
        palette = base + extras
    else:
        palette = base[:len(categories)]
    return {cat: col for cat, col in zip(categories, palette)}
    
st.sidebar.write(f'Welcome *{st.session_state.get("name")}*')
user_id = st.session_state.get("username")

today = datetime.now()
if today.month-1 == 0 :
    one_month_before = date(today.year-1,12,today.day)
else:
    one_month_before = date(today.year,today.month-1,today.day)

five_years_before = date(today.year-5, today.month, today.day)

d = st.date_input(
    "Select your time range",
    (one_month_before, today),
    five_years_before,
    today,
    format="YYYY.MM.DD",
    )

results = list(coll.find({
    "user_id": user_id,
    "timestamp": {"$gte":datetime.combine(d[0], time.min),"$lte":datetime.combine(d[1], time.max)}
}).sort("timestamp", -1))  # 最新的排最前面

if results:
    # 转成 DataFrame 展示（只取常用字段）
    df = pd.DataFrame([
        {
            "time": r.get("timestamp"),
            "category": r.get("category"),
            "content": r.get("content"),
        }
        for r in results

    ])
    st.write(f"Searched Logs：")
    st.dataframe(df)

    st.write(f"Activity Chart：")
    df['date'] = pd.to_datetime(df['time']).dt.date
    df['count'] = 1
    span_days = (pd.to_datetime(df['date']).max() - pd.to_datetime(df['date']).min()).days
    aggregate_by_month = span_days > 31
    
    if aggregate_by_month:
        # ---- 按月聚合 ----
        df['month'] = pd.to_datetime(df['date']).to_period('M').dt.to_timestamp()
        grouped = df.groupby(['month','category'])['count'].sum().reset_index()
    
        # 补齐月份 × 类别
        months = pd.period_range(grouped['month'].min().to_period('M'),
                                 grouped['month'].max().to_period('M'), freq='M').to_timestamp()
        categories = sorted(grouped['category'].unique())
        full = pd.MultiIndex.from_product([months, categories], names=['month','category']).to_frame(index=False)
        full = full.merge(grouped, on=['month','category'], how='left').fillna(0)
    
        # 供 X 轴展示的字符串，不带星期
        full['x_label'] = full['month'].dt.strftime('%Y-%m')
        title = "每月总数（按类别分色）"
    else:
        # ---- 按日聚合 ----
        grouped = df.groupby(['date','category'])['count'].sum().reset_index()
    
        # 补齐日期 × 类别
        dates = pd.date_range(grouped['date'].min(), grouped['date'].max(), freq='D').date
        categories = sorted(grouped['category'].unique())
        full = pd.MultiIndex.from_product([dates, categories], names=['date','category']).to_frame(index=False)
        full = full.merge(grouped, on=['date','category'], how='left').fillna(0)
    
        full['x_label'] = full['date'].astype(str)  # 仅日期字符串
        title = "每日总数（按类别分色）"
    
    # 固定颜色映射（稳定不跳色）
    color_map = build_category_colors(categories)
    
    # ========= 画图（Altair 堆叠柱） =========
    chart = (
        alt.Chart(full)
        .mark_bar()
        .encode(
            x=alt.X('x_label:N', title='日期'),
            y=alt.Y('count:Q', title='总数'),
            color=alt.Color(
                'category:N',
                legend=alt.Legend(title="类别"),
                scale=alt.Scale(domain=list(color_map.keys()), range=list(color_map.values()))
            ),
            tooltip=['x_label:N','category:N','count:Q']
        )
        .properties(width=1200, height=420)
    )
    
    st.altair_chart(chart, use_container_width=True)
else:
    st.info("no logs in this date range")

