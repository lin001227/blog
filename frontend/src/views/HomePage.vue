<template>
  <div class="page">
    <!-- Navigation -->
    <nav class="nav">
      <div class="nav-inner">
        <router-link to="/" class="nav-brand">风屿 · 随笔</router-link>
        <div class="nav-links">
          <a class="nav-link active" href="/">首页</a>
          <a class="nav-link" href="#">归档</a>
          <a class="nav-link" href="#">标签</a>
          <a class="nav-link" href="#">关于</a>
          <router-link class="nav-link" to="/admin/login">管理</router-link>
          <button class="dark-toggle" @click="toggleDark" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
            {{ isDark ? '☀️' : '🌙' }}
          </button>
        </div>
      </div>
    </nav>

    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <h1 class="hero-title">风屿 · 随笔</h1>
        <p class="hero-subtitle">记录思考，分享见解，在文字中找到宁静。</p>
      </div>
    </section>

    <div class="main-layout" v-if="articles.length > 0">
      <!-- Main Content -->
      <main class="content">
        <!-- Pinned Article -->
        <article v-if="pinnedArticle" class="article-card pinned-card">
          <div class="pinned-badge">
            <el-icon style="margin-right: 4px;"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/></svg></el-icon>
            置顶
          </div>
          <h2 class="article-title">
            <router-link :to="`/article/${pinnedArticle.id}`">{{ pinnedArticle.title }}</router-link>
          </h2>
          <div class="article-meta">
            <span>{{ formatDate(pinnedArticle.createdAt) }}</span>
            <span v-if="pinnedArticle.category"> · {{ pinnedArticle.category }}</span>
            <span class="view-count"> · 👁️ {{ pinnedArticle.viewCount ?? 0 }} 阅读</span>
          </div>
          <p class="article-excerpt">{{ excerpt(pinnedArticle.content) }}</p>
          <router-link :to="`/article/${pinnedArticle.id}`" class="read-more">阅读全文 →</router-link>
        </article>

        <!-- Article List -->
        <div class="section-title" v-if="regularArticles.length > 0">最新文章</div>
        <article v-for="article in regularArticles" :key="article.id" class="article-card">
          <h2 class="article-title">
            <router-link :to="`/article/${article.id}`">{{ article.title }}</router-link>
          </h2>
          <div class="article-meta">
            <span>{{ formatDate(article.createdAt) }}</span>
            <span v-if="article.category"> · {{ article.category }}</span>
            <span v-if="article.tags && article.tags.length"> · {{ article.tags }}</span>
            <span class="view-count"> · 👁️ {{ article.viewCount ?? 0 }} 阅读</span>
          </div>
          <p class="article-excerpt">{{ excerpt(article.content) }}</p>
          <router-link :to="`/article/${article.id}`" class="read-more">阅读全文 →</router-link>
        </article>

        <div v-if="loading" class="loading-text">加载中...</div>
      </main>

      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-card">
          <div class="sidebar-title">关于</div>
          <p class="sidebar-text">个人博客，记录技术、生活与思考。</p>
        </div>

        <div class="sidebar-card">
          <div class="sidebar-title">分类</div>
          <div class="tag-list">
            <span v-for="cat in categories" :key="cat" class="tag">{{ cat }}</span>
          </div>
          <div v-if="categories.length === 0" class="sidebar-text" style="font-size: 14px;">暂无分类</div>
        </div>

        <div class="sidebar-card">
          <div class="sidebar-title">时间线</div>
          <div class="archive-list">
            <div v-for="item in archiveMonths" :key="item" class="archive-item">{{ item }}</div>
            <div v-if="archiveMonths.length === 0" class="sidebar-text" style="font-size: 14px;">暂无归档</div>
          </div>
        </div>
      </aside>
    </div>

    <!-- Newsletter -->
    <section class="newsletter">
      <div class="newsletter-inner">
        <h3 class="newsletter-title">订阅动态</h3>
        <p class="newsletter-text">获取最新文章推送，不错过每一篇精彩内容。</p>
        <div class="newsletter-form">
          <input type="email" placeholder="输入邮箱地址" class="newsletter-input" />
          <button class="newsletter-btn">订阅</button>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-inner">
        <p>&copy; {{ new Date().getFullYear() }} 风屿 · 随笔. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getArticles } from '../api/articles'
