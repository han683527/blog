<template>
  <div class="navbar">
    <div class="nav-inner">
      <div class="nav-left">
        <router-link to="/" class="brand">ByteBlog</router-link>
        <router-link to="/" class="nav-link">首页</router-link>
      </div>
      <div class="nav-right">
        <template v-if="!user">
          <router-link to="/login">
            <el-button type="primary" size="small" round>登录</el-button>
          </router-link>
          <router-link to="/register">
            <el-button size="small" round>注册</el-button>
          </router-link>
        </template>
        <template v-else>
          <router-link to="/create">
            <el-button size="small" round>写文章</el-button>
          </router-link>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge">
            <el-popover placement="bottom" trigger="click" :width="350" @show="fetchNotifications">
              <template #reference>
                <el-button size="small" circle class="bell-btn">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/></svg>
                </el-button>
              </template>
              <div class="notification-panel">
                <div class="panel-header">
                  <span>通知</span>
                  <el-button v-if="notifications.length > 0" text size="small" @click="handleMarkAllAsRead">全部已读</el-button>
                </div>
                <div v-if="notifications.length === 0" class="empty">暂无通知</div>
                <div v-for="notification in notifications" :key="notification.id" class="notification-item" @click="handleMarkAsRead(notification.id)">
                  <router-link :to="'/article/' + notification.articleId" class="notification-link">
                    <div class="notification-content">
                      <span v-if="notification.type === 'LIKE'">{{ notification.actorNickname }} 赞了你的文章</span>
                      <span v-else-if="notification.type === 'COLLECT'">{{ notification.actorNickname }} 收藏了你的文章</span>
                      <span v-else-if="notification.type === 'COMMENT'">{{ notification.actorNickname }} 评论了你的文章</span>
                      <span v-else-if="notification.type === 'DELETE_COMMENT'">文章的作者 {{ notification.actorNickname }} 删除了你的评论</span>
                      <span class="notification-title">{{ notification.articleTitle }}</span>
                    </div>
                    <div class="notification-time">{{ notification.createTime }}</div>
                  </router-link>
                </div>
              </div>
            </el-popover>
          </el-badge>
          <router-link to="/profile" class="user-info">
            <img :src="user.avatar" class="avatar" v-if="user.avatar" />
            <span class="nickname">{{ user.nickname }}</span>
          </router-link>
          <el-button size="small" text @click="handleLogout">退出</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, watch} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {getUser} from '@/api/user'
import {getNotifications, markAsRead, markAllAsRead, getUnreadCount} from "@/api/notifications"
import {logout} from '@/api/auth'

const router = useRouter()
const route = useRoute()
const user = ref(null)
const unreadCount = ref(0)
const eventSource = ref(null)
const notifications = ref([])

async function fetchUser() {
  if (!localStorage.getItem('accessToken')) {
    user.value = null
    return
  }
  try {
    const res = await getUser()
    user.value = res.data.data
    unreadCount.value = (await getUnreadCount()).data.data
    connectSSE()
  } catch {
    user.value = null
  }
}

onMounted(fetchUser)

watch(() => route.path, () => {
  if (localStorage.getItem('accessToken') && !user.value) {
    fetchUser()
  } else if (!localStorage.getItem('accessToken') && user.value) {
    user.value = null
  }
})

function handleLogout() {
  logout()
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  user.value = null
  disconnectSSE()
  router.push('/')
}

function connectSSE() {
  const token = localStorage.getItem('accessToken')
  if (!token) return
  if (eventSource.value) eventSource.value.close()
  const es = new EventSource('/notification/subscribe?token=' + encodeURIComponent(token))
  eventSource.value = es
  es.addEventListener('notification', (e) => {
    try {
      const data = JSON.parse(e.data)
      window.dispatchEvent(new CustomEvent('sse-notification', {detail: data}))
    } catch {}
    getUnreadCount().then(res => { unreadCount.value = res.data.data })
  })
  es.addEventListener('comment', (e) => {
    try {
      const data = JSON.parse(e.data)
      window.dispatchEvent(new CustomEvent('sse-comment', {detail: data}))
    } catch {}
    getUnreadCount().then(res => { unreadCount.value = res.data.data })
  })
  es.onerror = () => {}
}

function disconnectSSE() {
  if (eventSource.value) {
    eventSource.value.close()
    eventSource.value = null
  }
}

async function fetchNotifications() {
  const res = await getNotifications({page: 1, size: 10})
  notifications.value = res.data.data.list
}

async function handleMarkAsRead(id) {
  await markAsRead(id)
  unreadCount.value = Math.max(0, unreadCount.value - 1)
  notifications.value = notifications.value.filter(n => n.id !== id)
}

async function handleMarkAllAsRead() {
  await markAllAsRead()
  unreadCount.value = 0
  notifications.value = []
}
</script>

<style scoped>
.navbar {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 200;
}

.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  font-size: 20px;
  font-weight: 700;
  text-decoration: none;
  color: #409eff;
  letter-spacing: 1px;
}

.nav-link {
  text-decoration: none;
  color: #606266;
  font-size: 14px;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #409eff;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.badge {
  margin-right: 4px;
}

.bell-btn {
  font-size: 18px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}

.nickname {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

/* 通知面板 */
.notification-panel {
  max-height: 360px;
  overflow-y: auto;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 12px 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #f0f4f8;
}

.notification-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f4f8;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-link {
  display: block;
  text-decoration: none;
  color: inherit;
}

.notification-item:hover {
  background: #f8faff;
}

.notification-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.notification-title {
  display: block;
  color: #409eff;
  font-size: 12px;
  margin-top: 2px;
}

.notification-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}

.empty {
  text-align: center;
  color: #c0c4cc;
  padding: 30px 0;
  font-size: 13px;
}
</style>
