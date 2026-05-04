---
version: alpha
name: 风屿 · 随笔
description: 温暖石色系的个人博客 — 极简、干净、阅读优先的设计语言。
colors:
  # Header & Brand
  brand-accent: "#2563eb"
  brand-accent-hover: "#1d4ed8"
  
  # Light Mode Backgrounds
  bg-page: "#fafaf9"
  bg-card: "#ffffff"
  bg-nav: "rgba(250, 250, 249, 0.92)"
  bg-tag: "#f5f5f4"
  bg-pinned: "#eff6ff"
  bg-pre: "#f5f5f4"
  bg-code: "#f5f5f4"
  bg-input: "#ffffff"
  bg-toc-hover: "#f5f5f4"
  
  # Light Mode Borders
  border-default: "#e7e5e4"
  border-input: "#d6d3d1"
  border-blockquote: "#d6d3d1"
  
  # Light Mode Text
  text-primary: "#1c1917"
  text-secondary: "#78716c"
  text-muted: "#a8a29e"
  text-body: "#292524"
  
  # Light Mode Shadows
  shadow-card: "0 1px 3px rgba(0, 0, 0, 0.04)"
  shadow-card-hover: "0 4px 12px rgba(0, 0, 0, 0.06)"
  
  # Semantic Colors
  color-success: "#16a34a"
  color-error: "#dc2626"
  bg-success: "#f0fdf4"
  bg-error: "#fef2f2"
  border-success: "#bbf7d0"
  border-error: "#fecaca"

  # Dark Mode Backgrounds
  dark-bg-page: "#0c0a09"
  dark-bg-card: "#1c1917"
  dark-bg-nav: "rgba(12, 10, 9, 0.95)"
  dark-bg-tag: "#292524"
  dark-bg-pinned: "#1e3a5f"
  dark-bg-pre: "#292524"
  dark-bg-code: "#292524"
  dark-bg-input: "#292524"
  dark-bg-toc-hover: "#292524"
  
  # Dark Mode Borders
  dark-border: "#292524"
  dark-border-input: "#44403c"
  dark-border-blockquote: "#44403c"
  
  # Dark Mode Text
  dark-text-primary: "#fafaf9"
  dark-text-secondary: "#a8a29e"
  dark-text-muted: "#78716c"
  dark-text-body: "#d6d3d1"
  dark-brand-accent: "#60a5fa"
  dark-brand-accent-hover: "#93c5fd"
  
  # Dark Mode Shadows
  dark-shadow-card: "0 1px 3px rgba(0, 0, 0, 0.3)"
  dark-shadow-card-hover: "0 4px 12px rgba(0, 0, 0, 0.5)"

  # Dark Semantic
  dark-color-success: "#4ade80"
  dark-color-error: "#f87171"
  dark-bg-success: "rgba(22, 163, 74, 0.1)"
  dark-bg-error: "rgba(220, 38, 38, 0.1)"
  dark-border-success: "rgba(74, 222, 128, 0.3)"
  dark-border-error: "rgba(248, 113, 113, 0.3)"

typography:
  h1:
    fontFamily: "'Noto Serif SC', serif"
    fontSize: 32px
    fontWeight: 700
    letterSpacing: 2px
  h2:
    fontFamily: "'Noto Serif SC', serif"
    fontSize: 20px
    fontWeight: 600
  body:
    fontFamily: "'Inter', 'Noto Serif SC', -apple-system, BlinkMacSystemFont, sans-serif"
    fontSize: 15px
    lineHeight: 1.8
  body-small:
    fontFamily: "'Inter', 'Noto Serif SC', sans-serif"
    fontSize: 14px
    lineHeight: 1.7
  meta:
    fontFamily: "'Inter', sans-serif"
    fontSize: 12px
    color: "{colors.text-muted}"
  sidebar-title:
    fontFamily: "'Noto Serif SC', serif"
    fontSize: 15px
    fontWeight: 600

rounded:
  sm: 4px
  md: 8px
  lg: 10px
  xl: 12px

spacing:
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 40px
  
components:
  article-card:
    backgroundColor: "{colors.bg-card}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.lg}"
    border: "1px solid {colors.border-default}"
    shadow: "{colors.shadow-card}"
    padding: "20px 24px"
    hoverTransform: "translateY(-3px)"
    hoverShadow: "{colors.shadow-card-hover}"
    hoverBorder: "1px solid {colors.brand-accent}"
  
  pinned-article-card:
    backgroundColor: "{colors.bg-card}"
    rounded: "{rounded.lg}"
    border: "1px solid {colors.border-default}"
    shadow: "{colors.shadow-card}"
    leftBorder: "3px solid {colors.brand-accent}"
  
  sidebar-card:
    backgroundColor: "{colors.bg-card}"
    rounded: "{rounded.lg}"
    border: "1px solid {colors.border-default}"
    headerPadding: "16px 20px 12px"
    bodyPadding: "12px 20px 18px"
  
  skeleton:
    backgroundColor: "{colors.bg-tag}"
    rounded: "{rounded.sm}"
    pulseDuration: "1.8s ease-in-out"
  
  button-primary:
    backgroundColor: "{colors.brand-accent}"
    textColor: "#ffffff"
    rounded: "{rounded.md}"
    padding: "12px 28px"
    fontSize: 14px
    fontWeight: 600
  
  ranking-bar:
    height: 10px
    rounded: 5px
    backgroundColor: "{colors.bg-tag}"
    fillRounded: 5px
    fillTransition: "width 0.6s ease"
