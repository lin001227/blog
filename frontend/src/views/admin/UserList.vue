<template>
  <div class="admin-page">
    <nav class="admin-nav">
      <div class="admin-nav-inner">
        <router-link to="/admin" class="admin-nav-brand">风屿 · 管理</router-link>
        <div class="admin-nav-links">
          <router-link to="/admin" class="admin-nav-link">概览</router-link>
          <router-link to="/admin/articles" class="admin-nav-link">文章</router-link>
          <router-link v-if="auth.isAdmin" to="/admin/users" class="admin-nav-link">用户</router-link>
          <a class="admin-nav-link" href="/" target="_blank">查看博客</a>
          <el-dropdown trigger="click" @command="handleUserCommand" class="admin-user-dropdown">
            <div class="admin-user-trigger">
              <el-avatar :size="28" shape="square" style="background: var(--text-accent); cursor: pointer;">
                <span style="font-size: 13px;">{{ auth.displayName.charAt(0) }}</span>
              </el-avatar>
              <el-icon style="margin-left: 2px; color: var(--text-secondary);">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled style="cursor: default;">
                  <div style="padding: 4px 0;">
                    <div style="font-weight: 600; color: var(--text-primary); font-size: 14px;">{{ auth.displayName }}</div>
                    <div style="font-size: 12px; color: var(--text-secondary); margin-top: 2px;" v-if="auth.isAdmin">管理员</div>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="dark" divided>
                  <span style="display: flex; align-items: center; gap: 6px;">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/></svg>
                    切换主题
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <span style="display: flex; align-items: center; gap: 6px; color: #e74c3c;">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/></svg>
                    退出登录
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </nav>

    <main class="admin-main">
      <div class="admin-header">
        <h1 class="admin-heading">用户管理</h1>
        <router-link to="/admin/users/new">
          <el-button type="primary">
            <el-icon style="margin-right: 4px;"><Plus /></el-icon>
            新建用户
          </el-button>
        </router-link>
      </div>

      <el-table :data="users" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="displayName" label="显示名称" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'success'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '编辑' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click="editUser(row)">
              编辑
            </el-button>
            <el-popconfirm
              v-if="row.username !== 'admin'"
              title="确定删除此用户？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button text size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { getUsers, deleteUser } from '../../api/users'
import { ElMessage } from 'element-plus'
const router = useRouter()
const auth = useAuthStore()
const users = ref([])
const loading = ref(true)

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

function editUser(row) {
  router.push(`/admin/users/${row.id}/edit`)
}

async function handleDelete(row) {
  try {
    await deleteUser(row.id)
    users.value = users.value.filter(u => u.id !== row.id)
    ElMessage.success('删除成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '删除失败')
  }
}

onMounted(async () => {
  try {
    const res = await getUsers()
    users.value = res.data || []
  } catch (e) {
    console.error('Failed to load users:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: var(--bg-page);
  transition: background 0.3s ease;
}
.admin-nav {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: background 0.3s ease, border-color 0.3s ease;
}
.admin-nav-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.admin-nav-brand {
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
}
.admin-nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}
.admin-nav-link {
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
}
.admin-nav-link:hover,
.admin-nav-link.active,
.admin-nav-link.router-link-exact-active {
  color: var(--text-primary);
}

.admin-user-dropdown {
  margin-left: 12px;
}
.admin-user-trigger {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.admin-main {
  max-width: 1100px;
  margin: 0 auto;
  padding: 36px 24px;
}
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.admin-heading {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}
</style>
