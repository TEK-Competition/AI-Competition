const formatTime = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return `${[year, month, day].map(formatNumber).join('/')} ${[hour, minute, second].map(formatNumber).join(':')}`
}

const formatNumber = n => {
  n = n.toString()
  return n[1] ? n : `0${n}`
}

function calculateDate(num){
  // 获取当前时间
  let now = new Date();
  // 设置时间为明天的此时
  now.setDate(now.getDate() + num);
  // 获取时间戳（毫秒）
  let result = now.getTime();
  return result;
}

module.exports = {
  formatTime,
  calculateDate
}
