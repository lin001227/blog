import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomePage.vue'),
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('../views/ArticleDetail.vue'),
  },
  {
    path: '/archive',
    name: 'Archive',
    component: () => import('../views/Archive.vue'),
  },
  {
    path: '/admin/login',
    name: 'Login',
    component: () => import('../views/admin/Login.vue'),
  },
  {
    path: '/admin',
    name: 'Dashboard',
    component: () => import('../views/admin/Dashboard.vue'),
    meta: { requireAuth: true },
  },
  {
    path: '/admin/articles',
    name: 'ArticleList',
    component: () => import('../views/admin/ArticleList.vue'),
    meta: { requireAuth: true },
  },
  {
    path: '/admin/articles/new',
    name: 'NewArticle',
    component: () => import('../views/admin/ArticleEditor.vue'),
    meta: { requireAuth: true },
  },
  {
    path: '/admin/articles/:id/edit',
    name: 'EditArticle',
    component: () => import('../views/admin/ArticleEditor.vue'),
    meta: { requireAuth: true },
  },
  {
    path: '/admin/users',
    name: 'UserList',
    component: () => import('../views/admin/UserList.vue'),
    meta: { requireAuth: true, requireAdmin: true },
  },
  {
    path: '/admin/users/new',
    name: 'NewUser',
    component: () => import('../views/admin/UserEditor.vue'),
    meta: { requireAuth: true, requireAdmin: true },
  },
  {
    path: '/admin/users/:id/edit',
    name: 'EditUser',
    component: () => import('../views/admin/UserEditor.vue'),
    meta: { requireAuth: true, requireAdmin: true },
  },
  {
    path: '/admin/comments',
    name: 'CommentList',
    component: () => import('../views/admin/CommentList.vue'),
    meta: { requireAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('blog-user') || 'null')

  if (to.meta.requireAuth && !token) {
    next('/admin/login')
  } else if (to.meta.requireAdmin && (!user || user.role !== 'ADMIN')) {
    next('/admin')
  } else {
    next()
  }
})

export default router
