// index.js 
import Message from 'tdesign-miniprogram/message/index';
const travelPlanApi = require('../../../apis/travel-plan');
const app = getApp();
Page({
  data: {
    env: app.globalData.config.ENV,
    refresherTriggered: false,
    travelPlanList: [],
    tabBarValue: 'List',
    tabBarList: [
      { value: 'List', icon: 'table', },
      { value: 'Edit', icon: 'add-circle-filled', },
      { value: 'User', icon: 'user', },
    ],
  },
  onTabBarChange(e) {
    const value = e.detail.value;
    if (value === 'Edit') {
      wx.navigateTo({
        url: '/pages/travel-plan/edit/index',
      })
    } else if (value === 'User') {
      wx.navigateTo({
        url: '/pages/user-center/index',
      })
    }
  },
  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    this.getListData("");
  },
  clearSearch() {
    this.getListData("");
  },
  searchTravelPlan(e) {
    const destination = e.detail.value;
    this.getListData(destination);
  },
  onRefresherRefresh() {
    this.setData({
      refresherTriggered: true
    });
    this.getListData("");
  },
  getListData(destination) {
    travelPlanApi.queryForList(destination).then(response => {
      this.setData({
        travelPlanList: response.data,
        refresherTriggered: false
      });
    });
  },
  toDetailPage(e) {
    const dataset = e.currentTarget.dataset;
    const id = dataset.id;
    const status = dataset.status;
    if (status == 'IN_PROGRESS') {
      Message.info({
        context: this,
        offset: [90, 32],
        duration: 3000,
        content: '正在处理，请耐心等待...',
      });
    } else if (status == 'FAIL') {
      travelPlanApi.reGenerate(id).then(response => {
        this.getListData("");
      });
    } else {
      wx.navigateTo({
        url: '/pages/travel-plan/detail/index?id=' + id,
      })
    }

  }
})