import request from './index.js'

export function login(data) {
    return request.post('/auth/login', data)
}

export function register(data) {
    return request.post('/auth/register', data)
}

export function sendCode(email) {
    return request.post('/auth/code', null, { params: { email } })
}

export function logout() {
    return request.post('/auth/logout')
}