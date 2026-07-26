<template>
  <div>
    <h1>评论管理</h1>
    <el-table :data="comments" stripe>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column label="内容" width="180">
        <template #default="{ row }">
          <span class="content-cell">{{ row.content }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="评论者"/>
      <el-table-column prop="articleId" label="归属"/>
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
        @current-change="onChangePage"
    />
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from "element-plus"
import {deleteComment, pageComments} from "@/api/comment"

const comments = ref([])
const total = ref(0)
const current = ref(1)

onMounted(() => {
  loadComments()
})

async function loadComments() {
  try {
    const res = await pageComments({page: current.value, size: 10})
    comments.value = res.data.data.list
    total.value = res.data.data.total
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function onChangePage(page) {
  current.value = page
  loadComments()
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该评论?', '提示')
    await deleteComment(id)
    ElMessage.success("删除成功")
    current.value = 1
    loadComments()
  } catch (e) {}
}
</script>

<style scoped>
h1 {
  font-size: 24px;
  margin-bottom: 20px;
}
.content-cell {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>