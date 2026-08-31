import client from './client'

export const login = (email, password) =>
  client.post('/auth/login', { email, password }).then((res) => res.data)

export const register = (payload) =>
  client.post('/auth/register', payload).then((res) => res.data)
