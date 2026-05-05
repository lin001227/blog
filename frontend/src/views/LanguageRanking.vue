<template>
  <!-- Header -->
  <section class="ranking-header">
    <div class="ranking-header-inner">
      <h1 class="ranking-title">🤖 大模型排行榜</h1>
      <p class="ranking-subtitle">
        基于社区 ELO 评分 · {{ currentMonth }}
        <el-tag size="small" effect="plain" class="source-tag">每日自动更新</el-tag>
      </p>
    </div>
  </section>

  <!-- Content -->
  <div class="main-layout">
    <main class="content">
      <!-- Loading -->
      <template v-if="loading">
        <div v-for="n in 10" :key="n" class="skeleton-row">
          <div class="skeleton-rank skeleton-pulse"></div>
          <div class="skeleton-bar-area">
            <div class="skeleton-bar skeleton-pulse" :style="{ width: `${70 - n * 4}%` }"></div>
          </div>
        </div>
      </template>

      <!-- Empty -->
      <div v-else-if="rankings.length === 0" class="empty-state">
        暂无排行数据
      </div>

      <!-- Ranking List -->
      <div v-else class="ranking-list">
        <div
          v-for="(item, index) in rankings"
          :key="item.id"
          class="ranking-row"
          :class="{ 'top-three': index < 3 }"
        >
          <!-- Rank Badge -->
          <div class="rank-badge" :class="`rank-${index + 1}`">
            <span v-if="index < 3" class="rank-icon">{{ ['🥇', '🥈', '🥉'][index] }}</span>
            <span v-else class="rank-num">{{ index + 1 }}</span>
          </div>

          <!-- Language Info -->
          <div class="lang-info">
            <div class="lang-name-row">
              <span v-if="item.iconUrl" class="lang-icon">
                <img :src="item.iconUrl" :alt="item.languageName" class="lang-icon-img" />
              </span>
              <span class="lang-name">{{ item.languageName }}</span>
              <span v-if="item.trend === 'up'" class="trend-up" title="上升">↑</span>
              <span v-else-if="item.trend === 'down'" class="trend-down" title="下降">↓</span>
              <span v-else class="trend-stable" title="持平">→</span>
            </div>
            <div v-if="item.description" class="lang-desc">{{ item.description }}</div>
          </div>

          <!-- Progress Bar -->
          <div class="bar-wrapper">
            <div class="bar-track">
              <div
                class="bar-fill"
                :style="{
                  width: item.percentage + '%',
                  background: item.color || 'var(--text-accent)',
                }"
              ></div>
            </div>
          </div>

          <!-- Percentage -->
          <div class="percentage" :style="{ color: item.color || 'var(--text-accent)' }">
            {{ item.percentage }}%
          </div>
        </div>
      </div>
    </main>

    <!-- Sidebar -->
    <aside class="sidebar">
      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-title">📊 统计</span></template>
        <div class="stat-list">
          <div class="stat-item">
            <span class="stat-label">收录语言</span>
            <span class="stat-value">{{ rankings.length }} 种</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">上升中</span>
            <span class="stat-value trend-up">{{ upCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">下降中</span>
            <span class="stat-value trend-down">{{ downCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">数据月份</span>
            <span class="stat-value">{{ currentMonth }}</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="sidebar-card">
        <template #header><span class="sidebar-title">🔍 说明</span></template>
        <p class="sidebar-text">
          本排行榜基于大语言模型社区评分数据，每日自动更新。
        </p>
        <p class="sidebar-text" style="margin-top: 8px">
          数据综合反映当前大模型的能力排行，仅供参考。
          <router-link to="/" class="back-link">← 返回博客</router-link>
        </p>
      </el-card>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getLanguageRankings } from '../api/languageRankings'

const rankings = ref([])
const loading = ref(true)

const upCount = computed(() => rankings.value.filter(r => r.trend === 'up').length)
const downCount = computed(() => rankings.value.filter(r => r.trend === 'down').length)
const currentMonth = computed(() => {
  const m = rankings.value[0]?.month
  return m ? m + ' 月' : '未知'
})

onMounted(async () => {
  try {
    const res = await getLanguageRankings()
    rankings.value = res.data || []
  } catch (e) {
    console.error('Failed to load language rankings:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* Header */
.ranking-header {
  padding: 60px 24px 40px;
  text-align: center;
}
.ranking-header-inner {
  max-width: 600px;
  margin: 0 auto;
}
.ranking-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.ranking-subtitle {
  font-size: 15px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}
.source-tag {
  font-size: 11px;
  height: 20px;
  line-height: 20px;
  padding: 0 6px;
  border-radius: 4px;
  background: var(--bg-tag) !important;
  color: var(--text-accent) !important;
  border: none !important;
}

/* Main Layout */
.main-layout {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 24px 60px;
  display: flex;
  gap: 40px;
  align-items: flex-start;
}
.content {
  flex: 1;
  min-width: 0;
}

/* Ranking List */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ranking-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 10px;
  transition: all 0.2s ease;
  box-shadow: var(--shadow-card);
}
.ranking-row:hover {
  box-shadow: var(--shadow-card-hover);
  border-color: var(--text-accent);
}
.ranking-row.top-three {
  background: linear-gradient(135deg, var(--bg-card) 0%, var(--bg-tag) 100%);
}

/* Rank Badge */
.rank-badge {
  width: 40px;
  text-align: center;
  flex-shrink: 0;
}
.rank-icon {
  font-size: 22px;
  line-height: 1;
}
.rank-num {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-muted);
}

/* Language Info */
.lang-info {
  width: 180px;
  flex-shrink: 0;
}
.lang-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.lang-icon-img {
  width: 18px;
  height: 18px;
  display: block;
}
.lang-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.trend-up {
  font-size: 14px;
  color: #22c55e;
  font-weight: 700;
}
.trend-down {
  font-size: 14px;
  color: #ef4444;
  font-weight: 700;
}
.trend-stable {
  font-size: 14px;
  color: var(--text-muted);
}
.lang-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Progress Bar */
.bar-wrapper {
  flex: 1;
  min-width: 0;
}
.bar-track {
  height: 10px;
  background: var(--bg-tag);
  border-radius: 5px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.6s ease;
  min-width: 4px;
}

/* Percentage */
.percentage {
  width: 55px;
  text-align: right;
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
}

/* Sidebar */
.sidebar {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.sidebar-card {
  border-radius: 10px !important;
  border: 1px solid var(--border) !important;
  background: var(--bg-card) !important;
}
.sidebar-card :deep(.el-card__header) {
  padding: 16px 20px 12px;
  border-bottom: 1px solid var(--border);
}
.sidebar-card :deep(.el-card__body) {
  padding: 12px 20px 18px;
}
.sidebar-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.sidebar-text {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
  margin: 0;
}
.stat-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}
.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.back-link {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-accent);
  text-decoration: none;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 24px;
  color: var(--text-muted);
  font-size: 15px;
}

/* Skeleton Loading */
.skeleton-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  margin-bottom: 6px;
  border-radius: 10px;
  background: var(--bg-card);
  border: 1px solid var(--border);
}
.skeleton-rank {
  width: 40px;
  height: 22px;
  flex-shrink: 0;
  border-radius: 4px;
}
.skeleton-bar-area {
  flex: 1;
  display: flex;
  align-items: center;
  height: 46px;
}
.skeleton-bar {
  height: 10px;
  border-radius: 5px;
}
.skeleton-pulse {
  background: var(--bg-tag);
  animation: skeleton-pulse 1.8s ease-in-out infinite;
}
@keyframes skeleton-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

/* Responsive */
@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
    padding: 0 16px 40px;
  }
  .sidebar {
    width: 100%;
  }
  .ranking-header {
    padding: 40px 16px 32px;
  }
  .ranking-title {
    font-size: 26px;
  }
  .ranking-row {
    padding: 12px 14px;
    gap: 10px;
  }
  .lang-info {
    width: 120px;
  }
  .lang-desc {
    display: none;
  }
  .percentage {
    width: 48px;
    font-size: 14px;
  }
}
</style>
