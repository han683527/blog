import request from './index.js'

export function getTags(params) {
    return request.post('/tag/list', params)
}