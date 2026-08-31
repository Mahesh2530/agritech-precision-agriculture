import client from './client'

export const getFieldHistory = (fieldId, metric, from, to) =>
  client.get(`/fields/${fieldId}/readings`, { params: { metric, from, to } }).then((res) => res.data)

export const getFieldLatest = (fieldId) =>
  client.get(`/fields/${fieldId}/latest`).then((res) => res.data)
