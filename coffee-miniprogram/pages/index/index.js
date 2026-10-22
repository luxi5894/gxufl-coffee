const { request, isLoggedIn } = require('../../utils/request')
const { resolveImage } = require('../../utils/config')
const { mapProduct, money } = require('../../utils/format')

const app = getApp()

Page({
  data: {
    heroUrl: resolveImage('/images/brand-hero.jpg'),
    greeting: '早安',
    dateText: '',
    categories: [],
    recommendList: [],
    products: [],
    page: 1,
    pageSize: 6,
    hasMore: false,
    loading: true
  },

  onLoad() {
    this.setGreeting()
    this.loadAll()
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar) {
      tabBar.setData({ selected: 0 })
      tabBar.refresh()
    }
  },

  onPullDownRefresh() {
    this.loadAll().finally(() => wx.stopPullDownRefresh())
  },

  setGreeting() {
    const now = new Date()
    const hour = now.getHours()
    let greeting = '晚上好'
    if (hour < 6) greeting = '深夜好'
    else if (hour < 11) greeting = '早安'
    else if (hour < 14) greeting = '午后好'
    else if (hour < 18) greeting = '下午好'

    const week = ['日', '一', '二', '三', '四', '五', '六'][now.getDay()]
    this.setData({
      greeting,
      dateText: `${now.getMonth() + 1}月${now.getDate()}日 · 周${week}`
    })
  },

  async loadAll() {
    this.setData({ loading: true, page: 1 })
    try {
      const [categories, recommended, pageData] = await Promise.all([
        request({ url: '/categories', silent: true }),
        request({ url: '/products/recommended', silent: true }),
        request({ url: `/products?page=1&pageSize=${this.data.pageSize}`, silent: true })
      ])
      this.setData({
        categories: categories || [],
        recommendList: (recommended || []).map(mapProduct),
        products: (pageData.records || []).map(mapProduct),
        hasMore: (pageData.total || 0) > (pageData.records || []).length,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  async loadMore() {
    if (!this.data.hasMore) return
    const page = this.data.page + 1
    this.setData({ page })
    try {
      const pageData = await request({
        url: `/products?page=${page}&pageSize=${this.data.pageSize}`,
        silent: true
      })
      const merged = this.data.products.concat((pageData.records || []).map(mapProduct))
      this.setData({
        products: merged,
        hasMore: (pageData.total || 0) > merged.length
      })
    } catch (e) {
      this.setData({ page: page - 1 })
    }
  },

  goMenu() {
    app.globalData.menuPreset = { focus: true }
    wx.switchTab({ url: '/pages/menu/menu' })
  },

  goCategory(e) {
    app.globalData.menuPreset = { categoryId: e.currentTarget.dataset.id }
    wx.switchTab({ url: '/pages/menu/menu' })
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` })
  },

  onShareAppMessage() {
    return {
      title: 'GXUFL 咖啡厅 · 一整个东南亚，都在这一杯里',
      path: '/pages/index/index'
    }
  }
})
