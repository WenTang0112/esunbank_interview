import http from './http'

export async function register(payload) {
  const { data } = await http.post('/users/register', payload)
  return data
}

export async function login(payload) {
  const { data } = await http.post('/users/login', payload)
  return data
}
