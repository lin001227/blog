<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
import { useDarkMode } from './composables/useDarkMode'

useDarkMode()
</script>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Scrollbar dark mode */
.dark ::-webkit-scrollbar {
  width: 8px;
}
.dark ::-webkit-scrollbar-track {
  background: var(--bg-card);
}
.dark ::-webkit-scrollbar-thumb {
  background: var(--border-input);
  border-radius: 4px;
}
.dark ::-webkit-scrollbar-thumb:hover {
  background: #57534e; /* TODO: scrollbar thumb hover color, no matching CSS var */
}
</style>
