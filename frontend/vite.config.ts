import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const isDevelopment = mode === 'development'

  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
          configure: (proxy, _options) => {
            console.log(` Proxying /api requests to: http://localhost:8080`)
          }
        }
      }
    },
    define: {
      // Make API base URL available globally
      __API_BASE_URL__: JSON.stringify(isDevelopment ? '/api' : 'https://codegen.xaff.dev/api'),
      __DEV_MODE__: JSON.stringify(isDevelopment)
    }
  }
});