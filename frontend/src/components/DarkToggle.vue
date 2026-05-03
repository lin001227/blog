<template>
  <button
    class="theme-toggle"
    :class="{ 'is-dark': isDark }"
    @click="toggleDark"
    :title="isDark ? '切换亮色模式' : '切换暗色模式'"
    aria-label="切换主题"
  >
    <!-- Sun icon (light mode) -->
    <svg class="icon-sun" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="5" />
      <line x1="12" y1="1" x2="12" y2="3" />
      <line x1="12" y1="21" x2="12" y2="23" />
      <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
      <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
      <line x1="1" y1="12" x2="3" y2="12" />
      <line x1="21" y1="12" x2="23" y2="12" />
      <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
      <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
    </svg>
    <!-- Moon icon (dark mode) -->
    <svg class="icon-moon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    </svg>
  </button>
</template>

<script setup>
import { useDarkMode } from '../composables/useDarkMode'
const { isDark, toggleDark } = useDarkMode()
</script>

<style scoped>
.theme-toggle {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: var(--bg-card);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  overflow: hidden;
  padding: 0;
  line-height: 1;
}
.theme-toggle:hover {
  color: var(--text-accent);
  border-color: var(--text-accent);
}
.theme-toggle svg {
  position: absolute;
  width: 18px;
  height: 18px;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
/* Sun: visible in light mode, rotates out in dark */
.icon-sun {
  opacity: 1;
  transform: rotate(0deg) scale(1);
}
.is-dark .icon-sun {
  opacity: 0;
  transform: rotate(90deg) scale(0.5);
}
/* Moon: hidden in light mode, rotates in in dark */
.icon-moon {
  opacity: 0;
  transform: rotate(-90deg) scale(0.5);
}
.is-dark .icon-moon {
  opacity: 1;
  transform: rotate(0deg) scale(1);
}
</style>
