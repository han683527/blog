<template>
  <div class="detail-page">
    <div v-if="loading" class="loading-state">加载中...</div>
    <div v-else class="detail-container">
      <!-- 文章头部 -->
      <div class="article-header">
        <h1>{{ article.title }}</h1>
        <div class="header-meta">
          <router-link :to="'/user/' + article.authorId" class="author-link">
            <img :src="article.authorAvatar" class="author-avatar" v-if="article.authorAvatar" />
            <span>{{ article.authorName }}</span>
          </router-link>
          <span class="dot">·</span>
          <span>阅读 {{ article.viewCount }}</span>
          <span class="dot">·</span>
          <span>点赞 {{ article.likeCount }}</span>
          <span class="dot">·</span>
          <span>收藏 {{ article.collectCount }}</span>
        </div>
      </div>

      <!-- 正文 -->
      <div class="article-content" v-html="article.content" />

      <!-- 点赞/收藏 -->
      <div class="action-bar" v-if="user">
        <el-button @click="handleLike" :type="article.isLike ? 'primary' : 'default'" round>
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor" style="margin-right:4px"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
          {{ article.isLike ? '已赞' : '点赞' }}
        </el-button>
        <el-button @click="handleCollect" :type="article.isCollect ? 'primary' : 'default'" round>
          <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor" style="margin-right:4px"><path d="M17 3H7c-1.1 0-1.99.9-1.99 2L5 21l7-3 7 3V5c0-1.1-.9-2-2-2z"/></svg>
          {{ article.isCollect ? '已收藏' : '收藏' }}
        </el-button>
        <el-button v-if="user?.id === article.authorId" @click="$router.push('/edit/' + article.id)" round>编辑</el-button>
      </div>

      <!-- 评论 -->
      <div class="comments-section">
        <h3 class="comments-title">评论 ({{ article.commentCount }})</h3>
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <router-link :to="'/user/' + c.userId" class="comment-user">
            <img :src="c.userAvatar" class="comment-avatar" v-if="c.userAvatar" />
            <span>{{ c.userName }}</span>
          </router-link>
          <div class="comment-body">{{ c.content }}</div>
          <div class="comment-footer">
            <span class="comment-time">{{ c.createTime }}</span>
            <el-button v-if="user?.id === article.authorId" type="danger" size="small" text @click="handleDeleteComment(c.id)">删除</el-button>
          </div>
        </div>
        <div class="add-comment">
          <el-input v-model="commentText" placeholder="写下你的评论..." class="comment-input" />
          <el-button type="primary" @click="handleAddComment" round>发表</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted} from "vue"
import {useRoute, useRouter} from "vue-router"
import {getUser} from "@/api/user"
import {getArticle} from "@/api/articles"
import {getComments, addComment, deleteComment} from "@/api/comment"
import {ElMessage, ElMessageBox} from "element-plus"
import {toggleLike} from "@/api/like"
import {toggleCollect} from "@/api/collect"

const route = useRoute()
const router = useRouter()
const user = ref(null)
const article = ref({})
const loading = ref(true)
const comments = ref([])
const commentText = ref('')

