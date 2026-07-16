import request from "./index.js"

export function toggleCollect(articleId) {
    return request.post('/collect/' +articleId)
}