<template>
  <div class="home">
    <h1>文章列表</h1>
    <div class="search">
      <el-input v-model="keyword" placeholder="搜点什么..." clearable @keyup.enter="handleSearch" style="width: 300px"/>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>
    <!-- 分类筛选 -->
    <div class="categories">
      <el-button v-for="c in categories" :key="c.id" :type="selectCategory === c.id ? 'primary' : 'default'" size="small" @click="filterByCategory(c.id)">
        {{ c.categoryName }}
      </el-button>
    </div>
    <!-- 标签筛选 -->
    <div class="tags">
      <el-button v-for="t in tags" :key="t.id" :type="selectTag.includes(t.id) ? 'primary' : 'default'" size="small" @click="filterByTag(t.id)">
        {{ t.tagName }}
      </el-button>
    </div>
    <!-- 文章列表 -->
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
    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="10"
          :current-page="currentPage"
          @current-change="onPageChange"
      />
      </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '@/api/articles'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'

const articles = ref([])
const loading = ref(true)
const categories = ref([])
const selectCategory = ref(null)
const tags = ref([])
const selectTag = ref([])
const currentPage = ref(1)
const total = ref(0)
const keyword = ref('')

onMounted(async () => {
  try {
    const [artRes, catRes, tagRes] = await Promise.all([
        getArticles({page: 1, size: 10}),
        getCategories({ page: 1, size: 10 }),
        getTags({ page: 1, size: 10 })
    ])
    articles.value = artRes.data.data.list
    total.value = artRes.data.data.total
    categories.value = catRes.data.data.list
    tags.value = tagRes.data.data.list
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
})

async function onPageChange(page) {
  currentPage.value = page
  loading.value = true
  loadArticles().finally(() => { loading.value = false })
}

async function loadArticles() {
  const params = { page: currentPage.value, size: 10 }
  if(keyword.value) params.keyword = keyword.value
  if (selectCategory.value) {
    params.categoryId = selectCategory.value
  }
  if (selectTag.value.length > 0) {
    params.tagIds = selectTag.value
  }
  try {
    const res = await getArticles(params)
    articles.value = res.data.data.list
    total.value = res.data.data.total
  } catch {
    articles.value = []
    total.value = 0
  }
}

async function filterByCategory(id) {
  if (selectCategory.value === id) {
    selectCategory.value = null
  } else {
    selectCategory.value = id
  }
  //筛选重置页数
  currentPage.value = 1
  // 加载中
  loading.value = true
  // 最终显示文章列表
  loadArticles().finally(async () => { loading.value = false })
}

async function filterByTag(id) {
  const index = selectTag.value.indexOf(id)
  if (index > -1) {
    selectTag.value.splice(index, 1)
  } else {
    selectTag.value.push(id)
  }
  //筛选重置页数
  currentPage.value = 1
  // 加载中
  loading.value = true
  // 最终显示文章列表
  loadArticles().finally(async () => { loading.value = false })
}

function handleSearch() {
  currentPage.value = 1
  loading.value = true
  loadArticles().finally(() => { loading.value = false })
}
</script>

<style scoped>
.search{
  padding: 20px;
}
.home {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
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
