import client from './client'

export const getWindSpeedData = ({ state = 'Andhra Pradesh', district = 'KRISHNA', agency = 'Andhra Pradesh GW', format = 'JSON' } = {}) =>
  client.get('/external/wind-speed', { params: { state, district, agency, format } }).then((res) => res.data)
