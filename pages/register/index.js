// pages/register/index.js
import Message from 'tdesign-miniprogram/message/index';
const authApi = require('../../apis/auth');
Page({

  /**
   * 页面的初始数据
   */
  data: {
    hasUsername: true,
    hasAccount: true,
    hasPassword: true,
    hasConfirmPassword: true,
    isTwoPasswordSame: true,
    passwordIconName: 'browse-off',
    confirmPasswordIconName: 'browse-off',
    passwordInputType: 'password',
    confirmPasswordInputType: 'password',

    currentPassword: ''
  },
  handleBack() {
    wx.navigateTo({
      url: '/pages/login/index',
    })
  },
  onUsernameInput(e) {
    const username = e.detail.value;
    this.validateUsername(username);
  },
  onAccountInput(e) {
    const account = e.detail.value;
    this.validateAccount(account);
  },
  onPasswordInput(e) {
    const password = e.detail.value;
    this.validatePassword(password);
  },
  onConfirmPasswordInput(e) {
    const password = e.detail.value;
    this.validateConfirmPassword(password);
  },
  formSubmit(e) {
    const { value } = e.detail;
    this.validateUsername(value.username);
    this.validateAccount(value.account);
    this.validatePassword(value.password);
    this.validateConfirmPassword(value.confirmPassword);
    const { hasUsername, hasAccount, hasPassword, isTwoPasswordSame } = this.data
    if (!hasUsername || !hasAccount || !hasPassword || !isTwoPasswordSame) {
      return;
    }
    wx.login({
      success: (res) => {
        value.wechatCode = res.code;
        authApi.register(value).then(response => {
          Message.success({
            context: this,
            offset: [90, 32],
            duration: 3000,
            content: '注册成功！',
          });
          setTimeout(() => {
            // 动画完成后跳转页面
            wx.navigateTo({
              url: '/pages/login/index',
            })
          }, 1000);
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
  validateUsername(username) {
    if (!!username) {
      this.setData({
        hasUsername: true
      })
    } else {
      this.setData({
        hasUsername: false
      })
    }
  },
  validatePassword(password) {
    if (!!password) {
      this.setData({
        hasPassword: true,
        currentPassword: password
      })
    } else {
      this.setData({
        hasPassword: false
      })
    }
  },
  validateConfirmPassword(password) {
    if (!!password) {
      this.setData({
        hasConfirmPassword: true
      })
    } else {
      this.setData({
        hasConfirmPassword: false
      })
    }
    if (this.data.currentPassword === password) {
      this.setData({
        isTwoPasswordSame: true
      })
    } else {
      this.setData({
        isTwoPasswordSame: false
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
  onConfirmSuffixIconClick(e) {
    const { trigger } = e.detail;
    const { confirmPasswordIconName } = this.data
    if ('suffix-icon' == trigger) {
      if (confirmPasswordIconName == 'browse-off') {
        this.setData({
          confirmPasswordIconName: 'browse',
          confirmPasswordInputType: 'text'
        });
      } else {
        this.setData({
          confirmPasswordIconName: 'browse-off',
          confirmPasswordInputType: 'password'
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