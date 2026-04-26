import request from '@/utils/request'

export function getParkingOverviewStats() {
  return request({
    url: '/parking/overview/stats',
    method: 'get'
  })
}

export function getParkingTopLots(limit = 5) {
  return request({
    url: '/parking/overview/top-lots',
    method: 'get',
    params: { limit }
  })
}

export function getParkingRecentOrders(limit = 5) {
  return request({
    url: '/parking/overview/recent-orders',
    method: 'get',
    params: { limit }
  })
}
