import request from "./index.js";

export function getArticles(params) {
    return request.post('/article/list',params)
}

export function getArticle(id) {
    return request.get('/article/' + id);
}