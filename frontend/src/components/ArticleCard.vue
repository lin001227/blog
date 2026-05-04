<template>
  <el-card
    class="article-card"
    :class="{ 'pinned-card': pinned }"
    shadow="hover"
    tabindex="0"
    role="link"
    @click="$router.push(`/article/${article.id}`)"
    @keydown.enter="$router.push(`/article/${article.id}`)"
    @keydown.space.prevent="$router.push(`/article/${article.id}`)"
  >
    <template #header>
      <div class="article-card-header">
        <div v-if="pinned" class="pinned-badge">
          <el-icon><StarFilled /></el-icon> 置顶
        </div>
        <h2 class="article-card-title">{{ article.title }}</h2>
        <div class="article-card-meta">
          <span class="meta-stat">
            <el-icon><View /></el-icon> {{ article.viewCount ?? 0 }}
          </span>
          <span class="meta-stat">
            <el-icon><ChatDotSquare /></el-icon> {{ article.commentCount ?? 0 }}
          </span>
          <span class="meta-sep">·</span>
          <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
          <el-tag
            v-if="article.category"
            size="small"
            effect="plain"
            class="meta-tag"
          >
            {{ article.category }}
          </el-tag>
        </div>
      </div>
    </template>
    <p class="article-excerpt">{{ excerpt(article.content) }}</p>
  </el-card>
</template>

<script setup>
import { StarFilled, View, ChatDotSquare } from '@element-plus/icons-vue'

const props = defineProps({
  article: { type: Object, required: true },
  pinned: { type: Boolean, default: false },
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function excerpt(content) {
  if (!content) return ''
  const text = content
    .replace(/<[^>]*>/g, '')
    .replace(/[#*\[\]`>|-]/g, ' ')
    .trim()
  return text.substring(0, 150) + (text.length > 150 ? '...' : '')
}
</script>

<style scoped>
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

/* Responsive */
@media (max-width: 768px) {
  .article-card :deep(.el-card__header) {
    padding: 16px 18px 10px;
  }
  .article-card :deep(.el-card__body) {
    padding: 4px 18px 16px;
  }
}
</style>
