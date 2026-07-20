<template>
  <div class="public-profile">
    <div class="user-header">
      <img :src="user.avatar" class="avatar" v-if="user.avatar">
      <div v-else class="avatar-placeholder">无头像</div>
      <h2>{{ user.nickname }}</h2>
    </div>

    <h3>他的文章</h3>
    <div v-if="loading">加载中...</div>
    <div v-else>
      <div v-for="article in articles" :key="article.id" class="item">
        <router-link :to="'/article/' + article.id" class="title">{{ article.title }}</router-link>
        <div class="meta">
          <span>{{ article.createTime }}</span>
          <span>评论 {{ article.commentCount }}</span>
          <span>赞 {{ article.likeCount }}</span>
          <span>收藏 {{ article.collectCount }}</span>
        </div>
      </div>
      <div v-if="articles.length === 0" class="empty">还没有文章</div>
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
  const useRes = await getUserById(route.params.id)
  user.value = useRes.data.data
  const artRes = await getArticles({authorId: route.params.id, page: 1,size: 10 })
  articles.value = artRes.data.data.list
  loading.value = false
})
</script>

<style scoped>
.public-profile {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.user-header {
  text-align: center;
  padding: 40px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  margin: 0 auto;
}

.item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 16px;
  font-weight: bold;
  text-decoration: none;
  color: #333;
}

.meta {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.meta span {
  margin-right: 12px;
}

.empty {
  text-align: center;
  color: #999;
  padding: 40px;
}
</style>