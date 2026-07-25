<template>
  <div class="login">
    <el-card>
      <el-form label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="form.email"/>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import {ref} from 'vue'
import {login} from '@/api/login'
import {ElMessage} from "element-plus"
import {useRouter} from 'vue-router'
import {getUser} from "@/api/user"

const form = ref({})
const router = useRouter()

async function handleLogin() {
  try {
    const res = await login(form.value)
    localStorage.setItem('accessToken', res.data.data.accessToken)
    localStorage.setItem('refreshToken', res.data.data.refreshToken)

    // 查当前用户角色
    const useRes = await getUser()
    if (useRes.data.data.role !== 'admin') {
      // 权限不足处理
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      ElMessage.error('权限不足')
      return
    }
    ElMessage.success('登录成功')
    router.push('/articles')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  }
}
</script>

<style scoped>
.login {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
</style>