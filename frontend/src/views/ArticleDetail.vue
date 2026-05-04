<template>
  <div v-if="article">
    <div class="article-layout" :class="{ 'no-toc': tocItems.length === 0 }">
      <!-- LEFT: article main content -->
      <div class="article-main">
        <div class="article-container">
          <router-link to="/" class="back-link">← 返回首页</router-link>

          <article class="article">
            <h1 class="article-title">{{ article.title }}</h1>

            <div class="article-meta">
              <span>{{ formatDate(article.createdAt) }}</span>
              <span v-if="article.category"> · {{ article.category }}</span>
              <span v-if="updatedText"> · {{ updatedText }}</span>
              <span class="view-count"> · 👁️ {{ article.viewCount ?? 0 }} 次阅读</span>
              <span class="comment-count"> · 💬 {{ article.commentCount ?? 0 }} 条评论</span>
            </div>

            <div class="tags" v-if="article.tags && article.tags.length">
              <span class="tag" v-for="tag in tagsList" :key="tag">{{ tag }}</span>
            </div>

            <!-- Mobile TOC (shown on narrow screens) -->
            <div class="mobile-toc" v-if="tocItems.length > 0">
              <button class="mobile-toc-toggle" @click="showMobileToc = !showMobileToc">
                📑 目录 <span class="toc-toggle-arrow" :class="{ open: showMobileToc }">▼</span>
              </button>
              <nav class="mobile-toc-nav" v-show="showMobileToc">
                <a
                  v-for="(item, i) in tocItems" :key="i"
                  :href="'#' + item.id"
                  :class="['toc-link', 'toc-level-' + item.level]"
                  @click.prevent="scrollToHeading(item.id); showMobileToc = false"
                >{{ item.text }}</a>
              </nav>
            </div>

            <div class="article-content" v-html="contentWithAnchors"></div>
          </article>

          <div class="article-footer">
            <router-link to="/" class="back-link">← 返回首页</router-link>
          </div>

          <!-- Comment Section -->
          <div class="comment-section">
            <div class="comment-section-header">
              <h3 class="comment-section-title">💬 评论 <span class="comment-count-badge">{{ comments.length }}</span></h3>
            </div>

            <!-- Comment Form -->
            <div class="comment-form">
              <div class="comment-form-card">
                <div class="comment-form-row">
                  <el-input
                    v-model="commentForm.nickname"
                    placeholder="昵称 *"
                    :maxlength="50"
                    size="large"
                    clearable
                  >
                    <template #prefix><span style="opacity:0.5">👤</span></template>
                  </el-input>
                  <el-input
                    v-model="commentForm.email"
                    placeholder="邮箱（选填）"
                    :maxlength="255"
                    size="large"
                    clearable
                  >
                    <template #prefix><span style="opacity:0.5">📧</span></template>
                  </el-input>
                </div>
                <el-input
                  v-model="commentForm.content"
                  type="textarea"
                  placeholder="写下你的评论..."
                  :maxlength="2000"
                  :rows="4"
                  :autosize="{ minRows: 4, maxRows: 10 }"
                  show-word-limit
                  class="comment-textarea-el"
                />
                <div class="comment-form-actions">
                  <span></span>
                  <el-button type="primary" size="large" :loading="submittingComment" @click="submitComment">
                    ✏️ 发表评论
                  </el-button>
                </div>
                <transition name="msg-fade">
                  <div v-if="commentError" class="comment-msg comment-msg-error">⚠️ {{ commentError }}</div>
                  <div v-if="commentSuccess" class="comment-msg comment-msg-success">✅ 评论已提交，等待审核后显示</div>
                </transition>
              </div>
            </div>

            <!-- Comment List -->
            <div v-if="comments.length === 0" class="comment-empty">
              <div class="comment-empty-icon">💬</div>
              <p class="comment-empty-text">暂无评论，快来抢沙发吧~</p>
            </div>
            <div v-else class="comment-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item" :class="{ 'comment-item-pinned': comment.pinned }">
                <div v-if="comment.pinned" class="comment-pinned-tag"><span>📌</span> 置顶评论</div>
                <div class="comment-main">
                  <div class="comment-header">
                    <span class="comment-avatar" :style="{ background: nameColor(comment.nickname) }">{{ comment.nickname.charAt(0) }}</span>
                    <div class="comment-header-info">
                      <span class="comment-nickname">{{ comment.nickname }}</span>
                      <span class="comment-time">
                        <span class="comment-relative-time">{{ formatRelativeTime(comment.createdAt) }}</span>
                        <span class="comment-time-sep">·</span>
                        <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                      </span>
                    </div>
                  </div>
                  <div class="comment-body">{{ comment.content }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- RIGHT: Desktop TOC sidebar -->
      <aside class="toc-sidebar" v-if="tocItems.length > 0">
        <div class="toc-title">📑 目录</div>
        <nav class="toc-nav">
          <a
            v-for="(item, i) in tocItems" :key="i"
            :href="'#' + item.id"
            :class="['toc-link', 'toc-level-' + item.level, { active: activeTocId === item.id }]"
            @click.prevent="scrollToHeading(item.id)"
          >{{ item.text }}</a>
        </nav>
      </aside>
    </div>
  </div>

  <div v-else-if="loading">
    <div class="article-layout">
      <div class="article-main">
        <div class="article-container">
          <div class="skeleton-back skeleton-pulse" style="width:80px;height:14px;margin-bottom:24px;border-radius:3px"></div>
          <div class="skeleton-title skeleton-pulse" style="width:80%;height:28px;margin-bottom:12px;border-radius:4px"></div>
          <div class="skeleton-meta-row" style="margin-bottom:20px">
            <div class="skeleton-meta skeleton-pulse" style="width:90px"></div>
            <div class="skeleton-meta skeleton-pulse" style="width:60px"></div>
            <div class="skeleton-meta skeleton-pulse" style="width:100px"></div>
          </div>
          <div class="skeleton-content">
            <div class="skeleton-line skeleton-pulse"></div>
            <div class="skeleton-line skeleton-pulse" style="width:92%"></div>
            <div class="skeleton-line skeleton-pulse" style="width:85%"></div>
            <div class="skeleton-line skeleton-pulse" style="width:97%"></div>
            <div class="skeleton-line skeleton-pulse" style="width:88%"></div>
            <div style="height:16px"></div>
            <div class="skeleton-line skeleton-pulse"></div>
            <div class="skeleton-line skeleton-pulse" style="width:78%"></div>
            <div class="skeleton-line skeleton-pulse" style="width:90%"></div>
            <div class="skeleton-line skeleton-pulse" style="width:65%"></div>
          </div>
        </div>
      </div>
      <aside class="toc-sidebar">
        <div class="toc-title">📑 目录</div>
        <nav class="toc-nav">
          <div class="skeleton-toc-item skeleton-pulse" style="width:85%"></div>
          <div class="skeleton-toc-item skeleton-pulse" style="width:70%"></div>
          <div class="skeleton-toc-item skeleton-pulse" style="width:60%;margin-left:10px"></div>
          <div class="skeleton-toc-item skeleton-pulse" style="width:75%;margin-left:10px"></div>
          <div class="skeleton-toc-item skeleton-pulse" style="width:65%"></div>
        </nav>
      </aside>
    </div>
  </div>
  <div v-else class="loading">文章未找到</div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle } from '../api/articles'
