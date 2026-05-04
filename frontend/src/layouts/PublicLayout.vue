<template>
  <div class="public-layout">
    <header class="public-header">
      <div class="public-header-inner">
        <router-link to="/" class="public-brand">风屿 · 随笔</router-link>
        <div class="public-nav">
          <router-link to="/" class="public-nav-link" :class="{ active: $route.path === '/' }">首页</router-link>
          <router-link to="/archive" class="public-nav-link" :class="{ active: $route.path === '/archive' }">归档</router-link>
          <router-link to="/readings" class="public-nav-link" :class="{ active: $route.path === '/readings' }">阅读</router-link>
          <router-link to="/rankings" class="public-nav-link" :class="{ active: $route.path === '/rankings' }">排行</router-link>
          <router-link to="/admin/login" class="public-nav-link">管理</router-link>
          <DarkToggle />
        </div>
      </div>
    </header>
    <main class="public-main">
      <router-view />
    </main>
    <footer class="public-footer">
      <div class="public-footer-inner">
        <p>&copy; {{ new Date().getFullYear() }} 风屿 · 随笔. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import DarkToggle from '../components/DarkToggle.vue'
import { useDarkMode } from '../composables/useDarkMode'

useDarkMode()
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  transition: background 0.3s ease;
}

.public-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--bg-nav);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.public-header-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 24px;
  height: var(--nav-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.public-brand {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
  font-family: 'Noto Serif SC', serif;
  letter-spacing: 1px;
}

.public-nav {
  display: flex;
  align-items: center;
  gap: 20px;
}

.public-nav-link {
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
  font-weight: 500;
}

.public-nav-link:hover,
.public-nav-link.active {
  color: var(--text-primary);
}

/* Main Content */
.public-main {
  flex: 1;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 40px 24px 60px;
  width: 100%;
}

/* Footer */
.public-footer {
  padding: 32px 24px;
  text-align: center;
}

.public-footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
}

.public-footer p {
  font-size: 13px;
  color: var(--text-muted);
}
</style>
