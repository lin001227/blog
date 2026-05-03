<template>
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

.admin-nav-link:hover,
.admin-nav-link.active,
.admin-nav-link.router-link-exact-active {
  color: var(--text-primary);
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
