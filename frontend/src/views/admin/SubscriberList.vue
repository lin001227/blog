<template>
  <div>
    <div class="admin-header">
      <h1 class="admin-heading">
        订阅管理
        <el-tag v-if="subscribers.length > 0" type="primary" size="small" style="margin-left:12px;vertical-align:middle">
          {{ subscribers.length }} 位订阅者
        </el-tag>
      </h1>
      <p class="admin-subtitle">管理所有邮件订阅用户</p>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="subscribers" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="email" label="邮箱" min-width="280">
          <template #default="{ row }">
            <span class="subscriber-email">📧 {{ row.email }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small" effect="plain">
              {{ row.status === 'active' ? '活跃' : '已退订' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="订阅时间" width="180">
          <template #default="{ row }">
            <span class="time-cell">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              title="确定删除该订阅？"
              description="删除后将不再向该邮箱推送新文章"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
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
import { getAdminSubscribers, deleteAdminSubscriber } from '../../api/subscribers'
import { ElMessage } from 'element-plus'

const subscribers = ref([])
const loading = ref(true)

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

async function handleDelete(row) {
  try {
    await deleteAdminSubscriber(row.id)
    ElMessage.success(`已删除订阅：${row.email}`)
    subscribers.value = subscribers.value.filter(s => s.id !== row.id)
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '删除失败')
  }
}

onMounted(async () => {
  try {
    const res = await getAdminSubscribers()
    subscribers.value = res.data
  } catch (e) {
    ElMessage.error('加载订阅列表失败')
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.admin-header {
  margin-bottom: 24px;
}

.admin-heading {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.admin-subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.subscriber-email {
  font-family: 'Inter', monospace;
  font-size: 14px;
  color: var(--text-primary);
}

.time-cell {
  font-size: 13px;
  color: var(--text-muted);
}

.table-card {
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--bg-card);
}
</style>
