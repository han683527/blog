<template>
  <div>
    <h1>分类管理</h1>
    <!-- 新增分类 -->
    <div class="add">
      <el-input v-model="newCategoryName" placeholder="分类名称" />
      <el-button type="primary" @click="handleCreate">新增</el-button>
    </div>
    <el-table :data="categories" stripe>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="categoryName" label="分类名"/>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑分类" width="400px">
      <el-input v-model="editForm.categoryName" placeholder="分类名称"/>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
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
import {ref, onMounted, reactive} from 'vue'
import {ElMessage, ElMessageBox} from "element-plus"
import {createCategory, deleteCategory, pageCategories, updateCategory} from "@/api/category"

const categories = ref([])
const total = ref(0)
const current = ref(1)
const newCategoryName = ref("")
const editVisible = ref(false)
const editForm = ref({ id: null, categoryName: '' })

onMounted(() => {
  loadCategories()
})

async function loadCategories() {
  try {
    const res = await pageCategories({ page: current.value, size: 10 })
    categories.value = res.data.data.list
    total.value = res.data.data.total
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function handleCreate() {
  // 去除收尾空格 trim()
  if (!newCategoryName.value.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  try {
    await createCategory({ categoryName: newCategoryName.value })
    ElMessage.success('创建成功')
    newCategoryName.value = ''
    current.value = 1
    loadCategories()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function onPageChange(page) {
  current.value = page
  loadCategories()
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该分类?', '提示')
    await deleteCategory(id)
    ElMessage.success('删除成功')
    current.value = 1
    loadCategories()
  } catch {}
}

function handleEdit(row) {
  editForm.value = { categoryId: row.id, categoryName: row.categoryName }
  editVisible.value = true
}

async function handleUpdate() {
  try {
    await updateCategory(editForm.value)
    ElMessage.success('修改成功')
    editVisible.value = false
    loadCategories()
  } catch (e) {
    ElMessage.error(e.message)
  }
}
</script>

<style scoped>
h1 {
  font-size: 24px;
  margin-bottom: 20px;
}
.add {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}
</style>