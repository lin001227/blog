<template>
  <div>
    <h1 style="font-size:20px;font-weight:600;margin:0 0 20px;color:var(--text-primary)">📖 精选阅读管理</h1>

    <!-- Add URL -->
    <el-card shadow="never" class="add-card">
      <div class="add-form">
        <el-input
          v-model="newUrl"
          placeholder="粘贴文章链接..."
          clearable
          class="url-input"
          @keyup.enter="addArticle"
        />
        <el-input
          v-model="newCategory"
          placeholder="分类（可选）"
          clearable
          class="category-input"
        />
        <el-button type="primary" :loading="loading" @click="addArticle">
          {{ loading ? '抓取中...' : '📥 抓取' }}
        </el-button>
      </div>
    </el-card>

    <!-- Articles Table -->
    <el-card shadow="never" class="table-card">
      <el-table :data="articles" style="width:100%" v-loading="tableLoading" stripe>
        <el-table-column prop="title" label="标题" min-width="250">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:8px;">
              <el-tag v-if="row.status === 'pending'" size="small" type="warning">抓取中</el-tag>
              <el-tag v-if="row.status === 'failed'" size="small" type="danger">失败</el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.category" size="small" effect="plain">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'success'" size="small" type="success">成功</el-tag>
            <el-tag v-else-if="row.status === 'pending'" size="small" type="warning">待处理</el-tag>
            <el-tag v-else size="small" type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="摘要预览" min-width="200">
          <template #default="{ row }">
            <span style="font-size:13px;color:var(--text-secondary);">
              {{ row.summary ? row.summary.substring(0, 60) + '...' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="90">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click="editRow(row)">编辑</el-button>
            <el-button text size="small" @click="refetchArticle(row.id)" :loading="refetchingId === row.id">重试</el-button>
            <el-popconfirm title="确定删除？" @confirm="deleteArticle(row.id)">
              <template #reference>
                <el-button text size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑摘要" width="600px">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="摘要">
          <el-input v-model="editForm.summary" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="editForm.category" />
        </el-form-item>
        <el-form-item label="标签（逗号分隔）">
          <el-input v-model="editForm.tags" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAdminExternalArticles,
  createExternalArticle,
  updateExternalArticle,
  deleteExternalArticle,
  refetchExternalArticle,
} from '../../api/externalArticles'

const articles = ref([])
const newUrl = ref('')
const newCategory = ref('')
const loading = ref(false)
const tableLoading = ref(false)
const refetchingId = ref(null)

const editDialogVisible = ref(false)
const editForm = ref({ id: null, summary: '', category: '', tags: '' })
const saving = ref(false)

async function loadArticles() {
  tableLoading.value = true
  try {
    const res = await getAdminExternalArticles()
    articles.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    tableLoading.value = false
  }
}

async function addArticle() {
  const url = newUrl.value.trim()
  if (!url) {
    ElMessage.warning('请输入链接')
    return
  }
  loading.value = true
  try {
    await createExternalArticle({ url, category: newCategory.value.trim() })
    ElMessage.success('已添加并开始抓取')
    newUrl.value = ''
    newCategory.value = ''
    await loadArticles()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '添加失败')
  } finally {
    loading.value = false
  }
}

function editRow(row) {
  editForm.value = {
    id: row.id,
    summary: row.summary || '',
    category: row.category || '',
    tags: row.tags || '',
  }
  editDialogVisible.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await updateExternalArticle(editForm.value.id, {
      summary: editForm.value.summary,
      category: editForm.value.category,
      tags: editForm.value.tags,
    })
    ElMessage.success('已保存')
    editDialogVisible.value = false
    await loadArticles()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function refetchArticle(id) {
  refetchingId.value = id
  try {
    await refetchExternalArticle(id)
    ElMessage.success('重新抓取完成')
    await loadArticles()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '重试失败')
  } finally {
    refetchingId.value = null
  }
}

async function deleteArticle(id) {
  try {
    await deleteExternalArticle(id)
    ElMessage.success('已删除')
    await loadArticles()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

onMounted(loadArticles)
</script>

<style scoped>
.add-card {
  margin-bottom: 20px;
  border-radius: 10px;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
}
.add-form {
  display: flex;
  gap: 10px;
  align-items: center;
}
.url-input {
  flex: 1;
}
.category-input {
  width: 150px;
}
.table-card {
  border-radius: 10px;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
}
:deep(.el-table) {
  --el-table-border-color: var(--border);
}
</style>
