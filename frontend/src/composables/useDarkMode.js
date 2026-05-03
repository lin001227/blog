import { ref, watch, onMounted } from 'vue'

const isDark = ref(false)
const isInitialized = ref(false)

function applyTheme(dark, animate = false) {
  const html = document.documentElement
  if (animate) {
    html.classList.add('theme-transitioning')
  }
  if (dark) {
    html.classList.add('dark')
  } else {
    html.classList.remove('dark')
  }
  isDark.value = dark
  if (animate) {
    setTimeout(() => html.classList.remove('theme-transitioning'), 500)
  }
}

function toggleDark() {
  applyTheme(!isDark.value, true)
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
