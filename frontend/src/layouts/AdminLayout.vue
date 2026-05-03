<template>
  <el-container class="admin-layout">
    <el-header class="admin-header-bar" height="60px">
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
            <el-menu-item index="/admin/external-articles">阅读</el-menu-item>
            <el-menu-item v-if="auth.isAdmin" index="/admin/users">用户</el-menu-item>
            <el-menu-item index="/admin/comments">评论</el-menu-item>
          </el-menu>
          <div class="mobile-menu-wrapper">
            <el-dropdown trigger="click" @command="mobileNav">
              <el-button class="mobile-menu-btn" text>
                <el-icon size="20"><Menu /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/admin">概览</el-dropdown-item>
                  <el-dropdown-item command="/admin/articles">文章</el-dropdown-item>
                  <el-dropdown-item command="/admin/external-articles">阅读</el-dropdown-item>
                  <el-dropdown-item v-if="auth.isAdmin" command="/admin/users">用户</el-dropdown-item>
                  <el-dropdown-item command="/admin/comments">评论</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="admin-header-actions">
            <a class="home-link" @click="goHome" href="javascript:">查看博客</a>
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="admin-user-trigger">
                <el-avatar :size="28" shape="square" class="user-avatar">
                  <span style="font-size:13px">{{ auth.displayName.charAt(0) }}</span>
                </el-avatar>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="admin-user-menu">
                  <div class="user-menu-header">
                    <el-avatar :size="36" shape="square" class="user-menu-avatar">
                      {{ auth.displayName.charAt(0) }}
                    </el-avatar>
                    <div class="user-menu-info">
                      <div class="user-menu-name">{{ auth.displayName }}</div>
                      <div class="user-menu-role">{{ auth.isAdmin ? '管理员' : '编辑' }}</div>
                    </div>
                  </div>
                  <el-divider style="margin:8px 0" />
                  <el-dropdown-item command="dark">
                    <el-icon><Moon /></el-icon>
                    切换主题
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" class="logout-item">
                    <span class="logout-text">
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
import { Moon, SwitchButton, Menu } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/admin/articles')) return '/admin/articles'
  if (path.startsWith('/admin/external-articles')) return '/admin/external-articles'
  if (path.startsWith('/admin/users')) return '/admin/users'
  if (path.startsWith('/admin/comments')) return '/admin/comments'
  return '/admin'
})

function goHome() {
  window.open('/', '_blank')
}

function mobileNav(command) {
  router.push(command)
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
  background: var(--bg-page);
}

.admin-header-bar {
  background: var(--bg-nav);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
  transition: background 0.3s ease, border-color 0.3s ease;
}

.admin-header-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  height: var(--nav-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.admin-brand {
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
  white-space: nowrap;
  letter-spacing: 1px;
  margin-right: 24px;
  flex-shrink: 0;
}

.admin-header-right {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
  gap: 4px;
}

.admin-menu {
  border-bottom: none !important;
  flex: 1;
  justify-content: flex-end;
  background: transparent;
}

.admin-menu .el-menu-item {
  font-size: 14px;
  height: var(--nav-height);
  line-height: var(--nav-height);
  color: var(--text-secondary) !important;
  border-bottom: 2px solid transparent !important;
  transition: color 0.2s, border-color 0.2s;
}

.admin-menu .el-menu-item:hover {
  color: var(--text-primary) !important;
  background: transparent !important;
}

.admin-menu .el-menu-item.is-active {
  color: var(--text-accent) !important;
  border-bottom-color: var(--text-accent) !important;
  font-weight: 500;
}

.admin-header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 12px;
  flex-shrink: 0;
}

/* 查看博客 - 与导航菜单完全一致 */
.home-link {
  font-size: 14px;
  line-height: var(--nav-height);
  height: var(--nav-height);
  color: var(--text-secondary);
  text-decoration: none;
  cursor: pointer;
  padding: 0 4px;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
  user-select: none;
}
.home-link:hover {
  color: var(--text-primary) !important;
  background: transparent !important;
  border-bottom-color: transparent;
}

.admin-user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 2px;
  border-radius: 10px;
  transition: background 0.2s;
  border: 1.5px solid transparent;
}
.admin-user-trigger:hover {
  border-color: var(--border);
  background: var(--bg-tag);
}
.user-avatar {
  background: var(--text-accent) !important;
  cursor: pointer;
  flex-shrink: 0;
}

.admin-main-content {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 28px 24px;
  width: 100%;
}

/* Mobile menu (hidden by default) */
.mobile-menu-wrapper {
  display: none;
}

@media (max-width: 900px) {
  .admin-menu {
    display: none;
  }
  .mobile-menu-wrapper {
    display: flex;
    align-items: center;
  }
}

@media (max-width: 768px) {
  .admin-main-content {
    padding: 20px 16px;
  }
  .home-link {
    display: none;
  }
}
</style>

<!-- Global styles for teleported dropdown menu -->
<style>
.admin-user-menu {
  min-width: 200px !important;
  padding: 8px !important;
  border-radius: 10px !important;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
  box-shadow: var(--shadow-card-hover) !important;
}
.admin-user-menu .user-menu-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 8px;
  margin-bottom: 4px;
}
.admin-user-menu .user-menu-avatar {
  background: var(--text-accent) !important;
  flex-shrink: 0;
  font-size: 16px !important;
}
.admin-user-menu .user-menu-info {
  flex: 1;
  min-width: 0;
}
.admin-user-menu .user-menu-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
}
.admin-user-menu .user-menu-role {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}
.admin-user-menu .el-dropdown-menu__item {
  font-size: 13px;
  color: var(--text-secondary);
  border-radius: 10px;
  padding: 8px 12px !important;
  margin: 2px 0;
  transition: background 0.15s;
}
.admin-user-menu .el-dropdown-menu__item:hover {
  background: var(--bg-tag) !important;
  color: var(--text-primary) !important;
}
.admin-user-menu .el-dropdown-menu__item .el-icon {
  margin-right: 8px;
  font-size: 16px;
  color: var(--text-muted);
}
.admin-user-menu .logout-item .logout-text {
  color: #e74c3c; /* TODO: semantic error color */
  display: flex;
  align-items: center;
  gap: 4px;
}
.admin-user-menu .logout-item .logout-text .el-icon {
  color: #e74c3c; /* TODO: semantic error color */
}
.admin-user-menu .el-divider {
  border-color: var(--border);
}
</style>
