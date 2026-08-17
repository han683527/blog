<template>
  <div id="app" :class="{ dark: isDark }">
    <BlogNav :is-dark="isDark" @toggle-dark="toggleDark" />
    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import BlogNav from '@/components/BlogNav.vue'

const isDark = ref(false)

onMounted(() => {
  const saved = localStorage.getItem('darkMode')
  if (saved === 'true' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    isDark.value = true
  }
  applyTheme()
})

watch(isDark, () => {
  localStorage.setItem('darkMode', isDark.value)
  applyTheme()
})

function applyTheme() {
  document.documentElement.classList.toggle('dark', isDark.value)
}

function toggleDark() {
  isDark.value = !isDark.value
}
</script>

<style>
:root {
  --bg-body: #f0f4f8;
  --bg-card: #ffffff;
  --bg-navbar: #ffffff;
  --bg-hover: #f8faff;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-muted: #909399;
  --text-placeholder: #c0c4cc;
  --border-color: #ebeef5;
  --border-light: #e4e7ed;
  --border-divider: #f0f4f8;
  --link-color: #409eff;
  --el-bg-color: #ffffff;
  --el-bg-color-overlay: #ffffff;
  --el-border-color: #dcdfe6;
  --el-text-color-primary: #303133;
  --el-text-color-secondary: #909399;
  --el-fill-color: #f5f7fa;
  --el-fill-color-light: #f0f4f8;
  --el-mask-color: rgba(255,255,255,0.8);
}

.dark {
  --bg-body: #0a0a0a;
  --bg-card: #1a1a1a;
  --bg-navbar: #0d0d0d;
  --bg-hover: #222222;
  --text-primary: #e0e0e0;
  --text-secondary: #a0a0a0;
  --text-muted: #707070;
  --text-placeholder: #505050;
  --border-color: #2a2a2a;
  --border-light: #333333;
  --border-divider: #2a2a2a;
  --link-color: #5a9eff;
  --el-bg-color: #1a1a1a;
  --el-bg-color-overlay: #222222;
  --el-border-color: #333333;
  --el-text-color-primary: #e0e0e0;
  --el-text-color-secondary: #707070;
  --el-fill-color: #222222;
  --el-fill-color-light: #2a2a2a;
  --el-mask-color: rgba(0,0,0,0.6);
}

body {
  margin: 0;
  background: var(--bg-body);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: var(--text-primary);
  transition: background 0.3s, color 0.3s;
}
</style>