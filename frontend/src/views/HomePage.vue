<template>
  <!-- Hero -->
  <section class="hero">
    <div class="hero-inner">
      <h1 class="hero-title">风屿 · 随笔</h1>
      <p class="hero-subtitle">记录思考，分享见解，在文字中找到宁静。</p>
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索文章..."
          :prefix-icon="Search"
          size="large"
          clearable
          class="hero-search"
          @keyup.enter="doSearch"
        />
        <el-button type="primary" size="large" :loading="searching" @click="doSearch" class="hero-search-btn">
          <el-icon style="margin-right:4px"><Search /></el-icon>
          {{ searching ? '搜索中' : '搜索' }}
        </el-button>
      </div>
    </div>
  </section>

  <div class="main-layout" v-if="searchResults !== null">
    <main class="content">
      <div class="section-title">
        搜索结果：{{ searchQuery ? `"${searchQuery}"` : '' }}
        <span class="result-count">共 {{ searchResults.length }} 条</span>
      </div>
      <div v-if="searchResults.length === 0" class="empty-state">未找到相关文章</div>
      <el-card v-for="article in searchResults" :key="article.id" class="article-card" shadow="hover" tabindex="0" role="link" @click="$router.push(`/article/${article.id}`)" @keydown.enter="$router.push(`/article/${article.id}`)" @keydown.space.prevent="$router.push(`/article/${article.id}`)">
        <template #header>
          <div class="article-card-header">
            <h2 class="article-card-title">{{ article.title }}</h2>
            <div class="article-card-meta">
              <span>{{ formatDate(article.createdAt) }}</span>
              <span v-if="article.category"><el-tag size="small" effect="plain">{{ article.category }}</el-tag></span>
            </div>
          </div>
        </template>
        <p class="article-excerpt">{{ excerpt(article.content) }}</p>
        <template #footer>
          <el-button text type="primary" @click.stop="$router.push(`/article/${article.id}`)">阅读全文 →</el-button>
        </template>
      </el-card>
      <div style="text-align:center;padding:20px 0;">
        <el-button @click="clearSearch">← 返回全部文章</el-button>
      </div>
    </main>
  </div>

  <div class="main-layout" v-else-if="articles.length > 0">
    <main class="content">
      <!-- Pinned Articles -->
      <el-card v-for="pinnedArticle in pinnedArticles" :key="pinnedArticle.id" class="article-card pinned-card" shadow="hover" tabindex="0" role="link" @click="$router.push(`/article/${pinnedArticle.id}`)" @keydown.enter="$router.push(`/article/${pinnedArticle.id}`)" @keydown.space.prevent="$router.push(`/article/${pinnedArticle.id}`)">
        <template #header>
          <div class="article-card-header">
            <div class="pinned-badge"><el-icon><StarFilled /></el-icon> 置顶</div>
            <h2 class="article-card-title">{{ pinnedArticle.title }}</h2>
            <div class="article-card-meta">
              <span>{{ formatDate(pinnedArticle.createdAt) }}</span>
              <span v-if="pinnedArticle.category"><el-tag size="small" effect="plain">{{ pinnedArticle.category }}</el-tag></span>
              <span><el-icon><View /></el-icon> {{ pinnedArticle.viewCount ?? 0 }} 阅读</span>
              <span><el-icon><ChatDotSquare /></el-icon> {{ pinnedArticle.commentCount ?? 0 }}</span>
            </div>
          </div>
        </template>
        <p class="article-excerpt">{{ excerpt(pinnedArticle.content) }}</p>
        <template #footer>
          <el-button text type="primary" @click.stop="$router.push(`/article/${pinnedArticle.id}`)">阅读全文 →</el-button>
        </template>
      </el-card>

      <!-- Article List -->
      <div class="section-title" v-if="regularArticles.length > 0">最新文章</div>
      <el-card v-for="article in regularArticles" :key="article.id" class="article-card" shadow="hover" tabindex="0" role="link" @click="$router.push(`/article/${article.id}`)" @keydown.enter="$router.push(`/article/${article.id}`)" @keydown.space.prevent="$router.push(`/article/${article.id}`)">
        <template #header>
          <div class="article-card-header">
            <h2 class="article-card-title">{{ article.title }}</h2>
            <div class="article-card-meta">
              <span>{{ formatDate(article.createdAt) }}</span>
              <span v-if="article.category"><el-tag size="small" effect="plain">{{ article.category }}</el-tag></span>
              <span><el-icon><View /></el-icon> {{ article.viewCount ?? 0 }} 阅读</span>
              <span><el-icon><ChatDotSquare /></el-icon> {{ article.commentCount ?? 0 }}</span>
            </div>
          </div>
        </template>
        <p class="article-excerpt">{{ excerpt(article.content) }}</p>
        <template #footer>
          <el-button text type="primary" @click.stop="$router.push(`/article/${article.id}`)">阅读全文 →</el-button>
        </template>
      </el-card>

      <div v-if="loading" class="empty-state">加载中...</div>
    </main>

    <!-- Sidebar -->
    <aside class="sidebar">
      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-title">关于</span></template>
        <p class="sidebar-text">个人博客，记录技术、生活与思考。</p>
      </el-card>

      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-title">分类</span></template>
        <div class="tag-list">
          <el-tag v-for="cat in categories" :key="cat" size="small" effect="plain" style="margin: 2px;">{{ cat }}</el-tag>
        </div>
        <div v-if="categories.length === 0" class="sidebar-text">暂无分类</div>
      </el-card>

      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-title">时间线</span></template>
        <router-link to="/archive" class="archive-link">查看完整归档 →</router-link>
      </el-card>
    </aside>
  </div>

  <!-- Newsletter -->
  <section class="newsletter">
    <div class="newsletter-inner">
      <h3 class="newsletter-title">订阅动态</h3>
      <p class="newsletter-text">获取最新文章推送，不错过每一篇精彩内容。</p>
      <div class="newsletter-form">
        <el-input
          v-model="subscribeEmail"
          placeholder="输入邮箱地址"
          size="large"
          class="newsletter-input-el"
          @keyup.enter="subscribe"
        />
        <el-button type="primary" size="large" :loading="subscribing" @click="subscribe">
          {{ subscribing ? '提交中...' : '订阅' }}
        </el-button>
      </div>
      <div v-if="subscribeMsg" :class="['subscribe-msg', 'subscribe-' + subscribeMsgType]">
        {{ subscribeMsg }}
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, StarFilled, View, ChatDotSquare } from '@element-plus/icons-vue'
import { getArticles, searchArticles } from '../api/articles'

