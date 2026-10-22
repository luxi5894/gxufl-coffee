const { resolveImage } = require('./config')

const STATUS = {
  0: { label: '待制作', className: 'pending' },
  1: { label: '制作中', className: 'making' },
  2: { label: '待取餐', className: 'ready' },
  3: { label: '已完成', className: 'done' },
  4: { label: '已取消', className: 'cancelled' }
}

const money = (value, fixed = false) => {
  const num = Number(value || 0)
  if (fixed) return num.toFixed(2)
  return Number.isInteger(num) ? String(num) : num.toFixed(2)
}

const formatTime = (value) => {
  if (!value) return ''
  const text = String(value)
  if (text.length >= 16) return text.slice(5, 16)
  return text
}

const maskPhone = (phone) => {
  if (!phone || phone.length !== 11) return phone || ''
  return `${phone.slice(0, 3)} **** ${phone.slice(7)}`
}

const mapProduct = (product) => {
  const images = (product.images && product.images.length ? product.images : [product.image])
    .filter(Boolean)
    .map(resolveImage)
  const specs = (product.specs || []).map((spec) => ({
    ...spec,
    priceText: money(Number(product.price || 0) + Number(spec.priceDelta || 0))
  }))
  return {
    ...product,
    images,
    imageUrl: images[0] || '',
    priceText: money(product.price),
    originalPriceText: product.originalPrice ? money(product.originalPrice) : '',
    ratingText: Number(product.rating || 0).toFixed(1),
    tags: product.tags || [],
    temperature: product.temperature || [],
    specs,
    hasSpecs: specs.length > 1
  }
}

const mapCartItem = (item) => ({
  ...item,
  imageUrl: resolveImage(item.productImage),
  priceText: money(item.price),
  checked: item.productStatus === 1
})

const mapOrder = (order) => {
  const status = STATUS[order.status] || { label: '未知', className: 'done' }
  const items = (order.items || []).map((item) => ({
    ...item,
    imageUrl: resolveImage(item.productImage),
    priceText: money(item.price)
  }))
  return {
    ...order,
    items,
    statusText: status.label,
    statusClass: status.className,
    totalText: money(order.totalAmount, true),
    createdAtText: formatTime(order.createdAt),
    itemCountText: order.items && order.items.length > 1
      ? `${order.items[0].productName} 等 ${order.items.length} 款`
      : (order.items && order.items[0] ? order.items[0].productName : ''),
    canCancel: order.status === 0,
    canConfirm: order.status === 2,
    canReview: order.status === 3,
    showPickup: order.status === 2 || order.status === 1
  }
}

const mapReview = (review) => ({
  ...review,
  imageUrl: resolveImage(review.productImage),
  avatar: review.nickname ? String(review.nickname).slice(0, 1) : '·',
  ratingText: Number(review.rating || 0).toFixed(0),
  createdAtText: formatTime(review.createdAt)
})

const initial = (name) => (name ? String(name).slice(0, 1) : '·')

module.exports = {
  STATUS,
  money,
  formatTime,
  maskPhone,
  mapProduct,
  mapCartItem,
  mapOrder,
  mapReview,
  initial
}
