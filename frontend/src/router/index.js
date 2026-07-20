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
    },

    // 用户个人中心
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
    },

    // 写文章
    {
      path: '/create',
      name: 'Create',
      component: () => import('@/views/ArticleCreate.vue')
    },

    // 公开用户信息
    {
        path: '/user/:id',
        name: 'UserPublic',
        component: () => import('@/views/UserPublic.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router