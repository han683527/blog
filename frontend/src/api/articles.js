import request from "./index.js";

export function getArticles(params) {
    return request.post('/article/list', params)
}

export function getArticle(id) {
    return request.get('/article/' + id)
}

export function createArticle(data) {
    return request.post('/article', data)
}

export function deleteArticle(id) {
    return request.delete('/article/' + id)
}

export function updateArticle(data) {
    return request.put('/article', data)
}

export function getRecommend() {
    return request.post('/article/recommend')
}