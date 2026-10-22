import { createRouter, createWebHistory } from 'vue-router'
import { TOKEN_KEY } from '@/api/http'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '经营概览', subtitle: '今日的咖啡香气与生意脉络' }
      },
      {
        path: 'products',
        name: 'products',
        component: () => import('@/views/Products.vue'),
        meta: { title: '商品管理', subtitle: '维护菜单、规格、库存与推荐位' }
      },
      {
        path: 'categories',
        name: 'categories',
        component: () => import('@/views/Categories.vue'),
        meta: { title: '分类管理', subtitle: '以东南亚六国为脉络组织菜单' }
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/Orders.vue'),
        meta: { title: '订单管理', subtitle: '从下单到取餐的完整动线' }
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', subtitle: '认识每一位到访的咖啡客人' }
      },
      {
        path: 'reviews',
        name: 'reviews',
        component: () => import('@/views/Reviews.vue'),
        meta: { title: '评价管理', subtitle: '倾听反馈，照看品牌口碑' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (to.name !== 'login' && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && token) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
