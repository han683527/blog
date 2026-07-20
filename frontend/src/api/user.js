import request from "./index.js"

export function getUser() {
    return request.get('/user/info')
}

export function updateProfile(data) {
    return request.post('/user/profile', data)
}

export function updateAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/user/avatar', formData, {
        headers: { 'Content-Type' : 'multipart/form-data' },
    })
}

export function getUserById(id) {
    return request.get('/user/public/' + id)
}