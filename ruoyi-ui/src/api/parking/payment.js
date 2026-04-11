import request from '@/utils/request'

export function listPayments(query) {
  return request({
    url: '/parking/payment/list',
    method: 'get',
    params: query
  })
}

export function getPayment(paymentId) {
  return request({
    url: '/parking/payment/' + paymentId,
    method: 'get'
  })
}

export function addPayment(data) {
  return request({
    url: '/parking/payment',
    method: 'post',
    data: data
  })
}

export function updatePayment(data) {
  return request({
    url: '/parking/payment',
    method: 'put',
    data: data
  })
}

export function delPayment(paymentIds) {
  return request({
    url: '/parking/payment/' + paymentIds,
    method: 'delete'
  })
}

export function refundPayment(data) {
  return request({
    url: '/parking/payment/refund',
    method: 'put',
    data: data
  })
}
