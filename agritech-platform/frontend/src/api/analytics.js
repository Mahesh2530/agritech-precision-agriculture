import client from './client'

export const getFieldSummary = (fieldId, days = 14) =>
  client.get(`/analytics/fields/${fieldId}/summary`, { params: { days } }).then((res) => res.data)
