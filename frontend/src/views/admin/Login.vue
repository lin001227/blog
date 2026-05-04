<template>
  <div class="login-page">
    <!-- 装饰性背景光晕 -->
    <div class="login-bg" aria-hidden="true">
      <div class="bg-blob bg-blob-1" />
      <div class="bg-blob bg-blob-2" />
      <div class="bg-blob bg-blob-3" />
    </div>

    <div class="login-container">
      <div class="login-card">
        <!-- 品牌标识 -->
        <div class="login-header">
          <router-link to="/" class="brand-link">
            <span class="brand-icon">
              <svg width="20" height="20" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="20" cy="20" r="19" stroke="currentColor" stroke-width="1.5" opacity="0.15" />
                <path d="M10 28L20 12L30 28" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
                <path d="M15 24.5L20 18L25 24.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.4" />
              </svg>
            </span>
            <span class="brand-name">风屿 · 随笔</span>
          </router-link>
        </div>

        <div class="login-body">
          <h2 class="card-title">欢迎回来</h2>
          <p class="card-desc">请登录您的管理员账号</p>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="login-form"
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

            <div class="form-options">
              <el-checkbox v-model="rememberPassword" size="small">
                记住密码
              </el-checkbox>
            </div>

            <transition name="msg-fade">
              <el-alert
                v-if="errorMsg"
                :title="errorMsg"
                type="error"
                show-icon
                :closable="false"
                class="error-alert"
              />
            </transition>

            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form>

          <div class="login-footer">
            <router-link to="/" class="back-link">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M19 12H5" />
                <path d="M12 19l-7-7 7-7" />
              </svg>
              返回首页
            </router-link>
          </div>
        </div>
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
/* ============ 页面布局 ============ */
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
  padding: 24px;
  overflow: hidden;
  transition: background 0.3s ease;
}

/* ============ 背景光晕 ============ */
.login-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.25;
}

.bg-blob-1 {
  width: 400px;
  height: 400px;
  top: -120px;
  right: -80px;
  background: radial-gradient(circle, #3b82f6, #60a5fa);
  animation: blob-drift-1 20s ease-in-out infinite;
}

.bg-blob-2 {
  width: 350px;
  height: 350px;
  bottom: -100px;
  left: -80px;
  background: radial-gradient(circle, #8b5cf6, #a78bfa);
  animation: blob-drift-2 25s ease-in-out infinite;
}

.bg-blob-3 {
  width: 250px;
  height: 250px;
  top: 50%;
  left: 50%;
  translate: -50% -50%;
  background: radial-gradient(circle, #06b6d4, #22d3ee);
  animation: blob-drift-3 18s ease-in-out infinite;
}

@keyframes blob-drift-1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(-40px, 30px) scale(1.1); }
  66% { transform: translate(20px, -20px) scale(0.95); }
}

@keyframes blob-drift-2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(40px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

@keyframes blob-drift-3 {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.15; }
  50% { transform: translate(-45%, -55%) scale(1.3); opacity: 0.25; }
}

/* ============ 容器与卡片 ============ */
.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: var(--bg-card);
  border-radius: 16px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  animation: card-enter 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes card-enter {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ============ 品牌头部 ============ */
.login-header {
  padding: 32px 32px 0;
}

.brand-link {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--text-primary);
  transition: opacity 0.2s;
}

.brand-link:hover {
  opacity: 0.8;
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--text-accent);
  color: #ffffff;
  flex-shrink: 0;
}

.brand-name {
  font-family: 'Noto Serif SC', serif;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1px;
}

/* ============ 卡片内容 ============ */
.login-body {
  padding: 24px 32px 32px;
}

.card-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.card-desc {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 24px;
}

/* ============ 表单 ============ */
.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  padding-bottom: 6px;
}

.login-form :deep(.el-input__wrapper) {
  background: var(--bg-input);
  border: 1px solid var(--border-input);
  border-radius: 10px;
  box-shadow: none;
  padding: 4px 12px;
  transition: border-color 0.25s, box-shadow 0.25s;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--text-accent);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--text-accent);
  box-shadow:
    0 0 0 3px color-mix(in srgb, var(--text-accent) 15%, transparent);
}

.login-form :deep(.el-input__inner) {
  color: var(--text-primary);
  font-size: 14px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: var(--text-muted);
  font-size: 13px;
}

.login-form :deep(.el-input__prefix) {
  margin-right: 6px;
}

.login-form :deep(.el-input__prefix-inner > svg) {
  color: var(--text-muted);
  transition: color 0.25s;
}

.login-form :deep(.el-input__wrapper.is-focus .el-input__prefix-inner > svg) {
  color: var(--text-accent);
}

/* ============ 表单选项 ============ */
.form-options {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 4px;
}

.form-options :deep(.el-checkbox) {
  cursor: pointer;
}

.form-options :deep(.el-checkbox__label) {
  font-size: 13px;
  color: var(--text-secondary);
}

/* ============ 错误提示 ============ */
.error-alert {
  margin-bottom: 16px;
  border-radius: 8px;
}

.msg-fade-enter-active,
.msg-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.msg-fade-enter-from,
.msg-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* ============ 登录按钮 ============ */
.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.login-btn:not(.is-loading):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px color-mix(in srgb, var(--el-color-primary) 35%, transparent);
}

.login-btn:not(.is-loading):active {
  transform: translateY(0);
}

/* ============ 页脚 ============ */
.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
  text-decoration: none;
  transition: color 0.2s;
}

.back-link:hover {
  color: var(--text-accent);
}

/* ============ 响应式 ============ */
@media (max-width: 480px) {
  .login-page {
    padding: 16px;
    align-items: flex-start;
    padding-top: 10vh;
  }

  .login-card {
    border-radius: 12px;
  }

  .login-header {
    padding: 24px 24px 0;
  }

  .login-body {
    padding: 20px 24px 24px;
  }

  .card-title {
    font-size: 20px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .bg-blob {
    animation: none;
  }

  .login-card {
    animation: none;
  }
}
</style>
