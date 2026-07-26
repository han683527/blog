<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <h2>管理后台</h2>
    </div>
    <el-menu
        :default-active="route.path"
        router
        background-color="#fff"
        text-color="#409eff"
        active-text-color="#fff"
    >
      <el-menu-item index="/">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="/users">
        <el-icon><UserFilled /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
      <el-menu-item index="/articles">
        <el-icon><Document /></el-icon>
        <span>文章管理</span>
      </el-menu-item>
      <el-menu-item index="/comments">
        <el-icon><ChatDotSquare /></el-icon>
        <span>评论管理</span>
      </el-menu-item>
      <el-menu-item index="/tags">
        <el-icon><PriceTag /></el-icon>
        <span>标签管理</span>
      </el-menu-item>
      <el-menu-item index="/categories">
        <el-icon><Folder /></el-icon>
        <span>分类管理</span>
      </el-menu-item>
    </el-menu>
    <div class="sidebar-footer">
      <span>{{ user?.nickname }}</span>
      <el-button size="small" @click="handleLogout">退出</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logout } from '@/api/login'
import { getUser } from '@/api/user'
import {
  HomeFilled, UserFilled, Document,
  ChatDotSquare, PriceTag, Folder
} from '@element-plus/icons-vue'

const route = useRoute()
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
.sidebar {
  width: 220px;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  background: #fff;
  box-shadow: 2px 0 8px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  z-index: 100;
}
.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #e8edf3;
}
.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  color: #409eff;
}
.el-menu {
  border-right: none !important;
  flex: 1;
}
.el-menu-item {
  margin: 4px 10px;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
}
.el-menu-item.is-active {
  background-color: #409eff !important;
}
.el-menu-item:hover {
  background-color: #ecf5ff;
}
.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #e8edf3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}
</style>
