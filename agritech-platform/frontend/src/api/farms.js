import client from './client'

export const listFarms = () => client.get('/farms').then((res) => res.data)
export const createFarm = (payload) => client.post('/farms', payload).then((res) => res.data)
export const listFields = (farmId) => client.get(`/farms/${farmId}/fields`).then((res) => res.data)
export const createField = (farmId, payload) =>
  client.post(`/farms/${farmId}/fields`, payload).then((res) => res.data)
