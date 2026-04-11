import request from '@/utils/request'

export function listVehicles(query) {
  return request({
    url: '/parking/vehicle/list',
    method: 'get',
    params: query
  })
}

export function listVehicleOptions(query) {
  return request({
    url: '/parking/vehicle/options',
    method: 'get',
    params: query
  })
}

export function getVehicle(vehicleId) {
  return request({
    url: '/parking/vehicle/' + vehicleId,
    method: 'get'
  })
}

export function addVehicle(data) {
  return request({
    url: '/parking/vehicle',
    method: 'post',
    data: data
  })
}

export function updateVehicle(data) {
  return request({
    url: '/parking/vehicle',
    method: 'put',
    data: data
  })
}

export function delVehicle(vehicleIds) {
  return request({
    url: '/parking/vehicle/' + vehicleIds,
    method: 'delete'
  })
}

export function setDefaultVehicle(vehicleId) {
  return request({
    url: '/parking/vehicle/default/' + vehicleId,
    method: 'put'
  })
}
