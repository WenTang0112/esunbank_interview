<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authState, clearAuth } from './store/auth'

const route = useRoute()
const router = useRouter()

const isAuthed = computed(() => Boolean(authState.token))
const showAuthPages = computed(() => route.name === 'login' || route.name === 'register')

function logout() {
  clearAuth()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="app-shell">
    <div class="ambient ambient-a" />
    <div class="ambient ambient-b" />

    <header class="topbar">
      <div>
        <p class="eyebrow">玉山金控後端面試實作</p>
        <h1>圖書借閱管理台</h1>
      </div>

      <nav class="menu">
        <RouterLink to="/books">館藏清單</RouterLink>
        <RouterLink to="/my-borrows">我的借閱</RouterLink>
        <RouterLink v-if="!isAuthed" to="/login">登入</RouterLink>
        <RouterLink v-if="!isAuthed" to="/register">註冊</RouterLink>
        <button v-if="isAuthed" class="ghost-btn" @click="logout">登出</button>
      </nav>
    </header>

    <main :class="['page-wrap', { compact: showAuthPages }]">
      <RouterView />
    </main>
  </div>
</template>
