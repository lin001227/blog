<template>
  <div class="admin-page">
    <nav class="admin-nav">
      <div class="admin-nav-inner">
        <router-link to="/admin" class="admin-nav-brand">风屿 · 管理</router-link>
        <div class="admin-nav-links">
          <router-link to="/admin" class="admin-nav-link">概览</router-link>
          <router-link to="/admin/articles" class="admin-nav-link">文章</router-link>
          <router-link v-if="auth.isAdmin" to="/admin/users" class="admin-nav-link">用户</router-link>
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
        <h1 class="editor-heading">{{ isEdit ? '编辑用户' : '新建用户' }}</h1>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="editor-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="3-50个字符" :disabled="isEdit" size="large" />
        </el-form-item>
        <el-form-item :label="isEdit ? '新密码（留空不修改）' : '密码'" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少6个字符" show-password size="large" />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" placeholder="用户显示名称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="user@example.com" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio value="ADMIN">管理员</el-radio>
            <el-radio value="EDITOR">编辑</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建用户' }}
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
import { getUser, createUser, updateUser } from '../../api/users'
import { ElMessage } from 'element-plus'
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const isEdit = computed(() => !!route.params.id)
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  username: '',
  password: '',
  displayName: '',
  email: '',
  role: 'EDITOR',
  enabled: true,
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3-50字符', trigger: 'blur' },
  ],
  password: [
    { min: 6, max: 100, message: '密码至少6个字符', trigger: 'blur' },
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
  router.push('/admin/users')
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!isEdit.value && !form.password) {
    ElMessage.error('请设置密码')
    return
  }

  submitting.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password || undefined,
      displayName: form.displayName || undefined,
      email: form.email || undefined,
      role: form.role,
      enabled: form.enabled,
    }

    if (isEdit.value) {
      await updateUser(route.params.id, payload)
      ElMessage.success('用户已更新')
    } else {
      await createUser(payload)
      ElMessage.success('用户已创建')
    }
    router.push('/admin/users')
  } catch (e) {
    const msg = e.response?.data?.error || (isEdit.value ? '更新失败' : '创建失败')
    ElMessage.error(msg)
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (isEdit.value) {
    try {
      const res = await getUser(route.params.id)
      const user = res.data
      form.username = user.username || ''
      form.displayName = user.displayName || ''
      form.email = user.email || ''
      form.role = user.role || 'EDITOR'
      form.enabled = user.enabled !== false
    } catch (e) {
      ElMessage.error('加载用户失败')
      router.push('/admin/users')
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
  max-width: 640px;
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
.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}
</style>
