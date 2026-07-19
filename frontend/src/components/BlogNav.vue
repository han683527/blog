<template>
  <div class="nav">
    <div class="nav-left">
      <router-link to="/" class="brand">ByteBlog</router-link>
      <router-link to="/" class="link">首页</router-link>
    </div>
    <div class="nav-right">
      <template v-if="!user">
        <router-link to="/login">
          <el-button type="primary" size="small">登录</el-button>
        </router-link>
        <router-link to="/register">
          <el-button size="small">注册</el-button>
        </router-link>
      </template>
      <template v-else>
        <router-link to="/create">
          <el-button size="small">写文章</el-button>
        </router-link>
          <span class="nickname">{{ user.nickname }}</span>
        <router-link to="/profile">
          <img :src="user.avatar" class="avatar" />
        </router-link>
        <el-button size="small" @click="handleLogout">退出登录</el-button>
      </template>
    </div>
  </div>
</template>

<script setup>

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUser } from '@/api/user'

const router = useRouter()
const user = ref(null)

onMounted(async () => {
  if(localStorage.getItem('accessToken')) {
    try {
      const res = await getUser()
      user.value = res.data.data
    } catch {}
  }
})

function handleLogout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  user.value = null
  router.push('/')
}
</script>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 50px;
  border-bottom: 1px solid #eee;
}
.nav-left {
  display: flex;
  align-items: center;
  gap: 24px;
}
.brand {
  font-size: 20px;
  font-weight: bold;
  text-decoration: none;
  color: #333;
}
.link{
  text-decoration: none;
  color: #666
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.nickname {
  font-weight: bold;
  color: #409eff;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
}
</style>