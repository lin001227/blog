import { ref, watch, onMounted } from 'vue'

const isDark = ref(false)
const isInitialized = ref(false)

function applyTheme(dark) {
  if (dark) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
  isDark.value = dark
}

function toggleDark() {
  applyTheme(!isDark.value)
  localStorage.setItem('blog-dark-mode', isDark.value ? 'dark' : 'light')
}

export function useDarkMode() {
  if (!isInitialized.value) {
    isInitialized.value = true
    onMounted(() => {
      const stored = localStorage.getItem('blog-dark-mode')
      if (stored) {
        applyTheme(stored === 'dark')
      } else {
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
        applyTheme(prefersDark)
      }
    })
  }
  return { isDark, toggleDark }
}