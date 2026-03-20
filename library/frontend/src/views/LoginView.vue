<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../api/auth'
import { setAuth } from '../store/auth'

const router = useRouter()
const route = useRoute()

const form = reactive({
  phoneNumber: '0912345678',
  password: '0912345678',
})

const loading = ref(false)
const message = ref('')

function mapLoginErrorToZh(error) {
  const raw = error?.message || ''
  const lower = raw.toLowerCase()

  if (!raw) {
    return '登入失敗，請稍後再試。'
  }

  if (lower.includes('network') || lower.includes('timeout')) {
    return '登入失敗，無法連線伺服器，請確認後端服務是否啟動。'
  }

  if (
    lower.includes('invalid') ||
    lower.includes('unauthorized') ||
    lower.includes('401') ||
    lower.includes('password') ||
    lower.includes('credential') ||
    lower.includes('帳號') ||
    lower.includes('密碼')
  ) {
    return '登入失敗，手機號碼或密碼錯誤。'
  }

  return `登入失敗：${raw}`
}

async function submitLogin() {
  loading.value = true
  message.value = ''

  try {
    const response = await login({ ...form })
    setAuth({
      token: response.token,
      phoneNumber: response.phoneNumber,
      userId: response.userId,
    })

    router.push(route.query.redirect || '/books')
  } catch (error) {
    message.value = mapLoginErrorToZh(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="panel card-entry">
    <h2>歡迎回來</h2>
    <p class="sub">登入後即可借書與還書。</p>

    <form class="stack-form" @submit.prevent="submitLogin">
      <label>
        手機號碼
        <input v-model.trim="form.phoneNumber" required maxlength="10" placeholder="0912345678" />
      </label>

      <label>
        密碼
        <input v-model="form.password" type="password" required placeholder="請輸入密碼" />
      </label>

      <button class="primary-btn" :disabled="loading">
        {{ loading ? '登入中...' : '登入' }}
      </button>

      <p v-if="message" class="feedback error">{{ message }}</p>
    </form>
  </section>
</template>
