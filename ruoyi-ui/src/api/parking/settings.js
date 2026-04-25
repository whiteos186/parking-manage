import request from '@/utils/request'

export function getParkingSettings() {
  return request({
    url: '/parking/settings',
    method: 'get'
  })
}

export function updateParkingSettings(data) {
  return request({
    url: '/parking/settings',
    method: 'put',
    data
  })
}
