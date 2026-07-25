import {createRouter, createWebHistory} from 'vue-router'
import {getUser} from '@/api/user'
import {ElMessage} from "element-plus";

const routes = [
    // 后台管理登录
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue')
    },
    // 首页
    {
        path: '/',
        name: 'Home',
        component: () => import('@/views/admin/Home.vue')
    },
    // 用户管理
    {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/admin/Users.vue')
    },
    // 文章管理
    {
        path: '/articles',
        name: 'Articles',
        component: () => import('@/views/admin/Articles.vue')
    },
    // 评论管理
    {
        path: '/comments',
        name: 'Comments',
        component: () => import('@/views/admin/Comments.vue')
    },
    // 分类管理
    {
        path: '/categories',
        name: 'Categories',
        component: () => import('@/views/admin/Categories.vue')
    },
    // 标签管理
    {
        path: '/tags',
        name: 'Tags',
        component: () => import('@/views/admin/Tags.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach(async(to, from, next) => {
    if(to.path === '/login') {
        next()
        return
    }

    const token = localStorage.getItem('accessToken')
    if (!token) {
        next('/login')
        return
    }

    try {
        const res = await getUser()
        if(res.data.data.role !== 'admin') {
            ElMessage.error('权限不足')
            next('/login')
            return
        }
    } catch {
        next('/login')
        return
    }

    next()
})

export default router