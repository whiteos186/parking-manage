import request from '@/utils/request'

export function listLotAdmins(query) {
  return request({
    url: '/parking/lotadmin/list',
    method: 'get',
    params: query
  })
}

export function getLotAdmin(lotAdminId) {
  return request({
    url: '/parking/lotadmin/' + lotAdminId,
    method: 'get'
  })
}

export function addLotAdmin(data) {
  return request({
    url: '/parking/lotadmin',
    method: 'post',
    data: data
  })
}

export function updateLotAdmin(data) {
  return request({
    url: '/parking/lotadmin',
    method: 'put',
    data: data
  })
}

export function delLotAdmin(lotAdminId) {
  return request({
    url: '/parking/lotadmin/' + lotAdminId,
    method: 'delete'
  })
}

export function listLotAdminUserOptions() {
  return request({
    url: '/parking/lotadmin/user-options',
    method: 'get'
  })
}
