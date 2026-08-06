<template>
  <div class="home-page">
    <div class="home-layout">
      <!-- 左侧筛选 -->
      <div class="left-panel">
        <div class="filter-card">
          <div class="filter-section">
            <div class="filter-header">
              <el-icon><svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg></el-icon>
              <span>分类</span>
            </div>
            <div class="filter-tags">
              <el-tag
                v-for="c in categories" :key="c.id"
                :type="selectCategory === c.id ? 'primary' : 'info'"
                @click="filterByCategory(c.id)"
                class="filter-tag"
                effect="plain"
              >{{ c.categoryName }}</el-tag>
              <span v-if="categories.length === 0" class="filter-empty">暂无分类</span>
            </div>
          </div>
          <el-divider />
          <div class="filter-section">
            <div class="filter-header">
              <el-icon><svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M21.41 11.58l-9-9C12.05 2.22 11.55 2 11 2H4c-1.1 0-2 .9-2 2v7c0 .55.22 1.05.59 1.42l9 9c.36.36.86.58 1.41.58.55 0 1.05-.22 1.41-.59l7-7c.37-.36.59-.86.59-1.41 0-.55-.23-1.06-.59-1.42zM5.5 7C4.67 7 4 6.33 4 5.5S4.67 4 5.5 4 7 4.67 7 5.5 6.33 7 5.5 7z"/></svg></el-icon>
              <span>标签</span>
            </div>
            <div class="filter-tags">
              <el-tag
                v-for="t in tags" :key="t.id"
                :type="selectTag.includes(t.id) ? 'primary' : 'info'"
                @click="filterByTag(t.id)"
                class="filter-tag"
                effect="plain"
              >{{ t.tagName }}</el-tag>
              <span v-if="tags.length === 0" class="filter-empty">暂无标签</span>
            </div>
          </div>
        </div>

        <!-- 推荐位 -->
        <div class="filter-card recommend-card">
          <div class="filter-section">
            <div class="filter-header">
              <el-icon><svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg></el-icon>
              <span>为你推荐</span>
            </div>
            <div v-if="recommends.length === 0" class="filter-empty">暂无推荐</div>
            <div v-else class="recommend-list">
              <div
                v-for="(article, index) in recommends.slice(0, 8)"
                :key="article.id"
                class="recommend-item"
                @click="$router.push('/article/' + article.id)"
              >
                <span class="recommend-index" :class="{ hot: index < 3 }">{{ index + 1 }}</span>
                <span class="recommend-title">{{ article.title }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧文章列表 -->
      <div class="right-panel">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <div class="search-inner">
            <el-icon class="search-icon"><svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></el-icon>
            <el-input v-model="keyword" placeholder="搜索文章..." clearable @keyup.enter="handleSearch" class="search-input" :prefix-icon="null" />
            <el-button type="primary" @click="handleSearch" round>搜索</el-button>
          </div>
        </div>
        <div v-if="loading" class="status">
          <el-icon class="loading-icon"><svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M12 4V1L8 5l4 4V6c3.31 0 6 2.69 6 6 0 1.01-.25 1.97-.7 2.8l1.46 1.46C19.54 15.03 20 13.57 20 12c0-4.42-3.58-8-8-8zm0 14c-3.31 0-6-2.69-6-6 0-1.01.25-1.97.7-2.8L5.24 7.74C4.46 8.97 4 10.43 4 12c0 4.42 3.58 8 8 8v3l4-4-4-4v3z"/></svg></el-icon>
          加载中...
        </div>
        <div v-else-if="articles.length === 0" class="status">
          <el-icon><svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12z"/></svg></el-icon>
          <p>暂无文章</p>
        </div>
        <div v-else class="article-list">
          <div v-for="article in articles" :key="article.id" class="article-card" @click="$router.push('/article/' + article.id)">
            <div v-if="getImages(article.content).length" class="card-imgs">
              <img v-for="img in getImages(article.content).slice(0, 3)" :key="img" :src="img" class="card-img" />
            </div>
            <h2>
              <router-link :to="'/article/' + article.id" @click.stop v-html="article.highlightTitle || article.title"></router-link>
            </h2>
            <p class="content-preview" v-html="article.highlightContent || stripMarkdown(article.content)" />
            <div class="card-meta">
              <span v-if="getCategoryName(article.categoryId)" class="meta-tag category-tag">{{ getCategoryName(article.categoryId) }}</span>
              <span v-for="tagId in article.tags?.slice(0, 3)" :key="tagId" class="meta-tag tag-tag">{{ getTagName(tagId) }}</span>
            </div>
            <div class="card-footer">
              <span class="author-text">
                <el-icon><svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg></el-icon>
                {{ article.authorName }}
              </span>
              <span class="meta-item">
                <el-icon><svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg></el-icon>
                {{ article.viewCount }}
              </span>
              <span class="meta-item">
                <el-icon><svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg></el-icon>
                {{ article.likeCount }}
              </span>
            </div>
          </div>
        </div>
        <div class="pagination" v-if="total > 10">
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
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {getArticles, getRecommend} from '@/api/articles'
import {getCategories} from '@/api/category'
import {getTags} from '@/api/tag'

const articles = ref([])
const recommends = ref([])
const loading = ref(true)
const total = ref(0)
const currentPage = ref(1)
const keyword = ref('')
const categories = ref([])
const tags = ref([])
const selectCategory = ref(null)
const selectTag = ref([])

onMounted(async () => {
  try {
    const artRes = await getArticles({page: 1, size: 10})
    articles.value = artRes.data.data.list
    total.value = artRes.data.data.total
  } catch (e) {
    console.error('加载文章失败', e)
  }
  try {
    const catRes = await getCategories({page: 1, size: 100})
    categories.value = catRes.data.data.list
  } catch (e) {
    console.error('加载分类失败', e)
  }
  try {
    const tagRes = await getTags({page: 1, size: 100})
    tags.value = tagRes.data.data.list
  } catch (e) {
    console.error('加载标签失败', e)
  }
  // 推荐(游客/新用户返回热门,有行为返回个性化),失败静默不阻塞首页
  try {
    const recRes = await getRecommend()
    recommends.value = recRes.data.data || []
  } catch (e) {
    console.error('加载推荐失败', e)
  }
  loading.value = false
})

async function loadArticles() {
  const params = {page: currentPage.value, size: 10}
  if (keyword.value) params.keyword = keyword.value
  if (selectCategory.value) params.categoryId = selectCategory.value
  if (selectTag.value.length > 0) params.tagIds = selectTag.value
  try {
    const res = await getArticles(params)
    articles.value = res.data.data.list
    total.value = res.data.data.total
  } catch {
    articles.value = []
    total.value = 0
  }
}

function filterByCategory(id) {
  selectCategory.value = selectCategory.value === id ? null : id
  currentPage.value = 1
  loading.value = true
  loadArticles().finally(() => { loading.value = false })
}

function filterByTag(id) {
  const idx = selectTag.value.indexOf(id)
  if (idx > -1) selectTag.value.splice(idx, 1)
  else selectTag.value.push(id)
  currentPage.value = 1
  loading.value = true
  loadArticles().finally(() => { loading.value = false })
}

function handleSearch() {
  currentPage.value = 1
  loading.value = true
  loadArticles().finally(() => { loading.value = false })
}

function onPageChange(page) {
  currentPage.value = page
  loading.value = true
  loadArticles().finally(() => { loading.value = false })
}

function getCategoryName(id) {
  const c = categories.value.find(c => c.id === id)
  return c ? c.categoryName : ''
}

function getTagName(id) {
  const t = tags.value.find(t => t.id === id)
  return t ? t.tagName : ''
}

function getImages(md) {
  if (!md) return []
  const urls = []
  const re = /!\[.*?\]\((.*?)\)/g
  let m
  while ((m = re.exec(md)) !== null) {
    if (m[1]) urls.push(m[1])
  }
  return urls
}

function stripMarkdown(md) {
  if (!md) return ''
  return md
    .replace(/!\[.*?\]\(.*?\)/g, '')
    .replace(/\[([^\]]*)\]\(.*?\)/g, '$1')
    .replace(/[#*`~>]{1,}/g, '')
    .replace(/---+/g, '')
    .replace(/\n{2,}/g, ' ')
    .trim()
}
</script>

<style scoped>
.home-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* ===== 搜索栏 ===== */
.search-bar {
  margin-bottom: 16px;
}

.search-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 24px;
  padding: 4px 4px 4px 16px;
  transition: box-shadow 0.2s, border-color 0.2s, background 0.3s;
}

.search-inner:focus-within {
  border-color: var(--link-color);
  box-shadow: 0 0 0 3px rgba(64,158,255,0.12);
}

.search-icon {
  color: var(--text-placeholder);
  display: flex;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  background: none;
  box-shadow: none !important;
  padding: 0;
}

/* ===== 布局 ===== */
.home-layout {
  display: flex;
  gap: 28px;
  align-items: flex-start;
}

/* ===== 左侧筛选 ===== */
.left-panel {
  width: 200px;
  flex-shrink: 0;
  position: sticky;
  top: 28px;
}

.filter-card {
  background: var(--bg-card);
  border-radius: 10px;
  padding: 16px;
  border: 1px solid var(--border-color);
  transition: background 0.3s, border-color 0.3s;
}

.filter-section {
  margin-bottom: 4px;
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.filter-tag {
  cursor: pointer;
}

.filter-empty {
  font-size: 12px;
  color: var(--text-placeholder);
}

.filter-card :deep(.el-divider) {
  margin: 12px 0;
}

/* ===== 推荐位 ===== */
.recommend-card {
  margin-top: 12px;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.recommend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.recommend-item:hover {
  background: var(--bg-hover);
}

.recommend-index {
  flex-shrink: 0;
  min-width: 18px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  color: var(--text-placeholder);
}

.recommend-index.hot {
  color: var(--link-color);
}

.recommend-title {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-item:hover .recommend-title {
  color: var(--link-color);
}

/* ===== 右侧 ===== */
.right-panel {
  flex: 1;
  min-width: 0;
}

.status {
  text-align: center;
  color: var(--text-placeholder);
  padding: 60px 0;
}

.status p {
  margin: 8px 0 0;
  font-size: 14px;
}

.loading-icon {
  display: block;
  margin: 0 auto 8px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ===== 文章卡片 ===== */
.article-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 10px;
  width: 480px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, background 0.3s, border-color 0.3s;
}

.card-imgs {
  display: flex;
  gap: 6px;
  height: 160px;
  margin-bottom: 10px;
  overflow: hidden;
}
.card-img {
  flex: 1 1 0;
  min-width: 0;
  height: 100%;
  object-fit: cover;
  object-position: center top;
  border-radius: 6px;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
}

.article-card h2 {
  margin: 0 0 6px;
  font-size: 16px;
  width: 440px;
}

.article-card a {
  text-decoration: none;
  color: var(--text-primary);
  transition: color 0.2s;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-card a:hover {
  color: var(--link-color);
}

.content-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 10px;
  line-height: 1.6;
  text-align: left;
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.meta-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}

.category-tag {
  color: #409eff;
  background: #ecf5ff;
}

.tag-tag {
  color: #67c23a;
  background: #f0f9eb;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 12px;
  color: var(--text-placeholder);
}

.author-text {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-muted);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ===== 搜索高亮 ===== */
.content-preview :deep(em),
.article-card h2 :deep(em) {
  color: red;
  font-style: normal;
}

/* ===== 分页 ===== */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
