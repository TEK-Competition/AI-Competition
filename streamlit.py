import yaml
import streamlit as st
import streamlit_authenticator as stauth

with open('login.yaml') as file:
    config = yaml.safe_load(file)

authenticator = stauth.Authenticate(
    config['credentials'],
    config['cookie']['name'],
    config['cookie']['key'],
    config['cookie']['expiry_days']
)
side_bar_logo = "images/logo.png"

def login():
    authenticator.login()

def logout():
    authenticator.logout(key="Logout")

login_page = st.Page(login, title="Log in", icon=":material/login:")
logout_page = st.Page(logout, title="Log out", icon=":material/logout:")

lab = st.Page("pages/lab.py", title="Lab", icon=":material/build:", default=True)

history = st.Page("pages/history.py", title="History", icon=":material/history:")

try:
    authenticator.login()
    st.logo(side_bar_logo)
    st.set_page_config(
        page_title="小红富士实验室",
        page_icon="🍎",
        initial_sidebar_state="expanded",
    )
    
    if st.session_state.get('authentication_status'):
        #authenticator.logout(location="sidebar",key="Logout")
        pg = st.navigation(
            {
                "Account": [logout_page],
                "Tools": [lab,history],
                }
            )
        pg.run()
        while st.session_state.get('Logout'):
            pg = st.navigation([login_page])
            pg.run()
    elif st.session_state.get('authentication_status') is False:
        st.error('Username/password is incorrect')
    elif st.session_state.get('authentication_status') is None:
        st.warning('Please enter your username and password')
except Exception as e:
    st.error(e)
