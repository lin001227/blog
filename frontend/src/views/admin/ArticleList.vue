<template>
  <div class="admin-page">
    <!-- Admin Nav -->
    <nav class="admin-nav">
      <div class="admin-nav-inner">
        <router-link to="/admin" class="admin-nav-brand">风屿 · 管理</router-link>
        <div class="admin-nav-links">
          <router-link to="/admin" class="admin-nav-link">概览</router-link>
          <router-link to="/admin/articles" class="admin-nav-link">文章</router-link>
          <router-link v-if="auth.isAdmin" to="/admin/users" class="admin-nav-link">用户</router-link>
          <router-link to="/admin/comments" class="admin-nav-link">评论</router-link>
          <a class="admin-nav-link" href="/" target="_blank">查看博客</a>
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
        <h1 class="admin-heading">文章管理</h1>
        <router-link to="/admin/articles/new">
          <el-button type="primary">
            <el-icon style="margin-right: 4px;"><Plus /></el-icon>
            新建文章
          </el-button>
        </router-link>
      </div>

      <el-table
        :data="articles"
        v-loading="loading"
        stripe
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="title" label="标题" min-width="240">
          <template #default="{ row }">
            <div class="article-title-cell">
              <span v-if="row.pinned" class="pinned-dot" title="置顶">📌</span>
              {{ row.title }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="标签" width="180">
          <template #default="{ row }">
            <el-tag
              v-for="tag in getTags(row)"
              :key="tag"
              size="small"
              style="margin-right: 4px; margin-bottom: 2px;"
            >
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="!!row.pinned"
              size="small"
              @click.stop
              @change="(val) => togglePinned(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click.stop="editArticle(row)">
              编辑
            </el-button>
            <el-popconfirm
              title="确定删除此文章？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm.stop="handleDelete(row)"
            >
              <template #reference>
                <el-button text size="small" type="danger" @click.stop>
                  删除
                </el-button>
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
import { getAdminArticles, updateArticle, deleteArticle } from '../../api/articles'
import { ElMessage } from 'element-plus'
import { useDarkMode } from '../../composables/useDarkMode'

const { isDark, toggleDark } = useDarkMode()

const router = useRouter()
const auth = useAuthStore()

const articles = ref([])
const loading = ref(true)

function getTags(row) {
  if (!row.tags) return []
  if (Array.isArray(row.tags)) return row.tags
  return String(row.tags).split(/[,，\s]+/).filter(Boolean)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function handleLogout() {
  auth.logout()
  router.push('/admin/login')
}

function handleRowClick(row) {
  router.push(`/admin/articles/${row.id}/edit`)
}

function editArticle(row) {
  router.push(`/admin/articles/${row.id}/edit`)
}

async function togglePinned(row, val) {
  try {
    await updateArticle(row.id, { pinned: val })
    row.pinned = val
    ElMessage.success(val ? '已置顶' : '已取消置顶')
  } catch (e) {
    ElMessage.error('操作失败')
    console.error(e)
  }
}

async function handleDelete(row) {
  try {
    await deleteArticle(row.id)
    articles.value = articles.value.filter(a => a.id !== row.id)
    ElMessage.success('删除成功')
  } catch (e) {
    ElMessage.error('删除失败')
    console.error(e)
  }
}

onMounted(async () => {
  try {
    const res = await getAdminArticles()
    articles.value = res.data || []
  } catch (e) {
    console.error('Failed to load articles:', e)
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
.admin-nav-link.router-link-exact-active {
  color: var(--text-primary);
}

/* Dark mode toggle */
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

.article-title-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}
.pinned-dot {
  font-size: 14px;
}
</style>
