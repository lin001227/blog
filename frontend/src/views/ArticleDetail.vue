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
          <button class="dark-toggle" @click="toggleDark" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
            {{ isDark ? '☀️' : '🌙' }}
          </button>
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
          </div>

          <div class="tags" v-if="article.tags && article.tags.length">
            <span class="tag" v-for="tag in tagsList" :key="tag">{{ tag }}</span>
          </div>

          <div class="article-content" v-html="renderedContent"></div>
        </article>

        <div class="article-footer">
          <router-link to="/" class="back-link">← 返回首页</router-link>
        </div>

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
              <span class="comment-nickname">{{ comment.nickname }}</span>
              <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle } from '../api/articles'
import { getArticleComments, createComment } from '../api/comments'
import { useDarkMode } from '../composables/useDarkMode'
import { ElMessage } from 'element-plus'

const { isDark, toggleDark } = useDarkMode()

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
  return article.value.content
})

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

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-body);
  font-family: 'Noto Serif SC', serif;
}
.article-content p {
  margin-bottom: 1.2em;
}
.article-content h1,
.article-content h2,
.article-content h3,
.article-content h4 {
  font-family: 'Noto Serif SC', serif;
  color: var(--text-primary);
  margin: 1.5em 0 0.6em;
  font-weight: 600;
}
.article-content h2 { font-size: 22px; }
.article-content h3 { font-size: 18px; }
.article-content ul,
.article-content ol {
  padding-left: 24px;
  margin-bottom: 1.2em;
}
.article-content li {
  margin-bottom: 0.4em;
}
.article-content blockquote {
  border-left: 3px solid var(--border-blockquote);
  padding-left: 16px;
  margin: 1.2em 0;
  color: var(--text-secondary);
  font-style: italic;
}
.article-content pre {
  background: var(--bg-pre);
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
  margin: 1.2em 0;
  font-size: 14px;
  line-height: 1.6;
  transition: background 0.3s ease;
}
.article-content code {
  background: var(--bg-code);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 14px;
  transition: background 0.3s ease;
}
.article-content pre code {
  background: none;
  padding: 0;
  border-radius: 0;
}
.article-content a {
  color: var(--text-accent);
  text-decoration: none;
}
.article-content a:hover {
  text-decoration: underline;
}
.article-content img {
  max-width: 100%;
  border-radius: 6px;
  margin: 1.2em 0;
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
</style>
