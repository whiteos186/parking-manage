import request from '@/utils/request'

export function listMonthlyOrders(query) {
  return request({
    url: '/parking/monthly/list',
    method: 'get',
    params: query
  })
}

export function getMonthlyOrder(monthlyOrderId) {
  return request({
    url: '/parking/monthly/' + monthlyOrderId,
    method: 'get'
  })
}

export function addMonthlyOrder(data) {
  return request({
    url: '/parking/monthly',
    method: 'post',
    data: data
  })
}

export function updateMonthlyOrder(data) {
  return request({
    url: '/parking/monthly',
    method: 'put',
    data: data
  })
}

export function delMonthlyOrder(monthlyOrderIds) {
  return request({
    url: '/parking/monthly/' + monthlyOrderIds,
    method: 'delete'
  })
}

export function payMonthlyOrder(data) {
  return request({
    url: '/parking/monthly/pay',
    method: 'put',
    data: data
  })
}

export function cancelMonthlyOrder(data) {
  return request({
    url: '/parking/monthly/cancel',
    method: 'put',
    data: data
  })
}
