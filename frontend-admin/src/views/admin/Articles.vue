<template>
  <div>
    <h1>文章管理</h1>
    <el-table :data="articles" stripe>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="title" label="标题"/>
      <el-table-column prop="content" label="正文"/>
      <el-table-column prop="authorId" label="作者"/>
      <el-table-column prop="createTime" label="创建时间"/>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="10"
        :current-page="current"
        @current-change="onPageChange"
    />
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from "element-plus"
import {deleteArticle, pageArticles} from "@/api/article"

const articles = ref([])
const total = ref(0)
const current = ref(1)

onMounted(() => {
  loadArticles()
})

async function loadArticles() {
  try {
    const res = await pageArticles({page: current.value, size: 10})
    articles.value = res.data.data.list
    total.value = res.data.data.total
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function onPageChange(page) {
  current.value = page
  loadArticles()
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该文章?', '提示')
    await deleteArticle(id)
    ElMessage.success("删除成功")
    current.value = 1
    loadArticles()
  } catch (e) {}
}
</script>

<style scoped>
h1 {
  font-size: 24px;
  margin-bottom: 20px;
}
</style>