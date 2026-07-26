<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h2>欢迎回来</h2>
        <p>登录你的账号</p>
      </div>
      <el-form :model="form" size="large">
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="auth-btn" round>登录</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
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
  if (!form.value.email || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await login(form.value)
    localStorage.setItem('accessToken', res.data.data.accessToken)
    localStorage.setItem('refreshToken', res.data.data.refreshToken)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 80px);
  padding: 24px;
}

.auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 380px;
  border: 1px solid #ebeef5;
}

.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.auth-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #303133;
}

.auth-header p {
  margin: 0;
  font-size: 14px;
  color: #c0c4cc;
}

.auth-btn {
  width: 100%;
}

.auth-footer {
  text-align: center;
  font-size: 13px;
  color: #909399;
  margin-top: 16px;
}

.auth-footer a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>
