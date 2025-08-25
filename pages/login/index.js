// pages/login/index.js
const authApi = require('../../apis/auth');
Page({

  /**
   * 页面的初始数据
   */
  data: {
    hasAccount: true,
    hasPassword: true,
    passwordIconName: 'browse-off',
    passwordInputType: 'password',
  },
  onGoHome() {
    wx.reLaunch({
      url: '/pages/index/index',
    });
  },
  goToRegister(){
    wx.reLaunch({
      url: '/pages/register/index',
    });
  },
  onAccountInput(e) {
    const account = e.detail.value;
    this.validateAccount(account);
  },
  onPasswordInput(e) {
    const password = e.detail.value;
    this.validatePassword(password);
  },
  formSubmit(e) {
    const { value } = e.detail;
    this.validateAccount(value.account);
    this.validatePassword(value.password);
    const { hasAccount, hasPassword } = this.data
    if (!hasAccount || !hasPassword) {
      return;
    }
    wx.login({
      success: (res) => {
        value.wechatCode = res.code;
        authApi.accountLogin(value).then(response => {
          const resData = response.data;
          const accessToken = resData.accessToken;
          //设置token
          wx.setStorageSync('accessToken', accessToken);
          wx.reLaunch({
            url: '/pages/travel-plan/list/index'
          })
        });
      },
    });
  },
  validateAccount(account) {
    if (!!account) {
      this.setData({
        hasAccount: true
      })
    } else {
      this.setData({
        hasAccount: false
      })
    }
  },
  validatePassword(password) {
    if (!!password) {
      this.setData({
        hasPassword: true
      })
    } else {
      this.setData({
        hasPassword: false
      })
    }
  },
  onSuffixIconClick(e) {
    const { trigger } = e.detail;
    const { passwordIconName } = this.data
    if ('suffix-icon' == trigger) {
      if (passwordIconName == 'browse-off') {
        this.setData({
          passwordIconName: 'browse',
          passwordInputType: 'text'
        });
      } else {
        this.setData({
          passwordIconName: 'browse-off',
          passwordInputType: 'password'
        });
      }
    }
  },
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  }
})