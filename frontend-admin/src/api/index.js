import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/',
    timeout: 15000
})

// 请求拦截器 —— 每次发请求前自动执行
request.interceptors.request.use(
    (config) => {
        // 从 localStorage 取出 token
        const token = localStorage.getItem('accessToken')
        if (token) {
            // 有 token 加到请求头,后端靠这个验证身份
            config.headers.Authorization = 'Bearer ' + token
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器 —— 收到响应后自动执行
request.interceptors.response.use(
    (response) => {
        const res = response.data
        // 后端统一返回 Result<T>, code=200 为成功
        if (res.code !== 200) {
            // 非 200 直接抛出错误,调用 catch 处理
            return Promise.reject(new Error(res.message))
        }
        return response
    },
    (error) => {
        return Promise.reject(error)
    }
)

export default request