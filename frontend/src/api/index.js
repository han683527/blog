import axios from "axios";
import { ElMessage } from "element-plus";

const request = axios.create({
    baseURL: '/',
    timeout: 15000,
})

let refreshing = null
let logoutNotified = false

function skipRefresh(url) {
    if (!url) return true
    return url.includes('/auth/login')
        || url.includes('/auth/register')
        || url.includes('/auth/refresh')
        || url.includes('/auth/logout')
        || url.includes('/auth/code')
}

export async function refreshTokens() {
    if (refreshing) return refreshing
    refreshing = (async () => {
        const refreshToken = localStorage.getItem('refreshToken')
        if (!refreshToken) {
            throw new Error('未登录')
        }
        // 用裸 axios,避免走进本实例的 401 拦截造成死循环
        const res = await axios.post('/auth/refresh', { refreshToken }, { timeout: 15000 })
        if (res.data?.code !== 200) {
            throw new Error(res.data?.message || '登录已过期')
        }
        const { accessToken, refreshToken: nextRefresh } = res.data.data
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', nextRefresh)
        logoutNotified = false
        window.dispatchEvent(new Event('auth-token-refreshed'))
        return accessToken
    })().finally(() => {
        refreshing = null
    })
    return refreshing
}

function clearAuth() {
    const hadSession = localStorage.getItem('accessToken') || localStorage.getItem('refreshToken')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    if (hadSession && !logoutNotified) {
        logoutNotified = true
        ElMessage.warning('当前登录状态已失效，请重新登录')
        window.dispatchEvent(new Event('auth-logout'))
    }
}

export async function authFetch(url, options = {}) {
    const send = () => {
        const headers = { ...(options.headers || {}) }
        const token = localStorage.getItem('accessToken')
        if (token) {
            headers.Authorization = 'Bearer ' + token
        }
        return fetch(url, { ...options, headers })
    }
    let res = await send()
    if (res.status === 401 && localStorage.getItem('refreshToken')) {
        try {
            await refreshTokens()
            res = await send()
        } catch {
            clearAuth()
        }
    }
    return res
}

request.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken')
        if (token) {
            logoutNotified = false
            config.headers.Authorization = 'Bearer ' + token
        }
        return config
    },
    (error) => Promise.reject(error)
)

request.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code !== 200) {
            return Promise.reject(new Error(res.message))
        }
        return response
    },
    async (error) => {
        const original = error.config
        if (!original || original._retried || skipRefresh(original.url)) {
            return Promise.reject(error)
        }
        if (error.response?.status !== 401) {
            return Promise.reject(error)
        }
        original._retried = true
        try {
            await refreshTokens()
            original.headers = original.headers || {}
            original.headers.Authorization = 'Bearer ' + localStorage.getItem('accessToken')
            return request(original)
        } catch {
            clearAuth()
            return Promise.reject(error)
        }
    }
)

export default request
