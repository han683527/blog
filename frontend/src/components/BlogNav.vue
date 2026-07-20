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
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge">
          <el-popover placement="bottom" trigger="click" :width="350" @show="fetchNotifications">
            <template #reference>
              <el-button size="small" circle>🔔</el-button>
            </template>
            <div class="notification-panel">
              <div v-if="notifications.length === 0" class="empty">暂无通知</div>
              <div v-for="notification in notifications" :key="notification.id" class="notification-item"
                   @click="handleMarkAsRead(notification.id)">
                <div class="notification-content">
                  <span v-if="notification.type === 'LIKE'">{{ notification.actorNickname }} 赞了你的文章</span>
                  <span v-else-if="notification.type === 'COLLECT'">{{ notification.actorNickname }} 收藏了你的文章</span>
                  <span v-else-if="notification.type === 'COMMENT'">{{ notification.actorNickname }} 评论了你的文章</span>
                  <router-link :to="'/article/' + notification.articleId" class="notification-title">{{ notification.articleTitle }}</router-link>
                </div>
                <div class="notification-time">{{ notification.createTime }}</div>
              </div>
              <el-button size="small" @click="handleMarkAllAsRead" class="read-all">全部已读</el-button>
            </div>
          </el-popover>
        </el-badge>
        <span class="nickname">{{ user.nickname }}</span>
        <router-link to="/profile">
          <img :src="user.avatar" class="avatar"/>
        </router-link>
        <el-button size="small" @click="handleLogout">退出登录</el-button>
      </template>
    </div>
  </div>
</template>

<script setup>

import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {getUser} from '@/api/user'
import {getNotifications, markAsRead, markAllAsRead, getUnreadCount} from "@/api/notifications";

const router = useRouter()
const user = ref(null)
const unreadCount = ref(0)
const notifications = ref([])

onMounted(async () => {
  if (localStorage.getItem('accessToken')) {
    try {
      const res = await getUser()
      user.value = res.data.data
      unreadCount.value = (await getUnreadCount()).data.data
    } catch {
    }
  }
})

function handleLogout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  user.value = null
  router.push('/')
}

async function fetchNotifications() {
  const res = await getNotifications({page: 1, size: 10})
  notifications.value = res.data.data.list
}

async function handleMarkAsRead(id) {
  await markAsRead(id)
  unreadCount.value--
}

async function handleMarkAllAsRead() {
  await markAllAsRead()
  unreadCount.value = 0
  notifications.value.forEach(n => n.readFlag = true)
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

.link {
  text-decoration: none;
  color: #666
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.badge {
  margin-right: 4px;
}

.notification-panel {
  padding: 8px 0;
}

.notification-item {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-content {
  font-size: 13px;
}

.notification-title {
  margin-left: 4px;
  color: #409eff;
  text-decoration: none;
}

.notification-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.read-all {
  width: 100%;
  margin-top: 8px;
}

.empty {
  text-align: center;
  color: #999;
  padding: 20px;
  font-size: 13px;
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