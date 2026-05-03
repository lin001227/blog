<template>
  <div>
    <div class="admin-header">
      <h1 class="admin-heading">文章管理</h1>
    </div>

    <!-- Toolbar -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-inner">
        <el-input
          v-model="searchQuery"
          placeholder="搜索文章标题或内容..."
          :prefix-icon="Search"
          clearable
          class="search-input"
          @input="onSearchInput"
          @clear="clearSearch"
        />
        <div class="filter-group">
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
        <div class="toolbar-actions">
          <router-link to="/admin/articles/new">
            <el-button type="primary" size="default">
              <el-icon style="margin-right:4px"><Plus /></el-icon>
              新建
            </el-button>
          </router-link>
        </div>
      </div>
    </el-card>

    <!-- Batch Action Bar -->
    <div v-if="selectedIds.length > 0" class="batch-bar">
      <span class="batch-info">已选择 <strong>{{ selectedIds.length }}</strong> 篇文章</span>
      <el-button size="small" type="warning" plain @click="batchPin(true)">📌 批量置顶</el-button>
      <el-button size="small" plain @click="batchPin(false)">📍 取消置顶</el-button>
      <el-popconfirm title="确定删除选中的文章？" confirm-button-text="删除" @confirm="batchDelete">
        <template #reference>
          <el-button size="small" type="danger" plain>🗑️ 批量删除</el-button>
        </template>
      </el-popconfirm>
      <el-button size="small" text @click="clearSelection">取消选择</el-button>
    </div>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
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
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="title-cell">
              <span v-if="row.pinned" class="pinned-icon" title="置顶">📌</span>
              <span class="cell-text">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" show-overflow-tooltip />
        <el-table-column label="评论" width="60" align="center">
          <template #default="{ row }">{{ row.commentCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="阅读" width="60" align="center">
          <template #default="{ row }">{{ row.viewCount ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="标签" width="170">
          <template #default="{ row }">
            <el-tag
              v-for="tag in getTags(row)"
              :key="tag"
              size="small"
              style="margin-right:4px;margin-bottom:2px"
            >{{ tag }}</el-tag>
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
        <el-table-column prop="createdAt" label="创建时间" width="130">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click.stop="editArticle(row)">编辑</el-button>
            <el-popconfirm title="确定删除此文章？" confirm-button-text="删除" cancel-button-text="取消" @confirm.stop="handleDelete(row)">
              <template #reference>
                <el-button text size="small" type="danger" @click.stop>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Pagination -->
    <div v-if="totalPages > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalElements"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
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

const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

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

function clearSelection() {
  selectedIds.value = []
  if (tableRef.value) tableRef.value.clearSelection()
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

function onSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

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

async function batchPin(pinned) {
  const ids = [...selectedIds.value]
  if (ids.length === 0) return
  try {
    await batchPinArticles(ids, pinned)
    ElMessage.success(pinned ? '已批量置顶' : '已取消置顶')
    clearSelection()
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
    clearSelection()
    loadArticles()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

async function loadArticles() {
  loading.value = true
  try {
    const q = searchQuery.value.trim()
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (q) params.q = q

    const res = await searchAdminArticles(params)
    const data = res.data
    let items = data.content || []

    if (filterPinned.value !== null) {
      items = items.filter(a => a.pinned === filterPinned.value)
    }

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

onMounted(loadArticles)
</script>

<style scoped>
/* Header */
.admin-header {
  margin-bottom: 20px;
}
.admin-heading {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

/* Toolbar */
.toolbar-card {
  margin-bottom: 16px;
  border-radius: 10px !important;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
}
.toolbar-card :deep(.el-card__body) {
  padding: 16px 20px;
}
.toolbar-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.search-input {
  flex: 1;
  min-width: 200px;
  max-width: 380px;
}
.filter-group {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
.toolbar-actions {
  margin-left: auto;
  flex-shrink: 0;
}

/* Batch Bar */
.batch-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  margin-bottom: 16px;
  background: var(--bg-pinned);
  border: 1px solid var(--border);
  border-radius: 10px;
  flex-wrap: wrap;
}
.batch-info {
  font-size: 13px;
  color: var(--text-secondary);
  margin-right: 4px;
}
.batch-info strong {
  color: var(--text-accent);
  font-weight: 600;
}

/* Table Card */
.table-card {
  border-radius: 10px !important;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
}
.title-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
}
.cell-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.pinned-icon {
  font-size: 14px;
  flex-shrink: 0;
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0;
}

@media (max-width: 768px) {
  .table-card {
    overflow-x: auto;
  }
  .toolbar-inner {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input {
    max-width: none;
  }
  .toolbar-actions {
    margin-left: 0;
  }
}
</style>
