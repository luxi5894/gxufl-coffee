const { request, isLoggedIn } = require('../../utils/request')
const { money } = require('../../utils/format')

const app = getApp()

Page({
  data: {
    mode: 'cart',
    cartItemIds: [],
    items: [],
    itemCount: 0,
    totalText: '0.00',
    remark: '',
    submitting: false
  },

  onLoad() {
    if (!isLoggedIn()) {
      wx.navigateBack()
      return
    }
    const cartCheckout = app.globalData.checkout
    const direct = app.globalData.checkoutDirect

    let preview = []
    let mode = 'cart'
    let cartItemIds = []

    if (cartCheckout) {
      preview = cartCheckout.preview || []
      cartItemIds = cartCheckout.cartItemIds || []
      mode = 'cart'
    } else if (direct) {
      preview = direct.preview || []
      mode = 'direct'
    }

    if (!preview.length) {
      wx.showToast({ title: '没有可结算的商品', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 600)
      return
    }

    let total = 0
    let itemCount = 0
    const items = preview.map((item, index) => {
      const line = Number(item.price) * Number(item.quantity)
      total += line
      itemCount += Number(item.quantity)
      return {
        ...item,
        key: `${item.productId || item.productName}-${index}`,
        lineTotal: money(line, true)
      }
    })

    this.setData({
      mode,
      cartItemIds,
      items,
      itemCount,
      totalText: money(total, true)
    })
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value })
  },

  async submit() {
    if (this.data.submitting) return
    this.setData({ submitting: true })
    try {
      const payload = { remark: this.data.remark }
      if (this.data.mode === 'cart') {
        payload.cartItemIds = this.data.cartItemIds
      } else {
        payload.items = this.data.items.map((item) => ({
          productId: item.productId,
          specId: item.specId || null,
          quantity: item.quantity
        }))
      }
      const order = await request({ url: '/orders', method: 'POST', data: payload, loading: true })
      app.globalData.checkout = null
      app.globalData.checkoutDirect = null
      app.globalData.lastOrder = order
      wx.showModal({
        title: '下单成功',
        content: `取餐号 ${order.pickupCode}\n请留意订单的制作进度`,
        showCancel: false,
        confirmText: '查看订单',
        confirmColor: '#C8A97E',
        success: () => {
          wx.switchTab({ url: '/pages/orders/orders' })
        }
      })
    } catch (e) {
      this.setData({ submitting: false })
    }
  }
})
