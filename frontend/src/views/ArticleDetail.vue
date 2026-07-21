<template>
  <div class="detail">
    <div v-if="loading">加载中...</div>
    <div v-else>
      <!-- 标题 -->
      <h1>{{ article.title }}</h1>
      <div class="author-info">
        <router-link :to="'/user/' + article.authorId" class="author-link">
          <img :src="article.authorAvatar" class="author-avatar" v-if="article.authorAvatar"/>
          <span>{{ article.authorName }}</span>
        </router-link>
      </div>
      <!-- 具体信息 -->
      <div class="meta">
        <span>阅读 {{ article.viewCount }}</span>
        <span>点赞 {{ article.likeCount }}</span>
        <span>收藏 {{ article.collectCount }}</span>
      </div>
      <!-- 正文 -->
      <div class="content" v-html="article.content"/>
      <!-- 点赞/收藏 -->
      <div class="actions" v-if="user">
        <el-button @click="handleLike" :type="article.isLike ? 'primary' : 'default'">
          {{ article.isLike ? '已赞' : '点赞' }}
        </el-button>
        <el-button @click="handleCollect" :type="article.isCollect ? 'primary' : 'default'">
          {{ article.isCollect ? '已收藏' : '收藏' }}
        </el-button>
      </div>
      <!-- 评论列表 -->
      <div class="comments">
        <h3>评论 ({{ article.commentCount }})</h3>
        <!-- 使用 userId 而不是 id(否则会导致跳转错误)-->
        <div v-for="c in comments" :key="c.id" class="content-item">
          <div class="comment-user">
            <router-link :to="'/user/' + c.userId">
              <img :src="c.userAvatar" class="comment-avatar" v-if="c.userAvatar"/>
              <span>{{ c.userName }}</span>
            </router-link>
          </div>
          <div class="comment-body">{{ c.content }}</div>
          <div class="comment-time">{{ c.createTime }}</div>
          <el-button v-if="user.id === article.authorId" type="danger" size="small" @click="handleDeleteComment(c.id)">
            删除
          </el-button>
        </div>
      </div>
      <!-- 发表评论 -->
      <div class="add-comment">
        <el-input v-model="commentText" placeholder="写下你的评论..."/>
        <el-button type="primary" @click="handleAddComment">发表</el-button>
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
  // 获取文章详情
  const res = await getArticle(route.params.id)
  article.value = res.data.data

  // 获取当前用户
  if (localStorage.getItem('accessToken')) {
    const userRes = await getUser()
    user.value = userRes.data.data
  }

  // 获取评论列表
  const commentRes = await getComments({articleId: route.params.id, page: 1, size: 10})
  comments.value = commentRes.data.data.list
  window.addEventListener('sse-comment', onSseComment)
  loading.value = false
})

async function handleLike() {
  try {
    await toggleLike(route.params.id)
    article.value.isLike = !article.value.isLike
    article.value.likeCount += article.value.isLike ? 1 : -1
    // // 重新获取文章详情,刷新点赞状态和数量
    // const res = await getArticle(route.params.id)
    // article.value = res.data.data
  } catch (e) {
    ElMessage.error(e.message || '点赞失败')
  }
}

async function handleCollect() {
  try {
    await toggleCollect(route.params.id)
    article.value.isCollect = !article.value.isCollect
    article.value.collectCount += article.value.isCollect ? 1 : -1
    // // 重新获取收藏详情
    // const res = await getArticle(route.params.id)
    // article.value = res.data.data
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
    // 重新加载评论列表
    const res = await getComments({articleId: route.params.id, page: 1, size: 10})
    comments.value = res.data.data.list
  } catch (e) {
    ElMessage.error(e.message || '评论失败')
  }
}

async function handleDeleteComment(id) {
  await ElMessageBox.confirm('确定要删除这条评论吗?', '提示')
  await deleteComment(id)
  const res = await getComments({articleId: route.params.id, page: 1, size: 10})
  comments.value = res.data.data.list
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

onUnmounted(() => {
  window.removeEventListener('sse-comment', onSseComment)
})
</script>

<style scoped>
.author-info {
  margin: 10px 0;
}

.author-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #409eff;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-user {
  margin-bottom: 4px;
}

.comment-user a {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  color: #409eff;
  font-size: 13px;
}

.comment-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.meta {
  color: #666;
  margin: 10px 0;
}

.meta span {
  margin-right: 16px;
}

.content {
  margin: 20px 0;
  line-height: 1.8;
}

.actions {
  margin: 20px 0;
}

.comments {
  margin-top: 40px;
}

.content-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.comment-time {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.add-comment {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}
</style>