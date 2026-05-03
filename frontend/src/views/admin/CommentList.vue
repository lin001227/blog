<template>
  <div>
    <div class="admin-header">
      <h1 class="admin-heading">
        评论管理
        <el-tag v-if="pendingCount > 0" type="warning" size="small" style="margin-left:12px;vertical-align:middle">
          {{ pendingCount }} 条待审核
        </el-tag>
      </h1>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="comments" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="nickname" label="昵称" width="100" />
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="comment-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="所属文章" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <a class="comment-article-link" :href="`/article/${row.articleId}`" target="_blank" @click.stop>
              <span class="comment-article-icon">📄</span>
              <span class="comment-article-title">{{ row.articleTitle || '(未知文章)' }}</span>
            </a>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="plain">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="150">
          <template #default="{ row }">
            <span class="comment-time-cell">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="comment-actions">
              <div class="comment-status-actions" v-if="row.status === 'PENDING'">
                <el-button text size="small" type="success" @click="handleApprove(row)">通过</el-button>
                <el-button text size="small" type="warning" @click="handleReject(row)">拒绝</el-button>
              </div>
              <el-button
                text
                size="small"
                :type="row.pinned ? 'warning' : 'default'"
                @click="handlePin(row)"
                :title="row.pinned ? '取消置顶' : '置顶'"
              >{{ row.pinned ? '📌 已置顶' : '📍 置顶' }}</el-button>
              <el-popconfirm title="确定删除此评论？" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button text size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminComments, getPendingCount, approveComment, rejectComment, deleteComment, pinComment } from '../../api/comments'
import { ElMessage } from 'element-plus'

const router = useRouter()
const comments = ref([])
const loading = ref(true)
const pendingCount = ref(0)

function statusType(status) {
  return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'info' }[status] || 'info'
}
function statusLabel(status) {
  return { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[status] || status
}
function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
async function handleApprove(row) {
  try {
    await approveComment(row.id)
    row.status = 'APPROVED'
    pendingCount.value = Math.max(0, pendingCount.value - 1)
    ElMessage.success('已通过')
  } catch (e) { ElMessage.error('操作失败') }
}
async function handleReject(row) {
  try {
    await rejectComment(row.id)
    row.status = 'REJECTED'
    pendingCount.value = Math.max(0, pendingCount.value - 1)
    ElMessage.success('已拒绝')
  } catch (e) { ElMessage.error('操作失败') }
}
async function handleDelete(row) {
  try {
    await deleteComment(row.id)
    comments.value = comments.value.filter(c => c.id !== row.id)
    if (row.status === 'PENDING') pendingCount.value = Math.max(0, pendingCount.value - 1)
    ElMessage.success('已删除')
  } catch (e) { ElMessage.error('删除失败') }
}
async function handlePin(row) {
  try {
    await pinComment(row.id)
    row.pinned = !row.pinned
    ElMessage.success(row.pinned ? '已置顶' : '已取消置顶')
  } catch (e) { ElMessage.error('操作失败') }
}

onMounted(async () => {
  try {
    const [res, countRes] = await Promise.all([getAdminComments(), getPendingCount()])
    comments.value = res.data || []
    pendingCount.value = countRes.data?.count || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<style scoped>
.comment-content {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-size: 14px;
  color: var(--text-secondary);
}

.comment-article-link {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  color: var(--text-primary);
  font-size: 13px;
  transition: color 0.2s;
  max-width: 100%;
}

.comment-article-link:hover {
  color: var(--text-accent);
}

.comment-article-icon {
  flex-shrink: 0;
  font-size: 14px;
}

.comment-article-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.comment-status-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.comment-time-cell {
  font-size: 13px;
  color: var(--text-muted);
}

/* Table Card - unified style */
:deep(.table-card) {
  border-radius: 10px !important;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
}
:deep(.table-card .el-card__body) {
  padding: 0;
}
:deep(.el-table) {
  --el-table-border-color: var(--border);
}

/* Admin header - unified */
:deep(.admin-header) {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  :deep(.table-card) {
    overflow-x: auto;
  }
}
</style>