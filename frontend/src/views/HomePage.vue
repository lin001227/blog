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

  <div class="main-layout">
    <main class="content">
      <!-- Search Results -->
      <template v-if="searchResults !== null">
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
                <span class="meta-stat"><el-icon><View /></el-icon> {{ article.viewCount ?? 0 }}</span>
                <span class="meta-stat"><el-icon><ChatDotSquare /></el-icon> {{ article.commentCount ?? 0 }}</span>
                <span class="meta-sep">·</span>
                <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
                <el-tag v-if="article.category" size="small" effect="plain" class="meta-tag">{{ article.category }}</el-tag>
              </div>
            </div>
          </template>
          <p class="article-excerpt">{{ excerpt(article.content) }}</p>
        </el-card>
        <div style="text-align:center;padding:20px 0;">
          <el-button @click="clearSearch">← 返回全部文章</el-button>
        </div>
      </template>

      <!-- Articles / Skeleton -->
      <template v-else>
        <el-skeleton :loading="loading && articles.length === 0" animated :count="loading ? 5 : 1" :throttle="0">
          <template #template>
            <div class="article-card el-skeleton-card">
              <div class="sk-card-header">
                <el-skeleton-item variant="h3" class="sk-title" />
                <div class="sk-meta-row">
                  <el-skeleton-item variant="caption" class="sk-meta" />
                  <el-skeleton-item variant="caption" class="sk-meta" />
                  <span class="meta-sep">·</span>
                  <el-skeleton-item variant="caption" class="sk-meta" style="width:55px" />
                  <el-skeleton-item variant="caption" class="sk-meta" style="width:45px" />
                </div>
              </div>
              <div class="sk-card-body">
                <el-skeleton-item variant="p" />
                <el-skeleton-item variant="p" style="width:92%" />
                <el-skeleton-item variant="p" style="width:86%" />
                <el-skeleton-item variant="p" style="width:95%" />
                <el-skeleton-item variant="p" style="width:60%" />
              </div>
            </div>
          </template>
          <template #default>
            <template v-if="articles.length > 0">
              <el-card v-for="pinnedArticle in pinnedArticles" :key="pinnedArticle.id" class="article-card pinned-card" shadow="hover" tabindex="0" role="link" @click="$router.push(`/article/${pinnedArticle.id}`)" @keydown.enter="$router.push(`/article/${pinnedArticle.id}`)" @keydown.space.prevent="$router.push(`/article/${pinnedArticle.id}`)">
                <template #header>
                  <div class="article-card-header">
                    <div class="pinned-badge"><el-icon><StarFilled /></el-icon> 置顶</div>
                    <h2 class="article-card-title">{{ pinnedArticle.title }}</h2>
                    <div class="article-card-meta">
                      <span><el-icon><View /></el-icon> {{ pinnedArticle.viewCount ?? 0 }}</span>
                      <span><el-icon><ChatDotSquare /></el-icon> {{ pinnedArticle.commentCount ?? 0 }}</span>
                      <span class="meta-dot">·</span>
                      <span>{{ formatDate(pinnedArticle.createdAt) }}</span>
                      <el-tag v-if="pinnedArticle.category" size="small" effect="plain">{{ pinnedArticle.category }}</el-tag>
                    </div>
                  </div>
                </template>
                <p class="article-excerpt">{{ excerpt(pinnedArticle.content) }}</p>
              </el-card>

              <div class="section-title" v-if="regularArticles.length > 0">最新文章</div>
              <el-card v-for="article in regularArticles" :key="article.id" class="article-card" shadow="hover" tabindex="0" role="link" @click="$router.push(`/article/${article.id}`)" @keydown.enter="$router.push(`/article/${article.id}`)" @keydown.space.prevent="$router.push(`/article/${article.id}`)">
                <template #header>
                  <div class="article-card-header">
                    <h2 class="article-card-title">{{ article.title }}</h2>
                    <div class="article-card-meta">
                      <span class="meta-stat"><el-icon><View /></el-icon> {{ article.viewCount ?? 0 }}</span>
                      <span class="meta-stat"><el-icon><ChatDotSquare /></el-icon> {{ article.commentCount ?? 0 }}</span>
                      <span class="meta-sep">·</span>
                      <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
                      <el-tag v-if="article.category" size="small" effect="plain" class="meta-tag">{{ article.category }}</el-tag>
                    </div>
                  </div>
                </template>
                <p class="article-excerpt">{{ excerpt(article.content) }}</p>
              </el-card>
            </template>
            <div v-if="!loading && articles.length === 0" class="empty-state">暂无文章</div>
          </template>
        </el-skeleton>
      </template>
    </main>

    <!-- Sidebar - hidden during search -->
    <aside class="sidebar" v-if="searchResults === null">
      <el-skeleton :loading="loading && articles.length === 0" animated :count="loading ? 3 : 1" :throttle="0">
        <template #template>
          <div class="sidebar-card el-skeleton-card">
            <div class="sk-sidebar-header">
              <el-skeleton-item variant="h3" class="sk-sidebar-title" />
            </div>
            <div class="sk-sidebar-body">
              <el-skeleton-item variant="p" />
              <el-skeleton-item variant="p" style="width:60%" />
            </div>
          </div>
        </template>
        <template #default>
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
        </template>
      </el-skeleton>
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
  max-width: 100%;
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
  width: 100%;
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
  padding: 6px 24px 20px;
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
  gap: 6px;
  flex-wrap: wrap;
}
.meta-stat {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: var(--text-muted);
}
.meta-stat .el-icon {
  font-size: 13px;
}
.meta-sep {
  color: var(--border);
  font-weight: 300;
  margin: 0 1px;
}
.meta-date {
  color: var(--text-muted);
}
.meta-tag {
  font-size: 11px !important;
  padding: 0 6px !important;
  height: 20px !important;
  line-height: 20px !important;
  border: none !important;
  background: var(--bg-tag) !important;
  color: var(--text-secondary) !important;
}
.article-excerpt {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0;
}

