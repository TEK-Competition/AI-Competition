const request = require('../utils/request.js');

function getDetailById(id) {
  return request.execute({
    url: `/travelPlan/${id}`,
    method: 'GET'
  });
}

function queryForList(data) {
  return request.execute({
    url: `/travelPlan/list?destination=${data}`,
    method: 'GET'
  });
}

function generate(data) {
  return request.execute({
    url: '/travelPlan/generate',
    method: 'POST',
    data: data
  });
}

function reGenerate(id) {
  return request.execute({
    url: `/travelPlan/generate/${id}`,
    method: 'POST'
  });
}

module.exports = {
  getDetailById,
  queryForList,
  generate,
  reGenerate
};
