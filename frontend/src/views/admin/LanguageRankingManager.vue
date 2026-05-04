<template>
  <div>
    <h1 style="font-size:20px;font-weight:600;margin:0 0 20px;color:var(--text-primary)">📊 语言排行管理</h1>

    <!-- Add / Edit Form -->
    <el-card shadow="never" class="form-card">
      <div class="form-title">{{ editingId ? '✏️ 编辑语言' : '➕ 添加语言' }}</div>
      <el-form :model="form" label-position="top" class="rank-form">
        <div class="form-grid">
          <el-form-item label="语言名称" required>
            <el-input v-model="form.languageName" placeholder="例: Python" />
          </el-form-item>
          <el-form-item label="排名顺序" required>
            <el-input-number v-model="form.rankOrder" :min="1" :max="100" style="width:100%" />
          </el-form-item>
          <el-form-item label="百分比" required>
            <el-input-number v-model="form.percentage" :min="0" :max="100" :precision="1" :step="0.1" style="width:100%" />
          </el-form-item>
          <el-form-item label="趋势">
            <el-select v-model="form.trend" style="width:100%">
              <el-option label="↑ 上升" value="up" />
              <el-option label="→ 持平" value="stable" />
              <el-option label="↓ 下降" value="down" />
            </el-select>
          </el-form-item>
          <el-form-item label="颜色（十六进制）">
            <el-input v-model="form.color" placeholder="#3776AB" />
          </el-form-item>
          <el-form-item label="图标 URL">
            <el-input v-model="form.iconUrl" placeholder="https://..." />
          </el-form-item>
          <el-form-item label="月份">
            <el-input v-model="form.month" placeholder="2026-04" style="width:140px" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" placeholder="简短描述" />
          </el-form-item>
        </div>
        <div class="form-actions">
          <el-button v-if="editingId" @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="saving" @click="save">
            {{ editingId ? '保存修改' : '添加' }}
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- Rankings Table -->
    <el-card shadow="never" class="table-card">
      <el-table :data="rankings" style="width:100%" v-loading="tableLoading" stripe>
        <el-table-column label="#" width="60">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="languageName" label="语言" width="140">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:6px;">
              <span
                style="display:inline-block;width:12px;height:12px;border-radius:50%;flex-shrink:0;"
                :style="{ background: row.color || 'var(--text-accent)' }"
              ></span>
              <span style="font-weight:500">{{ row.languageName }}</span>
              <span v-if="row.trend === 'up'" style="color:#22c55e;font-weight:700">↑</span>
              <span v-else-if="row.trend === 'down'" style="color:#ef4444;font-weight:700">↓</span>
              <span v-else style="color:var(--text-muted)">→</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="rankOrder" label="排序" width="70" />
        <el-table-column prop="percentage" label="百分比" width="90">
          <template #default="{ row }">{{ row.percentage }}%</template>
        </el-table-column>
        <el-table-column prop="month" label="月份" width="80" />
        <el-table-column prop="description" label="描述" min-width="180">
          <template #default="{ row }">
            <span style="font-size:13px;color:var(--text-secondary)">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click="editRow(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="deleteRow(row.id)">
              <template #reference>
                <el-button text size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAdminLanguageRankings,
  createLanguageRanking,
  updateLanguageRanking,
  deleteLanguageRanking,
} from '../../api/languageRankings'

const rankings = ref([])
const tableLoading = ref(false)
const saving = ref(false)
const editingId = ref(null)

const defaultForm = () => ({
  languageName: '',
  rankOrder: 1,
  percentage: 0,
  trend: 'stable',
  color: '',
  iconUrl: '',
  description: '',
  month: '',
})

const form = ref(defaultForm())

async function loadRankings() {
  tableLoading.value = true
  try {
    const res = await getAdminLanguageRankings()
    rankings.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    tableLoading.value = false
  }
}

function editRow(row) {
  editingId.value = row.id
  form.value = {
    languageName: row.languageName,
    rankOrder: row.rankOrder,
    percentage: row.percentage,
    trend: row.trend,
    color: row.color || '',
    iconUrl: row.iconUrl || '',
    description: row.description || '',
    month: row.month || '',
  }
}

function cancelEdit() {
  editingId.value = null
  form.value = defaultForm()
}

async function save() {
  if (!form.value.languageName) {
    ElMessage.warning('请填写语言名称')
    return
  }
  if (!form.value.percentage && form.value.percentage !== 0) {
    ElMessage.warning('请填写百分比')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateLanguageRanking(editingId.value, form.value)
      ElMessage.success('保存成功')
    } else {
      await createLanguageRanking(form.value)
      ElMessage.success('添加成功')
    }
    cancelEdit()
    await loadRankings()
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '操作失败')
  } finally {
    saving.value = false
  }
}

async function deleteRow(id) {
  try {
    await deleteLanguageRanking(id)
    ElMessage.success('已删除')
    await loadRankings()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(loadRankings)
</script>

<style scoped>
.form-card {
  margin-bottom: 20px;
  border-radius: 10px;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
}
.form-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 20px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 4px;
}
.table-card {
  border-radius: 10px;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
}
:deep(.el-table) {
  --el-table-border-color: var(--border);
}
.rank-form :deep(.el-form-item) {
  margin-bottom: 16px;
}
.rank-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: var(--text-secondary);
  padding-bottom: 4px;
}
.rank-form :deep(.el-input-number .el-input-number__decrease),
.rank-form :deep(.el-input-number .el-input-number__increase) {
  display: none;
}
.rank-form :deep(.el-input-number .el-input__wrapper) {
  padding-right: 0;
}
</style>
