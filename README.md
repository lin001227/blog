# 📝 风屿 · 随笔 — 个人博客

基于 **Spring Boot 3 + Vue 3 + MariaDB** 的全栈博客系统，Docker 一键部署。

包含文章管理、评论系统、邮件订阅、大模型排行榜、精选阅读、全文搜索、RSS 订阅等完整功能。

## ✨ 功能

### 📄 文章系统
- 📝 文章发布、编辑、删除（Markdown 支持、代码高亮）
- 📌 置顶文章 + 批量置顶/取消置顶
- 🏷️ 文章分类与标签
- 🔍 全文搜索（标题 + 内容，前端实时搜索 + 管理端后端分页）
- 📋 文章归档（按年月分组展示）
- 👁️ 阅读数统计
- 💬 评论数显示

### 💬 评论系统
- 访客提交评论（昵称 + 邮箱 + 内容）
- 后台审核管理（通过 / 拒绝 / 删除）
- 评论置顶功能

### 📡 订阅 & 推送
- 📧 首页邮件订阅表单（真实 API 对接）
- 🎉 订阅成功自动发送欢迎邮件（HTML 风格与博客一致）
- 📤 **新文章发布 → 自动邮件推送所有订阅者**
- 📋 管理后台订阅管理（查看 / 删除订阅邮箱）
- 📡 RSS 订阅支持

### 🤖 大模型排行榜
- 首页展示热门 AI 模型排行榜（TOP 10）
- 百分比 + 趋势箭头 + 品牌图标
- 管理后台可编辑 / 拖拽排序

### 📖 精选阅读
- 外链文章管理（URL 自动抓取 + AI 摘要）
- 精选阅读独立页面
- 支持重新抓取 / 刷新

### 🎨 前端设计
- 🌙 深色模式（暖灰色系，一键切换，过渡动画）
- 📱 响应式布局（桌面 / 平板 / 手机）
- 🦴 骨架屏加载状态（所有页面）
- ✨ 页面过渡动画
- 📑 文章目录 TOC（滚动跟随高亮）
- 🔗 Open Graph 社交分享预览
- 🏠 首页重构（ArticleCard 组件化，语义颜色变量）

### 🔐 管理后台
| 页面 | 功能 |
|------|------|
| 📊 概览 | 数据看板（文章数 / 评论数 / 订阅数 / 阅读量统计） |
| 📝 文章 | 文章列表 + 批量置顶/删除 + 后端分页搜索 |
| 💬 评论 | 评论审核管理（通过 / 拒绝 / 置顶 / 删除） |
| 📖 阅读 | 精选阅读管理（添加 / 编辑 / 重新抓取） |
| 🤖 排行 | 大模型排行榜管理（编辑 / 拖拽排序） |
| 📧 订阅 | 查看所有订阅邮箱，支持删除 |
| 👥 用户 | 用户管理（仅管理员可见） |

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | Vue 3 + Element Plus + Pinia + marked + Vue Router |
| **后端** | Java 17 + Spring Boot 3.2.5 + Spring Security + JWT + JPA |
| **数据库** | MariaDB 10.11 |
| **邮件** | QQ邮箱 SMTP（587/TLS） |
| **部署** | Docker + Docker Compose + Nginx |

## 🚀 快速启动

```bash
# 克隆项目
git clone https://github.com/lin001227/blog.git
cd blog

# 一键启动（需要 Docker 环境）
docker compose up -d
```

启动后访问：

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost |
| 后端 API | http://localhost:19999/api |
| 数据库 | localhost:3307 |

### 后台管理

| 账号 | 密码 |
|------|------|
| admin | admin123 |

管理端入口：`http://localhost/admin/login`

### 邮箱配置（可选）

在 `docker-compose.yml` 的 `backend.environment` 中配置：

```yaml
SPRING_MAIL_HOST: smtp.qq.com
SPRING_MAIL_PORT: 587
SPRING_MAIL_USERNAME: your-email@qq.com
SPRING_MAIL_PASSWORD: your-smtp-auth-code
APP_MAIL_FROM: your-email@qq.com
```

## 🐳 Docker 部署

```bash
# 构建并启动所有服务
docker compose up -d

# 查看状态
docker compose ps

# 查看日志
docker compose logs -f

# 停止服务
docker compose down

# 更新后端（重新编译 JAR 后）
cd backend && mvn package -DskipTests && cd ..
docker compose build backend && docker compose up -d backend

# 更新前端（重新构建后）
cd frontend && npm run build && cd ..
docker compose build frontend && docker compose up -d frontend
```

