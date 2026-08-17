<template>
  <div id="app" class="app-layout">
    <AdminNav v-if="route.path !== '/login'" />
    <div class="main-content" :class="{ 'has-sidebar': route.path !== '/login' }">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import AdminNav from "./components/AdminNav.vue"

const route = useRoute()
</script>

<style>
body {
  margin: 0;
  background: #f0f4f8;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
.app-layout {
  display: flex;
  min-height: 100vh;
}
.main-content {
  flex: 1;
  padding: 0;
}
.main-content.has-sidebar {
  margin-left: 220px;
  padding: 24px;
}
</style>
