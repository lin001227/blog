<template>
  <!-- Header -->
  <section class="readings-header">
    <div class="readings-header-inner">
      <h1 class="readings-title">📖 精选阅读</h1>
      <p class="readings-subtitle">来自网络各处的优质文章 · 每篇附 AI 摘要</p>
    </div>
  </section>

  <!-- Content -->
  <div class="main-layout">
    <main class="content">
      <div v-if="loading">
        <div v-for="n in 5" :key="n" class="skeleton-card">
          <div class="skeleton-header">
            <div class="skeleton-title skeleton-pulse"></div>
            <div class="skeleton-actions">
              <span class="skeleton-badge skeleton-pulse"></span>
              <span class="skeleton-btn skeleton-pulse"></span>
            </div>
            <div class="skeleton-meta-row">
              <span class="skeleton-meta skeleton-pulse" style="width:60px"></span>
              <span class="skeleton-meta skeleton-pulse" style="width:50px"></span>
              <span class="skeleton-meta skeleton-pulse" style="width:55px"></span>
            </div>
          </div>
          <div class="skeleton-excerpt">
            <div class="skeleton-line skeleton-pulse"></div>
            <div class="skeleton-line skeleton-pulse" style="width:90%"></div>
            <div class="skeleton-line skeleton-pulse" style="width:60%"></div>
          </div>
        </div>
      </div>

      <div v-else-if="articles.length === 0" class="empty-state">
        暂无收录文章
      </div>

      <el-card
        v-for="article in articles"
        :key="article.id"
        class="reading-card"
        shadow="hover"
      >
        <div class="card-inner">
          <div class="card-body">
            <!-- Header with title + actions below -->
            <div class="card-header">
              <h2 class="card-title">{{ article.title }}</h2>
              <div v-if="article.summaryType === 'ai'" class="card-actions">
                <el-tag size="small" class="ai-badge">AI 摘要</el-tag>
                <el-button text type="primary" @click="openUrl(article.url)" class="read-original">
                  阅读原文 ↗
                </el-button>
              </div>
              <div class="card-meta">
                <span class="card-source">🌐 {{ article.source }}</span>
                <span class="card-date">{{ formatDate(article.createdAt) }}</span>
                <el-tag v-if="article.category" size="small" effect="plain">{{ article.category }}</el-tag>
              </div>
            </div>

            <!-- Summary -->
            <p class="card-summary">{{ article.summary }}</p>
          </div>
        </div>
      </el-card>
    </main>

    <!-- Sidebar -->
    <aside class="sidebar">
      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-title">📊 统计</span></template>
        <div class="stat-list">
          <div class="stat-item">
            <span class="stat-label">已收录</span>
            <span class="stat-value">{{ articles.length }} 篇</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">来源站点</span>
            <span class="stat-value">{{ uniqueSources }} 个</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-title">🏷️ 分类</span></template>
        <div class="tag-list">
          <el-tag
            v-for="cat in categories"
            :key="cat"
            size="small"
            effect="plain"
            style="margin:2px;cursor:pointer"
            tabindex="0" role="button"
            @click="filterCategory = cat === filterCategory ? '' : cat"
            @keydown.enter="filterCategory = cat === filterCategory ? '' : cat"
            @keydown.space.prevent="filterCategory = cat === filterCategory ? '' : cat"
            :type="filterCategory === cat ? 'primary' : 'info'"
          >
            {{ cat }}
          </el-tag>
        </div>
        <div v-if="categories.length === 0" class="sidebar-text">暂无分类</div>
      </el-card>

      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-title">💡 说明</span></template>
        <p class="sidebar-text">
          精选阅读自动抓取文章内容并通过 AI 生成摘要，方便快速了解文章核心内容。
          <router-link to="/" class="back-link">← 返回博客</router-link>
        </p>
      </el-card>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getExternalArticles } from '../api/externalArticles'

const articles = ref([])
const loading = ref(true)
const filterCategory = ref('')

