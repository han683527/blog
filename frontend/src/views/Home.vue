<template>
  <div class="home">
    <h1>文章列表</h1>
    <div v-if="loading">加载中...</div>
    <div v-else>
      <div v-for="article in articles" :key="article.id" class="article-card">
        <h2>
          <router-link :to="'/article/' +article.id">{{ article.title }}</router-link>
        </h2>
        <p>{{ article.content }}</p>
        <div>
          <span>阅读量: {{ article.viewCount }}</span>
          <span>点赞: {{ article.likeCount }}</span>
          <span>收藏: {{ article.collectCount }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '@/api/articles'

const articles = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getArticles({ page: 1, size: 10})
    articles.value = res.data.data.list
  } catch (e) {
    console.error('文章加载失败',e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.article-card {
  border: 1px solid #ddd;
  padding: 16px;
  margin-bottom: 12px;
  border-radius: 8px;
}
.meta span {
  margin-right: 12px;
  color: #666;
}
.article-card a {
  text-decoration: none;
  color: inherit;
}
</style>