const router = useRouter()
const articles = ref([])
const loading = ref(true)

const searchQuery = ref('')
const searchResults = ref(null)
const searching = ref(false)

async function doSearch() {
  const q = searchQuery.value.trim()
  if (!q) return
  searching.value = true
  try {
    const res = await searchArticles(q)
    searchResults.value = res.data || []
  } catch (e) {
    searchResults.value = []
  } finally {
    searching.value = false
  }
}
function clearSearch() {
  searchQuery.value = ''
  searchResults.value = null
}

const pinnedArticles = computed(() => articles.value.filter(a => a.pinned))
const regularArticles = computed(() => articles.value.filter(a => !a.pinned))

const categories = computed(() => {
  const cats = new Set()
  articles.value.forEach(a => { if (a.category) cats.add(a.category) })
  return Array.from(cats)
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

const subscribeEmail = ref('')
const subscribing = ref(false)
const subscribeMsg = ref('')
const subscribeMsgType = ref('success')

async function subscribe() {
  const email = subscribeEmail.value.trim()
  if (!email) {
    subscribeMsg.value = '请输入邮箱地址'
    subscribeMsgType.value = 'error'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    subscribeMsg.value = '请输入有效的邮箱地址'
    subscribeMsgType.value = 'error'
    return
  }

  subscribing.value = true
  subscribeMsg.value = ''
  try {
    const subs = JSON.parse(localStorage.getItem('blog-subscribers') || '[]')
    if (subs.includes(email)) {
      subscribeMsg.value = '该邮箱已订阅 ✓'
      subscribeMsgType.value = 'info'
    } else {
      subs.push(email)
      localStorage.setItem('blog-subscribers', JSON.stringify(subs))
      subscribeMsg.value = '🎉 订阅成功！感谢你的关注'
      subscribeMsgType.value = 'success'
      subscribeEmail.value = ''
    }
  } catch (e) {
    subscribeMsg.value = '订阅失败，请稍后重试'
    subscribeMsgType.value = 'error'
  } finally {
    subscribing.value = false
    setTimeout(() => { subscribeMsg.value = '' }, 5000)
  }
}
</script>

<style scoped>
/* Hero */
.hero {
  padding: 60px 24px;
  text-align: center;
}
.hero-inner {
  max-width: 600px;
  margin: 0 auto;
}
.hero-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
  letter-spacing: 2px;
}
.hero-subtitle {
  font-size: 16px;
  color: var(--text-muted);
  margin-bottom: 32px;
  line-height: 1.6;
}
.search-box {
  display: flex;
  gap: 8px;
  max-width: 480px;
  margin: 0 auto;
}
.hero-search {
  flex: 1;
}
.hero-search-btn {
  flex-shrink: 0;
}

/* Main Layout */
.main-layout {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 24px 40px;
  display: flex;
  gap: 40px;
  align-items: flex-start;
}
.content {
  flex: 1;
  min-width: 0;
}
.sidebar {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Section Title */
.section-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20px;
  margin-top: 8px;
}
.result-count {
  font-size: 13px;
  color: var(--text-muted);
  font-weight: 400;
  margin-left: 8px;
}

/* Article Cards */
.article-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.25s ease;
  border-radius: 10px !important;
  background: var(--bg-card) !important;
  border: 1px solid var(--border) !important;
  box-shadow: var(--shadow-card) !important;
}
.article-card:focus-visible {
  outline: 2px solid var(--text-accent);
  outline-offset: 2px;
}
.article-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card-hover) !important;
  border-color: var(--text-accent) !important;
}
.article-card :deep(.el-card__header) {
  padding: 20px 24px 12px;
  border-bottom: none;
}
.article-card :deep(.el-card__body) {
  padding: 6px 24px 14px;
}
.article-card :deep(.el-card__footer) {
  padding: 4px 24px 18px;
  border-top: 1px solid var(--border);
}
.pinned-card {
  border-left: 3px solid var(--text-accent) !important;
}
.article-card-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.pinned-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 2px;
}
.article-card-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  margin: 0;
}
.article-card-meta {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.article-excerpt {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0;
}

/* Sidebar */
.sidebar-card {
  margin-bottom: 0;
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
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}
.sidebar-text {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
  margin: 0;
}
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.archive-link {
  font-size: 13px;
  color: var(--text-accent);
  text-decoration: none;
}
.archive-link:hover {
  text-decoration: underline;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 24px;
  color: var(--text-muted);
  font-size: 14px;
}

/* Newsletter */
.newsletter {
  padding: 48px 24px;
  text-align: center;
  border-top: 1px solid var(--border);
}
.newsletter-inner {
  max-width: 480px;
  margin: 0 auto;
}
.newsletter-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.newsletter-text {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 20px;
}
.newsletter-form {
  display: flex;
  gap: 8px;
}
.newsletter-input-el {
  flex: 1;
}
.subscribe-msg {
  margin-top: 12px;
  font-size: 13px;
  padding: 8px 12px;
  border-radius: 10px;
}
.subscribe-success {
  /* TODO: theme with CSS variables once semantic colors are defined */
  color: #16a34a;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}
.subscribe-error {
  /* TODO: theme with CSS variables once semantic colors are defined */
  color: #dc2626;
  background: #fef2f2;
  border: 1px solid #fecaca;
}
.subscribe-info {
  color: var(--text-accent);
  background: var(--bg-pinned);
  border: 1px solid #bfdbfe; /* TODO: theme border with CSS variable */
}
.dark .subscribe-success {
  /* TODO: theme with CSS variables once semantic colors are defined */
  background: rgba(22,163,74,0.1);
  border-color: rgba(22,163,74,0.3);
}
.dark .subscribe-error {
  /* TODO: theme with CSS variables once semantic colors are defined */
  background: rgba(220,38,38,0.1);
  border-color: rgba(220,38,38,0.3);
}
.dark .subscribe-info {
  background: var(--bg-pinned);
  border-color: rgba(37,99,235,0.3); /* TODO: theme border with CSS variable */
}

/* Responsive */
@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
  .hero-title {
    font-size: 28px;
  }
  .search-box {
    flex-direction: column;
  }
  .newsletter-form {
    flex-direction: column;
  }
}
</style>
