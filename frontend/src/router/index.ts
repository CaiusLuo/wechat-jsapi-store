import { createRouter, createWebHistory } from 'vue-router'
import { getAdminToken } from '@/api/admin'

const appTitle = import.meta.env.VITE_APP_TITLE || '示例教辅资料订购系统'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/h5',
    },
    {
      path: '/h5',
      name: 'h5-home',
      component: () => import('@/views/h5/ProfileHome.vue'),
      meta: {
        title: '购书主页',
        fullTitle: '示例教辅资料订购系统 - 微信 JSAPI 商城',
      },
    },
    {
      path: '/h5/books/:id',
      name: 'h5-book-detail',
      component: () => import('@/views/h5/BookDetail.vue'),
      meta: { title: '书籍详情' },
    },
    {
      path: '/h5/orders',
      name: 'h5-orders',
      component: () => import('@/views/h5/OrderList.vue'),
      meta: { title: '我的订单' },
    },
    {
      path: '/h5/order/create',
      name: 'h5-create-order',
      component: () => import('@/views/h5/CreateOrder.vue'),
      meta: { title: '填写订单' },
    },
    {
      path: '/h5/pay/:orderNo',
      name: 'h5-pay',
      component: () => import('@/views/h5/Pay.vue'),
      meta: { title: '订单支付' },
    },
    {
      path: '/h5/order/:orderNo',
      name: 'h5-order-detail',
      component: () => import('@/views/h5/OrderDetail.vue'),
      meta: { title: '订单详情' },
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('@/views/admin/Login.vue'),
      meta: { title: '后台登录' },
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/Layout.vue'),
      redirect: '/admin/dashboard',
      meta: { requiresAdmin: true },
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: 'Dashboard', requiresAdmin: true },
        },
        {
          path: 'books',
          name: 'admin-books',
          component: () => import('@/views/admin/BookManage.vue'),
          meta: { title: '书籍管理', requiresAdmin: true },
        },
        {
          path: 'config',
          name: 'admin-h5-config',
          component: () => import('@/views/admin/H5ConfigManage.vue'),
          meta: { title: 'H5 配置', requiresAdmin: true },
        },
        {
          path: 'h5-config',
          redirect: '/admin/config',
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('@/views/admin/OrderList.vue'),
          meta: { title: '订单管理', requiresAdmin: true },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const fullTitle = to.meta.fullTitle
  document.title =
    typeof fullTitle === 'string'
      ? fullTitle
      : `${String(to.meta.title || appTitle)} - ${appTitle}`

  if (to.meta.requiresAdmin && !getAdminToken()) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
