import client from './client'

export const listDevices = (fieldId) =>
  client.get(`/irrigation/fields/${fieldId}/devices`).then((res) => res.data)

export const sendCommand = (deviceId, activate, durationMinutes) =>
  client.post(`/irrigation/devices/${deviceId}/command`, { activate, durationMinutes }).then((res) => res.data)

export const listEvents = (deviceId) =>
  client.get(`/irrigation/devices/${deviceId}/events`).then((res) => res.data)