### 容器架构

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Frontend   │     │   Backend    │     │   Database   │
│   Nginx:80   │────▶│  :19999:8080 │────▶│ MariaDB:3307 │
│  Vue 3 SPA   │     │ Spring Boot  │     │              │
└──────────────┘     └──────────────┘     └──────────────┘
```

Nginx 反向代理 `/api/` 请求到后端，前端直接访问 `http://localhost` 即可。

### 📄 Nginx 配置详情

当前部署使用的 Nginx 配置（`frontend/nginx.conf`）：

```nginx
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # 兼容不带斜杠的访问：/blog -> /blog/
    rewrite ^/blog$ /blog/ last;

    # /blog/api 反向代理到后端（优先匹配）
    location /blog/api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # /blog 前端静态文件
    location ^~ /blog/ {
        alias /usr/share/nginx/html/;
        try_files $uri $uri/ /blog/index.html;
        index index.html;
    }

    # 缓存静态资源
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript image/svg+xml;
}
```

**关键配置说明：**
- `rewrite ^/blog$ /blog/ last;` — 兼容 `/blog` 和 `/blog/` 两种访问方式，不触发浏览器跳转
- `location /blog/api/` — 将 API 请求反向代理到 Spring Boot 后端
- `location ^~ /blog/` — SPA 路由回退，所有前端路由由 `index.html` 处理
- 静态资源（图片/CSS/JS）缓存 7 天，减少重复加载
- Gzip 压缩所有文本类型响应，提升传输速度

### 🖥️ WSL 部署配置

#### 当前部署环境

| 组件 | 值 |
|------|------|
| 环境 | Arch Linux (WSL2) |
| WSL 发行版 | Arch Linux with systemd |
| 网络模式 | Standard (非镜像模式) |
| WSL IP | `192.168.31.41` |
| 宿主机 IP | `172.20.50.25` |
| WSL 前端端口 | `80`（标准 HTTP，无需带端口号） |

#### WSL 配置 (`/etc/wsl.conf`)

```ini
[boot]
systemd=true
```

#### Windows 侧配置（可选，解决局域网访问问题）

如果局域网设备（手机/平板）无法通过 Windows IP 访问，需要启用 **WSL 镜像网络模式**：

在 Windows 用户目录创建或编辑 `%USERPROFILE%\.wslconfig`：

```ini
[wsl2]
networkingMode=mirrored
```

然后重启 WSL（在 Windows PowerShell 中执行）：

```powershell
wsl --shutdown
```

### 🔧 生产环境端口映射

当前部署使用以下端口映射（与开发环境 `docker-compose up` 默认不同）：

| 服务 | 容器名 | 内部端口 | 宿主机映射 | 说明 |
|------|--------|----------|------------|------|
| 前端 | `blog-frontend` | 80 | `0.0.0.0:80→80` | 标准 HTTP 端口 |
| 后端 | `blog-backend` | 8080 | 未映射 | 仅容器内网访问 |
| 数据库 | `blog-db` | 3306 | 未映射 | 仅容器内网访问 |

> **注意**：生产部署中后端和数据库不暴露端口到宿主机，仅通过 Docker 内部网络通信。

### 📡 访问地址

| 场景 | 地址 |
|------|------|
| WSL 本机 | `http://192.168.31.41/blog/` |
| 宿主机 (Windows) | `http://172.20.50.25/blog/` |
| 局域网 (同 WiFi) | `http://192.168.31.41/blog/` |
| 管理后台 | `http://<IP>/admin/login` |

### 🔥 防火墙与网络排查

如果局域网设备无法访问：

1. **Windows 防火墙拦截**（最常见原因）

   以管理员身份打开 Windows PowerShell，执行：

   ```powershell
   New-NetFirewallRule -DisplayName "Docker Blog 80" -Direction Inbound -LocalPort 80 -Protocol TCP -Action Allow
   ```

2. **WSL 未启用镜像网络**

   参考上方 Windows 侧配置，启用 `networkingMode=mirrored`。

3. **验证服务状态**

   ```bash
   # WSL 内检查
   docker ps -a --filter name=blog
    ss -tlnp | grep 80
   curl -s -o /dev/null -w "%{http_code}" http://192.168.31.41/
   ```

## 📁 项目结构

