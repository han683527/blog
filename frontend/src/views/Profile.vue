<template>
  <div class="profile-page">
    <div class="profile-layout">
      <!-- 左侧: 头像 + 资料编辑 -->
      <div class="left">
        <div class="profile-card">
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="selectFile">
              <img :src="user.avatar" class="avatar" v-if="user.avatar" />
              <div v-else class="avatar-placeholder">上传头像</div>
              <div class="avatar-overlay">更换头像</div>
            </div>
            <h3>{{ user.nickname }}</h3>
            <span class="email">{{ user.email }}</span>
          </div>
          <el-divider />
          <el-button type="primary" size="small" round @click="showEdit = true" class="edit-btn">编辑资料</el-button>
        </div>
      </div>

      <!-- 右侧: 内容列表 -->
      <div class="right">
        <div class="content-card">
          <el-tabs v-model="activeTab" class="profile-tabs">
            <el-tab-pane label="我的文章" name="articles">
              <div v-if="loading" class="tab-status">加载中...</div>
              <div v-else>
                <div v-for="article in articles" :key="article.id" class="list-item">
                  <div class="item-main" @click="$router.push('/article/' + article.id)">
                    <div class="item-title">
                    {{ article.title }}
                    <el-tag v-if="article.status === 0" size="small" type="warning">草稿</el-tag>
                  </div>
                    <div class="item-meta">
                      <span>{{ article.createTime }}</span>
                      <span>评论 {{ article.commentCount }}</span>
                      <span>赞 {{ article.likeCount }}</span>
                      <span>收藏 {{ article.collectCount }}</span>
                    </div>
                  </div>
                  <el-button type="primary" size="small" text @click="$router.push('/edit/' + article.id)">编辑</el-button>
                  <el-button type="danger" size="small" text @click="handleDeleteArticle(article.id)">删除</el-button>
                </div>
                <div v-if="articles.length === 0" class="tab-status">还没有文章</div>
                <div class="pagination" v-if="total > 10">
                  <el-pagination small background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="onArticlePageChange" />
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的评论" name="comments">
              <div v-if="loading" class="tab-status">加载中...</div>
              <div v-else>
                <div v-for="comment in comments" :key="comment.id" class="list-item">
                  <div class="item-main" @click="$router.push('/article/' + comment.articleId)">
                    <div class="comment-cell">{{ comment.content }}</div>
                    <div class="item-meta">
                      <span>{{ comment.createTime }}</span>
                    </div>
                  </div>
                  <el-button type="danger" size="small" text @click="handleDeleteComment(comment.id)">删除</el-button>
                </div>
                <div v-if="comments.length === 0" class="tab-status">还没有评论</div>
                <div class="pagination" v-if="total > 10">
                  <el-pagination small background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="onCommentPageChange" />
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的点赞" name="likes">
              <div v-if="loading" class="tab-status">加载中...</div>
              <div v-else>
                <div v-for="like in likes" :key="like.id" class="list-item">
                  <div class="item-main" @click="$router.push('/article/' + like.id)">
                    <div class="item-title">{{ like.title }}</div>
                    <div class="item-meta">
                      <span>{{ like.createTime }}</span>
                      <span>评论 {{ like.commentCount }}</span>
                      <span>赞 {{ like.likeCount }}</span>
                      <span>收藏 {{ like.collectCount }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="likes.length === 0" class="tab-status">还没有点赞</div>
                <div class="pagination" v-if="total > 10">
                  <el-pagination small background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="onLikePageChange" />
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="我的收藏" name="collects">
              <div v-if="loading" class="tab-status">加载中...</div>
              <div v-else>
                <div v-for="collect in collects" :key="collect.id" class="list-item">
                  <div class="item-main" @click="$router.push('/article/' + collect.id)">
                    <div class="item-title">{{ collect.title }}</div>
                    <div class="item-meta">
                      <span>{{ collect.createTime }}</span>
                      <span>评论 {{ collect.commentCount }}</span>
                      <span>赞 {{ collect.likeCount }}</span>
                      <span>收藏 {{ collect.collectCount }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="collects.length === 0" class="tab-status">还没有收藏</div>
                <div class="pagination" v-if="total > 10">
                  <el-pagination small background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="onCollectPageChange" />
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>

  <!-- 编辑资料弹窗 -->
  <el-dialog v-model="showEdit" title="编辑资料" width="420px" destroy-on-close append-to-body>
    <el-form :model="form" label-width="70px" size="small">
      <el-form-item label="邮箱">
        <el-input v-model="form.email" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="旧密码">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showEdit = false" round>取消</el-button>
      <el-button type="primary" @click="handelUpdate" round>保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {ref, onMounted, watch} from 'vue'
import {getUser, updateProfile, updateAvatar} from '@/api/user'
import {getArticles, deleteArticle} from '@/api/articles'
import {getComments, deleteComment} from "@/api/comment";
import {getMyLikes} from '@/api/like'
import {getMyCollects} from '@/api/collect'
import {ElMessage, ElMessageBox} from 'element-plus'

const form = ref({})
const user = ref({})
const showEdit = ref(false)
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const activeTab = ref('articles')
const articles = ref([])
const comments = ref([])
const likes = ref([])
const collects =ref([])

onMounted(async () => {
  const res = await getUser()
  user.value = res.data.data
  form.value.nickname = user.value.nickname
  loadMyArticles()
})

async function handelUpdate() {
  if (!form.value.oldPassword && !form.value.newPassword && form.value.nickname === user.value.nickname) {
    ElMessage.info('没有需要保存的修改')
    return
  }
  try {
    await updateProfile({
      nickname: form.value.nickname || undefined,
      oldPassword: form.value.oldPassword || undefined,
      newPassword: form.value.newPassword || undefined
    })
    user.value.nickname = form.value.nickname
    ElMessage.success('保存成功')
    showEdit.value = false
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

const fileInput = document.createElement('input')
fileInput.type = 'file'
fileInput.accept = 'image/*'
fileInput.onchange = async () => {
  const file = fileInput.files[0]
  if (!file) return
  try {
    const res = await updateAvatar(file)
    user.value.avatar = res.data.data
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error(e.message || '图片上传失败')
  }
}

function selectFile() {
  fileInput.click()
}

async function loadMyArticles() {
  loading.value = true
  try {
    const res = await getArticles({authorId: user.value.id, page: page.value, size: 10})
    total.value = res.data.data.total
    articles.value = res.data.data.list
  } finally {
    loading.value = false
  }
}

async function loadMyComments() {
  loading.value = true
  try {
    const res = await getComments({userId: user.value.id, page: page.value, size: 10})
    total.value = res.data.data.total
    comments.value = res.data.data.list
  } finally {
    loading.value = false
  }
}

async function loadMyLikes() {
  loading.value = true
  try {
    const res = await getMyLikes({page: page.value, size: 10})
    total.value = res.data.data.total
    likes.value = res.data.data.list
  } finally {
    loading.value = false
  }
}

async function loadMyCollects() {
  loading.value = true
  try {
    const res = await getMyCollects({ page: page.value, size: 10})
    collects.value = res.data.data.list
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onArticlePageChange(newPage) {
  page.value = newPage
  loadMyArticles()
}

function onCommentPageChange(newPage) {
  page.value = newPage
  loadMyComments()
}

function onLikePageChange(newPage) {
  page.value = newPage
  loadMyLikes()
}

function onCollectPageChange(newPage) {
  page.value = newPage
  loadMyCollects()
}

watch(activeTab, (tab) => {
  if (tab === 'articles' && articles.value.length === 0) loadMyArticles()
  if (tab === 'comments' && comments.value.length === 0) loadMyComments()
  if (tab === 'likes' && likes.value.length === 0) loadMyLikes()
  if (tab === 'collects' && collects.value.length === 0) loadMyCollects()
})

async function handleDeleteArticle(id) {
  await ElMessageBox.confirm('确定删除这篇文章吗?','提示')
  await deleteArticle(id)
  ElMessage.success('删除成功')
  comments.value = []
  likes.value = []
  collects.value = []
  loadMyArticles()
}

async function handleDeleteComment(id) {
  await ElMessageBox.confirm('确定要删除这条评论吗?','提示')
  await deleteComment(id)
  ElMessage.success('删除成功')
  loadMyComments()
}
</script>

<style scoped>
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px;
}

.profile-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* ===== 左侧 ===== */
.left {
  width: 300px;
  flex-shrink: 0;
  position: sticky;
  top: 32px;
}

.profile-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  border: 1px solid #ebeef5;
}

.avatar-section {
  text-align: center;
  padding: 8px 0;
}

.avatar-wrapper {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  margin: 0 auto 12px;
  position: relative;
  cursor: pointer;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 11px;
  padding: 4px 0;
  text-align: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-section h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.email {
  font-size: 13px;
  color: #c0c4cc;
}

.profile-card :deep(.el-divider) {
  margin: 16px 0;
}

.edit-btn {
  width: 100%;
  margin-bottom: 12px;
}

/* ===== 右侧 ===== */
.right {
  flex: 1;
  min-width: 0;
  width: 500px;
}

.content-card {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  border: 1px solid #ebeef5;
  width: 100%;
  box-sizing: border-box;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.tab-status {
  text-align: center;
  color: #c0c4cc;
  padding: 40px 0;
  font-size: 14px;
}

/* ===== 列表项 ===== */
.list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f4f8;
  width: 100%;
  box-sizing: border-box;
}

.list-item:last-child {
  border-bottom: none;
}

.item-main {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.item-main:hover .item-title {
  color: #409eff;
}

.item-meta {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

.item-meta span {
  margin-right: 12px;
}

.comment-cell {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
