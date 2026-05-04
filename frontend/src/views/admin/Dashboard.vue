<template>
  <div class="dashboard">
    <!-- Header Section -->
    <div class="dash-header">
      <div class="header-content">
        <div class="header-greeting">
          <h1 class="header-title">欢迎回来，{{ auth.displayName }}</h1>
          <p class="header-date">{{ currentDate }}</p>
        </div>
        <div class="header-decoration" aria-hidden="true">
          <svg width="120" height="48" viewBox="0 0 120 48" fill="none">
            <path d="M0 36L15 30L30 38L45 24L60 32L75 18L90 26L105 12L120 20" stroke="currentColor" stroke-width="2" opacity="0.2" stroke-linecap="round" />
            <path d="M0 42L15 36L30 44L45 30L60 38L75 24L90 32L105 18L120 26" stroke="currentColor" stroke-width="1.5" opacity="0.1" stroke-linecap="round" />
          </svg>
        </div>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="stats-grid">
      <!-- Articles -->
      <div class="stat-card card-blue">
        <div class="stat-icon-wrap">
          <el-icon :size="20"><Document /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ renderCount(stats.totalArticles) }}</div>
          <div class="stat-label">文章总数</div>
          <div v-if="stats.articlesThisWeek > 0" class="stat-trend up">
            <el-icon :size="10"><Top /></el-icon>
            <span>本周 +{{ stats.articlesThisWeek }}</span>
          </div>
          <div v-else class="stat-trend">累计发布</div>
        </div>
      </div>

      <!-- Views -->
      <div class="stat-card card-green">
        <div class="stat-icon-wrap">
          <el-icon :size="20"><View /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ renderCount(stats.totalViews) }}</div>
          <div class="stat-label">总阅读量</div>
          <div class="stat-trend">流量总计</div>
        </div>
      </div>

      <!-- Comments -->
      <div class="stat-card card-orange">
        <div class="stat-icon-wrap">
          <el-icon :size="20"><ChatDotSquare /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ renderCount(stats.totalComments) }}</div>
          <div class="stat-label">评论总数</div>
          <div v-if="stats.pendingComments > 0" class="stat-trend pending">
            <el-icon :size="10"><WarningFilled /></el-icon>
            <span>{{ stats.pendingComments }} 条待审核</span>
          </div>
          <div v-else class="stat-trend">暂无待审核</div>
        </div>
      </div>

      <!-- Users -->
      <div class="stat-card card-purple">
        <div class="stat-icon-wrap">
          <el-icon :size="20"><UserFilled /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-number">{{ renderCount(stats.totalUsers) }}</div>
          <div class="stat-label">用户总数</div>
          <div class="stat-trend">管理员 + 编辑</div>
        </div>
      </div>
    </div>

    <!-- Panels Row -->
    <div class="dash-grid">
      <!-- Top Articles -->
      <div class="panel">
        <div class="panel-header">
          <div class="panel-header-left">
            <el-icon :size="18" class="panel-icon"><TrendCharts /></el-icon>
            <h2 class="panel-title">阅读量 TOP 5</h2>
          </div>
        </div>

        <div v-if="loading" class="skeleton-wrap">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="topArticles.length === 0" class="empty-state">
          <el-empty description="暂无数据" />
        </div>
        <div v-else class="top-list">
          <div v-for="(article, index) in topArticles" :key="article.id" class="top-item">
            <div class="top-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
            <div class="top-info">
              <router-link :to="'/admin/articles/' + article.id + '/edit'" class="top-title">
                {{ article.title }}
              </router-link>
              <div class="top-meta">
                <span class="top-views">{{ article.viewCount || 0 }} 阅读</span>
                <span class="meta-dot">·</span>
                <span>{{ formatDate(article.createdAt) }}</span>
              </div>
            </div>
            <div class="top-bar-track">
              <div class="top-bar-fill" :style="{ width: (article.viewCount / maxViews * 100) + '%' }" />
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Articles -->
      <div class="panel">
        <div class="panel-header">
          <div class="panel-header-left">
            <el-icon :size="18" class="panel-icon"><Timer /></el-icon>
            <h2 class="panel-title">最近文章</h2>
          </div>
        </div>

        <div v-if="loading" class="skeleton-wrap">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="recentArticles.length === 0" class="empty-state">
          <el-empty description="暂无数据" />
        </div>
        <div v-else class="recent-list">
          <div v-for="article in recentArticles" :key="article.id" class="recent-item">
            <div class="recent-status-icon">
              <el-icon v-if="article.pinned" :size="16" class="pinned-star"><StarFilled /></el-icon>
              <el-icon v-else :size="16" class="doc-icon"><Document /></el-icon>
            </div>
            <div class="recent-info">
              <router-link :to="'/admin/articles/' + article.id + '/edit'" class="recent-title">
                {{ article.title }}
              </router-link>
              <div class="recent-meta">
                <span v-if="article.category" class="cat-badge">{{ article.category }}</span>
                <span class="recent-date">{{ formatDate(article.createdAt) }}</span>
              </div>
            </div>
            <div class="recent-views">
              <el-icon :size="13"><View /></el-icon>
              <span>{{ article.viewCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions">
      <div class="panel-header">
        <div class="panel-header-left">
          <el-icon :size="18" class="panel-icon"><Lightning /></el-icon>
          <h2 class="panel-title">快捷操作</h2>
        </div>
      </div>
      <div class="action-buttons">
        <router-link to="/admin/articles/new">
          <el-button type="primary" size="large">
            <el-icon><EditPen /></el-icon>
            写新文章
          </el-button>
        </router-link>
        <router-link to="/admin/articles">
          <el-button size="large">
            <el-icon><FolderOpened /></el-icon>
            管理文章
          </el-button>
        </router-link>
        <router-link v-if="stats.pendingComments > 0" to="/admin/comments">
          <el-button size="large" type="warning">
            <el-icon><ChatDotSquare /></el-icon>
            审核评论 ({{ stats.pendingComments }})
          </el-button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { getDashboard } from '../../api/dashboard'
import {
  Document, View, ChatDotSquare, UserFilled,
  TrendCharts, Timer, Lightning, StarFilled,
  EditPen, FolderOpened, Top, WarningFilled,
} from '@element-plus/icons-vue'

const auth = useAuthStore()
const loading = ref(true)

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

onMounted(async () => {
  try {
    const res = await getDashboard()
    stats.value = res.data.stats || stats.value
    topArticles.value = res.data.topArticles || []
    recentArticles.value = res.data.recentArticles || []
  } catch (e) {
    console.error('加载仪表盘失败:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* ============ Layout ============ */
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ============ Header ============ */
.dash-header {
  margin-bottom: 8px;
}

.header-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.header-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
  line-height: 1.3;
}

.header-date {
  font-size: 14px;
  color: var(--text-muted);
}

.header-decoration {
  flex-shrink: 0;
  color: var(--text-accent);
  opacity: 0.6;
  margin-top: 4px;
}

/* ============ Stats Grid ============ */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card-hover);
}

.card-blue::before  { background: linear-gradient(90deg, #3b82f6, #60a5fa); }
.card-green::before { background: linear-gradient(90deg, #22c55e, #4ade80); }
.card-orange::before { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.card-purple::before { background: linear-gradient(90deg, #8b5cf6, #a78bfa); }

.stat-card::after {
  content: '';
  position: absolute;
  top: -50%;
  right: -30%;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  opacity: 0.04;
  pointer-events: none;
}

.card-blue::after   { background: radial-gradient(circle, #3b82f6, transparent); }
.card-green::after  { background: radial-gradient(circle, #22c55e, transparent); }
.card-orange::after { background: radial-gradient(circle, #f59e0b, transparent); }
.card-purple::after { background: radial-gradient(circle, #8b5cf6, transparent); }

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  flex-shrink: 0;
  color: #ffffff;
}

.card-blue   .stat-icon-wrap { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.card-green  .stat-icon-wrap { background: linear-gradient(135deg, #22c55e, #4ade80); }
.card-orange .stat-icon-wrap { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.card-purple .stat-icon-wrap { background: linear-gradient(135deg, #8b5cf6, #a78bfa); }

.stat-body {
  flex: 1;
  min-width: 0;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.1;
  margin-bottom: 2px;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: var(--text-muted);
}

.stat-trend.up {
  color: #22c55e;
  font-weight: 500;
}

.stat-trend.pending {
  color: #f59e0b;
  font-weight: 500;
}

/* ============ Panels Grid ============ */
.dash-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.panel {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border);
  transition: background 0.3s ease;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-icon {
  color: var(--text-accent);
}

.panel-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}

/* ============ Skeleton / Empty ============ */
.skeleton-wrap {
  padding: 8px 0;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

/* ============ Top Articles ============ */
.top-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.top-item {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
}

.top-rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.rank-1 { background: linear-gradient(135deg, #f59e0b, #fbbf24); color: #ffffff; }
.rank-2 { background: linear-gradient(135deg, #94a3b8, #cbd5e1); color: #ffffff; }
.rank-3 { background: linear-gradient(135deg, #d97706, #f59e0b); color: #ffffff; }
.rank-4,
.rank-5 {
  background: var(--bg-page);
  color: var(--text-secondary);
  border: 1px solid var(--border);
}

.top-info {
  flex: 1;
  min-width: 0;
}

.top-title {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s;
}

.top-title:hover {
  color: var(--text-accent);
}

.top-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 1px;
}

.meta-dot {
  opacity: 0.4;
}

.top-bar-track {
  width: 56px;
  height: 4px;
  background: var(--bg-page);
  border-radius: 2px;
  overflow: hidden;
  flex-shrink: 0;
}

.top-bar-fill {
  height: 100%;
  background: var(--text-accent);
  border-radius: 2px;
  transition: width 0.6s cubic-bezier(0.16, 1, 0.3, 1);
  opacity: 0.5;
}

/* ============ Recent Articles ============ */
.recent-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border);
  transition: background 0.15s;
  border-radius: 6px;
  margin: 0 -6px;
  padding: 8px 6px;
}

.recent-item:last-child {
  border-bottom: none;
}

.recent-item:hover {
  background: var(--bg-toc-hover);
}

.recent-status-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: var(--bg-page);
}

.pinned-star {
  color: #f59e0b;
}

.doc-icon {
  color: var(--text-muted);
}

.recent-info {
  flex: 1;
  min-width: 0;
}

.recent-title {
  display: block;
  font-size: 14px;
  color: var(--text-primary);
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.2s;
}

.recent-title:hover {
  color: var(--text-accent);
}

.recent-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.cat-badge {
  background: var(--bg-tag);
  padding: 1px 7px;
  border-radius: 4px;
  font-size: 11px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.recent-date {
  white-space: nowrap;
}

.recent-views {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
  flex-shrink: 0;
}

/* ============ Quick Actions ============ */
.quick-actions {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border);
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  cursor: pointer;
}

.action-buttons .el-button [class*='el-icon'] {
  margin-right: 4px;
}

/* ============ Responsive ============ */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dash-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .header-decoration {
    display: none;
  }

  .header-title {
    font-size: 20px;
  }

  .stat-card {
    padding: 16px;
    gap: 12px;
  }

  .stat-number {
    font-size: 24px;
  }

  .panel {
    padding: 16px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .stat-card,
  .top-bar-fill {
    transition: none;
    animation: none;
  }

  .stat-card:hover {
    transform: none;
  }
}
</style>