```
blog/
├── backend/                # Spring Boot 后端
│   └── src/main/java/com/blog/
│       ├── config/         # 配置类（JWT、CORS、安全、异步）
│       ├── controller/     # 控制器（文章、评论、订阅、排行等）
│       ├── dto/            # 数据传输对象
│       ├── entity/         # 实体类（文章、评论、订阅、排行等）
│       ├── repository/     # JPA 数据访问层
│       └── service/        # 业务逻辑层（含邮件服务、AI 摘要）
│
├── frontend/               # Vue 3 前端
│   └── src/
│       ├── api/            # API 请求封装
│       ├── layouts/        # 布局组件（Admin / Public）
│       ├── views/          # 页面组件
│       │   └── admin/      # 管理后台页面
│       ├── stores/         # Pinia 状态管理
│       ├── components/     # 公共组件（ArticleCard、DarkToggle）
│       ├── router/         # 路由配置
│       └── composables/    # 组合式函数（深色模式等）
│
├── db/                     # 数据库初始化脚本
├── docs/                   # 产品需求 & 设计规范文档
├── docker-compose.yml
└── .gitignore
```

## 🔌 API 概览

### 公开接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 管理员登录 |
| GET | `/api/articles` | 文章列表（置顶优先） |
| GET | `/api/articles/{id}` | 文章详情（自动增加阅读数） |
| GET | `/api/articles/archive` | 文章归档（按年月分组） |
| GET | `/api/articles/search?q=` | 全文搜索 |
| GET | `/api/articles/{id}/comments` | 获取文章评论 |
| POST | `/api/comments` | 提交评论 |
| POST | `/api/subscribe` | 邮件订阅 |
| GET | `/api/external-articles` | 精选阅读列表 |
| GET | `/api/language-rankings` | 大模型排行榜 |
| GET | `/api/rss` | RSS 订阅 |

### 管理端接口（需 JWT 认证）

#### 文章管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/articles` | 文章列表 |
| POST | `/api/admin/articles` | 创建文章 |
| PUT | `/api/admin/articles/{id}` | 更新文章 |
| DELETE | `/api/admin/articles/{id}` | 删除文章 |
| GET | `/api/admin/articles/search?q=&page=&size=` | 文章搜索（后端分页） |
| PUT | `/api/admin/articles/batch/pin` | 批量置顶/取消置顶 |
| DELETE | `/api/admin/articles/batch` | 批量删除 |

#### 评论管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/comments` | 评论列表 |
| GET | `/api/admin/comments/pending-count` | 待审核评论数 |
| PUT | `/api/admin/comments/{id}/approve` | 通过评论 |
| PUT | `/api/admin/comments/{id}/reject` | 拒绝评论 |
| PUT | `/api/admin/comments/{id}/pin` | 置顶/取消置顶评论 |
| DELETE | `/api/admin/comments/{id}` | 删除评论 |

#### 订阅管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/subscribers` | 订阅列表 |
| DELETE | `/api/admin/subscribers/{id}` | 删除订阅 |

#### 精选阅读管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/external-articles` | 精选阅读管理列表 |
| POST | `/api/admin/external-articles` | 添加精选文章 |
| PUT | `/api/admin/external-articles/{id}` | 更新 |
| DELETE | `/api/admin/external-articles/{id}` | 删除 |
| POST | `/api/admin/external-articles/{id}/refetch` | 重新抓取 |

#### 排行榜管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/language-rankings` | 排行榜管理列表 |
| POST | `/api/admin/language-rankings` | 添加排行榜数据 |
| PUT | `/api/admin/language-rankings/{id}` | 更新 |
| DELETE | `/api/admin/language-rankings/{id}` | 删除 |
| POST | `/api/admin/language-rankings/reorder` | 拖拽排序 |

#### 用户管理（仅管理员）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 用户列表 |
| POST | `/api/admin/users` | 创建用户 |
| PUT | `/api/admin/users/{id}` | 更新用户 |
| DELETE | `/api/admin/users/{id}` | 删除用户 |

#### 其他
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard` | 数据看板概览 |
| GET | `/api/auth/me` | 当前用户信息 |

## ⚙️ 环境变量

后端支持通过环境变量配置：

```yaml
# 数据库
SPRING_DATASOURCE_URL: jdbc:mariadb://db:3306/blog
SPRING_DATASOURCE_USERNAME: blog
SPRING_DATASOURCE_PASSWORD: blog123

# JWT
APP_JWT_SECRET: your-jwt-secret-key-base64

# 邮件（可选）
SPRING_MAIL_HOST: smtp.qq.com
SPRING_MAIL_PORT: 587
SPRING_MAIL_USERNAME: your-email@qq.com
SPRING_MAIL_PASSWORD: your-smtp-auth-code
APP_MAIL_FROM: your-email@qq.com

# 博客
APP_BLOG_URL: http://localhost
```

## 📄 开源协议

MIT
