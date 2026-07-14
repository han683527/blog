<template>
  <el-menu mode="horizontal" class="nav">
    <el-menu-item class="brand">
      <router-link to="/">ByteBlog</router-link>
    </el-menu-item>

    <el-menu-item index="home">
      <router-link to='/'>首页</router-link>
    </el-menu-item>


    <router-link to="/login">
      <el-button type="primary" size="small">登录</el-button>
    </router-link>
    <router-link to="/register">
      <el-button size="small">注册</el-button>
    </router-link>

  </el-menu>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.brand {
  font-size: 20px;
  font-weight: bold;
}

.brand a {
  text-decoration: none;
  color: inherit;
}

.nav-right {
  margin-left: auto;
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
<script setup lang="ts">
</script>