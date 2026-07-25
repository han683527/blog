import request from  './index'

export function createTag(params) {
    return request.post('/admin/tag', params)
}

export function deleteTag(id) {
    return request.delete('/admin/tag/' + id)
}

export function updateTag(params) {
    return request.put('/admin/tag', params)
}

export function pageTags(params) {
    return request.post('/admin/tag/list', params)
}