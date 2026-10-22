const { request, isLoggedIn } = require('../../utils/request')
const { mapCartItem, money } = require('../../utils/format')

const app = getApp()

Page({
  data: {
    loggedIn: false,
    loading: false,
    items: [],
    allChecked: false,
    selectedCount: 0,
    totalCount: 0,
    totalText: '0.00'
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar) {
      tabBar.setData({ selected: 2 })
      tabBar.refresh()
    }
    this.setData({ loggedIn: isLoggedIn() })
    if (isLoggedIn()) {
      this.loadCart()
    } else {
      this.setData({ items: [], allChecked: false, selectedCount: 0, totalCount: 0, totalText: '0.00' })
    }
  },

  async loadCart() {
    this.setData({ loading: true })
    try {
      const raw = await request({ url: '/cart', silent: true })
      const items = (raw || []).map(mapCartItem)
      this.apply(items)
    } catch (e) {
      this.setData({ items: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  apply(items) {
    let totalCount = 0
    let selectedCount = 0
    let total = 0
    let validCount = 0
    let checkedCount = 0
    items.forEach((item) => {
      totalCount += item.quantity
      if (item.productStatus === 1) {
        validCount += 1
        if (item.checked) {
          checkedCount += 1
          selectedCount += item.quantity
          total += Number(item.price) * item.quantity
        }
      }
    })
    this.setData({
      items,
      totalCount,
      selectedCount,
      totalText: money(total, true),
      allChecked: validCount > 0 && checkedCount === validCount
    })
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar) tabBar.refresh()
  },

  toggleItem(e) {
    const index = Number(e.currentTarget.dataset.index)
    const item = this.data.items[index]
    if (item.productStatus !== 1) return
    const items = this.data.items.slice()
    items[index] = { ...item, checked: !item.checked }
    this.apply(items)
  },

  toggleAll() {
    const next = !this.data.allChecked
    const items = this.data.items.map((item) => ({
      ...item,
      checked: item.productStatus === 1 ? next : false
    }))
    this.apply(items)
  },

  changeQty(e) {
    const index = Number(e.currentTarget.dataset.index)
    const delta = Number(e.currentTarget.dataset.delta)
    const item = this.data.items[index]
    const nextQty = item.quantity + delta
    if (nextQty < 1) return
    if (item.stock && nextQty > Math.min(99, item.stock)) {
      wx.showToast({ title: '库存不足', icon: 'none' })
      return
    }
    const items = this.data.items.slice()
    items[index] = { ...item, quantity: nextQty }
    this.apply(items)
    this.queueUpdate(item.id, nextQty)
  },

  queueUpdate(id, quantity) {
    this.timers = this.timers || {}
    clearTimeout(this.timers[id])
    this.timers[id] = setTimeout(async () => {
      try {
        await request({ url: `/cart/${id}`, method: 'PUT', data: { quantity }, silent: true })
      } catch (e) {
        this.loadCart()
      }
    }, 260)
  },

  removeItem(e) {
    const id = Number(e.currentTarget.dataset.id)
    wx.showModal({
      title: '移除商品',
      content: '确定把这杯咖啡从购物车移除吗？',
      confirmText: '移除',
      confirmColor: '#C8A97E',
      success: async (res) => {
        if (!res.confirm) return
        await request({ url: `/cart/${id}`, method: 'DELETE' })
        wx.showToast({ title: '已移除', icon: 'none' })
        this.loadCart()
      }
    })
  },

  clearCart() {
    wx.showModal({
      title: '清空购物车',
      content: '将移除购物车中的全部商品。',
      confirmText: '清空',
      confirmColor: '#C8A97E',
      success: async (res) => {
        if (!res.confirm) return
        await request({ url: '/cart', method: 'DELETE' })
        wx.showToast({ title: '已清空', icon: 'none' })
        this.loadCart()
      }
    })
  },

  checkout() {
    const selected = this.data.items.filter((item) => item.checked && item.productStatus === 1)
    if (!selected.length) {
      wx.showToast({ title: '请选择要结算的商品', icon: 'none' })
      return
    }
    app.globalData.checkout = {
      cartItemIds: selected.map((item) => item.id),
      preview: selected.map((item) => ({
        productName: item.productName,
        productImage: item.productImage,
        imageUrl: item.imageUrl,
        specName: item.specName,
        price: item.priceText,
        quantity: item.quantity
      }))
    }
    wx.navigateTo({ url: '/pages/checkout/checkout' })
  },

  goMenu() {
    wx.switchTab({ url: '/pages/menu/menu' })
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  }
})
