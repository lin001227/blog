<template>
  <div class="admin-page">
    <!-- Admin Nav -->
    <nav class="admin-nav">
      <div class="admin-nav-inner">
        <router-link to="/admin" class="admin-nav-brand">风屿 · 管理</router-link>
        <div class="admin-nav-links">
          <router-link to="/admin" class="admin-nav-link">概览</router-link>
          <router-link to="/admin/articles" class="admin-nav-link">文章</router-link>
          <router-link v-if="auth.isAdmin" to="/admin/users" class="admin-nav-link">用户</router-link>
          <router-link to="/admin/comments" class="admin-nav-link">评论</router-link>
          <a class="admin-nav-link" href="/" target="_blank">查看博客</a>
          <div class="admin-user">
            <el-avatar :size="28" shape="square" style="background: var(--text-accent); vertical-align: middle;">
              <span style="font-size: 13px;">{{ auth.displayName.charAt(0) }}</span>
            </el-avatar>
            <span class="admin-user-name">{{ auth.displayName }}</span>
          </div>
          <el-button text size="small" @click="handleLogout" style="color: var(--text-secondary);">
            退出登录
          </el-button>
          <button class="dark-toggle" @click="toggleDark" :title="isDark ? '切换亮色模式' : '切换暗色模式'">
            {{ isDark ? '☀️' : '🌙' }}
          </button>
        </div>
      </div>
    </nav>

    <main class="admin-main">
      <div class="editor-header">
        <h1 class="editor-heading">{{ isEdit ? '编辑文章' : '新建文章' }}</h1>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="editor-form"
      >
        <div class="form-grid">
          <el-form-item label="标题" prop="title" class="form-title">
            <el-input
              v-model="form.title"
              placeholder="请输入文章标题"
              size="large"
            />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="分类" prop="category" class="form-half">
              <el-input
                v-model="form.category"
                placeholder="例如：技术、生活"
              />
            </el-form-item>

            <el-form-item label="标签" prop="tags" class="form-half">
              <el-input
                v-model="form.tags"
                placeholder="用逗号分隔，例如：Vue,前端"
              />
            </el-form-item>
          </div>

          <el-form-item label="置顶">
            <el-switch v-model="form.pinned" />
          </el-form-item>
        </div>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="20"
            placeholder="支持 Markdown 格式"
            class="content-input"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '发布文章' }}
          </el-button>
          <el-button size="large" @click="handleCancel">取消</el-button>
        </div>
      </el-form>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { getArticle, createArticle, updateArticle } from '../../api/articles'
import { ElMessage } from 'element-plus'
import { useDarkMode } from '../../composables/useDarkMode'

const { isDark, toggleDark } = useDarkMode()

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const isEdit = computed(() => !!route.params.id)
const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)

const form = reactive({
  title: '',
  category: '',
  tags: '',
  content: '',
  pinned: false,
})

const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度不能超过200字', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' },
  ],
}

function handleLogout() {
  auth.logout()
  router.push('/admin/login')
}

function handleCancel() {
  router.push('/admin/articles')
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      title: form.title,
      content: form.content,
      category: form.category || '',
      tags: form.tags || '',
      pinned: form.pinned,
    }

    if (isEdit.value) {
      await updateArticle(route.params.id, payload)
      ElMessage.success('文章已更新')
    } else {
      await createArticle(payload)
      ElMessage.success('文章已发布')
    }
    router.push('/admin/articles')
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (isEdit.value) {
    loading.value = true
    try {
      const res = await getArticle(route.params.id)
      const article = res.data
      form.title = article.title || ''
      form.category = article.category || ''
      form.tags = article.tags || ''
      form.content = article.content || ''
      form.pinned = !!article.pinned
    } catch (e) {
      ElMessage.error('加载文章失败')
      console.error(e)
    } finally {
      loading.value = false
    }
  }
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: var(--bg-page);
  transition: background 0.3s ease;
}

.admin-nav {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: background 0.3s ease, border-color 0.3s ease;
}
.admin-nav-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.admin-nav-brand {
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
}
.admin-nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}
.admin-nav-link {
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
}
.admin-nav-link:hover,
.admin-nav-link.active,
.admin-nav-link.router-link-exact-active {
  color: var(--text-primary);
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 16px;
  border-left: 1px solid var(--border);
}
.admin-user-name {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
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

.admin-main {
  max-width: 860px;
  margin: 0 auto;
  padding: 36px 24px 60px;
}

.editor-header {
  margin-bottom: 28px;
}

.editor-heading {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.editor-form {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 32px;
  box-shadow: var(--shadow-card);
  transition: background 0.3s ease;
}

.form-grid {
  margin-bottom: 4px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

.content-input :deep(.el-textarea__inner) {
  font-family: 'Noto Serif SC', 'Inter', monospace;
  line-height: 1.8;
}
</style>
