<template>
  <div class="page">
    <!-- Navigation -->
    <nav class="nav">
      <div class="nav-inner">
        <router-link to="/" class="nav-brand">风屿 · 随笔</router-link>
        <div class="nav-links">
          <router-link class="nav-link" to="/">首页</router-link>
          <a class="nav-link" href="#">归档</a>
          <a class="nav-link" href="#">标签</a>
          <a class="nav-link" href="#">关于</a>
          <router-link class="nav-link" to="/admin/login">管理</router-link>
          <DarkToggle />
        </div>
      </div>
    </nav>

    <!-- Article -->
    <main class="main" v-if="article">
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

          <!-- Mobile TOC -->
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

        <!-- Desktop TOC Sidebar -->
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

        <!-- Comment Section -->
        <div class="comment-section">
          <h3 class="comment-section-title">评论 ({{ comments.length }})</h3>

          <!-- Comment Form -->
          <div class="comment-form">
            <div class="comment-form-row">
              <input v-model="commentForm.nickname" placeholder="昵称 *" class="comment-input" maxlength="50" />
              <input v-model="commentForm.email" placeholder="邮箱（选填）" class="comment-input" maxlength="255" />
            </div>
            <textarea v-model="commentForm.content" placeholder="写下你的评论..." class="comment-textarea" rows="4" maxlength="2000"></textarea>
            <div class="comment-form-actions">
              <span class="comment-form-hint">{{ commentForm.content.length }}/2000</span>
              <button class="comment-submit-btn" :disabled="submittingComment" @click="submitComment">
                {{ submittingComment ? '提交中...' : '发表评论' }}
              </button>
            </div>
            <div v-if="commentError" class="comment-error">{{ commentError }}</div>
            <div v-if="commentSuccess" class="comment-success">✅ 评论已提交，等待审核后显示</div>
          </div>

          <!-- Comment List -->
          <div v-if="comments.length === 0" class="comment-empty">暂无评论，快来抢沙发吧~</div>
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <span class="comment-avatar" :style="{ background: nameColor(comment.nickname) }">{{ comment.nickname.charAt(0) }}</span>
              <div class="comment-header-info">
                <span class="comment-nickname">{{ comment.nickname }}</span>
                <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
              </div>
            </div>
            <div class="comment-body">{{ comment.content }}</div>
          </div>
        </div>
      </div>
    </main>

    <div v-else-if="loading" class="loading">加载中...</div>
    <div v-else class="loading">文章未找到</div>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-inner">
        <p>&copy; {{ new Date().getFullYear() }} 风屿 · 随笔. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle } from '../api/articles'
import { getArticleComments, createComment } from '../api/comments'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import DarkToggle from '../components/DarkToggle.vue'

// Configure marked for better rendering
marked.setOptions({
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
  const content = article.value.content
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



/* Main */
.main {
  max-width: 740px;
  margin: 0 auto;
  padding: 40px 24px 60px;
}

.article-container {
  background: var(--bg-card);
  border-radius: 8px;
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
  font-size: 30px;
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
  margin-bottom: 24px;
}
.tag {
  display: inline-block;
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-tag);
  padding: 3px 10px;
  border-radius: 12px;
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

/* Comment Section */
.comment-section {
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid var(--border);
}
.comment-section-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
}
.comment-form {
  margin-bottom: 28px;
}
.comment-form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.comment-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid var(--border-input);
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  background: var(--bg-input);
  color: var(--text-primary);
  font-family: inherit;
  transition: border-color 0.2s;
}
.comment-input:focus {
  border-color: var(--text-accent);
}
.comment-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border-input);
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  resize: vertical;
  font-family: inherit;
  line-height: 1.6;
  background: var(--bg-input);
  color: var(--text-primary);
  transition: border-color 0.2s;
}
.comment-textarea:focus {
  border-color: var(--text-accent);
}
.comment-form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}
.comment-form-hint {
  font-size: 12px;
  color: var(--text-muted);
}
.comment-submit-btn {
  padding: 8px 20px;
  background: var(--text-accent);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.2s;
  font-weight: 500;
}
.comment-submit-btn:hover { opacity: 0.85; }
.comment-submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.comment-error {
  margin-top: 8px;
  font-size: 13px;
  color: #ef4444;
}
.comment-success {
  margin-top: 8px;
  font-size: 13px;
  color: #22c55e;
}
.comment-empty {
  text-align: center;
  padding: 32px;
  color: var(--text-muted);
  font-size: 14px;
}
.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
  user-select: none;
}
.comment-header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.comment-nickname {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.comment-time {
  font-size: 12px;
  color: var(--text-muted);
}
.comment-body {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-body);
}

/* TOC Sidebar (Desktop) */
.toc-sidebar {
  position: fixed;
  top: 50%;
  right: max(calc((100vw - 740px) / 2 - 280px), 20px);
  transform: translateY(-50%);
  width: 210px;
  max-height: 70vh;
  overflow-y: auto;
  background: var(--bg-card);
  border-radius: 10px;
  padding: 14px 0;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border: 1px solid var(--border);
  transition: background 0.3s ease, border-color 0.3s ease;
  scroll-behavior: smooth;
  z-index: 50;
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
  margin-bottom: 4px;
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
  border-radius: 8px;
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
@media (max-width: 1100px) {
  .toc-sidebar { display: none; }
}
/* Show desktop TOC on wide screens */
@media (min-width: 1101px) {
  .mobile-toc { display: none; }
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
  border-radius: 8px;
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
  border-radius: 0 6px 6px 0;
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
  border-radius: 8px;
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
</style>
