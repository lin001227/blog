<template>
  <div class="admin-page">
    <nav class="admin-nav">
      <div class="admin-nav-inner">
        <router-link to="/admin" class="admin-nav-brand">风屿 · 管理</router-link>
        <div class="admin-nav-links">
          <router-link to="/admin" class="admin-nav-link">概览</router-link>
          <router-link to="/admin/articles" class="admin-nav-link">文章</router-link>
          <router-link v-if="auth.isAdmin" to="/admin/users" class="admin-nav-link">用户</router-link>
          <router-link to="/admin/comments" class="admin-nav-link">评论</router-link>
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
        <h1 class="admin-heading">
          评论管理
          <el-tag v-if="pendingCount > 0" type="warning" size="small" style="margin-left: 12px; vertical-align: middle;">
            {{ pendingCount }} 条待审核
          </el-tag>
        </h1>
      </div>

      <el-table :data="comments" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="nickname" label="昵称" width="100" />
        <el-table-column prop="content" label="内容" min-width="300">
          <template #default="{ row }">
            <div class="comment-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" text size="small" type="success" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button v-if="row.status === 'PENDING'" text size="small" type="warning" @click="handleReject(row)">
              拒绝
            </el-button>
            <el-popconfirm title="确定删除此评论？" @confirm="handleDelete(row)">
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
import { useAuthStore } from '../../stores/auth'
import { getAdminComments, getPendingCount, approveComment, rejectComment, deleteComment } from '../../api/comments'
import { ElMessage } from 'element-plus'
const router = useRouter()
const auth = useAuthStore()
const comments = ref([])
const loading = ref(true)
const pendingCount = ref(0)

function statusType(status) {
  return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'info' }[status] || 'info'
}
function statusLabel(status) {
  return { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[status] || status
}
function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
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
async function handleApprove(row) {
  try {
    await approveComment(row.id)
    row.status = 'APPROVED'
    pendingCount.value = Math.max(0, pendingCount.value - 1)
    ElMessage.success('已通过')
  } catch (e) { ElMessage.error('操作失败') }
}
async function handleReject(row) {
  try {
    await rejectComment(row.id)
    row.status = 'REJECTED'
    pendingCount.value = Math.max(0, pendingCount.value - 1)
    ElMessage.success('已拒绝')
  } catch (e) { ElMessage.error('操作失败') }
}
async function handleDelete(row) {
  try {
    await deleteComment(row.id)
    comments.value = comments.value.filter(c => c.id !== row.id)
    if (row.status === 'PENDING') pendingCount.value = Math.max(0, pendingCount.value - 1)
    ElMessage.success('已删除')
  } catch (e) { ElMessage.error('删除失败') }
}

onMounted(async () => {
  try {
    const [res, countRes] = await Promise.all([getAdminComments(), getPendingCount()])
    comments.value = res.data || []
    pendingCount.value = countRes.data?.count || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
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
.comment-content {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-size: 14px;
  color: var(--text-body);
}
</style>
