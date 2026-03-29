import request from '@/utils/request'

export function getParkingOverviewStats() {
  return request({
    url: '/parking/overview/stats',
    method: 'get'
  })
}

export function listParkingLots(query) {
  return request({
    url: '/parking/lot/list',
    method: 'get',
    params: query
  })
}

export function listParkingSpaces(query) {
  return request({
    url: '/parking/space/list',
    method: 'get',
    params: query
  })
}
