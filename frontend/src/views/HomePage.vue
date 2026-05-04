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
        <ArticleCard v-for="article in searchResults" :key="article.id" :article="article" />
        <div style="text-align:center;padding:20px 0;">
          <el-button @click="clearSearch">← 返回全部文章</el-button>
        </div>
      </template>

      <!-- Articles / Skeleton -->
      <template v-else>
        <template v-if="loading && articles.length === 0">
          <div v-for="n in 5" :key="n" class="skeleton-card">
            <div class="sk-header">
              <div class="sk-title skeleton-pulse"></div>
              <div class="sk-meta-row">
                <span class="sk-meta skeleton-pulse"></span>
                <span class="sk-meta skeleton-pulse"></span>
                <span class="sk-dot">·</span>
                <span class="sk-meta skeleton-pulse" style="width:55px"></span>
                <span class="sk-meta skeleton-pulse" style="width:45px"></span>
              </div>
            </div>
            <div class="sk-body">
              <div class="sk-line skeleton-pulse"></div>
              <div class="sk-line skeleton-pulse" style="width:92%"></div>
              <div class="sk-line skeleton-pulse" style="width:86%"></div>
            </div>
          </div>
        </template>

        <template v-else>
          <template v-if="articles.length > 0">
            <ArticleCard
              v-for="article in pinnedArticles"
              :key="article.id"
              :article="article"
              :pinned="true"
            />

            <div v-if="regularArticles.length > 0" class="section-title">最新文章</div>
            <ArticleCard
              v-for="article in regularArticles"
              :key="article.id"
              :article="article"
            />
          </template>
          <div v-if="!loading && articles.length === 0" class="empty-state">暂无文章</div>
        </template>
      </template>
    </main>

    <!-- Sidebar - hidden during search -->
    <aside class="sidebar" v-if="searchResults === null">
      <template v-if="loading && articles.length === 0">
        <div v-for="n in 3" :key="n" class="sidebar-skeleton-card">
          <div class="sk-sidebar-title skeleton-pulse"></div>
          <div class="sk-sidebar-line skeleton-pulse"></div>
          <div class="sk-sidebar-line skeleton-pulse" style="width:60%"></div>
        </div>
      </template>
      <template v-else>
        <el-card shadow="never" class="sidebar-card">
          <template #header><span class="sidebar-title">关于</span></template>
          <p class="sidebar-text">个人博客，记录技术、生活与思考。</p>
        </el-card>
        <el-card shadow="never" class="sidebar-card">
          <template #header><span class="sidebar-title">分类</span></template>
          <div class="tag-list">
            <el-tag
              v-for="cat in categories"
              :key="cat"
              size="small"
              effect="plain"
              style="margin: 2px; cursor:pointer"
              tabindex="0"
              role="button"
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
          <template #header><span class="sidebar-title">时间线</span></template>
          <router-link to="/archive" class="archive-link">查看完整归档 →</router-link>
        </el-card>
        <el-card v-if="topRankings.length > 0" shadow="never" class="sidebar-card">
          <template #header><span class="sidebar-title">🤖 大模型排行</span></template>
          <div class="ranking-preview">
            <div
              v-for="(item, index) in topRankings"
              :key="item.id"
              class="ranking-item"
            >
              <span class="ranking-badge">{{ ['🥇','🥈','🥉'][index] }}</span>
              <span class="ranking-name">{{ item.languageName }}</span>
              <span class="ranking-pct">{{ item.percentage }}%</span>
            </div>
          </div>
          <router-link to="/rankings" class="archive-link">查看完整排行 →</router-link>
        </el-card>
      </template>
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
import { Search } from '@element-plus/icons-vue'
import { getArticles, searchArticles } from '../api/articles'
import { getLanguageRankings } from '../api/languageRankings'
import { subscribe as subscribeApi } from '../api/subscription'
import ArticleCard from '../components/ArticleCard.vue'

const articles = ref([])
const loading = ref(true)

const searchQuery = ref('')
const searchResults = ref(null)
const searching = ref(false)
const filterCategory = ref('')
const topRankings = ref([])

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

onMounted(async () => {
  try {
    const res = await getArticles()
    articles.value = res.data || []
  } catch (e) {
    console.error('Failed to load articles:', e)
  } finally {
    loading.value = false
  }
  // Load top 3 rankings
  try {
    const res = await getLanguageRankings()
    topRankings.value = (res.data || []).slice(0, 3)
  } catch (e) {
    // silently fail - rankings are optional
  }
})

// ========== Newsletter (temporary: localStorage only) ==========
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

  subscribing.value = true
  subscribeMsg.value = ''
  try {
    const res = await subscribeApi(email)
    subscribeMsg.value = res.data?.message || '🎉 订阅成功！感谢你的关注'
    subscribeMsgType.value = 'success'
    subscribeEmail.value = ''
  } catch (e) {
    subscribeMsg.value = e.response?.data?.error || '订阅失败，请稍后重试'
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

/* Sidebar Ranking Preview */
.ranking-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ranking-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.ranking-badge {
  font-size: 14px;
  flex-shrink: 0;
}
.ranking-name {
  flex: 1;
  color: var(--text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ranking-pct {
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 24px;
  color: var(--text-muted);
  font-size: 14px;
}

/* Skeleton Loading */
.skeleton-card {
  margin-bottom: 20px;
  border-radius: 10px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-card);
  padding: 20px 24px 12px;
  width: 100%;
}
.sk-header {
  margin-bottom: 14px;
}
.sk-title {
  display: block;
  height: 20px;
  width: 70%;
  border-radius: 4px;
  margin-bottom: 8px;
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
  border-radius: 3px;
}
.sk-dot {
  color: var(--border);
}
.sk-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-bottom: 8px;
}
.sk-line {
  height: 13px;
  width: 100%;
  border-radius: 3px;
}
.sidebar-skeleton-card {
  padding: 16px 20px 18px;
  border-radius: 10px;
  background: var(--bg-card);
  border: 1px solid var(--border);
}
.sk-sidebar-title {
  display: block;
  width: 40%;
  height: 16px;
  border-radius: 3px;
  margin-bottom: 12px;
}
.sk-sidebar-line {
  display: block;
  height: 13px;
  width: 100%;
  border-radius: 3px;
  margin-bottom: 8px;
}
.skeleton-pulse {
  background: var(--bg-tag);
  animation: skeleton-pulse 1.8s ease-in-out infinite;
}
@keyframes skeleton-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
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
  color: var(--color-success);
  background: var(--bg-success);
  border: 1px solid var(--border-success);
}
.subscribe-error {
  color: var(--color-error);
  background: var(--bg-error);
  border: 1px solid var(--border-error);
}
.subscribe-info {
  color: var(--text-accent);
  background: var(--bg-tag);
  border: 1px solid var(--border);
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
  .section-title {
    font-size: 18px;
  }
  .skeleton-card {
    padding: 16px 18px 10px;
  }
  .sk-body {
    gap: 8px;
    padding-bottom: 6px;
  }
  .sidebar-skeleton-card {
    padding: 12px 18px 14px;
  }
}
</style>
