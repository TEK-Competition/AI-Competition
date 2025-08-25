const request = require('../utils/request.js');

function register(data) {
  return request.execute({
    url: '/auth/register',
    method: 'POST',
    data: data
  });
}

function accountLogin(data) {
  return request.execute({
    url: '/auth/account/login',
    method: 'POST',
    data: data
  });
}

function wechatLogin(wechatCode) {
  const data = {
    wechatCode: wechatCode
  }
  return request.execute({
    url: '/auth/wechat/login',
    method: 'POST',
    data: data
  });
}

function logout() {
  return request.execute({
    url: '/auth/logout',
    method: 'POST'
  });
}


module.exports = {
  register,
  accountLogin,
  wechatLogin,
  logout
};