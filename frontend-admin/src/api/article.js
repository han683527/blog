import request from './index'

export function deleteArticle(id) {
    return request.delete('/admin/article/' + id)
}

export function updateArticle(params) {
    return request.put('/admin/article', params)
}

export function pageArticles(params) {
    return request.post('/admin/article/list', params)
}
