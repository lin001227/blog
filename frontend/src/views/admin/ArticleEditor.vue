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
          <el-dropdown trigger="click" @command="handleUserCommand" class="admin-user-dropdown">
            <div class="admin-user-trigger">
              <el-avatar :size="28" shape="square" style="background: var(--text-accent); cursor: pointer;">
                <span style="font-size: 13px;">{{ auth.displayName.charAt(0) }}</span>
              </el-avatar>
              <el-icon style="margin-left: 2px; color: var(--text-secondary);">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled style="cursor: default;">
                  <div style="padding: 4px 0;">
                    <div style="font-weight: 600; color: var(--text-primary); font-size: 14px;">{{ auth.displayName }}</div>
                    <div style="font-size: 12px; color: var(--text-secondary); margin-top: 2px;" v-if="auth.isAdmin">管理员</div>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="dark" divided>
                  <span style="display: flex; align-items: center; gap: 6px;">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/></svg>
                    切换主题
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <span style="display: flex; align-items: center; gap: 6px; color: #e74c3c;">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/></svg>
                    退出登录
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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

function handleUserCommand(command) {
    if (command === 'logout') {
      auth.logout()
      router.push('/admin/login')
    } else if (command === 'dark') {
      const html = document.documentElement
      const isDark = html.classList.contains('dark')
      html.classList.toggle('dark')
      html.classList.add('theme-transitioning')
      setTimeout(() => html.classList.remove('theme-transitioning'), 500)
      localStorage.setItem('theme', isDark ? 'light' : 'dark')
    }
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

.admin-user-dropdown {
  margin-left: 12px;
}
.admin-user-trigger {
  display: flex;
  align-items: center;
  cursor: pointer;
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
