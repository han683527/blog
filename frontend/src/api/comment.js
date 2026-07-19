import request from './index.js'

export function getComments(params) {
    return request.post('/comment/list', params)
}

export function addComment(data) {
    return request.post('/comment', data)
}

export function deleteComment(id) {
    return request.delete('/comment/' + id)
}