const uniqueSources = computed(() => {
  const sources = new Set(articles.value.map(a => a.source).filter(Boolean))
  return sources.size
})

const categories = computed(() => {
  const cats = new Set(articles.value.map(a => a.category).filter(Boolean))
  return Array.from(cats)
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function openUrl(url) {
  window.open(url, '_blank', 'noopener')
}

onMounted(async () => {
  try {
    const res = await getExternalArticles()
    articles.value = res.data || []
  } catch (e) {
    console.error('Failed to load external articles:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* Header */
.readings-header {
  padding: 60px 24px 40px;
  text-align: center;
}
.readings-header-inner {
  max-width: 600px;
  margin: 0 auto;
}
.readings-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.readings-subtitle {
  font-size: 15px;
  color: var(--text-muted);
}

/* Main Layout */
.main-layout {
  max-width: 1080px;
  margin: 0 auto;
  padding: 0 24px 60px;
  display: flex;
  gap: 40px;
  align-items: flex-start;
}
.content {
  flex: 1;
  min-width: 0;
}

/* Reading Cards */
.reading-card {
  margin-bottom: 20px;
  cursor: default;
  border-radius: 10px !important;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
  box-shadow: var(--shadow-card) !important;
  transition: all 0.25s ease;
  overflow: hidden;
}
.reading-card:hover {
  box-shadow: var(--shadow-card-hover) !important;
  border-color: var(--text-accent) !important;
}
.reading-card :deep(.el-card__body) {
  padding: 0;
}
.card-inner {
  overflow: hidden;
}
.card-body {
  padding: 20px 22px;
}
.card-header {
  margin-bottom: 12px;
}
.card-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  margin: 0 0 6px;
}
.card-meta {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.card-source {
  font-weight: 500;
  color: var(--text-secondary);
}
.card-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.ai-badge {
  font-size: 11px;
  height: 22px;
  line-height: 22px;
  padding: 0 8px !important;
  border-radius: 4px;
  border: none !important;
  background: var(--bg-tag) !important;
  color: var(--text-accent) !important;
  font-weight: 500;
}
.read-original {
  font-size: 13px;
  white-space: nowrap;
  padding: 0 2px;
}

/* Sidebar */
.sidebar {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.sidebar-card {
  border-radius: 10px !important;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
}
.sidebar-card :deep(.el-card__header) {
  padding: 16px 20px 12px;
  border-bottom: 1px solid var(--border);
}
.sidebar-card :deep(.el-card__body) {
  padding: 12px 20px 18px;
}
.sidebar-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.sidebar-text {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
  margin: 0;
}
.stat-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}
.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.back-link {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-accent);
  text-decoration: none;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 24px;
  color: var(--text-muted);
  font-size: 15px;
}

/* Responsive */
@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
    padding: 0 16px 40px;
  }
  .sidebar {
    width: 100%;
  }
  .card-body {
    padding: 16px;
  }
  .readings-header {
    padding: 40px 16px 32px;
  }
  .readings-title {
    font-size: 26px;
  }
}

/* Skeleton Loading */
.skeleton-card {
  margin-bottom: 20px;
  border-radius: 10px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-card);
  padding: 20px 22px;
  width: 100%;
}
.skeleton-header {
  margin-bottom: 14px;
}
.skeleton-title {
  height: 20px;
  width: 70%;
  border-radius: 4px;
  margin-bottom: 8px;
}
.skeleton-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.skeleton-badge {
  height: 22px;
  width: 62px;
  border-radius: 4px;
}
.skeleton-btn {
  height: 22px;
  width: 80px;
  border-radius: 4px;
}
.skeleton-meta-row {
  display: flex;
  gap: 10px;
}
.skeleton-meta {
  height: 12px;
  border-radius: 3px;
}
.skeleton-excerpt {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.skeleton-line {
  height: 13px;
  width: 100%;
  border-radius: 3px;
}
.skeleton-pulse {
  background: var(--bg-tag);
  animation: skeleton-pulse 1.8s ease-in-out infinite;
}
@keyframes skeleton-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}
</style>
