import request from '@/utils/request'

export function listTempOrders(query) {
  return request({
    url: '/parking/temp/list',
    method: 'get',
    params: query
  })
}

export function getTempOrder(id) {
  return request({
    url: '/parking/temp/' + id,
    method: 'get'
  })
}

export function addTempOrder(data) {
  return request({
    url: '/parking/temp',
    method: 'post',
    data: data
  })
}

export function updateTempOrder(data) {
  return request({
    url: '/parking/temp',
    method: 'put',
    data: data
  })
}

export function delTempOrder(ids) {
  return request({
    url: '/parking/temp/' + ids,
    method: 'delete'
  })
}

export function entryTempOrder(tempOrderId) {
  return request({
    url: '/parking/temp/entry/' + tempOrderId,
    method: 'put'
  })
}

export function exitTempOrder(tempOrderId) {
  return request({
    url: '/parking/temp/exit/' + tempOrderId,
    method: 'put'
  })
}

export function settleTempOrder(data) {
  return request({
    url: '/parking/temp/settle',
    method: 'put',
    data: data
  })
}
