// request.js
import Message from 'tdesign-miniprogram/message/index';
const app = getApp();
const baseUrl = app.globalData.config.API_URL; // 你的接口基础url 

function execute({
  url,
  method = 'GET',
  data = {},
  header = {}
}) {
  if (url !== '/auth/register' && url !== '/auth/account/login' && url !== '/auth/wechat/login') {
    // 请求前处理，例如添加公共请求头
    const accessToken = wx.getStorageSync('accessToken'); // 假设你有一个登录态token存储在本地
    if (accessToken) {
      header['Authorization'] = 'Bearer ' + accessToken;
    }
  }
  return new Promise((resolve, reject) => {
    wx.showLoading({
      title: '加载中~',
    });
    wx.request({
      url: baseUrl + url,
      method: method,
      data: data,
      header: header,
      success: function (res) {
        wx.hideLoading();
        // 响应后处理
        if (res.statusCode === 200) {
          resolve(res.data); // 返回数据
        } else if (res.statusCode === 401) {
          const errMessage = res.data.msg;
          Message.error({
            context: this,
            offset: [90, 32],
            duration: 2000,
            content: errMessage,
          });
          reject(new Error(errMessage));
          setTimeout(() => {
            // 动画完成后跳转页面
            wx.reLaunch({
              url: '/pages/index/index',
            })
          }, 3000);
        } else {
          const errMessage = res.data.msg;
          Message.error({
            context: this,
            offset: [90, 32],
            duration: 3000,
            content: errMessage,
          });
          reject(new Error(errMessage));
        }
      },
      fail: function (error) {
        wx.hideLoading();
        console.log('请求失败：', error)
        Message.error({
          context: this,
          offset: [90, 32],
          duration: 3000,
          content: '系统繁忙~请稍后再试~',
        });
        reject(new Error('系统繁忙~请稍后再试~'));
      }
    });
  });
}

module.exports = {
  execute
};