import { getArticleComments, createComment } from '../api/comments'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'

// Configure marked for better rendering
marked.use({
  breaks: true,
  gfm: true,
})

const route = useRoute()
const article = ref(null)
const loading = ref(true)

const comments = ref([])
const commentForm = ref({ nickname: '', email: '', content: '' })
const submittingComment = ref(false)
const commentError = ref('')
const commentSuccess = ref(false)

const tagsList = computed(() => {
  if (!article.value?.tags) return []
  if (Array.isArray(article.value.tags)) return article.value.tags
  return String(article.value.tags).split(/[,，\s]+/).filter(Boolean)
})

const updatedText = computed(() => {
  if (!article.value?.updatedAt || article.value.updatedAt === article.value.createdAt) return ''
  return `更新于 ${formatDate(article.value.updatedAt)}`
})

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  // Use marked to convert markdown to HTML
  return article.value.content
})

// Check if content is plain markdown (no HTML tags) and needs conversion
const renderedHtml = computed(() => {
  if (!article.value?.content) return ''
  // Fix literal \n characters -> real newlines (common when content is pasted)
  const content = article.value.content.replace(/\\n/g, '\n')
  // If content doesn't contain HTML tags, treat as markdown
  if (!/<[a-z][\s\S]*>/i.test(content)) {
    return marked.parse(content)
  }
  // Already has HTML tags - might be mixed with markdown, still run through marked
  // marked handles HTML tags in markdown gracefully
  return marked.parse(content)
})

