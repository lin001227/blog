<template>
  <div class="public-layout">
    <header class="public-header">
      <div class="public-header-inner">
        <router-link to="/" class="public-brand">风屿 · 随笔</router-link>
        
        <!-- Desktop Navigation -->
        <div class="public-nav">
          <router-link to="/" class="public-nav-link" :class="{ active: $route.path === '/' }">首页</router-link>
          <router-link to="/archive" class="public-nav-link" :class="{ active: $route.path === '/archive' }">归档</router-link>
          <router-link to="/readings" class="public-nav-link" :class="{ active: $route.path === '/readings' }">阅读</router-link>
          <router-link to="/rankings" class="public-nav-link" :class="{ active: $route.path === '/rankings' }">排行</router-link>
          <router-link to="/admin/login" class="public-nav-link">管理</router-link>
          <DarkToggle />
        </div>

        <!-- Mobile Hamburger Button -->
        <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen" aria-label="Toggle navigation">
          <span class="hamburger"></span>
        </button>
      </div>

      <!-- Mobile Dropdown Menu -->
      <div class="mobile-nav" :class="{ open: mobileMenuOpen }">
        <router-link to="/" class="mobile-nav-link" :class="{ active: $route.path === '/' }" @click="mobileMenuOpen = false">首页</router-link>
        <router-link to="/archive" class="mobile-nav-link" :class="{ active: $route.path === '/archive' }" @click="mobileMenuOpen = false">归档</router-link>
        <router-link to="/readings" class="mobile-nav-link" :class="{ active: $route.path === '/readings' }" @click="mobileMenuOpen = false">阅读</router-link>
        <router-link to="/rankings" class="mobile-nav-link" :class="{ active: $route.path === '/rankings' }" @click="mobileMenuOpen = false">排行</router-link>
        <router-link to="/admin/login" class="mobile-nav-link" @click="mobileMenuOpen = false">管理</router-link>
        <div class="mobile-theme-toggle">
          <DarkToggle />
        </div>
      </div>
    </header>
    
    <main class="public-main">
      <router-view />
    </main>
    
    <footer class="public-footer">
      <div class="public-footer-inner">
        <p>&copy; {{ new Date().getFullYear() }} 风屿 · 随笔. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DarkToggle from '../components/DarkToggle.vue'
import { useDarkMode } from '../composables/useDarkMode'

useDarkMode()
const mobileMenuOpen = ref(false)
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  transition: background 0.3s ease;
}

.public-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--bg-nav);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.public-header-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 24px;
  height: var(--nav-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.public-brand {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
  font-family: 'Noto Serif SC', serif;
  letter-spacing: 1px;
  white-space: nowrap;
}

/* Desktop Nav */
.public-nav {
  display: flex;
  align-items: center;
  gap: 20px;
}

.public-nav-link {
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.2s;
  font-weight: 500;
}

.public-nav-link:hover,
.public-nav-link.active {
  color: var(--text-primary);
}

/* Mobile Menu Button */
.mobile-menu-btn {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  z-index: 101;
}

.hamburger {
  display: block;
  width: 24px;
  height: 2px;
  background: var(--text-primary);
  position: relative;
  transition: all 0.3s ease;
}

.hamburger::before,
.hamburger::after {
  content: '';
  position: absolute;
  width: 24px;
  height: 2px;
  background: var(--text-primary);
  transition: all 0.3s ease;
}

.hamburger::before { top: -8px; }
.hamburger::after { bottom: -8px; }

/* Mobile Dropdown */
.mobile-nav {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transform: translateY(-10px);
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.mobile-nav.open {
  transform: translateY(0);
  opacity: 1;
  visibility: visible;
}

.mobile-nav-link {
  font-size: 16px;
  color: var(--text-secondary);
  text-decoration: none;
  padding: 8px 0;
  font-weight: 500;
  transition: color 0.2s;
}

.mobile-nav-link:hover,
.mobile-nav-link.active {
  color: var(--text-primary);
}

.mobile-theme-toggle {
  padding-top: 12px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: center;
}

/* Main Content */
.public-main {
  flex: 1;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 40px 24px 60px;
  width: 100%;
}

/* Footer */
.public-footer {
  padding: 32px 24px;
  text-align: center;
}

.public-footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
}

.public-footer p {
  font-size: 13px;
  color: var(--text-muted);
}

/* Responsive */
@media (max-width: 768px) {
  .public-nav {
    display: none;
  }
  
  .mobile-menu-btn {
    display: block;
  }
  
  .public-header-inner {
    padding: 0 16px;
  }
}
</style>
