const { request, isLoggedIn, ensureLogin } = require('../../utils/request')
const { mapProduct, money } = require('../../utils/format')

const app = getApp()

Page({
  data: {
    categories: [],
    activeCategoryId: null,
    keyword: '',
    searchFocus: false,
    products: [],
    page: 1,
    pageSize: 8,
    hasMore: false,
    loading: true,
    loadingMore: false,
    cartCount: 0,
    cartTotal: '0.00'
  },

  onLoad() {
    this.seq = 0
    this.consumePreset()
    this.loadCategories()
    this.loadProducts(true)
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar) {
      tabBar.setData({ selected: 1 })
      tabBar.refresh()
    }
    if (this.consumePreset()) {
      this.loadProducts(true)
    }
    this.loadCartSummary()
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadProducts(false)
    }
  },

  consumePreset() {
    const preset = app.globalData.menuPreset
    if (!preset) return false
    app.globalData.menuPreset = null
    const patch = {}
    if (preset.categoryId) patch.activeCategoryId = Number(preset.categoryId)
    if (preset.focus) patch.searchFocus = true
    this.setData(patch)
    return true
  },

  async loadCategories() {
    try {
      const data = await request({ url: '/categories', silent: true })
      this.setData({ categories: data || [] })
    } catch (e) {}
  },

  async loadProducts(reset) {
    if (!reset && this.data.loadingMore) return
    const seq = ++this.seq
    const page = reset ? 1 : this.data.page + 1
    if (reset) {
      this.setData({ loading: true, page: 1 })
    } else {
      this.setData({ loadingMore: true })
    }
    const params = [`page=${page}`, `pageSize=${this.data.pageSize}`]
    if (this.data.activeCategoryId !== null) params.push(`categoryId=${this.data.activeCategoryId}`)
    if (this.data.keyword) params.push(`keyword=${encodeURIComponent(this.data.keyword)}`)

    try {
      const data = await request({ url: `/products?${params.join('&')}`, silent: true })
      if (seq !== this.seq) return
      const mapped = (data.records || []).map(mapProduct)
      const merged = reset ? mapped : this.data.products.concat(mapped)
      this.setData({
        products: merged,
        page,
        hasMore: (data.total || 0) > merged.length,
        loading: false,
        loadingMore: false
      })
    } catch (e) {
      if (seq === this.seq) {
        this.setData({ loading: false, loadingMore: false })
      }
    }
  },

  reload() {
    this.loadProducts(true)
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value })
    clearTimeout(this.searchTimer)
    this.searchTimer = setTimeout(() => this.reload(), 350)
  },

  onSearchConfirm(e) {
    clearTimeout(this.searchTimer)
    this.setData({ keyword: e.detail.value })
    this.reload()
  },

  clearSearch() {
    clearTimeout(this.searchTimer)
    this.setData({ keyword: '' })
    this.reload()
  },

  selectCategory(e) {
    const raw = e.currentTarget.dataset.id
    const id = raw ? Number(raw) : null
    if (id === this.data.activeCategoryId) return
    this.setData({ activeCategoryId: id })
    this.reload()
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}` })
  },

  async addToCart(e) {
    const id = Number(e.currentTarget.dataset.id)
    const product = this.data.products.find((item) => item.id === id)
    if (!product) return
    if (!ensureLogin()) return
    if (product.hasSpecs) {
      wx.navigateTo({ url: `/pages/detail/detail?id=${id}` })
      return
    }
    try {
      await request({
        url: '/cart',
        method: 'POST',
        data: {
          productId: product.id,
          specId: product.specs[0] ? product.specs[0].id : null,
          quantity: 1
        }
      })
      wx.showToast({ title: '已加入购物车', icon: 'none' })
      const tabBar = this.getTabBar && this.getTabBar()
      if (tabBar) tabBar.refresh()
      this.loadCartSummary()
    } catch (e) {}
  },

  async loadCartSummary() {
    if (!isLoggedIn()) {
      this.setData({ cartCount: 0, cartTotal: '0.00' })
      return
    }
    try {
      const items = await request({ url: '/cart', silent: true })
      let count = 0
      let total = 0
      ;(items || []).forEach((item) => {
        if (item.productStatus === 1) {
          count += item.quantity
          total += Number(item.price) * item.quantity
        }
      })
      this.setData({ cartCount: count, cartTotal: money(total, true) })
    } catch (e) {}
  },

  goCart() {
    wx.switchTab({ url: '/pages/cart/cart' })
  }
})
