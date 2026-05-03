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
          <div class="admin-user">
            <el-avatar :size="28" shape="square" style="background: var(--text-accent); vertical-align: middle;">
              <span style="font-size: 13px;">{{ auth.displayName.charAt(0) }}</span>
            </el-avatar>
            <span class="admin-user-name">{{ auth.displayName }}</span>
          </div>
          <el-button text size="small" @click="handleLogout" style="color: var(--text-secondary);">
            退出登录
          </el-button>
          <button class="dark-toggle" @click="toggleDark" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
            {{ isDark ? '☀️' : '🌙' }}
          </button>
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
import { useDarkMode } from '../../composables/useDarkMode'

const { isDark, toggleDark } = useDarkMode()
const router = useRouter()
const auth = useAuthStore()
const users = ref([])
const loading = ref(true)

function handleLogout() {
  auth.logout()
  router.push('/admin/login')
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

.admin-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 16px;
  border-left: 1px solid var(--border);
}
.admin-user-name {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}
.dark-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
  line-height: 1;
  padding: 0;
}
.dark-toggle:hover {
  color: var(--text-accent);
  border-color: var(--text-accent);
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
