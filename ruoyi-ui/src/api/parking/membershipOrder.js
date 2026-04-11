import request from '@/utils/request'

export function listMembershipOrders(query) {
  return request({
    url: '/parking/membership/list',
    method: 'get',
    params: query
  })
}

export function getMembershipOrder(membershipOrderId) {
  return request({
    url: '/parking/membership/' + membershipOrderId,
    method: 'get'
  })
}

export function addMembershipOrder(data) {
  return request({
    url: '/parking/membership',
    method: 'post',
    data: data
  })
}

export function updateMembershipOrder(data) {
  return request({
    url: '/parking/membership',
    method: 'put',
    data: data
  })
}

export function delMembershipOrder(orderIds) {
  return request({
    url: '/parking/membership/' + orderIds,
    method: 'delete'
  })
}

export function payMembershipOrder(data) {
  return request({
    url: '/parking/membership/pay',
    method: 'put',
    data: data
  })
}

export function cancelMembershipOrder(data) {
  return request({
    url: '/parking/membership/cancel',
    method: 'put',
    data: data
  })
}
