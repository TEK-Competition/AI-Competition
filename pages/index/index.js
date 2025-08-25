// pages/index/index.js
const authApi = require('../../apis/auth');
Page({
  data: {},
  goToLogin: function () {
    wx.reLaunch({
      url: '/pages/login/index',
    })
  },
  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    wx.login({
      success: (res) => {
        const wechatCode = res.code;
        authApi.wechatLogin(wechatCode).then(response => {
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
})