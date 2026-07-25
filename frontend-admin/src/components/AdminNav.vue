<template>
  <div class="nav">
    <div class="left_nav">
      <router-link to="/">首页</router-link>
      <router-link to="/users">用户管理</router-link>
      <router-link to="/articles">文章管理</router-link>
      <router-link to="/comments">评论管理</router-link>
      <router-link to="/categories">分类管理</router-link>
      <router-link to="/tags">标签管理</router-link>
    </div>
    <div class="right_nav">
      <span>{{ user?.nickname }}</span>
      <el-button size="small" @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {logout} from '@/api/login'
import {getUser} from '@/api/user'

const router = useRouter()
const user = ref(null)

onMounted(async () => {
  try {
    const res = await getUser()
    user.value = res.data.data
  } catch {}
})

function handleLogout() {
  logout()
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  router.push('/login')
}
</script>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 50px;
  border-bottom: 1px solid #eee;
  background: #fff;
}
.left_nav {
  display: flex;
  gap: 20px;
}
.left_nav a {
  text-decoration: none;
  color: #666;
}
.right_nav {
  margin-left: auto;
  padding: 20px;
}
</style>