<template>
  <div class="login">
    <el-card class="login-card">
      <h2>登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="form.email"/>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <router-link to="/register">
            <el-button text>去注册</el-button>
          </router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const form = ref({})

async function handleLogin() {
  try {
    const res = await login(form.value)
    localStorage.setItem('accessToken', res.data.data.accessToken)
    localStorage.setItem('refreshToken', res.data.data.refreshToken)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error('登录失败')
  }
}
</script>

<style scoped>
.login {
  display: flex;
  justify-content: center;
  margin-top: 80px;
}
.login-card {
  width: 400px;
}
</style>