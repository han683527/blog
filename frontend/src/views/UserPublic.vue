<template>
  <div class="public-page">
    <div class="public-card">
      <!-- 用户信息 -->
      <div class="user-header">
        <img :src="user.avatar" class="avatar" v-if="user.avatar">
        <div v-else class="avatar-placeholder">无头像</div>
        <h2>{{ user.nickname }}</h2>
        <p class="user-email" v-if="user.email">{{ user.email }}</p>
      </div>

      <el-divider />

      <!-- 文章列表 -->
      <h3 class="section-title">TA 的文章</h3>
      <div v-if="loading" class="status-text">加载中...</div>
      <div v-else>
        <div v-for="article in articles" :key="article.id" class="article-item" @click="$router.push('/article/' + article.id)">
          <div class="article-title">{{ article.title }}</div>
          <div class="article-meta">
            <span>{{ article.createTime }}</span>
            <span>评论 {{ article.commentCount }}</span>
            <span>赞 {{ article.likeCount }}</span>
            <span>收藏 {{ article.collectCount }}</span>
          </div>
        </div>
        <div v-if="articles.length === 0" class="status-text">还没有文章</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRoute} from 'vue-router'
import {getUserById} from '@/api/user'
import {getArticles} from '@/api/articles'

const route = useRoute()
const user = ref({})
const articles = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const useRes = await getUserById(route.params.id)
    user.value = useRes.data.data
    const artRes = await getArticles({authorId: route.params.id, page: 1, size: 10 })
    articles.value = artRes.data.data.list
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.public-page {
  width: 600px;
  margin: 0 auto;
  padding: 32px 24px;
}

.public-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  border: 1px solid #ebeef5;
}

.user-header {
  text-align: center;
  padding: 24px 0 16px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #ecf5ff;
  box-shadow: 0 4px 12px rgba(64,158,255,0.12);
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  margin: 0 auto;
  box-shadow: 0 4px 12px rgba(64,158,255,0.12);
}

.user-header h2 {
  margin: 16px 0 6px;
  font-size: 22px;
  color: #303133;
}

.user-email {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.public-card :deep(.el-divider) {
  margin: 24px 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 16px;
  background: #409eff;
  border-radius: 2px;
}

.status-text {
  text-align: center;
  color: #c0c4cc;
  padding: 40px 0;
  font-size: 14px;
}

.article-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f4f8;
  cursor: pointer;
  transition: background 0.2s;
  border-radius: 6px;
  padding-left: 12px;
  padding-right: 12px;
  margin: 0 -12px;
}

.article-item:last-child {
  border-bottom: none;
}

.article-item:hover {
  background: #f8faff;
}

.article-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.article-item:hover .article-title {
  color: #409eff;
}

.article-meta {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 6px;
}

.article-meta span {
  margin-right: 14px;
}
</style>
