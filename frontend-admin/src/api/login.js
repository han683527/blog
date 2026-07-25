import request from './index.js'

// 登录请求,提交邮箱和密码给后端
export function login(params) {
    return request.post('/auth/login', params)
}

// 登出
export function logout() {
    return request.post('/auth/logout')
}


