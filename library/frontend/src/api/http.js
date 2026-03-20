import axios from 'axios'
import { authState, clearAuth } from '../store/auth'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const http = axios.create({
  baseURL,
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  if (authState.token) {
    config.headers.Authorization = `Bearer ${authState.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      clearAuth()
    }

    const message =
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error?.message ||
      'Request failed'

    return Promise.reject(new Error(message))
  },
)

export default http
