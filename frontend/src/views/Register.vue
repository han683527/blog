<template>
  <div class="register">
    <el-card class="register-card">
      <h2>注册</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="form.code" />
            <el-button @click="handleSendCode" :disabled="codeSending">
              {{ codeSending ? '发送中' : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister">注册</el-button>
          <router-link to="/login">
            <el-button>去登录</el-button>
          </router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router'
import { register,sendCode } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const form = ref({})
const codeSending = ref(false)

async function handleSendCode() {
  if(!form.value.email)
  {
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
.register {
  display: flex;
  justify-content: center;
  margin-top: 80px;
}
.register-card {
  width: 400px;
}
.code-row {
  display: flex;
  gap: 8px;
}
</style>