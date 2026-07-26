<template>
  <div class="admin-login">
    <div class="login-card">
      <div class="login-header">
        <h2>管理后台</h2>
        <p>管理员登录</p>
      </div>
      <el-form size="large">
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-btn" round>登录</el-button>
        </el-form-item>
      </el-form>
    </div>
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
.admin-login {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8edf5 0%, #f0f4f8 100%);
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 380px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #409eff;
}

.login-header p {
  margin: 0;
  font-size: 14px;
  color: #c0c4cc;
}

.login-btn {
  width: 100%;
}
</style>