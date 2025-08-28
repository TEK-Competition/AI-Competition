import { defineConfig } from 'vite';

export default defineConfig(({ mode }) => ({
  base: mode === 'production' ? '/pages/' : '/', // 仅生产环境添加 base
  // ...其他配置
}));