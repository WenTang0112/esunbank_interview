<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'

const router = useRouter()

const form = reactive({
  phoneNumber: '',
  password: '',
  userName: '',
})

const loading = ref(false)
const message = ref('')
const isError = ref(false)

async function submitRegister() {
  loading.value = true
  message.value = ''

  try {
    const response = await register({ ...form })
    isError.value = false
    message.value = `註冊成功，使用者編號：${response.userId}`
    setTimeout(() => router.push({ name: 'login' }), 900)
  } catch (error) {
    isError.value = true
    message.value = error.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="panel card-entry">
    <h2>建立帳號</h2>
    <p class="sub">請使用手機號碼註冊（09xxxxxxxx）。</p>

    <form class="stack-form" @submit.prevent="submitRegister">
      <label>
        手機號碼
        <input v-model.trim="form.phoneNumber" required maxlength="10" placeholder="0912345678" />
      </label>

      <label>
        使用者名稱
        <input v-model.trim="form.userName" required maxlength="100" placeholder="請輸入顯示名稱" />
      </label>

      <label>
        密碼
        <input v-model="form.password" type="password" required minlength="8" placeholder="至少 8 碼" />
      </label>

      <button class="primary-btn" :disabled="loading">
        {{ loading ? '建立中...' : '註冊' }}
      </button>

      <p v-if="message" :class="['feedback', { error: isError, ok: !isError }]">{{ message }}</p>
    </form>
  </section>
</template>
