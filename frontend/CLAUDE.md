# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev      # Start Vite dev server (port 5173, proxies /api to localhost:8080)
npm run build    # Production build to dist/
npm run preview  # Preview production build
```

No test or linting infrastructure is currently configured.

## Project Overview

Vue 3 SPA blog frontend, part of a `blog/` monorepo (Spring Boot 3 backend + MariaDB). Built with Element Plus, Pinia, Vue Router, and Axios. Deployed via Docker Compose (Nginx serves the SPA, reverse-proxies `/api` to the backend).

## Key Architecture

### File Structure

```
frontend/
├── src/
│   ├── api/          # Axios request wrappers (one file per domain: articles.js, auth.js, comments.js, users.js, dashboard.js, externalArticles.js)
│   │   └── request.js  # Shared Axios instance with JWT interceptor and 401 redirect
│   ├── components/   # Reusable UI components (currently only DarkToggle.vue)
│   ├── composables/  # Vue composables (useDarkMode.js — global singleton for light/dark theme)
│   ├── layouts/      # PublicLayout.vue (blog pages), AdminLayout.vue (admin pages with nav)
│   ├── router/       # Vue Router config (routes with lazy-loaded components, auth guards)
│   ├── stores/       # Pinia stores (index.js, auth.js — JWT token + user state)
│   └── views/        # Page components
│       └── admin/    # Admin pages (Dashboard, ArticleList, ArticleEditor, UserList, UserEditor, CommentList, ExternalArticleManager, Login)
├── index.html        # Root HTML with CSS variables for light/dark theming
├── vite.config.js    # Vite config with /api proxy
├── nginx.conf        # Nginx config for Docker deployment
└── Dockerfile        # Builds nginx:alpine image serving dist/
```

### Routing

- **Public routes** (`PublicLayout`): `/` (Home), `/article/:id` (Detail), `/archive`, `/readings`
- **Admin routes** (`AdminLayout`, auth required): `/admin` (Dashboard), `/admin/articles`, `/admin/articles/new`, `/admin/articles/:id/edit`, `/admin/users`, `/admin/comments`, `/admin/external-articles`
- **Login**: `/admin/login` (standalone, no layout)
- Route guard checks `localStorage` for JWT token. Admin-only routes check `user.role === 'ADMIN'`.

### API Layer

`src/api/request.js` creates an Axios instance with:
- `baseURL: '/api'` (proxied to backend in dev via Vite, in prod via Nginx)
- Request interceptor: attaches `Bearer` JWT token from `localStorage`
- Response interceptor: on 401, clears credentials and redirects to login

Each domain file exports functions that call `request.get/post/put/delete`.

### State Management

Pinia store in `src/stores/auth.js` manages auth state (user, token, isLoggedIn, isAdmin). User info and token are persisted to `localStorage` (keys: `token`, `blog-user`). Login action calls the auth API and persists credentials.

### Theming

Light/dark mode controlled by `.dark` class on `<html>`. CSS variables in `index.html` define all colors (backgrounds, text, borders, shadows, Element Plus overrides). `useDarkMode` composable reads `localStorage('blog-dark-mode')` and `prefers-color-scheme` on mount, exposes `isDark` and `toggleDark()`. Theme transitions are animated via a `theme-transitioning` class.

### Component Patterns

- **Script setup** syntax (`<script setup>`) used throughout
- Element Plus components with `el-` prefix, Chinese locale (`zhCn`)
- Icons from `@element-plus/icons-vue`
- Scoped styles using CSS variables for theming
- Skeleton loading states using `el-skeleton` with custom template structure
- `el-card` used for content containers with hover effects
- All admin views require auth via route guard; admin-only features check `auth.isAdmin`

### Docker Deployment

- `Dockerfile`: copies `dist/` to Nginx and applies `nginx.conf`
- Nginx config: serves static files, reverse-proxies `/api/` to backend at `http://backend:8080/api/`
- Gzip enabled for common text types
- Static assets cached for 7 days with `immutable`

## Backend (for context)

Spring Boot 3 at `blog/backend/`. Package structure: `config/` (JWT, CORS, security), `controller/`, `dto/`, `entity/`, `repository/` (Spring Data JPA), `service/`. Auth uses JWT with custom filter. Database is MariaDB 10.11. See `blog/README.md` for API endpoints.
