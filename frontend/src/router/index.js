import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    // 首页
    {
        path: '/',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
    },

    // 登录
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue')
    },

    // 注册
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue')
    },

    // 文章详情
    {
        path: '/article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/ArticleDetail.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router