import request from '@/utils/request'

export function listCustomers(query) {
  return request({
    url: '/parking/customer/list',
    method: 'get',
    params: query
  })
}

export function listCustomerOptions() {
  return request({
    url: '/parking/customer/options',
    method: 'get'
  })
}

export function getCustomer(customerId) {
  return request({
    url: '/parking/customer/' + customerId,
    method: 'get'
  })
}

export function addCustomer(data) {
  return request({
    url: '/parking/customer',
    method: 'post',
    data
  })
}

export function updateCustomer(data) {
  return request({
    url: '/parking/customer',
    method: 'put',
    data
  })
}

export function delCustomer(customerIds) {
  return request({
    url: '/parking/customer/' + customerIds,
    method: 'delete'
  })
}
