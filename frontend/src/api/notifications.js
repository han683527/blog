import request from './index.js'

export function getNotifications(params) {
    return request.post('notification/list', params)
}

export function markAsRead(params) {
    return request.post('notification/read', params)
}

export function markAllAsRead() {
    return request.post('notification/read/all')
}

export function getUnreadCount(params) {
    return request.post('notification/unread', params)
}