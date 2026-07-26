<template>
  <div class="home">
    <h1>首页</h1>

    <!-- 统计卡片 -->
    <div class="cards">
      <el-card class="card" @click="$router.push('/users')" shadow="hover">
        <div class="card-inner">
          <div class="card-icon" style="background:#ecf5ff;color:#409eff">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
          </div>
          <div>
            <p class="num">{{ stats.users }}</p>
            <p class="label">用户总数</p>
          </div>
        </div>
      </el-card>
      <el-card class="card" @click="$router.push('/articles')" shadow="hover">
        <div class="card-inner">
          <div class="card-icon" style="background:#f0f9eb;color:#67c23a">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm-1 7V3.5L18.5 9H13z"/></svg>
          </div>
          <div>
            <p class="num">{{ stats.articles }}</p>
            <p class="label">文章总数</p>
          </div>
        </div>
      </el-card>
      <el-card class="card" @click="$router.push('/comments')" shadow="hover">
        <div class="card-inner">
          <div class="card-icon" style="background:#fdf6ec;color:#e6a23c">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/></svg>
          </div>
          <div>
            <p class="num">{{ stats.comments }}</p>
            <p class="label">评论总数</p>
          </div>
        </div>
      </el-card>
      <el-card class="card" @click="$router.push('/tags')" shadow="hover">
        <div class="card-inner">
          <div class="card-icon" style="background:#f0f2ff;color:#606266">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M21.41 11.58l-9-9C12.05 2.22 11.55 2 11 2H4c-1.1 0-2 .9-2 2v7c0 .55.22 1.05.59 1.42l9 9c.36.36.86.58 1.41.58.55 0 1.05-.22 1.41-.59l7-7c.37-.36.59-.86.59-1.41 0-.55-.23-1.06-.59-1.42zM5.5 7C4.67 7 4 6.33 4 5.5S4.67 4 5.5 4 7 4.67 7 5.5 6.33 7 5.5 7z"/></svg>
          </div>
          <div>
            <p class="num">{{ stats.tags }}</p>
            <p class="label">标签总数</p>
          </div>
        </div>
      </el-card>
      <el-card class="card" @click="$router.push('/categories')" shadow="hover">
        <div class="card-inner">
          <div class="card-icon" style="background:#f5f0ff;color:#9060c8">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>
          </div>
          <div>
            <p class="num">{{ stats.categories }}</p>
            <p class="label">分类总数</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 最近文章 + 最近评论 -->
    <div class="sections">
      <el-card class="section" shadow="hover">
        <template #header>
          <div class="section-header">
            <span>最近文章</span>
            <el-button text size="small" @click="$router.push('/articles')">查看全部</el-button>
          </div>
        </template>
        <div v-if="recentArticles.length === 0" class="empty">暂无文章</div>
        <div v-else>
          <div v-for="a in recentArticles" :key="a.id" class="list-item" @click="$router.push('/articles')">
            <span class="list-title">{{ a.title }}</span>
            <span class="list-time">{{ a.createTime }}</span>
          </div>
        </div>
      </el-card>
      <el-card class="section" shadow="hover">
        <template #header>
          <div class="section-header">
            <span>最近评论</span>
            <el-button text size="small" @click="$router.push('/comments')">查看全部</el-button>
          </div>
        </template>
        <div v-if="recentComments.length === 0" class="empty">暂无评论</div>
        <div v-else>
          <div v-for="c in recentComments" :key="c.id" class="list-item" @click="$router.push('/comments')">
            <span class="list-title">{{ c.content }}</span>
            <span class="list-time">{{ c.createTime }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
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

const recentArticles = ref([])
const recentComments = ref([])

onMounted(async () => {
  const [u, a, c, t, ca, ra, rc] = await Promise.all([
    pageUsers({ page: 1, size: 1 }),
    pageArticles({ page: 1, size: 1 }),
    pageComments({ page: 1, size: 1 }),
    pageTags({ page: 1, size: 1 }),
    pageCategories({ page: 1, size: 1 }),
    pageArticles({ page: 1, size: 5 }),
    pageComments({ page: 1, size: 5 })
  ])
  stats.users = u.data.data.total
  stats.articles = a.data.data.total
  stats.comments = c.data.data.total
  stats.tags = t.data.data.total
  stats.categories = ca.data.data.total
  recentArticles.value = ra.data.data.list || []
  recentComments.value = rc.data.data.list || []
})
</script>

<style scoped>
h1 {

  font-size: 24px;
  margin-bottom: 24px;
  color: #303133;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.card {
  cursor: pointer;
  border: none;
}

.card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 4px 0;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.num {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 0;
  line-height: 1.2;
}

.label {
  font-size: 13px;
  color: #909399;
  margin: 4px 0 0;
}

.sections {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 15px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f4f8;
  cursor: pointer;
  transition: color 0.2s;
}

.list-item:last-child {
  border-bottom: none;
}

.list-item:hover .list-title {
  color: #409eff;
}

.list-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
  margin-right: 12px;
}

.list-time {
  font-size: 12px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.empty {
  text-align: center;
  color: #c0c4cc;
  padding: 30px 0;
  font-size: 14px;
}
</style>
