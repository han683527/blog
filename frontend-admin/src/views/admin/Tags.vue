<template>
  <div>
    <h1>标签管理</h1>
    <div class="add">
      <el-input v-model="newTagName" placeholder="标签名称" />
      <el-button type="primary" @click="handleCreate">新增</el-button>
    </div>
    <el-table :data="tags">
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="tagName" label="标签名" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="editVisible" title="编辑标签" width="400px">
      <el-input v-model="editForm.tagName" placeholder="标签名称" />
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
import {ref, onMounted} from "vue"
import {ElMessage, ElMessageBox} from "element-plus"
import {createTag,deleteTag,updateTag,pageTags} from "@/api/tag"

const tags = ref([])
const total = ref(0)
const current = ref(1)
const newTagName = ref("")
const editVisible = ref(false)
const editForm = ref({ id: null, tagName: '' })

onMounted(() => {
  loadTags()
})

async function loadTags() {
  try {
    const res = await pageTags({ page: current.value, size: 10 })
    tags.value = res.data.data.list
    total.value = res.data.data.total
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function handleCreate() {
  if (!newTagName.value.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  try {
    await createTag({ tagName: newTagName.value })
    ElMessage.success("创建成功")
    current.value = 1
    loadTags()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function onPageChange(page) {
  current.value = page
  loadTags()
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该标签?', '提示')
    await deleteTag(id)
    ElMessage.success('删除成功')
    current.value = 1
    loadTags()
  } catch {}
}

async function handleEdit(row) {
  editForm.value = { tagId: row.id, tagName: row.tagName }
  editVisible.value = true
}

async function handleUpdate() {
  try {
    await updateTag(editForm.value)
    ElMessage.success('修改成功')
    editVisible.value = false
    loadTags()
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