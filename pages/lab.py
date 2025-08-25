import streamlit as st

from analyseIngredient import image_bytes_to_data_url,url_input,data_url_input,classify_product_category
from textPostprocess import parse_top_level_sections,clean_title
from userRecord import connectDB,insertRecord


coll = connectDB()


st.sidebar.write(f'Welcome *{st.session_state.get("name")}*')
user_id = st.session_state.get("username")

st.sidebar.markdown(
    "<hr style='border: 2px solid #808080; margin: 20px 0;'>",
    unsafe_allow_html=True
    )

st.sidebar.title("User Input")

model = st.sidebar.selectbox(
    "Model",
    ("gpt-4.1-2025-04-14", "gpt-4o-2024-11-20")
)

container1 = st.container(border=True)
with container1:
    st.markdown(
        "<span style='font-size:24px; font-weight:bold; color:#808080;'>Image:</span>",
        unsafe_allow_html=True
    )
    st.markdown(
        "<hr style='border: 2px solid #808080; margin: 20px 0;'>",
        unsafe_allow_html=True
    )

container2 = st.container(border=True)
with container2:
    st.markdown(
        "<span style='font-size:24px; font-weight:bold; color:#808080;'>Text output:</span>",
        unsafe_allow_html=True
    )
    st.markdown(
        "<hr style='border: 2px solid #808080; margin: 20px 0;'>",
        unsafe_allow_html=True
    )

uploaded_file = st.sidebar.file_uploader("Choose an Image file")
st.sidebar.markdown(
    "<hr style='border: 2px solid #808080; margin: 20px 0;'>",
    unsafe_allow_html=True
    )
if uploaded_file is not None:
    bytes_data = uploaded_file.getvalue()
    file_name = uploaded_file.name
    file_extension = file_name.split(".")[-1]
    container1.image(bytes_data)

if st.sidebar.button("Analyse"):
    if uploaded_file is None:
        raise Exception("Please upload an Image")
    image_data_url = image_bytes_to_data_url(bytes_data,file_extension)
    content = data_url_input(model=model,image_data_url=image_data_url)
    sections = parse_top_level_sections(content)
    
    headers = []
    for item in sections:
        headers.append(clean_title(item["header"]))
    if len(headers)==0:
        headers.append("Other")

    tmp_tab = container2.tabs(headers)
    if len(tmp_tab) == 1:
        category = classify_product_category(model,content)
        with tmp_tab[0]:
            st.markdown(content)
    else:
        category = classify_product_category(model,sections[0]["content"])
        for tab,item in zip(tmp_tab,sections):
            with tab:
                st.markdown(item["content"])
                
    insertRecord(coll,user_id,category,content)
    
