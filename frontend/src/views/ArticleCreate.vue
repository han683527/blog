<template>
  <div class="create">
    <el-card class="create-card">
      <h2>{{ isEdit ? '编辑文章' : '写文章' }}</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="文章标题"/>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" clearable>
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagIds" placeholder="选择标签（最多3个）" multiple clearable :multiple-limit="3">
            <el-option v-for="t in tags" :key="t.id" :label="t.tagName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="15" placeholder="写点什么..."/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit(1)">{{ isEdit ? '保存修改' : '发布' }}</el-button>
          <el-button @click="handleSubmit(0)">{{ isEdit ? '保存为草稿' : '存草稿' }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>

import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createArticle, updateArticle, getArticle } from '@/api/articles'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { ElMessage } from "element-plus";

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const form = ref({})
const categories = ref([])
const tags = ref([])

onMounted (async () => {
  try {
    const catRes = await getCategories({ page: 1, size: 100 })
    categories.value = catRes.data.data.list
  } catch (e) {
    console.error('加载分类失败', e)
  }
  try {
    const tagRes = await getTags({ page: 1, size: 100 })
    tags.value = tagRes.data.data.list
  } catch (e) {
    console.error('加载标签失败', e)
  }
  if (isEdit.value) {
    try {
      const res = await getArticle(route.params.id)
      const article = res.data.data
      form.value = {
        id: article.id,
        title: article.title,
        content: article.content,
        categoryId: article.categoryId,
        tagIds: article.tags || []
      }
    } catch (e) {
      ElMessage.error('加载文章失败')
      router.push('/')
    }
  }
})

async function handleSubmit(status) {
  if (!form.value.title) {
    ElMessage.warning('请填写标题')
    return
  }
  if (status === 1 && !form.value.content) {
    ElMessage.warning('请填写正文')
    return
  }
  form.value.status = status
  try {
    if (isEdit.value) {
      await updateArticle(form.value)
      ElMessage.success('修改成功')
    } else {
      await createArticle(form.value)
      ElMessage.success(status === 1 ? '发布成功' : '草稿已保存')
    }
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}
</script>

<style scoped>
.create {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
.create-card {
  width: 800px;
}
</style>