onMounted(async () => {
  try {
    const res = await getArticle(route.params.id)
    article.value = res.data.data

    if (localStorage.getItem('accessToken')) {
      const userRes = await getUser()
      user.value = userRes.data.data
    }

    const commentRes = await getComments({articleId: route.params.id, page: 1, size: 10})
    comments.value = commentRes.data.data.list
    window.addEventListener('sse-comment', onSseComment)
    window.addEventListener('sse-notification', onSseNotification)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
})

async function handleLike() {
  try {
    await toggleLike(route.params.id)
    article.value.isLike = !article.value.isLike
    article.value.likeCount += article.value.isLike ? 1 : -1
  } catch (e) {
    ElMessage.error(e.message || '点赞失败')
  }
}

async function handleCollect() {
  try {
    await toggleCollect(route.params.id)
    article.value.isCollect = !article.value.isCollect
    article.value.collectCount += article.value.isCollect ? 1 : -1
  } catch (e) {
    ElMessage.error(e.message || '收藏失败')
  }
}

async function handleAddComment() {
  if (!commentText.value) {
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    await addComment({articleId: Number(route.params.id), content: commentText.value})
    ElMessage.success('评论成功')
    commentText.value = ''
    const res = await getComments({articleId: route.params.id, page: 1, size: 10})
    comments.value = res.data.data.list
    article.value.commentCount = (article.value.commentCount || 0) + 1
  } catch (e) {
    ElMessage.error(e.message || '评论失败')
  }
}

async function handleDeleteComment(id) {
  await ElMessageBox.confirm('确定要删除这条评论吗?', '提示')
  await deleteComment(id)
  const res = await getComments({articleId: route.params.id, page: 1, size: 10})
  comments.value = res.data.data.list
  article.value.commentCount = (article.value.commentCount || 0) - 1
}

function onSseComment(e) {
  const data = e.detail
  if (data.type === 'NEW_COMMENT' && Number(data.articleId) === Number(route.params.id)) {
    getComments({ articleId: route.params.id, page: 1, size: 10}).then(res => {
      comments.value = res.data.data.list
    })
  }
  if (data.type === 'DELETE_COMMENT' && Number(data.articleId) === Number(route.params.id)) {
    getComments({articleId: route.params.id, page: 1, size: 10}).then(res => {
      comments.value = res.data.data.list
    })
  }
}

function onSseNotification(e) {
  // SSE 通知仅用于更新通知栏,点赞/收藏计数已在 handleLike/handleCollect 中处理
}

onUnmounted(() => {
  window.removeEventListener('sse-comment', onSseComment)
  window.removeEventListener('sse-notification', onSseNotification)
})
</script>

<style scoped>
.detail-page {
  max-width: 900px;
  width: 100%;
  margin: 0 auto;
  padding: 32px 24px;
  box-sizing: border-box;
}

.loading-state {
  text-align: center;
  color: var(--text-placeholder);
  padding: 80px 0;
}

.detail-container {
  background: var(--bg-card);
  border-radius: 10px;
  padding: 40px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
  transition: background 0.3s, border-color 0.3s;
}

/* ===== 文章头部 ===== */
.article-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-divider);
}

.article-header h1 {
  font-size: 26px;
  color: var(--text-primary);
  margin: 0 0 16px;
  line-height: 1.4;
}

.header-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-muted);
  flex-wrap: wrap;
}

.author-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: var(--link-color);
  font-weight: 500;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.dot {
  color: var(--text-placeholder);
}

/* ===== 正文 ===== */
.article-content {
  line-height: 1.8;
  font-size: 15px;
  color: var(--text-primary);
  overflow: hidden;
  word-break: break-word;
  overflow-wrap: break-word;
  text-align: left;
}

.article-content :deep(p) {
  margin: 16px 0;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 12px 0;
}

.article-content :deep(blockquote) {
  border-left: 4px solid var(--link-color);
  padding: 8px 16px;
  margin: 16px 0;
  background: var(--bg-hover);
  color: var(--text-secondary);
}

.article-content :deep(pre) {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
}

.article-content :deep(code) {
  font-size: 13px;
}

/* ===== 操作栏 ===== */
.action-bar {
  margin: 32px 0;
  padding-top: 24px;
  border-top: 1px solid var(--border-divider);
  display: flex;
  gap: 12px;
}

/* ===== 评论 ===== */
.comments-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-divider);
  text-align: left;
}

.comments-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border-divider);
}

.comment-item:last-of-type {
  border-bottom: none;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: var(--link-color);
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
}

.comment-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-body {
  color: var(--text-primary);
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
  overflow-wrap: break-word;
}

.comment-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
}

.comment-time {
  font-size: 12px;
  color: var(--text-placeholder);
}

.add-comment {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.comment-input {
  flex: 1;
}
</style>
