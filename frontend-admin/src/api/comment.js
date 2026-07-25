import request from './index'

export function deleteComment(id) {
    return request.delete('/admin/comment/' + id)
}

export function pageComments(params) {
    return request.post('/admin/comment/list', params)
}

