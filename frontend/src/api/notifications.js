import request from './index.js'

export function getNotifications(params) {
    return request.post('/notification/list', params)
}

export function markAsRead(id) {
    return request.post('/notification/read/' + id)
}

export function markAllAsRead() {
    return request.post('/notification/read/all')
}

export function getUnreadCount(params) {
    return request.get('/notification/unread', params)
}