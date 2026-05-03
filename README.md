# 📝 个人博客

基于 **Spring Boot 3 + Vue 3 + MariaDB** 的全栈博客系统，Docker 一键部署。

## ✨ 功能

- 📄 文章发布、编辑、删除（Markdown 支持）
- 📝 Markdown 渲染（代码高亮、标题锚点、引用块）
- 📌 置顶文章 + 批量置顶/取消置顶
- 🏷️ 文章分类与标签
- 💬 文章评论（管理端评论管理）
- 🔍 全文搜索（标题 + 内容）
- 📋 文章归档（按年月分组）
- 📡 RSS 订阅支持
- 🔗 Open Graph 社交分享预览
- 📖 文章目录 TOC 导航
- 👤 管理后台（数据看板、用户管理、文章管理、评论管理）
- 🔐 JWT 登录认证
- 🌙 深色模式
- 📱 响应式设计

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | Vue 3 + Element Plus + Pinia + Vue Router |
| **后端** | Java 17 + Spring Boot 3 + Spring Security + JWT |
| **数据库** | MariaDB 10.11 |
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
```

### 容器架构

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Frontend    │     │   Backend    │     │   Database   │
│   Nginx:80    │────▶│  :19999:8080 │────▶│ MariaDB:3307 │
│  Vue 3 SPA   │     │ Spring Boot  │     │              │
└──────────────┘     └──────────────┘     └──────────────┘
```

Nginx 反向代理 `/api/` 请求到后端，前端直接访问 `http://localhost` 即可。

## 📁 项目结构

```
blog/
├── backend/                # Spring Boot 后端
│   ├── src/main/java/
│   │   ├── config/         # 配置类（JWT、CORS、安全）
│   │   ├── controller/     # 控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── entity/         # 实体类
│   │   ├── repository/     # 数据访问层
│   │   └── service/        # 业务逻辑层
│   └── Dockerfile
│
├── frontend/               # Vue 3 前端
│   ├── src/
│   │   ├── api/            # API 请求封装
│   │   ├── layouts/        # 布局组件（AdminLayout / PublicLayout）
│   │   ├── views/          # 页面组件
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── router/         # 路由配置
│   │   └── composables/    # 组合式函数
│   ├── nginx.conf
│   └── Dockerfile
│
├── db/
│   └── init.sql            # 数据库初始化脚本
│
├── docker-compose.yml
└── .gitignore
```

## 🔌 API 概览

### 公开接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 |
| GET | `/api/articles` | 查看文章列表（分页） |
| GET | `/api/articles/{id}` | 查看文章详情 |
| GET | `/api/articles/archive` | 文章归档 |
| GET | `/api/articles/search?keyword=` | 全文搜索 |
| POST | `/api/articles/{id}/comments` | 添加评论 |
| GET | `/api/feed` | RSS 订阅 |

### 管理端接口（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/articles` | 文章列表 |
| POST | `/api/admin/articles` | 创建文章 |
| GET | `/api/admin/articles/search?page=&size=&keyword=&category=&tag=` | 文章搜索（全后端分页） |
| PUT | `/api/admin/articles/{id}` | 更新文章 |
| DELETE | `/api/admin/articles/{id}` | 删除文章 |
| POST | `/api/admin/articles/batch/pin` | 批量置顶 |
| POST | `/api/admin/articles/batch/unpin` | 批量取消置顶 |
| POST | `/api/admin/articles/batch/delete` | 批量删除 |
| GET | `/api/admin/comments` | 评论列表 |
| DELETE | `/api/admin/comments/{id}` | 删除评论 |
| GET | `/api/admin/users` | 用户列表 |
| POST | `/api/admin/users` | 创建用户 |
| PUT | `/api/admin/users/{id}` | 更新用户 |
| DELETE | `/api/admin/users/{id}` | 删除用户 |
| GET | `/api/admin/dashboard` | 数据看板 |

## ⚙️ 环境变量

后端支持通过环境变量配置：

```yaml
SPRING_DATASOURCE_URL: jdbc:mariadb://db:3306/blog
SPRING_DATASOURCE_USERNAME: blog
SPRING_DATASOURCE_PASSWORD: blog123
APP_JWT_SECRET: your-jwt-secret-key
```

## 📄 开源协议

MIT
