<template>
  <el-container class="admin-layout">
    <el-header class="admin-header-bar" height="56px">
      <div class="admin-header-inner">
        <router-link to="/admin" class="admin-brand">风屿 · 管理</router-link>
        <div class="admin-header-right">
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            :ellipsis="false"
            class="admin-menu"
            router
          >
            <el-menu-item index="/admin">概览</el-menu-item>
            <el-menu-item index="/admin/articles">文章</el-menu-item>
            <el-menu-item v-if="auth.isAdmin" index="/admin/users">用户</el-menu-item>
            <el-menu-item index="/admin/comments">评论</el-menu-item>
          </el-menu>
          <div class="admin-header-actions">
            <el-button text size="small" @click="goHome">
              <template #icon><el-icon><View /></el-icon></template>
              查看博客
            </el-button>
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="admin-user-trigger">
                <el-avatar :size="28" shape="square" style="background: var(--el-color-primary); cursor: pointer;">
                  <span style="font-size: 13px;">{{ auth.displayName.charAt(0) }}</span>
                </el-avatar>
                <el-icon style="margin-left: 2px;"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item disabled style="cursor: default;">
                    <div style="padding: 4px 0;">
                      <div style="font-weight: 600; font-size: 14px;">{{ auth.displayName }}</div>
                      <div style="font-size: 12px; color: var(--el-text-color-secondary); margin-top: 2px;" v-if="auth.isAdmin">管理员</div>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item command="dark" divided>
                    <el-icon><Moon /></el-icon>
                    切换主题
                  </el-dropdown-item>
                  <el-dropdown-item command="logout">
                    <span style="color: #e74c3c; display: flex; align-items: center; gap: 4px;">
                      <el-icon><SwitchButton /></el-icon>
                      退出登录
                    </span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </el-header>
    <el-main class="admin-main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ArrowDown, Moon, SwitchButton, View } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/admin/articles')) return '/admin/articles'
  if (path.startsWith('/admin/users')) return '/admin/users'
  if (path.startsWith('/admin/comments')) return '/admin/comments'
  return '/admin'
})

function goHome() {
  window.open('/', '_blank')
}

function handleUserCommand(command) {
  if (command === 'logout') {
    auth.logout()
    router.push('/admin/login')
  } else if (command === 'dark') {
    const html = document.documentElement
    const isDark = html.classList.contains('dark')
    html.classList.toggle('dark')
    html.classList.add('theme-transitioning')
    setTimeout(() => html.classList.remove('theme-transitioning'), 500)
    localStorage.setItem('theme', isDark ? 'light' : 'dark')
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: var(--el-bg-color-page);
}

.admin-header-bar {
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-light);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.admin-header-inner {
  max-width: 1100px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.admin-brand {
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  text-decoration: none;
  white-space: nowrap;
  margin-right: 24px;
}

.admin-header-right {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
  gap: 8px;
}

.admin-menu {
  border-bottom: none !important;
  flex: 1;
  justify-content: flex-end;
  background: transparent;
}

.admin-menu .el-menu-item {
  font-size: 14px;
  height: 56px;
  line-height: 56px;
}

.admin-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 12px;
}

.admin-user-trigger {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 2px;
}

.admin-main-content {
  max-width: 1100px;
  margin: 0 auto;
  padding: 28px 20px;
  width: 100%;
}
</style>
