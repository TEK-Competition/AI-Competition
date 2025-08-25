// pages/travel-plan/detail/index.js
const travelPlanApi = require('../../../apis/travel-plan');
Page({

  /**
   * 页面的初始数据
   */
  data: {
    travelPlanDetail: {},
    currentStep: 0
  },
  onStepChange(e) {
 
  },
  handleBack() {
    wx.reLaunch({
      url: 'pages/travel-plan/list/index',
    });
  },
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    const id = options.id;
    travelPlanApi.getDetailById(id).then(response => {
      const travelPlanDetail = response.data;
      const travelPlanList = travelPlanDetail.travelPlanResultList;
      this.setData({
        travelPlanDetail: travelPlanDetail,
        currentStep: travelPlanList.length - 1
      })
    });
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