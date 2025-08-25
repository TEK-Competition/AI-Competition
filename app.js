const config = require('./config/index');

App({
  globalData: {
    config: config
  },
  onLaunch() {
    console.log('当前运行环境:', config.ENV);
  }
});