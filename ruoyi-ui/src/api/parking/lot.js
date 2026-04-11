import request from '@/utils/request'

export function listParkingLots(query) {
  return request({
    url: '/parking/lot/list',
    method: 'get',
    params: query
  })
}

export function listParkingLotOptions() {
  return request({
    url: '/parking/lot/options',
    method: 'get'
  })
}

export function getParkingLot(lotId) {
  return request({
    url: '/parking/lot/' + lotId,
    method: 'get'
  })
}

export function addParkingLot(data) {
  return request({
    url: '/parking/lot',
    method: 'post',
    data: data
  })
}

export function updateParkingLot(data) {
  return request({
    url: '/parking/lot',
    method: 'put',
    data: data
  })
}

export function delParkingLot(lotId) {
  return request({
    url: '/parking/lot/' + lotId,
    method: 'delete'
  })
}
