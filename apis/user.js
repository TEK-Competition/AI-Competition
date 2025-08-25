const request = require('../utils/request.js');

function getUserInfo() {
  return request.execute({
    url: '/externalUser/info',
    method: 'GET'
  });
}

module.exports = {
  getUserInfo
};
