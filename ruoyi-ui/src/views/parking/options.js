// Shared option constants for the parking module.
// Keep enum/label/tag-type definitions in one place so overview, lot and space pages
// stay in sync. If parking dictionaries are later seeded into sys_dict_data, swap
// these arrays for the dict-based component pattern used by other RuoYi modules.

export const LOT_STATUS_OPTIONS = [
  { label: '正常', value: '0', tagType: 'success' },
  { label: '停用', value: '1', tagType: 'info' }
]

export const SPACE_STATUS_OPTIONS = [
  { label: '空闲', value: '0', tagType: 'success' },
  { label: '占用', value: '1', tagType: 'danger' },
  { label: '停用', value: '2', tagType: 'info' },
  { label: '锁定', value: '3', tagType: 'warning' }
]

export const SPACE_TYPE_OPTIONS = [
  { label: '标准车位', value: '1' },
  { label: '充电车位', value: '2' },
  { label: '无障碍车位', value: '3' },
  { label: 'VIP车位', value: '4' }
]

export const VEHICLE_TYPE_OPTIONS = [
  { label: '小型车', value: '1', tagType: 'info' },
  { label: 'SUV', value: '2', tagType: 'primary' },
  { label: '新能源', value: '3', tagType: 'success' },
  { label: '其他', value: '4', tagType: 'info' }
]

export const VEHICLE_STATUS_OPTIONS = [
  { label: '启用', value: '0', tagType: 'success' },
  { label: '停用', value: '1', tagType: 'info' }
]

export const VEHICLE_DEFAULT_OPTIONS = [
  { label: '否', value: '0', tagType: 'info' },
  { label: '默认', value: '1', tagType: 'warning' }
]

export const TEMP_ORDER_PAY_STATUS_OPTIONS = [
  { label: '未支付', value: '0', tagType: 'info' },
  { label: '已支付', value: '1', tagType: 'success' },
  { label: '已退款', value: '2', tagType: 'warning' },
  { label: '已关闭', value: '3', tagType: 'info' }
]

export const TEMP_ORDER_BIZ_STATUS_OPTIONS = [
  { label: '停车中', value: '0', tagType: 'primary' },
  { label: '待支付', value: '1', tagType: 'warning' },
  { label: '已完成', value: '2', tagType: 'success' },
  { label: '已取消', value: '3', tagType: 'info' }
]

export const MONTHLY_ORDER_PAY_STATUS_OPTIONS = [
  { label: '未支付', value: '0', tagType: 'info' },
  { label: '已支付', value: '1', tagType: 'success' },
  { label: '已退款', value: '2', tagType: 'warning' },
  { label: '已关闭', value: '3', tagType: 'info' }
]

export const MONTHLY_ORDER_BIZ_STATUS_OPTIONS = [
  { label: '待生效', value: '0', tagType: 'info' },
  { label: '生效中', value: '1', tagType: 'success' },
  { label: '已过期', value: '2', tagType: 'warning' },
  { label: '已取消', value: '3', tagType: 'info' }
]

export const MEMBERSHIP_TYPE_OPTIONS = [
  { label: '银卡', value: '1', tagType: 'info' },
  { label: '金卡', value: '2', tagType: 'warning' },
  { label: '白金', value: '3', tagType: 'danger' }
]

export const MEMBERSHIP_PAY_STATUS_OPTIONS = [
  { label: '未支付', value: '0', tagType: 'info' },
  { label: '已支付', value: '1', tagType: 'success' },
  { label: '已退款', value: '2', tagType: 'warning' },
  { label: '已关闭', value: '3', tagType: 'danger' }
]

export const MEMBERSHIP_BIZ_STATUS_OPTIONS = [
  { label: '待生效', value: '0', tagType: 'info' },
  { label: '生效中', value: '1', tagType: 'success' },
  { label: '已过期', value: '2', tagType: 'warning' },
  { label: '已取消', value: '3', tagType: 'danger' }
]

export const PAYMENT_BIZ_TYPE_OPTIONS = [
  { label: '会员', value: '1', tagType: 'info' },
  { label: '月卡', value: '2', tagType: 'warning' },
  { label: '临停', value: '3', tagType: 'primary' }
]

export const PAYMENT_CHANNEL_OPTIONS = [
  { label: '微信', value: '1', tagType: 'success' },
  { label: '支付宝', value: '2', tagType: 'primary' },
  { label: '现金', value: '3', tagType: 'info' },
  { label: '其他', value: '4', tagType: 'info' }
]

export const PAYMENT_STATUS_OPTIONS = [
  { label: '处理中', value: '0', tagType: 'info' },
  { label: '成功', value: '1', tagType: 'success' },
  { label: '失败', value: '2', tagType: 'danger' },
  { label: '已退款', value: '3', tagType: 'warning' }
]

export const LOT_ADMIN_STATUS_OPTIONS = [
  { label: '启用', value: '0', tagType: 'success' },
  { label: '停用', value: '1', tagType: 'info' }
]

export function findLabel(options, value, fallback = '-') {
  const item = options.find(it => it.value === value)
  return item ? item.label : fallback
}

export function findTagType(options, value, fallback = 'info') {
  const item = options.find(it => it.value === value)
  return item && item.tagType ? item.tagType : fallback
}

export function formatVehicleOption(item) {
  if (!item) {
    return '-'
  }
  const parts = [item.plateNo || '未录入车牌']
  if (item.brandName) {
    parts.push(item.brandName)
  }
  if (item.vehicleColor) {
    parts.push(item.vehicleColor)
  }
  if (item.isDefault === '1') {
    parts.push('默认车辆')
  }
  return parts.join(' / ')
}

export const CUSTOMER_TYPE_OPTIONS = [
  { label: '业主车主', value: '1', tagType: 'primary' },
  { label: '会员客户', value: '2', tagType: 'success' },
  { label: '企业客户', value: '3', tagType: 'warning' },
  { label: '访客客户', value: '4', tagType: 'info' }
]

export const CUSTOMER_STATUS_OPTIONS = [
  { label: '启用', value: '0', tagType: 'success' },
  { label: '停用', value: '1', tagType: 'info' }
]

export function formatCustomerOption(item) {
  if (!item) {
    return '-'
  }
  const parts = [item.customerName || '未命名客户']
  if (item.customerCode) {
    parts.push(item.customerCode)
  }
  if (item.mobile) {
    parts.push(item.mobile)
  }
  return parts.join(' / ')
}

export function findCustomerLabel(options, customerId, fallback = '-') {
  const item = options.find(it => String(it.customerId) === String(customerId))
  return item ? formatCustomerOption(item) : fallback
}
