import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL} from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
    resolve: {
      alias: {
          '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 5173,
      proxy: {
          '/auth': {
              target: 'http://localhost:8080',
              bypass: (req) => {
                  if (req.headers.accept?.includes('text/html')) {
                      return '/index.html'
                  }
              }
          },
          '/article': {
              target: 'http://localhost:8080',
              bypass: (req) => {
                  if (req.headers.accept?.includes('text/html')) {
                      return '/index.html'
                  }
              }
          },
          '/comment': {
              target: 'http://localhost:8080',
              bypass: (req) => {
                  if (req.headers.accept?.includes('text/html')) {
                      return '/index.html'
                  }
              }
          },
          '/like': 'http://localhost:8080',
          '/collect': 'http://localhost:8080',
          '/user': {
              target: 'http://localhost:8080',
              bypass: (req) => {
                  if (req.headers.accept?.includes('text/html')) {
                      return '/index.html'
                  }
              }
          },
          '/notification': 'http://localhost:8080',
          '/category': 'http://localhost:8080',
          '/tag': 'http://localhost:8080',
          '/upload': 'http://localhost:8080',
      }
    }
})
