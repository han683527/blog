import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {fileURLToPath, URL} from 'node:url'

// https://vite.dev/config/
export default defineConfig({
    plugins: [vue()], // Vue 支持
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)) // '@' 指向当前目录的 src/
        }
    },
    server: {
        port: 5174, // 开发服务端口
        proxy: { // 代理,解决跨域问题
            '/auth': 'http://localhost:8080',
            '/admin': 'http://localhost:8080',
            '/user': 'http://localhost:8080',
        }
    }
})
