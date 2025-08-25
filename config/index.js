let config = {};

const deviceInfo = wx.getDeviceInfo();
const isDevTool = deviceInfo.platform === 'devtools';
config = isDevTool ? require('./dev') : require('./prod');

module.exports = config;