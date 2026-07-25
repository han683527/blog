import request from "./index"

export function deleteUser(id) {
    return request.delete('/admin/user/' + id)
}

export function pageUsers(params) {
    return request.post('/admin/user/list', params)
}

// 路由守卫(判断权限)和导航栏需要
export function getUser() {
    return request.get('/user/info')
}