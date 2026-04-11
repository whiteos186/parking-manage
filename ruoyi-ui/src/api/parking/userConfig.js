import request from '@/utils/request'

const CONFIG_KEY = 'quick_actions'

export function getQuickConfig() {
  return request({
    url: '/parking/userConfig',
    method: 'get',
    params: { key: CONFIG_KEY },
    silent: true // 表不存在或接口异常时静默降级到 localStorage，不弹错误提示
  })
}

export function saveQuickConfig(keys) {
  return request({
    url: '/parking/userConfig',
    method: 'put',
    data: { key: CONFIG_KEY, value: JSON.stringify(keys) }
  })
}