---

## 一、设计理念

**温暖石色系 · 极简阅读体验**

博客的设计灵感来自"暖石"——一种温润、自然、不张扬的质感。整体色调以米白和暖灰为主，搭配沉稳的蓝色作为点缀，营造出舒适、专注的阅读氛围。

### 设计关键词

- **干净** — 大量留白，减少视觉干扰
- **温暖** — 暖灰底色代替纯白，更柔和
- **沉稳** — 蓝色点缀，不跳跃不刺眼
- **阅读优先** — 文字间距、行高、字号都针对长时间阅读优化

---

## 二、色彩系统

### 浅色模式（默认）

| 用途 | 色值 | 用在哪儿 |
|------|------|---------|
| 页面底色 | `#fafaf9` | 整个页面的背景 |
| 卡片底色 | `#ffffff` | 文章卡片、侧边栏卡片 |
| 导航底色 | `rgba(250,250,249,0.92)` | 顶部导航栏（半透明毛玻璃） |
| 标签底色 | `#f5f5f4` | 分类标签、代码块背景 |
| 正文 | `#1c1917` | 文章标题、正文 |
| 次要文字 | `#78716c` | 日期、阅读量、辅助信息 |
| 灰色文字 | `#a8a29e` | 提示文字、占位符 |
| 主题蓝 | `#2563eb` | 链接、按钮、选中状态 |
| 边框 | `#e7e5e4` | 卡片边框、分割线 |
| 成功绿 | `#16a34a` | 订阅成功提示 |
| 失败红 | `#dc2626` | 错误提示 |

### 深色模式

| 用途 | 色值 |
|------|------|
| 页面底色 | `#0c0a09` |
| 卡片底色 | `#1c1917` |
| 导航底色 | `rgba(12,10,9,0.95)` |
| 标签底色 | `#292524` |
| 正文 | `#fafaf9` |
| 主题蓝 | `#60a5fa` |
| 边框 | `#292524` |

---

## 三、字体系统

| 层级 | 字体 | 字号 | 用途 |
|------|------|------|------|
| Hero 标题 | Noto Serif SC（思源宋体） | 32px | 首页大标题 |
| 文章标题 | Noto Serif SC | 17px | 文章卡片标题 |
| 版块标题 | Noto Serif SC | 20px | "最新文章"等区隔标题 |
| 正文 | Inter + Noto Serif SC | 15px | 文章内容 |
| 辅助文字 | Inter | 12px | 日期、阅读量等元信息 |

Noto Serif SC 是 Google 的思源宋体中文字体，适合长文阅读。Inter 是无衬线西文字体，数字和英文显示更清晰。

---

## 四、布局与间距

| 层级 | 间距 |
|------|------|
| 最大内容宽度 | 1100px（两侧留白自动居中） |
| 导航栏高度 | 60px |
| 文章卡片间距 | 20px |
| 文章卡片内边距 | 24px（左右） / 20px（上下） |
| 侧边栏宽度 | 260px |
| 侧边栏与主内容间距 | 40px |

### 响应式断点

| 屏幕宽度 | 行为 |
|---------|------|
| > 1100px | 居中显示，两侧留白 |
| ≤ 900px | 管理后台导航菜单折叠为汉堡菜单 |
| ≤ 768px | 侧边栏移至下方，内边距缩小 |

---

## 五、组件样式

### 文章卡片

- 白色卡片背景，1px 浅灰色边框
- 极其轻微的阴影（`0 1px 3px`）
- 悬停时：上移 3px，阴影加深，边框变蓝
- 置顶文章：左侧 3px 蓝色竖条标识

### 按钮

- 主按钮：蓝色填充，白色文字
- 文字按钮：无背景，纯文字
- 圆角 8px，无投影

### 骨架屏

- 加载时显示灰色占位动画（脉冲闪烁）
- 形状和大小与真实内容完全一致
- 避免页面突然跳动

### 排行榜进度条

- 高度 10px，圆角 5px
- 每行不同颜色（对应各品牌色）
- 平滑过渡动画 0.6s

---

## 六、动效与过渡

| 元素 | 动效 | 时长 |
|------|------|------|
| 文章卡片悬停 | 上移 + 阴影变化 | 0.25s |
| 深色模式切换 | 全局颜色过渡 | 0.45s（不闪烁） |
| 进度条填充 | 宽度动画 | 0.6s |
| 骨架屏脉冲 | 透明度闪烁 | 1.8s 循环 |
| 导航栏悬停 | 颜色渐变 | 0.2s |

---

## 七、设计原则（Do's and Don'ts）

### ✅ 要这样做

- **留白要多** — 卡片之间、文字周围保持充足空间
- **颜色要少** — 一个主题蓝 + 黑白灰就够了
- **文字要可读** — 行高至少 1.7，段落间距舒适
- **交互要反馈** — 点击、悬停都有微动效
- **手机要适配** — 所有页面在手机上都能正常浏览和操作

### ❌ 不要这样做

- 不要用花哨的背景或渐变
- 不要用过多的颜色（不超过 3 种主色）
- 不要用闪烁或弹跳等夸张动画
- 不要有弹窗广告或强制关注
- 不要有超过 3 种字体

---

> 📐 本设计规范基于 Google DESIGN.md 格式编写  
> 🎨 所有 CSS 变量定义在 `frontend/index.html` 的 `:root` 和 `.dark` 中
