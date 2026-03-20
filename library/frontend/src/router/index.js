import { createRouter, createWebHistory } from 'vue-router'
import { authState } from '../store/auth'
import RegisterView from '../views/RegisterView.vue'
import LoginView from '../views/LoginView.vue'
import BooksView from '../views/BooksView.vue'
import MyBorrowsView from '../views/MyBorrowsView.vue'

const routes = [
  { path: '/', redirect: '/books' },
  { path: '/register', name: 'register', component: RegisterView },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/books', name: 'books', component: BooksView, meta: { requiresAuth: true } },
  { path: '/my-borrows', name: 'my-borrows', component: MyBorrowsView, meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !authState.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if ((to.name === 'login' || to.name === 'register') && authState.token) {
    return { name: 'books' }
  }

  return true
})

export default router
