import request from './index.js'

export function getCategories(params) {
    return request.post('category/list', params)
}