const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/project',
    createProxyMiddleware({
      target: 'http://8.134.218.222:7000',
      changeOrigin: true,
      pathRewrite: {
        '^/project': ''
      }
    })
  );
};  