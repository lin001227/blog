# 📖 站外文章摘要 — 实施方案

## 一、功能概述

在博客中新增 **「精选阅读」** 板块，管理员添加外部文章链接 → 后端自动抓取内容 → 生成摘要 → 展示在博客上。

类似「今日热读」「每周精选」的效果，让博客从个人创作延伸到内容聚合。

---

## 二、整体流程

```
管理员粘贴URL
      ↓
后端 Jsoup 抓取网页内容（标题、正文、发布时间、封面图）
      ↓
提取摘要（两种方案二选一）
      ├─ 方案A: 调用 LLM API 智能总结
      └─ 方案B: 本地算法提取关键句（无需外网）
      ↓
存入 DB, 标记抓取状态
      ↓
前端展示：卡片列表 + 来源标记 + "阅读原文"链接
```

---

## 三、数据库设计

新建表 `external_articles`：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| url | VARCHAR(1024) | 原文链接 |
| title | VARCHAR(255) | 文章标题 |
| source | VARCHAR(100) | 来源网站名（自动解析域名） |
| cover_url | VARCHAR(512) | 封面图 |
| summary | TEXT | AI/算法生成的摘要 |
| original_content | MEDIUMTEXT | 原始正文（可选存储） |
| summary_type | VARCHAR(20) | `ai` / `extractive` |
| category | VARCHAR(50) | 手动/自动分类 |
| tags | VARCHAR(255) | 逗号分隔标签 |
| fetched_at | DATETIME | 抓取时间 |
| published_at | DATETIME | 原文发布时间 |
| status | VARCHAR(20) | `pending` / `success` / `failed` |
| error_msg | VARCHAR(500) | 失败原因 |
| created_at | DATETIME | 记录创建时间 |
| updated_at | DATETIME | 最后更新时间 |

---

## 四、后端实现

### 4.1 新增模块结构

```
backend/src/main/java/com/blog/
├── controller/
│   └── ExternalArticleController.java    # REST API
├── service/
│   ├── ExternalArticleService.java       # 业务逻辑
│   └── WebFetcherService.java            # 网页抓取 + 摘要
├── repository/
│   └── ExternalArticleRepository.java    # JPA
├── entity/
│   └── ExternalArticle.java              # 实体
├── dto/
│   └── ExternalArticleRequest.java       # 请求 DTO
│   └── ExternalArticleResponse.java      # 响应 DTO
└── config/
    └── WebFetcherConfig.java             # 抓取配置
```

### 4.2 网页抓取 (WebFetcherService)

使用 **Jsoup** 库，新增依赖到 pom.xml：

```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.18.3</version>
</dependency>
```

抓取流程：
1. `Jsoup.connect(url).userAgent("...").timeout(10000).get()`
2. 解析 `<title>` 获取标题
3. 解析 `<meta property="og:title">`、`<meta name="description">` 等 OG 标签
4. 解析 `<meta property="og:image">` 获取封面图
5. 解析 `<meta property="article:published_time">` 获取发布时间
6. 按 `<article>` → `<main>` → `<body>` 优先级提取正文文本
7. 去重、清理广告/导航噪音

### 4.3 摘要生成（两种方案）

#### 方案A — LLM API 智能总结（推荐）

通过 OpenAI 兼容 API 生成摘要：

```
POST https://api.openai.com/v1/chat/completions
Authorization: Bearer {API_KEY}

{
  "model": "gpt-4o-mini",
  "messages": [
    {"role": "system", "content": "你是文章摘要助手。用中文用3-5句话总结以下文章的核心内容，保持客观。"},
    {"role": "user", "content": "文章标题: {title}\n文章正文: {content(前3000字)}"}
  ],
  "max_tokens": 300
}
```

**优点**：摘要质量高，自然流畅
**注意**：需要代理（已有，走 127.0.0.1:17891）
**成本**：gpt-4o-mini 约 $0.15/百万 token，一篇文章约 3000 token → 约 ¥0.003/篇

#### 方案B — 本地算法摘要（离线可用）

使用 TextRank 算法提取关键句：

1. 正文分句（按 。！？分割）
2. 计算每句的 TF-IDF 权重
3. 句子间计算相似度（余弦相似度）
4. 构建图，PageRank 排序
5. 取 Top 3-5 句作为摘要

**优点**：无需联网，零成本
**缺点**：质量不如 LLM，可能断章取义

> **建议**：先实现方案A（LLM），方案B作为 fallback 兜底

