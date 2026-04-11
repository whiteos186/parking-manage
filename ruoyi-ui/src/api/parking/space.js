import request from '@/utils/request'

export function listParkingSpaces(query) {
  return request({
    url: '/parking/space/list',
    method: 'get',
    params: query
  })
}

export function getParkingSpace(spaceId) {
  return request({
    url: '/parking/space/' + spaceId,
    method: 'get'
  })
}

export function addParkingSpace(data) {
  return request({
    url: '/parking/space',
    method: 'post',
    data: data
  })
}

export function updateParkingSpace(data) {
  return request({
    url: '/parking/space',
    method: 'put',
    data: data
  })
}

export function delParkingSpace(spaceId) {
  return request({
    url: '/parking/space/' + spaceId,
    method: 'delete'
  })
}
