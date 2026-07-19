import request from "./index.js"

export function toggleCollect(articleId) {
    return request.post('/collect/' +articleId)
}

export function getMyCollects(params) {
    return request.post('/collect/list', params)
}