/* el-skeleton card — matches real article-card appearance */
.el-skeleton-card {
  padding: 0;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 20px;
}
.el-skeleton-card:hover {
  transform: none !important;
  border-color: var(--border) !important;
  box-shadow: var(--shadow-card) !important;
}
.sk-card-header {
  padding: 20px 24px 12px;
}
.sk-title {
  display: block;
  margin-bottom: 6px;
  width: 70%;
  height: 20px;
}
.sk-meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.sk-meta {
  display: inline-block;
  width: 40px;
  height: 12px;
}
.sk-card-body {
  padding: 6px 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
/* Sidebar skeleton */
.sk-sidebar-header {
  padding: 16px 20px 0;
}
.sk-sidebar-title {
  display: block;
  width: 40%;
  height: 16px;
}
.sk-sidebar-body {
  padding: 14px 20px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
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
    padding: 0 16px 32px;
  }
  .sidebar {
    width: 100%;
  }
  .hero-title {
    font-size: 28px;
  }
  .hero {
    padding: 40px 16px;
  }
  .search-box {
    flex-direction: column;
  }
  .newsletter-form {
    flex-direction: column;
  }
  .newsletter {
    padding: 32px 16px;
  }
  .article-card :deep(.el-card__header) {
    padding: 16px 18px 10px;
  }
  .article-card :deep(.el-card__body) {
    padding: 4px 18px 16px;
  }
  .section-title {
    font-size: 18px;
  }
  .sk-card-header {
    padding: 16px 18px 10px;
  }
  .sk-card-body {
    padding: 4px 18px 16px;
    gap: 8px;
  }
  .sk-sidebar-header {
    padding: 12px 18px 0;
  }
  .sk-sidebar-body {
    padding: 10px 18px 14px;
    gap: 8px;
  }
}
</style>
