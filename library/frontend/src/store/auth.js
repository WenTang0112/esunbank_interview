import { reactive } from 'vue'

const TOKEN_KEY = 'library_token'
const PHONE_KEY = 'library_phone'
const USER_ID_KEY = 'library_user_id'

export const authState = reactive({
  token: localStorage.getItem(TOKEN_KEY) || '',
  phoneNumber: localStorage.getItem(PHONE_KEY) || '',
  userId: localStorage.getItem(USER_ID_KEY) || '',
})

export function setAuth(payload) {
  authState.token = payload.token || ''
  authState.phoneNumber = payload.phoneNumber || ''
  authState.userId = payload.userId ? String(payload.userId) : ''

  localStorage.setItem(TOKEN_KEY, authState.token)
  localStorage.setItem(PHONE_KEY, authState.phoneNumber)
  localStorage.setItem(USER_ID_KEY, authState.userId)
}

export function clearAuth() {
  authState.token = ''
  authState.phoneNumber = ''
  authState.userId = ''

  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(PHONE_KEY)
  localStorage.removeItem(USER_ID_KEY)
}
