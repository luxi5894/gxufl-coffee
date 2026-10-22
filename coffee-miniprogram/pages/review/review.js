const { request, ensureLogin, isLoggedIn } = require('../../utils/request')
const { resolveImage } = require('../../utils/config')
const { mapReview } = require('../../utils/format')

Page({
  data: {
    tab: 'write',
    productOptions: [],
    productNames: [],
    productIndex: 0,
    selectedProduct: {},
    rating: 5,
    content: '',
    recentReviews: [],
    myReviews: [],
    loadingMine: false,
    submitting: false
  },

  onLoad(options) {
    const tab = options.tab === 'mine' ? 'mine' : 'write'
    this.setData({ tab })
    this.loadOptions(options.productId ? Number(options.productId) : null)
    if (tab === 'mine') this.loadMine()
  },

  async loadOptions(presetId) {
    try {
      const data = await request({ url: '/products?page=1&pageSize=50', silent: true })
      const options = (data.records || []).map((product) => ({
        id: product.id,
        name: product.name,
        imageUrl: resolveImage(product.image)
      }))
      if (!options.length) return
      let index = 0
      if (presetId) {
        const found = options.findIndex((item) => item.id === presetId)
        if (found >= 0) index = found
      }
      this.setData({
        productOptions: options,
        productNames: options.map((item) => item.name),
        productIndex: index,
        selectedProduct: options[index]
      })
      this.loadRecent(options[index].id)
    } catch (e) {}
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.tab) return
    this.setData({ tab })
    if (tab === 'mine') this.loadMine()
  },

  onProductChange(e) {
    const index = Number(e.detail.value)
    const product = this.data.productOptions[index]
    this.setData({ productIndex: index, selectedProduct: product })
    this.loadRecent(product.id)
  },

  async loadRecent(productId) {
    try {
      const data = await request({
        url: `/products/${productId}/reviews?page=1&pageSize=3`,
        silent: true
      })
      this.setData({ recentReviews: (data.records || []).map(mapReview) })
    } catch (e) {}
  },

  setRating(e) {
    this.setData({ rating: Number(e.currentTarget.dataset.value) })
  },

  onContent(e) {
    this.setData({ content: e.detail.value })
  },

  async submit() {
    if (this.data.submitting) return
    if (!ensureLogin()) return
    const { selectedProduct, rating, content } = this.data
    if (!selectedProduct.id) {
      wx.showToast({ title: '请选择咖啡', icon: 'none' })
      return
    }
    if (!content.trim()) {
      wx.showToast({ title: '请写下评价内容', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      await request({
        url: '/reviews',
        method: 'POST',
        data: {
          productId: selectedProduct.id,
          rating,
          content: content.trim()
        }
      })
      wx.showToast({ title: '评价已发布', icon: 'none' })
      this.setData({ content: '', rating: 5, tab: 'mine' })
      this.loadRecent(selectedProduct.id)
      this.loadMine()
    } catch (e) {
      this.setData({ submitting: false })
      return
    }
    this.setData({ submitting: false })
  },

  async loadMine() {
    if (!isLoggedIn()) {
      this.setData({ myReviews: [] })
      return
    }
    this.setData({ loadingMine: true })
    try {
      const data = await request({ url: '/reviews/mine?page=1&pageSize=20', silent: true })
      this.setData({ myReviews: (data.records || []).map(mapReview) })
    } catch (e) {
      this.setData({ myReviews: [] })
    } finally {
      this.setData({ loadingMine: false })
    }
  }
})
