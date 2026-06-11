import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/oa': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        // 手动分包：将大库拆分为独立 chunk，利用浏览器并行加载和缓存
        manualChunks(id) {
          if (id.includes('node_modules/element-plus'))
            return 'element-plus'
          if (id.includes('node_modules/@element-plus/icons-vue'))
            return 'element-icons'
          if (id.includes('node_modules/echarts'))
            return 'echarts'
          if (id.includes('node_modules/@fullcalendar'))
            return 'fullcalendar'
        }
      }
    }
  }
})
