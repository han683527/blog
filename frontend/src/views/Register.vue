<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h2>创建账号</h2>
        <p>注册一个新账号</p>
      </div>
      <el-form :model="form" size="large">
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item>
          <div class="code-row">
            <el-input v-model="form.code" placeholder="验证码" />
            <el-button @click="handleSendCode" :disabled="codeSending" round>
              {{ codeSending ? '发送中' : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.nickname" placeholder="昵称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" class="auth-btn" round>注册</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        已有账号？<router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router'
import { register, sendCode } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const form = ref({})
const codeSending = ref(false)

async function handleSendCode() {
  if (!form.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  codeSending.value = true
  try {
    await sendCode(form.value.email)
    ElMessage.success('验证码已发送')
  } catch {
    ElMessage.error('验证码发送失败')
  } finally {
    codeSending.value = false
  }
}

async function handleRegister() {
  if (!form.value.email || !form.value.code || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await register(form.value)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (e) {
    ElMessage.error('注册失败')
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
  width: 420px;
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

.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
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
