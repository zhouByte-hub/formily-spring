import { createRouter, createWebHistory } from 'vue-router'

// 导入页面组件
const LowCodeHome = () => import('../views/LowCodeHome.vue')
const SchemaList = () => import('../views/SchemaList.vue')

// 路由配置
const routes = [
  {
    path: '/',
    name: 'LowCodeHome',
    component: LowCodeHome
  },
  {
    path: '/schemas',
    name: 'SchemaList',
    component: SchemaList
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
