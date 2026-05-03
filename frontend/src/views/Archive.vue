<template>
  <div class="page">
    <!-- Navigation -->
    <nav class="nav">
      <div class="nav-inner">
        <router-link to="/" class="nav-brand">风屿 · 随笔</router-link>
        <div class="nav-links">
          <router-link class="nav-link" to="/">首页</router-link>
          <router-link class="nav-link active" to="/archive">归档</router-link>
          <router-link class="nav-link" to="/admin/login">管理</router-link>
          <DarkToggle />
        </div>
      </div>
    </nav>

    <!-- Header -->
    <section class="header">
      <div class="header-inner">
        <h1 class="header-title">📂 文章归档</h1>
        <p class="header-subtitle">共 {{ totalCount }} 篇文章 · 按时间倒序排列</p>
      </div>
    </section>

    <!-- Archive Timeline -->
    <div class="main-layout">
      <main class="content">
        <div v-if="loading" class="loading-text">加载中...</div>

        <div v-for="group in archive" :key="group.year + '-' + group.month" class="archive-group">
          <div class="archive-header">
            <span class="archive-month">{{ group.monthLabel }}</span>
            <span class="archive-count">{{ group.count }} 篇</span>
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
        <div class="sidebar-card">
          <div class="sidebar-title">📊 统计</div>
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
        </div>

        <div class="sidebar-card">
          <div class="sidebar-title">📅 归档导航</div>
          <div class="jump-list">
            <a v-for="group in archive" :key="group.year + '-' + group.month"
               :href="'#' + group.year + '-' + group.month"
               class="jump-item"
               @click.prevent="scrollTo(group.year + '-' + group.month)">
              {{ group.monthLabel }}
              <span class="jump-count">{{ group.count }}</span>
            </a>
          </div>
        </div>
      </aside>
    </div>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-inner">
        <p>&copy; {{ new Date().getFullYear() }} 风屿 · 随笔. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getArchive } from '../api/articles'
import DarkToggle from '../components/DarkToggle.vue'

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

<style>
.page {
  min-height: 100vh;
  background: var(--bg-page);
  transition: background 0.3s ease;
}

/* Nav */
.nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--bg-nav);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border);
  transition: background 0.3s ease, border-color 0.3s ease;
}
.nav-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 24px;
  height: var(--nav-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.nav-brand {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
  font-family: 'Noto Serif SC', serif;
  letter-spacing: 1px;
}
.nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}
.nav-link {
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
  font-weight: 500;
}
.nav-link:hover,
.nav-link.active {
  color: var(--text-primary);
}

/* Header */
.header {
  padding: 60px 24px 40px;
  text-align: center;
}
.header-inner {
  max-width: 640px;
  margin: 0 auto;
}
.header-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.header-subtitle {
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
.archive-group:target {
  animation: highlight 2s ease;
}
@keyframes highlight {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}
.archive-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}
.archive-month {
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
}
.archive-count {
  font-size: 13px;
  color: var(--text-muted);
  background: var(--bg-tag);
  padding: 2px 10px;
  border-radius: 10px;
  transition: background 0.3s ease;
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
  background: var(--bg-card);
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  transition: background 0.3s ease;
}
.sidebar-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border);
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
  background: var(--bg-tag);
  color: var(--text-primary);
}
.jump-count {
  font-size: 12px;
  color: var(--text-muted);
  background: var(--bg-tag);
  padding: 1px 8px;
  border-radius: 8px;
  transition: background 0.3s ease;
}

/* Footer */
.footer {
  padding: 32px 24px;
  text-align: center;
}
.footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
}
.footer p {
  font-size: 13px;
  color: var(--text-muted);
}
</style>
