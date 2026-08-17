import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 创建 Vue 应用实例
const app = createApp(App)
// app.use() 就是安装插件
// 注册路由——让应用知道"什么路径显示什么页面"
app.use(router)
// 注册组件库——让你在页面里用 <el-button> 等组件
app.use(ElementPlus)
// 挂载到 index.html 里的 <div id="app">
app.mount('#app')