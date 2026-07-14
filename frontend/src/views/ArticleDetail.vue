<template>
  <div class="detail">
    <div v-if="loading">加载中...</div>
    <div v-else>
      <!-- 标题 -->
      <h1>{{ article.title }}</h1>
      <!-- 具体信息 -->
      <div class="meta">
        <span>阅读 {{ article.viewCount }}</span>
        <span>点赞 {{ article.likeCount }}</span>
        <span>收藏 {{ article.collectCount }}</span>
      </div>
      <!-- 正文 -->
      <div class="content" v-html="article.content"/>
      <!-- 点赞/收藏 -->
      <div class="actions">
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
        <div v-for="c in comments" :key="c.id" class="content-item">
          <div class="comment-body">{{ c.content }}</div>
          <div class="comment-time">{{ c.createTime }}</div>
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
import { ref, onMounted } from "vue"
import { useRoute } from "vue-router"
import { getArticle } from "@/api/articles"
import { getComments, addComment} from "@/api/comment"
import { ElMessage} from "element-plus"

const route = useRoute()
const article = ref({})
const loading = ref(true)
const comments = ref([])
const commentText = ref('')

onMounted(async () => {
  // 1. 获取文章详情
  const res = await getArticle(route.params.id)
  article.value = res.data.data
  // 2. 获取评论列表
  const commentRes = await getComments({ articleId: route.params.id, page: 1, size: 10 })
  comments.value = commentRes.data.data.list
  loading.value = false
})

async function handleLike() {
  // TODO 调用点赞接口
}

async function handleCollect() {
  // TODO 调用收藏接口
}

async function handleAddComment() {
  // TODO 调用发表评论接口
}
</script>

<style scoped>
.detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.meta {
  color: #666;
  margin: 10px 0;
}
.meta span{
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