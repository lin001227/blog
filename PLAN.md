# 博客重构实施计划

## 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | Vue 3 (JS) + Vite + Vue Router + Pinia + Element Plus |
| **后端** | Java 17 + Spring Boot 3 + Spring Security + Spring Data JPA |
| **数据库** | MariaDB |
| **鉴权** | JWT + 登录保护 |
| **部署** | Docker Compose |

---

## 环境准备

### 安装清单

| 软件 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 后端运行 |
| Maven | 3.9+ | 后端构建 |
| Node.js | 20 LTS | 前端构建 |
| MariaDB | 10.11+ | 数据存储 |

---

## 项目目录结构

```
/home/web/blog/
├── docker-compose.yml          # 容器编排
├── backend/                    # Spring Boot 后端
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/blog/
│           │   ├── BlogApplication.java
│           │   ├── config/
│           │   │   ├── SecurityConfig.java
│           │   │   ├── CorsConfig.java
│           │   │   └── JwtAuthFilter.java
│           │   ├── entity/
│           │   │   ├── Article.java
│           │   │   └── AdminUser.java
│           │   ├── repository/
│           │   │   ├── ArticleRepository.java
│           │   │   └── AdminUserRepository.java
│           │   ├── service/
│           │   │   ├── ArticleService.java
│           │   │   └── AuthService.java
│           │   ├── controller/
│           │   │   ├── ArticleController.java
│           │   │   └── AuthController.java
│           │   └── dto/
│           │       ├── ArticleRequest.java
│           │       ├── ArticleResponse.java
│           │       ├── LoginRequest.java
│           │       └── LoginResponse.java
│           └── resources/
│               ├── application.yml
│               └── data.sql             # 初始化管理员账号
├── frontend/                   # Vue 3 前端
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/index.js
│       ├── api/
│       │   ├── request.js             # axios 封装
│       │   ├── articles.js
│       │   └── auth.js
│       ├── stores/
│       │   └── auth.js
│       ├── views/
│       │   ├── HomePage.vue           # 博客首页
│       │   ├── ArticleDetail.vue      # 文章详情
│       │   └── admin/
│       │       ├── Login.vue          # 管理员登录
│       │       ├── Dashboard.vue      # 后台首页
│       │       ├── ArticleList.vue    # 文章管理列表
│       │       └── ArticleEditor.vue  # 新建/编辑文章
│       └── components/
│           └── MarkdownEditor.vue     # Markdown 编辑器
└── db/
    └── init.sql                       # 数据库初始化脚本
```

---

## 后端设计

### 数据模型

#### Article 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | 主键 |
| title | VARCHAR(255) NOT NULL | 标题 |
| content | TEXT NOT NULL | 文章内容(Markdown) |
| category | VARCHAR(100) | 分类 |
| tags | VARCHAR(500) | 标签(逗号分隔) |
| pinned | BOOLEAN DEFAULT FALSE | 是否置顶 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### admin_user 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO_INCREMENT) | 主键 |
| username | VARCHAR(50) UNIQUE NOT NULL | 用户名 |
| password | VARCHAR(255) NOT NULL | BCrypt 加密密码 |
| display_name | VARCHAR(100) | 显示名称 |

### API 接口

#### 公开接口（无需登录）

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/auth/login` | 管理员登录，返回 JWT |
| GET | `/api/articles` | 获取公开文章列表(分页) |
| GET | `/api/articles/{id}` | 获取文章详情 |

#### 需登录接口（Header: `Authorization: Bearer <token>`）

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/admin/articles` | 新增文章 |
| PUT | `/api/admin/articles/{id}` | 编辑文章 |
| DELETE | `/api/admin/articles/{id}` | 删除文章 |
| GET | `/api/admin/articles` | 管理后台文章列表(包含置顶、编辑) |

### 鉴权流程

```
登录 → POST /api/auth/login → 返回 JWT Token
                              ↓
后续请求 → Header: Bearer <token> → JwtAuthFilter 验证 → 放行/拒绝
```

---

## 前端设计

### 路由

| 路径 | 页面 | 是否需要登录 |
|------|------|:-----------:|
| `/` | 博客首页 | ❌ |
| `/article/:id` | 文章详情页 | ❌ |
| `/admin/login` | 管理员登录 | ❌ |
| `/admin` | 后台管理首页 | ✅ |
| `/admin/articles` | 文章管理列表 | ✅ |
| `/admin/articles/new` | 新建文章 | ✅ |
| `/admin/articles/:id/edit` | 编辑文章 | ✅ |

### 前端组件树

```
App.vue
├── 公开部分
│   ├── NavBar.vue (导航栏，Element Plus Menu)
│   ├── HomePage.vue
│   │   ├── 置顶文章卡片
│   │   ├── 文章列表 (PostCard.vue)
│   │   └── SideBar.vue (信息/标签/归档)
│   └── ArticleDetail.vue
│       └── MarkdownRenderer (Element Plus 样式)
│
└── 管理后台部分
    ├── Login.vue
    └── AdminLayout.vue
        ├── 侧边栏菜单
        ├── Dashboard.vue
        ├── ArticleList.vue
        │   └── Element Plus Table
        └── ArticleEditor.vue
            └── MarkdownEditor.vue
```

---

## 实施步骤

### 第 1 步：安装环境

安装 Java 17、Maven、Node.js 20、MariaDB

### 第 2 步：初始化数据库

创建 blog 数据库，执行 init.sql 创建表并插入默认管理员

### 第 3 步：搭建后端

1. 创建 Maven 项目 + pom.xml
2. 配置 application.yml（连接 MariaDB）
3. 创建 Entity、Repository、Service、Controller
4. 实现 Spring Security + JWT 鉴权
5. 实现文章 CRUD API
6. 测试 API

### 第 4 步：搭建前端

1. 创建 Vite + Vue 3 项目
2. 配置 Vue Router + Pinia
3. 集成 Element Plus
4. 封装 axios (含 JWT 拦截器)
5. 实现公开页面（首页、文章详情）
6. 实现管理后台（登录、文章管理、编辑器）
7. 对接后端 API

### 第 5 步：Docker 部署

1. 编写 Dockerfile（前端 Nginx，后端 JAR）
2. 编写 docker-compose.yml（含 MariaDB）
3. 构建并启动
4. 替换原有 my-nginx 容器

### 第 6 步：验证

1. 访问首页，查看文章列表
2. 点击文章查看详情
3. 登录管理后台
4. 新建、编辑、删除文章
5. 前端显示更新

---

## 关键代码片段（样例）

### Spring Security 配置

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
      .authorizeHttpRequests(auth -> auth
          .requestMatchers("/api/auth/**", "/api/articles/**").permitAll()
          .requestMatchers("/api/admin/**").authenticated()
      )
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

### Vue 路由守卫

```javascript
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  if (to.meta.requireAuth && !authStore.isLoggedIn) {
    next('/admin/login');
  } else {
    next();
  }
});
```

### axios 拦截器

```javascript
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

---

## 预计改动/新增文件

**后端：** 约 20 个文件
**前端：** 约 20 个文件
**配置：** docker-compose.yml + Dockerfile × 2

---

## 实施方法

采用 **子代理驱动开发**——每个步骤拆分为独立子任务，由子代理并行或串行完成，确保代码质量和一致性。
