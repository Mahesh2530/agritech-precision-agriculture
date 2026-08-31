import client from './client'

export const listAlerts = (params) => client.get('/alerts', { params }).then((res) => res.data)
export const acknowledgeAlert = (id) => client.post(`/alerts/${id}/acknowledge`).then((res) => res.data)
