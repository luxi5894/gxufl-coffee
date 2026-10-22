export const ORDER_STATUS = {
  0: { label: '待制作', color: 'amber', action: '开始制作', next: 1 },
  1: { label: '制作中', color: 'steel', action: '制作完成', next: 2 },
  2: { label: '待取餐', color: 'sage', action: '完成取餐', next: 3 },
  3: { label: '已完成', color: 'stone', action: '', next: null },
  4: { label: '已取消', color: 'wine', action: '', next: null }
}

export const formatMoney = (value) => {
  const num = Number(value || 0)
  return num.toFixed(2)
}

export const formatCompactMoney = (value) => {
  const num = Number(value || 0)
  if (num >= 10000) return `${(num / 10000).toFixed(1)}万`
  return num.toFixed(0)
}

const PLACEHOLDER =
  'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22120%22 height=%22120%22%3E%3Crect width=%22120%22 height=%22120%22 fill=%22%23191412%22/%3E%3Ctext x=%2260%22 y=%2268%22 fill=%22%23544c45%22 font-size=%2212%22 text-anchor=%22middle%22%3ECOFFEE%3C/text%3E%3C/svg%3E'

export const assetUrl = (path) => {
  if (!path) return PLACEHOLDER
  if (/^https?:\/\//i.test(path)) return path
  return `/api${path.startsWith('/') ? '' : '/'}${path}`
}

export const maskPhone = (phone) => {
  if (!phone || phone.length !== 11) return phone || ''
  return `${phone.slice(0, 3)}****${phone.slice(7)}`
}

export const shortDate = (value) => {
  if (!value) return ''
  return String(value).slice(5, 16)
}

export const initial = (name) => (name ? String(name).slice(0, 1) : '·')
