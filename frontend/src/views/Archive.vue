<template>
  <!-- Header -->
  <section class="archive-header-section">
    <div class="archive-header-inner">
      <h1 class="archive-header-title">📂 文章归档</h1>
      <p class="archive-header-subtitle">共 {{ totalCount }} 篇文章 · 按时间倒序排列</p>
    </div>
  </section>

  <!-- Archive Timeline -->
  <div class="main-layout">
    <main class="content">
      <div v-if="loading" class="loading-text">加载中...</div>

      <div v-for="group in archive" :key="group.year + '-' + group.month" :id="group.year + '-' + group.month" class="archive-group">
        <div class="archive-group-header">
          <span class="archive-group-month">{{ group.monthLabel }}</span>
          <el-tag size="small" effect="plain">{{ group.count }} 篇</el-tag>
        </div>
        <div class="timeline">
          <div v-for="article in group.articles" :key="article.id" class="timeline-item">
            <div class="timeline-dot"></div>
            <div class="timeline-date">{{ formatDay(article.createdAt) }}</div>
            <div class="timeline-content">
              <router-link :to="`/article/${article.id}`" class="timeline-title">
                {{ article.title }}
              </router-link>
              <div class="timeline-meta">
                <span v-if="article.category">{{ article.category }}</span>
                <span v-if="article.tags"> · {{ article.tags }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!loading && archive.length === 0" class="empty-text">暂无文章</div>
    </main>

    <!-- Sidebar -->
    <aside class="sidebar">
      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-card-title">📊 统计</span></template>
        <div class="stat-list">
          <div class="stat-item">
            <span class="stat-label">文章总数</span>
            <span class="stat-value">{{ totalCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">归档月份</span>
            <span class="stat-value">{{ archive.length }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">最早文章</span>
            <span class="stat-value">{{ earliestDate }}</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-card-title">📅 归档导航</span></template>
        <div class="jump-list">
          <a v-for="group in archive" :key="group.year + '-' + group.month"
             :href="'#' + group.year + '-' + group.month"
             class="jump-item"
             @click.prevent="scrollTo(group.year + '-' + group.month)">
            {{ group.monthLabel }}
            <el-tag size="small" effect="plain" class="jump-count">{{ group.count }}</el-tag>
          </a>
        </div>
      </el-card>
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getArchive } from '../api/articles'

const archive = ref([])
const loading = ref(true)

const totalCount = computed(() => archive.value.reduce((sum, g) => sum + g.count, 0))

const earliestDate = computed(() => {
  if (archive.value.length === 0) return '-'
  const last = archive.value[archive.value.length - 1]
  const article = last.articles[last.articles.length - 1]
  return formatDay(article.createdAt)
})

function formatDay(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(async () => {
  try {
    const res = await getArchive()
    archive.value = res.data || []
  } catch (e) {
    console.error('Failed to load archive:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* Header */
.archive-header-section {
  padding: 60px 24px 40px;
  text-align: center;
}
.archive-header-inner {
  max-width: 640px;
  margin: 0 auto;
}
.archive-header-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.archive-header-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
}

/* Main Layout */
.main-layout {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 24px 60px;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 48px;
}

/* Archive Group */
.archive-group {
  margin-bottom: 36px;
  scroll-margin-top: 80px;
}
.archive-group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}
.archive-group-month {
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
}

/* Timeline */
.timeline {
  position: relative;
  padding-left: 28px;
}
.timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: var(--border);
}
.timeline-item {
  position: relative;
  padding: 0 0 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.timeline-dot {
  position: absolute;
  left: -24px;
  top: 6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--text-accent);
  border: 2px solid var(--bg-page);
  z-index: 1;
}
.timeline-date {
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
  min-width: 52px;
  padding-top: 2px;
}
.timeline-content {
  flex: 1;
  background: var(--bg-card);
  border-radius: 8px;
  padding: 16px 20px;
  transition: background 0.3s ease;
}
.timeline-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  text-decoration: none;
  line-height: 1.5;
  transition: color 0.2s;
}
.timeline-title:hover {
  color: var(--text-accent);
}
.timeline-meta {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 6px;
}
.loading-text {
  text-align: center;
  color: var(--text-muted);
  padding: 60px;
  font-size: 15px;
}
.empty-text {
  text-align: center;
  color: var(--text-muted);
  padding: 80px;
  font-size: 15px;
}

/* Sidebar */
.sidebar-card {
  margin-bottom: 16px;
}
.sidebar-card :deep(.el-card__header) {
  padding: 14px 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}
.sidebar-card :deep(.el-card__body) {
  padding: 12px 16px;
}
.sidebar-card-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.stat-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
.jump-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 360px;
  overflow-y: auto;
}
.jump-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.jump-item:hover {
  background: var(--el-fill-color-light);
  color: var(--text-primary);
}
.jump-count {
  font-size: 11px;
}

/* Responsive */
@media (max-width: 768px) {
  .main-layout {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  .archive-header-title {
    font-size: 26px;
  }
}
</style>