import { useDarkMode } from '../composables/useDarkMode'

const { isDark, toggleDark } = useDarkMode()

const articles = ref([])
const loading = ref(true)

const pinnedArticle = computed(() => articles.value.find(a => a.pinned))
const regularArticles = computed(() => articles.value.filter(a => !a.pinned))

const categories = computed(() => {
  const cats = new Set()
  articles.value.forEach(a => { if (a.category) cats.add(a.category) })
  return Array.from(cats)
})

const archiveMonths = computed(() => {
  const months = new Set()
  articles.value.forEach(a => {
    if (a.createdAt) {
      const d = new Date(a.createdAt)
      months.add(`${d.getFullYear()}年${d.getMonth() + 1}月`)
    }
  })
  return Array.from(months).sort().reverse()
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function excerpt(content) {
  if (!content) return ''
  const text = content.replace(/<[^>]*>/g, '').replace(/[#*\[\]`>|-]/g, ' ').trim()
  return text.substring(0, 150) + (text.length > 150 ? '...' : '')
}

onMounted(async () => {
  try {
    const res = await getArticles()
    articles.value = res.data || []
  } catch (e) {
    console.error('Failed to load articles:', e)
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

/* Hero */
.hero {
  padding: 80px 24px 60px;
  text-align: center;
}
.hero-inner {
  max-width: 640px;
  margin: 0 auto;
}
.hero-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
  letter-spacing: 2px;
}
.hero-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.7;
  font-weight: 400;
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

/* Article Cards */
.article-card {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 32px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-card);
  transition: box-shadow 0.2s, background 0.3s ease;
}
.article-card:hover {
  box-shadow: var(--shadow-card-hover);
}
.pinned-card {
  border: 1px solid var(--border);
}
.pinned-badge {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  color: var(--text-accent);
  background: var(--bg-pinned);
  padding: 2px 10px;
  border-radius: 12px;
  margin-bottom: 12px;
  font-weight: 500;
  transition: background 0.3s ease;
}
.section-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}
.article-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 10px;
  line-height: 1.4;
}
.article-title a {
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.2s;
}
.article-title a:hover {
  color: var(--text-accent);
}
.article-meta {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 14px;
}
.article-excerpt {
  font-size: 15px;
  color: var(--text-body);
  line-height: 1.7;
  margin-bottom: 16px;
}
.view-count {
  font-size: 12px;
  color: var(--text-muted);
}
.read-more {
  font-size: 14px;
  color: var(--text-accent);
  text-decoration: none;
  font-weight: 500;
  transition: opacity 0.2s;
}
.read-more:hover {
  opacity: 0.7;
}
.loading-text {
  text-align: center;
  color: var(--text-muted);
  padding: 40px;
  font-size: 15px;
}

/* Sidebar */
.sidebar-card {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-card);
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
.sidebar-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
}
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tag {
  display: inline-block;
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-tag);
  padding: 3px 10px;
  border-radius: 12px;
  cursor: default;
  transition: background 0.3s ease;
}
.archive-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.archive-item {
  font-size: 14px;
  color: var(--text-secondary);
  cursor: default;
  padding: 2px 0;
}

/* Newsletter */
.newsletter {
  background: var(--bg-newsletter);
  border-top: 1px solid var(--border);
  padding: 60px 24px;
  text-align: center;
  transition: background 0.3s ease, border-color 0.3s ease;
}
.newsletter-inner {
  max-width: 480px;
  margin: 0 auto;
}
.newsletter-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.newsletter-text {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}
.newsletter-form {
  display: flex;
  gap: 8px;
  max-width: 400px;
  margin: 0 auto;
}
.newsletter-input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid var(--border-input);
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
  background: var(--bg-input);
  color: var(--text-primary);
}
.newsletter-input:focus {
  border-color: var(--text-accent);
}
.newsletter-btn {
  padding: 10px 24px;
  background: var(--text-primary);
  color: var(--bg-page);
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.2s;
  font-family: inherit;
  font-weight: 500;
}
.newsletter-btn:hover {
  opacity: 0.8;
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
