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
  background: #1c1917;
}
.dark ::-webkit-scrollbar-thumb {
  background: #44403c;
  border-radius: 4px;
}
.dark ::-webkit-scrollbar-thumb:hover {
  background: #57534e;
}
</style>
