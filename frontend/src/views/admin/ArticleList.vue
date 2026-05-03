<template>
      <div class="admin-header">
        <h1 class="admin-heading">文章管理</h1>
        <router-link to="/admin/articles/new">
          <el-button type="primary">
            <el-icon style="margin-right: 4px;"><Plus /></el-icon>
            新建文章
          </el-button>
        </router-link>
      </div>

      <!-- Search & Filter Bar -->
      <div class="article-toolbar">
        <div class="article-search">
          <el-icon class="article-search-icon"><Search /></el-icon>
          <input
            v-model="searchQuery"
            class="article-search-input"
            placeholder="搜索文章标题或内容..."
            @input="onSearchInput"
          />
          <el-button v-if="searchQuery" text size="small" class="article-search-clear" @click="clearSearch">
            ✕
          </el-button>
        </div>
        <div class="article-filter-group">
          <el-button
            :type="filterPinned === null ? 'primary' : 'default'"
            size="small"
            plain
            @click="setFilter(null)"
          >全部</el-button>
          <el-button
            :type="filterPinned === true ? 'primary' : 'default'"
            size="small"
            plain
            @click="setFilter(true)"
          >📌 置顶</el-button>
          <el-button
            :type="filterPinned === false ? 'primary' : 'default'"
            size="small"
            plain
            @click="setFilter(false)"
          >普通</el-button>
        </div>
      </div>

      <!-- Batch Action Bar -->
      <div v-if="selectedIds.length > 0" class="batch-bar">
        <span class="batch-info">已选择 <strong>{{ selectedIds.length }}</strong> 篇文章</span>
        <el-button size="small" type="warning" plain @click="batchPin(true)">
          📌 批量置顶
        </el-button>
        <el-button size="small" type="default" plain @click="batchPin(false)">
          📍 取消置顶
        </el-button>
        <el-popconfirm title="确定删除选中的文章？" confirm-button-text="删除" @confirm="batchDelete">
          <template #reference>
            <el-button size="small" type="danger" plain>
              🗑️ 批量删除
            </el-button>
          </template>
        </el-popconfirm>
        <el-button size="small" text @click="selectedIds = []">
          取消选择
        </el-button>
      </div>

      <!-- Table -->
      <el-table
        ref="tableRef"
        :data="articles"
        v-loading="loading"
        stripe
        style="width: 100%"
        @row-click="handleRowClick"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="40" />
        <el-table-column prop="title" label="标题" min-width="240">
          <template #default="{ row }">
            <div class="article-title-cell">
              <span v-if="row.pinned" class="pinned-dot" title="置顶">📌</span>
              {{ row.title }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="110" />
        <el-table-column label="评论" width="60" align="center">
          <template #default="{ row }">
            {{ row.commentCount ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="阅读" width="60" align="center">
          <template #default="{ row }">
            {{ row.viewCount ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="标签" width="170">
          <template #default="{ row }">
            <el-tag
              v-for="tag in getTags(row)"
              :key="tag"
              size="small"
              style="margin-right: 4px; margin-bottom: 2px;"
            >
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="65" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="!!row.pinned"
              size="small"
              @click.stop
              @change="(val) => togglePinned(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="140">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click.stop="editArticle(row)">
              编辑
            </el-button>
            <el-popconfirm
              title="确定删除此文章？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm.stop="handleDelete(row)"
            >
              <template #reference>
                <el-button text size="small" type="danger" @click.stop>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="article-pagination" v-if="totalPages > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="totalElements"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="onPageChange"
          @size-change="onSizeChange"
          background
        />
      </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getAdminArticles,
  searchAdminArticles,
  updateArticle,
  deleteArticle,
  batchPinArticles,
  batchDeleteArticles,
} from '../../api/articles'
import { ElMessage } from 'element-plus'

const router = useRouter()
const tableRef = ref(null)

const articles = ref([])
const loading = ref(true)
const searchQuery = ref('')
const filterPinned = ref(null)
let searchTimer = null

// Pagination
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

// Selection
const selectedIds = ref([])

function getTags(row) {
  if (!row.tags) return []
  if (Array.isArray(row.tags)) return row.tags
  return String(row.tags).split(/[,\s]+/).filter(Boolean)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

function handleRowClick(row) {
  router.push(`/admin/articles/${row.id}/edit`)
}

function editArticle(row) {
  router.push(`/admin/articles/${row.id}/edit`)
}

async function togglePinned(row, val) {
  try {
    await updateArticle(row.id, { pinned: val })
    row.pinned = val
    ElMessage.success(val ? '已置顶' : '已取消置顶')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(row) {
  try {
    await deleteArticle(row.id)
    articles.value = articles.value.filter(a => a.id !== row.id)
    totalElements.value--
    ElMessage.success('删除成功')
    if (articles.value.length === 0 && currentPage.value > 1) {
      currentPage.value--
      loadArticles()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// ---- Selection ----
function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

// ---- Search & Filter ----
function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadArticles()
  }, 300)
}

function clearSearch() {
  searchQuery.value = ''
  currentPage.value = 1
  loadArticles()
}

function setFilter(val) {
  filterPinned.value = val
  currentPage.value = 1
  loadArticles()
}

function onPageChange(page) {
  currentPage.value = page
  loadArticles()
}

function onSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadArticles()
}

// ---- Batch Operations ----
async function batchPin(pinned) {
  const ids = [...selectedIds.value]
  if (ids.length === 0) return
  try {
    await batchPinArticles(ids, pinned)
    ElMessage.success(pinned ? '已批量置顶' : '已取消置顶')
    selectedIds.value = []
    // Clear table selection
    if (tableRef.value) tableRef.value.clearSelection()
    loadArticles()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function batchDelete() {
  const ids = [...selectedIds.value]
  if (ids.length === 0) return
  try {
    await batchDeleteArticles(ids)
    ElMessage.success(`已删除 ${ids.length} 篇文章`)
    selectedIds.value = []
    if (tableRef.value) tableRef.value.clearSelection()
    loadArticles()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// ---- Main Load (always backend paginated) ----
async function loadArticles() {
  loading.value = true
  try {
    const q = searchQuery.value.trim()
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (q) params.q = q

    const res = await searchAdminArticles(params)
    const data = res.data
    let items = data.content || []

    // Client-side filter for pinned status
    if (filterPinned.value !== null) {
      items = items.filter(a => a.pinned === filterPinned.value)
    }

    // If filtered results are empty but we're on page > 1, go back
    if (items.length === 0 && currentPage.value > 1) {
      currentPage.value--
      loadArticles()
      return
    }

    articles.value = items
    totalElements.value = data.totalElements || 0
    totalPages.value = data.totalPages || 0
  } catch (e) {
    console.error('Failed to load articles:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.article-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.article-search {
  flex: 1;
  min-width: 240px;
  max-width: 400px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  padding: 0 12px;
  transition: border-color 0.2s;
}

.article-search:focus-within {
  border-color: var(--el-color-primary);
}

.article-search-icon {
  color: var(--el-text-color-placeholder);
  flex-shrink: 0;
  font-size: 16px;
}

.article-search-input {
  flex: 1;
  padding: 9px 0;
  border: none;
  outline: none;
  background: transparent;
  color: var(--el-text-color-primary);
  font-size: 14px;
  font-family: inherit;
}

.article-search-input::placeholder {
  color: var(--el-text-color-placeholder);
}

.article-search-clear {
  color: var(--el-text-color-placeholder);
  flex-shrink: 0;
  font-size: 14px;
  padding: 2px 4px;
}

.article-filter-group {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

/* Batch Bar */
.batch-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  margin-bottom: 12px;
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-5);
  border-radius: 8px;
  flex-wrap: wrap;
}

.batch-info {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-right: 4px;
}

.batch-info strong {
  color: var(--el-color-primary);
  font-weight: 600;
}

/* Table */
.article-title-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pinned-dot {
  font-size: 14px;
}

/* Pagination */
.article-pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
}
</style>
