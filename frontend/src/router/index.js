import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/PublicLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/HomePage.vue'),
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('../views/ArticleDetail.vue'),
      },
      {
        path: 'archive',
        name: 'Archive',
        component: () => import('../views/Archive.vue'),
      },
      {
        path: 'readings',
        name: 'ExternalReadings',
        component: () => import('../views/ExternalReadings.vue'),
      },
    ],
  },
  {
    path: '/admin/login',
    name: 'Login',
    component: () => import('../views/admin/Login.vue'),
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requireAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/admin/Dashboard.vue'),
      },
      {
        path: 'articles',
        name: 'ArticleList',
        component: () => import('../views/admin/ArticleList.vue'),
      },
      {
        path: 'articles/new',
        name: 'NewArticle',
        component: () => import('../views/admin/ArticleEditor.vue'),
      },
      {
        path: 'articles/:id/edit',
        name: 'EditArticle',
        component: () => import('../views/admin/ArticleEditor.vue'),
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('../views/admin/UserList.vue'),
        meta: { requireAdmin: true },
      },
      {
        path: 'users/new',
        name: 'NewUser',
        component: () => import('../views/admin/UserEditor.vue'),
        meta: { requireAdmin: true },
      },
      {
        path: 'users/:id/edit',
        name: 'EditUser',
        component: () => import('../views/admin/UserEditor.vue'),
        meta: { requireAdmin: true },
      },
      {
        path: 'comments',
        name: 'CommentList',
        component: () => import('../views/admin/CommentList.vue'),
      },
      {
        path: 'external-articles',
        name: 'ExternalArticleManager',
        component: () => import('../views/admin/ExternalArticleManager.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('blog-user') || 'null')

  // Check if any matched route requires auth
  const requiresAuth = to.matched.some(r => r.meta.requireAuth)
  const requiresAdmin = to.matched.some(r => r.meta.requireAdmin)

  if (requiresAuth && !token) {
    next('/admin/login')
  } else if (requiresAdmin && (!user || user.role !== 'ADMIN')) {
    next('/admin')
  } else {
    next()
  }
})

export default router
