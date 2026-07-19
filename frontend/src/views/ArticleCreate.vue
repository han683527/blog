<template>
  <div class="create">
    <el-card class="create-card">
      <h2>写文章</h2>
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
          <el-select v-model="form.tagIds" placeholder="选择标签" multiple clearable>
            <el-option v-for="t in tags" :key="t.id" :label="t.tagName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="15" placeholder="写点什么..."/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">发布</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createArticle } from '@/api/articles'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { ElMessage } from "element-plus";

const router = useRouter()
const form = ref({})
const categories = ref([])
const tags = ref([])

onMounted (async () => {
  const [catRes, tagRes] = await Promise.all([
      getCategories({ page: 1, size: 10 }),
      getTags({ page: 1, size: 10 }),
  ])
  categories.value = catRes.data.data.list
  tags.value = tagRes.data.data.list
})

async function handleSubmit() {
  if (!form.value.title) {
    ElMessage.warning('请填写标题')
    return
  } else if (!form.value.content) {
    ElMessage.warning('请填写正文')
    return
  }
  try {
    await createArticle(form.value)
    ElMessage.success('发布成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '发布失败')
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