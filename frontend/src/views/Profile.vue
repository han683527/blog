<template>
  <!-- 两栏分布 -->
  <div class="profile-layout">
    <!-- 左侧: 头像 + 资料编辑 -->
    <div class="left">
      <el-card>
        <div class="avatar-section">
          <img :src="user.avatar" class="avatar" v-if="user.avatar" @click="selectFile"/>
          <div v-else class="avatar-placeholder" @click="selectFile">上传头像</div>
          <h3>{{ user.nickname }}</h3>
          <span class="email">{{ user.email }}</span>
        </div>

        <el-divider/>

        <el-form :model="form" label-width="70px">
          <el-form-item label="邮箱">
            <el-input v-model="form.email" disabled/>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname"/>
          </el-form-item>
          <el-form-item label="旧密码">
            <el-input v-model="form.oldPassword" type="password" show-password/>
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="form.newPassword" type="password" show-password/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handelUpdate">保存</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 右侧: 内容列表 -->
    <div class="right">
      <el-card>
        <el-tabs v-model="activeTab">
          <!-- 我的文章 -->
          <el-tab-pane label="我的文章" name="articles">
            <div v-if="loading">加载中...</div>
            <div v-else>
              <div v-for="article in articles" :key="article.id" class="item">
                <router-link :to="'/article/' + article.id" class="title">{{ article.title }}</router-link>
                <el-button type="danger" @click="handleDeleteArticle(article.id)">删除</el-button>
                <div class="meta">
                  <span>{{ article.createTime }}</span>
                  <span>评论 {{ article.commentCount }}</span>
                  <span>赞 {{ article.likeCount }}</span>
                  <span>收藏 {{ article.collectCount}}</span>
                </div>
              </div>
            </div>
            <div v-if="articles.length === 0" class="empty">还没有文章</div>
            <div class="pagination" v-if="total > 10">
              <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="onArticlePageChange" />
            </div>
          </el-tab-pane>
          <!-- 我的评论 -->
          <el-tab-pane label="我的评论" name="comments">
            <div v-if="loading">加载中...</div>
            <div v-else>
              <div v-for="comment in comments" :key="comment.id" class="item">
                <router-link :to="'/article/' + comment.articleId" class="title">{{ comment.content }}</router-link>
                <el-button type="danger" @click="handleDeleteComment(comment.id)">删除</el-button>
                <div class="meta">
                  <span>{{ comment.createTime }}</span>
                </div>
              </div>
              <div v-if="comments.length === 0" class="empty">还没有评论</div>
              <div class="pagination" v-if="total > 10">
                <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="onCommentPageChange" />
              </div>
            </div>
          </el-tab-pane>
          <!-- 我的点赞 -->
          <el-tab-pane label="我的点赞" name="likes">
            <div v-if="loading">加载中...</div>
            <div v-else>
              <div v-for="like in likes" :key="like.id" class="item">
                <router-link :to="'/article/' + like.id" class="title">{{ like.title }}</router-link>
                <div class="meta">
                  <span>{{ like.createTime }}</span>
                  <span>评论 {{ like.commentCount }}</span>
                  <span>赞 {{ like.likeCount }}</span>
                  <span>收藏 {{ like.collectCount}}</span>
                </div>
              </div>
              <div v-if="likes.length === 0" class="empty">还没有点赞</div>
            </div>
            <div class="pagination" v-if="total > 10">
              <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="onLikePageChange" />
            </div>
          </el-tab-pane>
          <!-- 我的收藏 -->
          <el-tab-pane label="我的收藏" name="collects">
            <div v-if="loading">加载中...</div>
            <div v-else>
              <div v-for="collect in collects" :key="collect.id" class="item">
                <router-link :to="'/article/' + collect.id" class="title">{{ collect.title }}</router-link>
                <div class="meta">
                  <span>{{ collect.createTime }}</span>
                  <span>评论 {{ collect.commentCount }}</span>
                  <span>赞 {{ collect.likeCount }}</span>
                  <span>收藏 {{ collect.collectCount }}</span>
                </div>
              </div>
              <div v-if="collects.length === 0" class="empty">还没有收藏</div>
            </div>
            <div class="pagination" v-if="total > 10">
              <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="onCollectPageChange" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
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
  try {
    await updateProfile({
      nickname: form.value.nickname,
      oldPassword: form.value.oldPassword || undefined,
      newPassword: form.value.newPassword || undefined
    })
    user.value.nickname = form.value.nickname
    ElMessage.success('保存成功')
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
  // 删除缓存
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
.profile-layout {
  display: flex;
  gap: 20px;
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  align-items: flex-start;
}

.left {
  width: 320px;
  flex-shrink: 0;
}

.right{
  flex: 1;
  min-height: 400px;
}

.avatar-section {
  text-align: center;
  padding: 10px 0;
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
  cursor: pointer;
  margin: 0 auto;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
}

.email {
  color: #999;
  font-size: 13px;
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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>