export function formatPrice(value) {
  const num = Number(value)
  if (Number.isNaN(num)) {
    return '0.00'
  }
  return num.toFixed(2)
}

export function findLabel(options = [], value, fallback = '-') {
  const item = options.find(it => String(it.value) === String(value))
  return item ? item.label : fallback
}

export function findTagType(options = [], value, fallback = 'info') {
  const item = options.find(it => String(it.value) === String(value))
  if (!item) {
    return fallback
  }
  if (item.raw && item.raw.listClass !== undefined) {
    return item.raw.listClass === 'primary' ? '' : (item.raw.listClass || fallback)
  }
  return item.tagType || fallback
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

export function findCustomerLabel(options = [], customerId, fallback = '-') {
  const item = options.find(it => String(it.customerId) === String(customerId))
  return item ? formatCustomerOption(item) : fallback
}
