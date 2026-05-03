<template>
  <div class="dashboard-page">
    <!-- Admin Nav -->
    <nav class="admin-nav">
      <div class="admin-nav-inner">
        <router-link to="/admin" class="admin-nav-brand">风屿 · 管理</router-link>
        <div class="admin-nav-links">
          <router-link to="/admin" class="admin-nav-link active">概览</router-link>
          <router-link to="/admin/articles" class="admin-nav-link">文章</router-link>
          <router-link to="/admin/comments" class="admin-nav-link">评论</router-link>
          <router-link v-if="auth.isAdmin" to="/admin/users" class="admin-nav-link">用户</router-link>
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

    <main class="dashboard-main">
      <div class="dashboard-header">
        <h1 class="dashboard-heading">欢迎回来，{{ auth.displayName }}</h1>
        <div class="header-subtitle">{{ currentDate }}</div>
      </div>

      <!-- Stats Grid -->
      <div class="stats-grid">
        <div class="stat-card accent-blue">
          <div class="stat-number">{{ stats.totalArticles }}</div>
          <div class="stat-label">📝 文章总数</div>
          <div class="stat-trend" v-if="stats.articlesThisWeek > 0">本周 +{{ stats.articlesThisWeek }}</div>
        </div>
        <div class="stat-card accent-green">
          <div class="stat-number">{{ renderCount(stats.totalViews) }}</div>
          <div class="stat-label">👁️ 总阅读量</div>
          <div class="stat-trend">流量总计</div>
        </div>
        <div class="stat-card accent-orange">
          <div class="stat-number">{{ stats.totalComments }}</div>
          <div class="stat-label">💬 评论总数</div>
          <div class="stat-trend pending" v-if="stats.pendingComments > 0">{{ stats.pendingComments }} 条待审核</div>
        </div>
        <div class="stat-card accent-purple">
          <div class="stat-number">{{ stats.totalUsers }}</div>
          <div class="stat-label">👥 用户总数</div>
          <div class="stat-trend">管理员 + 编辑</div>
        </div>
      </div>

      <!-- Charts Row -->
      <div class="dashboard-grid">
        <!-- Top Articles -->
        <div class="panel">
          <h2 class="section-title">🔥 阅读量 TOP 5</h2>
          <div v-if="topArticles.length === 0" class="empty-state">暂无数据</div>
          <div v-else class="top-list">
            <div v-for="(article, index) in topArticles" :key="article.id" class="top-item">
              <div class="top-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <div class="top-info">
                <router-link :to="'/admin/articles/' + article.id + '/edit'" class="top-title">
                  {{ article.title }}
                </router-link>
                <div class="top-meta">
                  <span>{{ article.viewCount || 0 }} 阅读</span>
                  <span class="meta-dot">·</span>
                  <span>{{ formatDate(article.createdAt) }}</span>
                </div>
              </div>
              <div class="top-bar-wrap">
                <div class="top-bar" :style="{ width: (article.viewCount / maxViews * 100) + '%' }"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Articles -->
        <div class="panel">
          <h2 class="section-title">🕐 最近文章</h2>
          <div v-if="recentArticles.length === 0" class="empty-state">暂无数据</div>
          <div v-else class="recent-list">
            <div v-for="article in recentArticles" :key="article.id" class="recent-item">
              <div class="recent-icon">{{ article.pinned ? '📌' : '📄' }}</div>
              <div class="recent-info">
                <router-link :to="'/admin/articles/' + article.id + '/edit'" class="recent-title">
                  {{ article.title }}
                </router-link>
                <div class="recent-meta">
                  <span class="recent-cat" v-if="article.category">{{ article.category }}</span>
                  <span>{{ formatDate(article.createdAt) }}</span>
                </div>
              </div>
              <div class="recent-views">
                <span class="view-icon">👁️</span> {{ article.viewCount || 0 }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="quick-actions">
        <h2 class="section-title">⚡ 快捷操作</h2>
        <div class="action-buttons">
          <router-link to="/admin/articles/new">
            <el-button type="primary" size="large">
              写新文章
            </el-button>
          </router-link>
          <router-link to="/admin/articles">
            <el-button size="large">
              管理文章
            </el-button>
          </router-link>
          <router-link v-if="stats.pendingComments > 0" to="/admin/comments">
            <el-button size="large" type="warning">
              审核评论 ({{ stats.pendingComments }})
            </el-button>
          </router-link>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { getDashboard } from '../../api/dashboard'
import { useDarkMode } from '../../composables/useDarkMode'

const { isDark, toggleDark } = useDarkMode()

const router = useRouter()
const auth = useAuthStore()

const stats = ref({
  totalArticles: 0,
  articlesThisWeek: 0,
  articlesThisMonth: 0,
  totalViews: 0,
  pinnedCount: 0,
  totalComments: 0,
  pendingComments: 0,
  totalUsers: 0,
})
const topArticles = ref([])
const recentArticles = ref([])

const maxViews = computed(() => {
  if (topArticles.value.length === 0) return 1
  return Math.max(...topArticles.value.map(a => a.viewCount || 0), 1)
})

const currentDate = computed(() => {
  const now = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${weekdays[now.getDay()]}`
})

function renderCount(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return n
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) return '刚刚'
    return hours + '小时前'
  }
  if (days === 1) return '昨天'
  if (days < 7) return days + '天前'
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function handleLogout() {
  auth.logout()
  router.push('/admin/login')
}

onMounted(async () => {
  try {
    const res = await getDashboard()
    stats.value = res.data.stats || stats.value
    topArticles.value = res.data.topArticles || []
    recentArticles.value = res.data.recentArticles || []
  } catch (e) {
    console.error('加载仪表盘失败:', e)
  }
})
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  background: var(--bg-page);
  transition: background 0.3s ease;
}

/* ===== Nav ===== */
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

/* ===== Main ===== */
.dashboard-main {
  max-width: 1100px;
  margin: 0 auto;
  padding: 40px 24px 60px;
}

.dashboard-header {
  margin-bottom: 32px;
}
.dashboard-heading {
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.header-subtitle {
  font-size: 14px;
  color: var(--text-muted);
}

/* ===== Stats ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}
.stat-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-card);
  transition: background 0.3s ease, transform 0.2s;
  border-top: 3px solid transparent;
  position: relative;
  overflow: hidden;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-card.accent-blue { border-top-color: #409eff; }
.stat-card.accent-green { border-top-color: #67c23a; }
.stat-card.accent-orange { border-top-color: #e6a23c; }
.stat-card.accent-purple { border-top-color: #9b59b6; }

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.stat-label {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 4px;
}
.stat-trend {
  font-size: 12px;
  color: var(--text-muted);
  opacity: 0.8;
}
.stat-trend.pending {
  color: #e6a23c;
  font-weight: 500;
}

/* ===== Grid ===== */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 32px;
}

.panel {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-card);
  transition: background 0.3s ease;
}

.section-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.empty-state {
  text-align: center;
  padding: 32px 0;
  color: var(--text-muted);
  font-size: 14px;
}

/* ===== Top Articles ===== */
.top-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.top-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  position: relative;
}
.top-rank {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}
.rank-1 { background: #e74c3c; }
.rank-2 { background: #e67e22; }
.rank-3 { background: #f39c12; }
.rank-4, .rank-5 { background: var(--border); color: var(--text-muted); }

.top-info {
  flex: 1;
  min-width: 0;
  z-index: 1;
}
.top-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  text-decoration: none;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.top-title:hover {
  color: var(--text-accent);
}
.top-meta {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}
.meta-dot {
  margin: 0 4px;
}

.top-bar-wrap {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100%;
  pointer-events: none;
  border-radius: 8px;
  overflow: hidden;
}
.top-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, rgba(64,158,255,0.08), rgba(64,158,255,0.03));
  border-radius: 8px;
  transition: width 0.6s ease;
  min-width: 4px;
}

/* ===== Recent Articles ===== */
.recent-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.recent-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}
.recent-item:hover {
  background: var(--bg-page);
}

.recent-icon {
  font-size: 16px;
  flex-shrink: 0;
}
.recent-info {
  flex: 1;
  min-width: 0;
}
.recent-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  text-decoration: none;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recent-title:hover {
  color: var(--text-accent);
}
.recent-meta {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.recent-cat {
  background: var(--bg-page);
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
}
.recent-views {
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
  flex-shrink: 0;
}
.view-icon {
  font-size: 12px;
}

/* ===== Quick Actions ===== */
.quick-actions {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-card);
  transition: background 0.3s ease;
}
.action-buttons {
  display: flex;
  gap: 12px;
}
</style>
