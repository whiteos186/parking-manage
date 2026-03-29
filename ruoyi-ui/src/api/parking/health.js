import request from '@/utils/request'

export function getParkingHealth() {
  return request({
    url: '/parking/health',
    method: 'get'
  })
}
