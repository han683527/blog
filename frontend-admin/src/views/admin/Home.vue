<template>
  <div>
    <h1>管理后台</h1>
    <div class="cards">
      <el-card class="card" @click="$router.push('/users')" style="cursor: pointer;">
        <h2>用户数</h2>
        <p class="num">{{ stats.users }}</p>
      </el-card>
      <el-card class="card" @click="$router.push('/articles')" style="cursor: pointer;">
        <h2>文章数</h2>
        <p class="num">{{ stats.articles }}</p>
      </el-card>
      <el-card class="card" @click="$router.push('/comments')" style="cursor: pointer;">
        <h2>评论数</h2>
        <p class="num">{{ stats.comments }}</p>
      </el-card>
      <el-card class="card" @click="$router.push('/tags')" style="cursor: pointer;">
        <h2>标签数</h2>
        <p class="num">{{ stats.tags }}</p>
      </el-card>
      <el-card class="card" @click="$router.push('/categories')" style="cursor: pointer;">
        <h2>分类数</h2>
        <p class="num">{{ stats.categories }}</p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { pageUsers } from '@/api/user'
import { pageArticles } from '@/api/article'
import { pageComments } from '@/api/comment'
import { pageTags } from '@/api/tag'
import { pageCategories } from '@/api/category'

const stats = reactive({
  users: 0,
  articles: 0,
  comments: 0,
  tags: 0,
  categories: 0
})

onMounted(async () => {
  // 并行查总数, size:1 只查数量不查列表,减少开销
  const [u, a, c, t, ca] = await Promise.all([
    pageUsers({ page: 1, size: 1 }),
    pageArticles({ page: 1, size: 1 }),
    pageComments({ page: 1, size: 1 }),
    pageTags({ page: 1, size: 1 }),
    pageCategories({ page: 1, size: 1 })
  ])
  stats.users = u.data.data.total
  stats.articles = a.data.data.total
  stats.comments = c.data.data.total
  stats.tags = t.data.data.total
  stats.categories = ca.data.data.total
})
</script>

<style scoped>
h1 {
  font-size: 24px;
  margin-bottom: 20px;
}
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}
.card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
}
.card:hover {
  transform: translateY(-4px);
}
.card h2 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #666;
}
.num {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
  margin: 0;
}
</style>