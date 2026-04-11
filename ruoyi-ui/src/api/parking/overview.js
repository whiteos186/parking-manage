import request from '@/utils/request'

export function getParkingOverviewStats() {
  return request({
    url: '/parking/overview/stats',
    method: 'get'
  })
}

export function getParkingRecentOrders(limit = 5) {
  return request({
    url: '/parking/overview/recent-orders',
    method: 'get',
    params: { limit }
  })
}
