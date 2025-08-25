// pages/travel-plan/edit/index.js
const util = require('../../../utils/util.js');
const travelPlanApi = require('../../../apis/travel-plan');

Page({

  /**
   * 页面的初始数据
   */
  data: {
    defaultPreferenceList: [
      '自然风光',
      '拍照打卡',
      '风土人情',
      '历史古迹',
      '烟火气',
      '亲子游',
      '小众探索'
    ],
    preference: [],

    startDateVisible: false,
    startDate: util.calculateDate(1),
    startDateText: '',
    endDateVisible: false,
    endDate: util.calculateDate(15),
    endDateText: ''


  },
  handleBack() {
    wx.reLaunch({
      url: '/pages/travel-plan/list/index',
    });
  },
  preferenceChange(e) {
    const vaule = e.target.dataset.value;
    const checked = e.detail.checked;
    const currentPreference = this.data.preference;
    if (checked) {
      if (!currentPreference.includes(vaule)) {
        currentPreference.push(vaule);
      }
    } else {
      const index = currentPreference.indexOf(vaule);
      if (index > -1) {
        currentPreference.splice(index, 1);
      }
    }
    this.setData({
      preference: currentPreference
    });
  },
  formSubmit(e) {
    const { placeOfDeparture, destination, peopleAmount, budgetRange, additionalInstructions } = e.detail.value;
    const formData = {
      placeOfDeparture, destination, budgetRange, peopleAmount, additionalInstructions,
      preference: this.data.preference,
      startDate: this.data.startDateText,
      endDate: this.data.endDateText
    }
    travelPlanApi.generate(formData).then(response => {
      wx.reLaunch({
        url: '/pages/travel-plan/list/index',
      });
    });
  },

  showStartDate(e) {
    this.setData({
      startDateVisible: true,
    });
  },

  onCloseStartDate(e) {
    this.setData({
      startDateVisible: false,
    });
  },

  onConfirmStartDate(e) {
    const {
      value
    } = e.detail;
    this.setData({
      startDateText: value
    });
  },

  showEndDate(e) {
    this.setData({
      endDateVisible: true,
    });
  },

  onCloseEndDate(e) {
    this.setData({
      endDateVisible: false,
    });
  },

  onConfirmEndDate(e) {
    const {
      value
    } = e.detail;
    this.setData({
      endDateText: value
    });
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