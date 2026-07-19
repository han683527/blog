import request from './index.js'

export function toggleLike(articleId) {
    return request.post('/like/' + articleId)
}

export function getMyLikes(params) {
    return request.post('/like/list', params)
}