import request from './index.js'

export function toggleLike(articleId) {
    return request.post('/like/' + articleId)
}