### 4.4 REST API

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/admin/external-articles` | 添加URL并抓取 | ✅ |
| GET | `/api/admin/external-articles` | 管理端列表 | ✅ |
| DELETE | `/api/admin/external-articles/{id}` | 删除 | ✅ |
| PUT | `/api/admin/external-articles/{id}` | 编辑摘要/分类/标签 | ✅ |
| POST | `/api/admin/external-articles/{id}/refetch` | 重新抓取 | ✅ |
| GET | `/api/external-articles` | 公开列表（分页） | ❌ |
| GET | `/api/external-articles/{id}` | 公开详情 | ❌ |

### 4.5 定时刷新（可选）

可配置定时任务，定期检查是否有新的外部文章需要抓取，或对已抓取的文章执行重新摘要。

---

## 五、前端实现

### 5.1 新页面结构

```
frontend/src/
├── views/
│   └── ExternalReadings.vue        # 公开页面 - 精选阅读
└── views/admin/
    └── ExternalArticleManager.vue   # 管理端 - 外部文章管理
```

### 5.2 公开页面 — ExternalReadings.vue

路由：`/readings`

布局与首页一致（main-layout + sidebar）：

```
┌──────────────────────────────────────┬──────────────────┐
│  📖 精选阅读                          │ 📊 统计           │
│                                      │                  │
│  ┌────────────────────────────────┐  │ 已收录 12 篇      │
│  │ [封面] 文章标题                │  │ 来自 8 个站点     │
│  │ 来源 · 3天前 · AI摘要          │  │                  │
│  │ ┌──────────────────────────┐  │  │ 🏷️ 分类过滤       │
│  │ │ 摘要内容...              │  │  │                  │
│  │ └──────────────────────────┘  │  │ 技术 (5)         │
│  │ 🔗 阅读原文 ↗                │  │ 生活 (3)         │
│  └────────────────────────────────┘  │ 工具 (4)         │
│                                      │                  │
│  ┌────────────────────────────────┐  │ 分页控件          │
│  │ ...                            │  │                  │
│  └────────────────────────────────┘  │                  │
└──────────────────────────────────────┴──────────────────┘
```

卡片设计：复用首页 el-card 的 10px 圆角 + 暖灰风格

### 5.3 管理端 — ExternalArticleManager.vue

路由：`/admin/external-articles`

与现有 ArticleList.vue 风格一致：

```
┌─────────────────────────────────────────────────────────┐
│ ➕ 添加链接  ┌─────────────────────────────────────┐     │
│              │ https://example.com/article/xxx     │     │
│              └─────────────────────────────────────┘     │
│              [分类 ▼] [开始抓取 ▶]                      │
├─────────────────────────────────────────────────────────┤
│ 状态筛选：[全部] [成功] [失败] [待处理]                    │
├─────────────────────────────────────────────────────────┤
│ 标题           来源    分类    状态    摘要预览      时间  操作 │
│ 如何调试...    blog.  ✅  成功  本文介绍了...  04-30  📝🗑️↻ │
│ ...                                                     │
└─────────────────────────────────────────────────────────┘
```

### 5.4 API 封装

新增 `frontend/src/api/externalArticles.js`

---

## 六、项目文件改动清单

### 新增文件
```
backend/
├── pom.xml                          # +jsoup 依赖
├── src/main/java/com/blog/
│   ├── entity/ExternalArticle.java
│   ├── dto/ExternalArticleRequest.java
│   ├── dto/ExternalArticleResponse.java
│   ├── repository/ExternalArticleRepository.java
│   ├── service/ExternalArticleService.java
│   └── service/WebFetcherService.java
│   ├── config/WebFetcherConfig.java
│   └── controller/ExternalArticleController.java

frontend/src/
├── api/externalArticles.js
├── views/ExternalReadings.vue
└── views/admin/ExternalArticleManager.vue
```

### 修改文件
```
backend/src/main/resources/application.yml   # +LLM API 配置
frontend/src/router/index.js                  # +新路由
```

---

## 七、实现优先级

| 优先级 | 内容 | 预估工时 |
|--------|------|---------|
| P0 | 后端实体 + Repository + 数据库迁移 | 0.5h |
| P0 | WebFetcherService（Jsoup 抓取 + 文本抽取） | 1h |
| P1 | LLM 摘要（OpenAI 兼容 API） | 1h |
| P1 | Controller + 管理端 API | 0.5h |
| P1 | 管理前端 ExternalArticleManager.vue | 1.5h |
| P2 | 公开前端 ExternalReadings.vue | 1.5h |
| P3 | 分类/标签/搜索 | 0.5h |
| P4 | 定时任务自动刷新 | 0.5h |

---

## 八、预估总计

**后端（Java）：** ~3.5h（含抓取、摘要、API）
**前端（Vue）：** ~3h（管理端 + 公开页面）
**总计：** ~6.5h 一整天的工作量

---

请确认方案，有问题可以讨论，确认后开始实现。