// --- Open Graph ---
function updateMetaTags(article) {
  const title = article ? `${article.title} - 风屿 · 随笔` : '风屿 · 随笔'
  const description = article
    ? renderedHtml.value.replace(/<[^>]*>/g, '').replace(/\s+/g, ' ').trim().substring(0, 200)
    : '记录思考，分享见解，在文字中找到宁静。'

  document.title = title
  setMeta('og:title', title)
  setMeta('og:description', description)
  setMeta('og:url', window.location.href)
  setMeta('twitter:title', title)
  setMeta('twitter:description', description)
  setMeta('name', 'description', description)
}
function setMeta(property, content, attr) {
  const prop = attr || 'property'
  let el = document.querySelector(`meta[${prop}="${property}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(prop, property)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

// --- TOC ---
function generateId(text, index) {
  return 'heading-' + index + '-' + text.toLowerCase().replace(/[^\w\u4e00-\u9fff]+/g, '-').replace(/^-|-$/g, '')
}

const contentWithAnchors = computed(() => {
  const html = renderedHtml.value
  // Add IDs to h1, h2, h3 for TOC anchors
  let counter = 0
  return html.replace(/<h([123])(.*?)>(.*?)<\/h[123]>/gi, (match, level, attrs, text) => {
    const id = generateId(text.replace(/<[^>]*>/g, ''), counter++)
    return `<h${level} id="${id}">${text}</h${level}>`
  })
})

const tocItems = computed(() => {
  const html = renderedHtml.value
  const items = []
  let counter = 0
  const regex = /<h([123])(.*?)>(.*?)<\/h[123]>/gi
  let m
  while ((m = regex.exec(html)) !== null) {
    const level = parseInt(m[1])
    const text = m[3].replace(/<[^>]*>/g, '')
    const id = generateId(text, counter++)
    items.push({ id, level, text })
  }
  return items
})

function scrollToHeading(id) {
  const el = document.getElementById(id)
  if (el) {
    const top = el.getBoundingClientRect().top + window.scrollY - 100
    window.scrollTo({ top, behavior: 'smooth' })
  }
}

// --- Active TOC tracking ---
const activeTocId = ref('')
const showMobileToc = ref(false)
let tocObserver = null

function setupCodeLangLabels() {
  nextTick(() => {
    document.querySelectorAll('.article-content pre code[class*="language-"]').forEach((code) => {
      const pre = code.parentElement
      if (!pre || pre.querySelector('.code-lang')) return
      const lang = Array.from(code.classList)
        .find(c => c.startsWith('language-'))
        ?.replace('language-', '') || ''
      if (!lang) return
      const badge = document.createElement('span')
      badge.className = 'code-lang'
      badge.textContent = lang
      pre.appendChild(badge)
    })
  })
}

function setupTocObserver() {
  // Wait for DOM to render headings with IDs
  nextTick(() => {
    const headings = document.querySelectorAll('.article-content h1, .article-content h2, .article-content h3')
    if (headings.length === 0) return

    tocObserver = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          activeTocId.value = entry.target.id
        }
      }
    }, { rootMargin: '-80px 0px -60% 0px' })

    headings.forEach(h => tocObserver.observe(h))
  })
}

onBeforeUnmount(() => {
  if (tocObserver) tocObserver.disconnect()
})

// Auto-scroll TOC sidebar to active item
watch(activeTocId, (id) => {
  if (!id) return
  // Desktop sidebar
  const tocSidebar = document.querySelector('.toc-sidebar')
  if (tocSidebar) {
    const activeLink = tocSidebar.querySelector('.toc-link.active')
    if (activeLink) {
      activeLink.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
    }
  }
  // Mobile TOC - just update without auto-scroll (it's stacked inline)
})

const COLORS = ['#f56a00','#7265e6','#ffbf00','#00a854','#108ee9','#cd5c5c','#7b68ee','#20b2aa','#ff6347','#9370db','#3cb371','#ff8c00','#48d1cc','#c71585','#2e8b57','#d2691e','#6495ed','#dc143c','#00ced1','#daa520']

function nameColor(name) {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return COLORS[Math.abs(hash) % COLORS.length]
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const now = Date.now()
  const d = new Date(dateStr).getTime()
  const diff = now - d
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  if (months < 12) return `${months}个月前`
  return `${years}年前`
}

async function submitComment() {
  if (!commentForm.value.nickname.trim()) { commentError.value = '请填写昵称'; return }
  if (!commentForm.value.content.trim()) { commentError.value = '请填写评论内容'; return }
  commentError.value = ''
  submittingComment.value = true
  commentSuccess.value = false
  try {
    await createComment({
      articleId: Number(route.params.id),
      nickname: commentForm.value.nickname.trim(),
      email: commentForm.value.email.trim() || undefined,
      content: commentForm.value.content.trim(),
    })
    commentForm.value.content = ''
    commentSuccess.value = true
    setTimeout(() => { commentSuccess.value = false }, 5000)
  } catch (e) {
    commentError.value = e.response?.data?.error || '提交失败，请稍后再试'
  } finally {
    submittingComment.value = false
  }
}

onMounted(async () => {
  try {
    const [articleRes, commentsRes] = await Promise.all([
      getArticle(route.params.id),
      getArticleComments(route.params.id).catch(() => ({ data: [] })),
    ])
    article.value = articleRes.data
    comments.value = commentsRes.data || []
    updateMetaTags(article.value)
    setupTocObserver()
    setupCodeLangLabels()
  } catch (e) {
    console.error('Failed to load:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style>
/* Article Page Layout - flex for sidebar */
.article-layout {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}
.article-layout.no-toc {
  justify-content: center;
}
.article-layout.no-toc .article-main {
  margin: 0 auto;
  flex: auto;
  max-width: 900px;
}
.article-main {
  flex: 1;
  min-width: 0;
  max-width: 780px;
}

/* Main */
.article-container {
  background: var(--bg-card);
  border-radius: 10px;
  padding: 40px;
  box-shadow: var(--shadow-card);
  transition: background 0.3s ease;
}

.back-link {
  display: inline-block;
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  margin-bottom: 24px;
  transition: color 0.2s;
}
.back-link:hover {
  color: var(--text-accent);
}

.article-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 14px;
}

.article-meta {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 14px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg-tag);
  padding: 3px 10px;
  border-radius: 10px;
  transition: background 0.3s ease;
}

.article-footer {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--border);
}

.loading {
  text-align: center;
  color: var(--text-muted);
  padding: 80px 24px;
  font-size: 15px;
}

/* Comment Section */
.comment-section {
  margin-top: 48px;
  padding-top: 36px;
  border-top: 1px solid var(--border);
}

.comment-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.comment-section-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 11px;
  background: var(--text-accent);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Comment Form */
.comment-form {
  margin-bottom: 32px;
}

.comment-form-card {
  background: var(--bg-tag);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 20px;
  transition: border-color 0.2s;
}

.comment-form-card:focus-within {
  border-color: var(--text-accent);
}

.comment-form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

/* Message fade transition */
.msg-fade-enter-active,
.msg-fade-leave-active {
  transition: all 0.3s ease;
}

.msg-fade-enter-from,
.msg-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.comment-msg {
  margin-top: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.4;
}

.comment-msg-error {
  /* TODO: theme with CSS variables once semantic colors are defined */
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.dark .comment-msg-error {
  /* TODO: theme with CSS variables once semantic colors are defined */
  background: rgba(220, 38, 38, 0.1);
  border-color: rgba(220, 38, 38, 0.3);
}

.comment-msg-success {
  /* TODO: theme with CSS variables once semantic colors are defined */
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

.dark .comment-msg-success {
  /* TODO: theme with CSS variables once semantic colors are defined */
  background: rgba(22, 163, 74, 0.1);
  border-color: rgba(22, 163, 74, 0.3);
}

/* Comment List */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.2s;
}

.comment-item:hover {
  border-color: var(--border-input);
  box-shadow: var(--shadow-card-hover);
}

.dark .comment-item:hover {
  box-shadow: var(--shadow-card-hover);
}

.comment-item-pinned {
  position: relative;
  border-color: var(--text-accent);
  border-left: 3px solid var(--text-accent);
}

.comment-item-pinned:hover {
  border-color: var(--text-accent);
}

.comment-pinned-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-accent);
  user-select: none;
}

.comment-pinned-tag span {
  font-size: 13px;
}

.comment-main {
  padding: 16px;
  padding-top: 10px;
}

.comment-item-pinned .comment-main {
  padding-top: 6px;
}

/* Comment Header */
.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  user-select: none;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.comment-header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.comment-nickname {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
}

.comment-time {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.comment-relative-time {
  font-weight: 500;
}

.comment-time-sep {
  opacity: 0.4;
}

.comment-date {
  opacity: 0.7;
}

/* Comment Body */
.comment-body {
  font-size: 14px;
  line-height: 1.75;
  color: var(--text-body);
  word-break: break-word;
  white-space: pre-wrap;
}

/* Empty State */
.comment-empty {
  text-align: center;
  padding: 48px 24px;
  border: 1px dashed var(--border);
  border-radius: 10px;
}

.comment-empty-icon {
  font-size: 36px;
  margin-bottom: 12px;
  opacity: 0.6;
}

.comment-empty-text {
  color: var(--text-muted);
  font-size: 14px;
}

/* TOC Sidebar (Desktop) - sticky */
.toc-sidebar {
  position: sticky;
  top: calc(var(--nav-height) + 24px);
  width: 210px;
  flex-shrink: 0;
  max-height: calc(100vh - var(--nav-height) - 48px);
  overflow-y: auto;
  background: var(--bg-card);
  border-radius: 10px;
  padding: 14px 0;
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border);
  transition: background 0.3s ease, border-color 0.3s ease;
  scroll-behavior: smooth;
}
.toc-sidebar::-webkit-scrollbar { width: 3px; }
.toc-sidebar::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }
.toc-sidebar::-webkit-scrollbar-track { background: transparent; }
.toc-sidebar::after {
  content: '';
  display: block;
  height: 12px;
}
.toc-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  padding: 0 16px 10px;
  border-bottom: 1px solid var(--border);
  margin: 0 0 8px;
  font-family: 'Noto Serif SC', serif;
}
.toc-nav { display: flex; flex-direction: column; }
.toc-link {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  text-decoration: none;
  padding: 4px 16px;
  border-left: 2px solid transparent;
  transition: all 0.15s;
  line-height: 1.5;
  word-break: break-all;
}
.toc-link:hover {
  color: var(--text-accent);
  background: var(--bg-toc-hover);
}
.toc-link.active {
  color: var(--text-accent);
  border-left-color: var(--text-accent);
  font-weight: 500;
  background: var(--bg-toc-hover);
}
.toc-level-2 { padding-left: 26px; font-size: 12.5px; }
.toc-level-3 { padding-left: 36px; font-size: 12px; }

/* Mobile TOC - collapsible inline */
.mobile-toc {
  margin: 20px 0 24px;
  border: 1px solid var(--border);
  border-radius: 10px;
  overflow: hidden;
  background: var(--bg-card);
}
.mobile-toc-toggle {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-card);
  border: none;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-family: inherit;
  transition: background 0.2s;
}
.mobile-toc-toggle:hover { background: var(--bg-toc-hover); }
.toc-toggle-arrow {
  font-size: 10px;
  transition: transform 0.2s;
}
.toc-toggle-arrow.open { transform: rotate(180deg); }
.mobile-toc-nav {
  border-top: 1px solid var(--border);
  padding: 8px 0;
  background: var(--bg-card);
}

/* Hide desktop TOC on narrow screens, show mobile TOC */
@media (max-width: 1077px) {
  .toc-sidebar { display: none; }
}
/* Show desktop TOC on wide screens */
@media (min-width: 1078px) {
  .mobile-toc { display: none; }
}

/* Mobile responsive */
@media (max-width: 1077px) {
  .article-main {
    max-width: 100%;
  }
}
@media (max-width: 768px) {
  .article-layout {
    flex-direction: column;
  }
  .article-container {
    padding: 24px 20px;
  }
}

/* Article Content - rendered markdown */
.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: var(--text-body);
}
.article-content h1,
.article-content h2,
.article-content h3 {
  scroll-margin-top: 100px;
  margin-top: 36px;
  margin-bottom: 14px;
  font-family: 'Noto Serif SC', serif;
  color: var(--text-primary);
  line-height: 1.4;
}
.article-content h1 { font-size: 26px; }
.article-content h2 { font-size: 22px; border-bottom: 1px solid var(--border); padding-bottom: 8px; }
.article-content h3 { font-size: 18px; }
.article-content p {
  margin-bottom: 16px;
  line-height: 1.8;
}
.article-content pre {
  background: var(--bg-pre);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 16px 20px;
  overflow-x: auto;
  margin-bottom: 20px;
  font-size: 14px;
  line-height: 1.6;
  position: relative;
}
.article-content .code-lang {
  position: absolute;
  top: 8px;
  right: 10px;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-muted);
  background: var(--bg-code);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  letter-spacing: 0.5px;
  user-select: none;
  pointer-events: none;
}
.article-content code {
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 0.9em;
}
.article-content :not(pre) > code {
  background: var(--bg-code);
  padding: 2px 6px;
  border-radius: 4px;
  color: var(--text-accent);
}
.article-content pre code {
  background: none;
  padding: 0;
  color: var(--text-body);
}
.article-content blockquote {
  border-left: 3px solid var(--text-accent);
  padding: 8px 16px;
  margin: 16px 0;
  background: var(--bg-tag);
  border-radius: 0 10px 10px 0;
  color: var(--text-secondary);
}
.article-content blockquote p {
  margin-bottom: 4px;
}
.article-content ul,
.article-content ol {
  padding-left: 24px;
  margin-bottom: 16px;
}
.article-content li {
  margin-bottom: 6px;
}
.article-content hr {
  border: none;
  border-top: 1px solid var(--border);
  margin: 32px 0;
}
.article-content table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
  font-size: 14px;
}
.article-content th,
.article-content td {
  border: 1px solid var(--border);
  padding: 8px 12px;
  text-align: left;
}
.article-content th {
  background: var(--bg-tag);
  font-weight: 600;
}
.article-content img {
  max-width: 100%;
  border-radius: 10px;
  margin: 20px 0;
}
.article-content strong {
  font-weight: 600;
  color: var(--text-primary);
}
.article-content a {
  color: var(--text-accent);
  text-decoration: none;
}
.article-content a:hover {
  text-decoration: underline;
}

/* Skeleton Loading */
.skeleton-back {
  background: var(--bg-tag);
}
.skeleton-title {
  background: var(--bg-tag);
}
.skeleton-meta-row {
  display: flex;
  gap: 10px;
}
.skeleton-meta {
  height: 12px;
  border-radius: 3px;
  background: var(--bg-tag);
}
.skeleton-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.skeleton-line {
  height: 14px;
  border-radius: 3px;
  background: var(--bg-tag);
}
.skeleton-toc-item {
  height: 14px;
  border-radius: 3px;
  margin: 8px 16px;
  background: var(--bg-tag);
}
.skeleton-pulse {
  animation: skeleton-pulse 1.8s ease-in-out infinite;
}
@keyframes skeleton-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

/* Responsive */
@media (max-width: 1078px) {
  .article-layout {
    flex-direction: column;
  }
  .toc-sidebar {
    display: none;
  }
  .article-main {
    max-width: 100%;
  }
  .article-layout.no-toc .article-main {
    flex: 1;
    max-width: 100%;
  }
}
@media (max-width: 768px) {
  .article-container {
    padding: 24px 18px;
  }
  .article-title {
    font-size: 24px;
  }
  .article-meta {
    font-size: 12px;
    flex-wrap: wrap;
  }
  .comment-form-row {
    flex-direction: column;
  }
  .comment-form-card {
    padding: 16px;
  }
  .comment-main {
    padding: 12px;
  }
  .comment-section {
    margin-top: 32px;
    padding-top: 24px;
  }
}
</style>
