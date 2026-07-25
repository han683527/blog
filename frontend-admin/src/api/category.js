import request from './index'

export function createCategory(params) {
    return request.post('/admin/category', params)
}

export function deleteCategory(id) {
    return request.delete('/admin/category/' + id)
}

export function updateCategory(params) {
    return request.put('/admin/category', params)
}

export function pageCategories(params) {
    return request.post('/admin/category/list', params)
}
