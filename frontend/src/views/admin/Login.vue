<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <router-link to="/" class="login-brand">风屿 · 随笔</router-link>
        <p class="login-subtitle">管理后台</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item style="margin-bottom: 4px;">
          <el-checkbox v-model="rememberPassword" size="small">
            记住密码
          </el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      </el-form>
      <div class="login-footer">
        <router-link to="/" class="login-footer-link">← 返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const loading = ref(false)
const errorMsg = ref('')
const rememberPassword = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

onMounted(() => {
  const saved = localStorage.getItem('blog-remember-credentials')
  if (saved) {
    try {
      const creds = JSON.parse(saved)
      form.username = creds.username || ''
      form.password = creds.password || ''
      rememberPassword.value = true
    } catch (e) {
      localStorage.removeItem('blog-remember-credentials')
    }
  }
})

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  errorMsg.value = ''
  try {
    await auth.login(form.username, form.password)
    if (rememberPassword.value) {
      localStorage.setItem('blog-remember-credentials', JSON.stringify({
        username: form.username,
        password: form.password,
      }))
    } else {
      localStorage.removeItem('blog-remember-credentials')
    }
    router.push('/admin')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  padding: 24px;
  transition: background 0.3s ease;
}
.login-card {
  max-width: 400px;
  background: var(--bg-card);
  border-radius: 10px;
  padding: 40px;
  box-shadow: var(--shadow-card);
  transition: background 0.3s ease;
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-brand {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
  letter-spacing: 1px;
}
.login-subtitle {
  font-size: 14px;
  color: var(--text-muted);
  margin-top: 8px;
}
.error-msg {
  text-align: center;
  font-size: 13px;
  color: #ef4444; /* TODO: use CSS variable for error color */
  margin-top: -8px;
  margin-bottom: 8px;
}
.login-footer {
  text-align: center;
  margin-top: 20px;
}
.login-footer-link {
  font-size: 13px;
  color: var(--text-muted);
  text-decoration: none;
  transition: color 0.2s;
}
.login-footer-link:hover {
  color: var(--text-accent);
}
</style>
