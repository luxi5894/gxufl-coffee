const { request, isLoggedIn } = require('../../utils/request')
const { mapOrder } = require('../../utils/format')

const app = getApp()

Page({
  data: {
    loggedIn: false,
    tabs: [
      { label: '全部', value: 'all' },
      { label: '待制作', value: '0' },
      { label: '制作中', value: '1' },
      { label: '待取餐', value: '2' },
      { label: '已完成', value: '3' },
      { label: '已取消', value: '4' }
    ],
    activeStatus: 'all',
    orders: [],
    page: 1,
    pageSize: 10,
    hasMore: false,
    loading: false,
    loadingMore: false,
    highlight: null
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar) {
      tabBar.setData({ selected: 3 })
      tabBar.refresh()
    }
    const loggedIn = isLoggedIn()
    this.setData({ loggedIn })
    if (!loggedIn) {
      this.setData({ orders: [] })
      return
    }
    const last = app.globalData.lastOrder
    if (last) {
      this.setData({ highlight: { pickupCode: last.pickupCode, orderNo: last.orderNo } })
      app.globalData.lastOrder = null
    }
    this.loadOrders(true)
  },

  onPullDownRefresh() {
    if (!isLoggedIn()) {
      wx.stopPullDownRefresh()
      return
    }
    this.loadOrders(true).finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadOrders(false)
    }
  },

  async loadOrders(reset) {
    if (!reset && this.data.loadingMore) return
    const page = reset ? 1 : this.data.page + 1
    this.setData({
      loading: reset,
      loadingMore: !reset,
      ...(reset ? { page: 1 } : {})
    })
    const params = [`page=${page}`, `pageSize=${this.data.pageSize}`]
    if (this.data.activeStatus !== 'all') {
      params.push(`status=${this.data.activeStatus}`)
    }
    try {
      const data = await request({ url: `/orders?${params.join('&')}`, silent: true })
      const mapped = (data.records || []).map(mapOrder)
      const merged = reset ? mapped : this.data.orders.concat(mapped)
      this.setData({
        orders: merged,
        page,
        hasMore: (data.total || 0) > merged.length,
        loading: false,
        loadingMore: false
      })
    } catch (e) {
      this.setData({ loading: false, loadingMore: false })
    }
  },

  switchStatus(e) {
    const value = e.currentTarget.dataset.value
    if (value === this.data.activeStatus) return
    this.setData({ activeStatus: value })
    this.loadOrders(true)
  },

  cancelOrder(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '取消订单',
      content: '确定取消这杯咖啡吗？库存会自动回补。',
      confirmText: '取消订单',
      confirmColor: '#C8A97E',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request({ url: `/orders/${id}/cancel`, method: 'POST' })
          wx.showToast({ title: '订单已取消', icon: 'none' })
          this.loadOrders(true)
        } catch (err) {}
      }
    })
  },

  confirmOrder(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认取餐',
      content: '请确认你已经拿到咖啡，订单将标记为已完成。',
      confirmText: '已取餐',
      confirmColor: '#C8A97E',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request({ url: `/orders/${id}/confirm`, method: 'POST' })
          wx.showToast({ title: '取餐完成，祝好胃口', icon: 'none' })
          this.loadOrders(true)
        } catch (err) {}
      }
    })
  },

  reviewOrder(e) {
    const productId = e.currentTarget.dataset.product
    wx.navigateTo({ url: `/pages/review/review?productId=${productId}` })
  },

  dismissHighlight() {
    this.setData({ highlight: null })
  },

  goMenu() {
    wx.switchTab({ url: '/pages/menu/menu' })
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  }
})
