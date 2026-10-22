const { request, isLoggedIn, ensureLogin } = require('../../utils/request')
const { mapProduct, mapReview, money } = require('../../utils/format')

const app = getApp()

Page({
  data: {
    id: null,
    product: null,
    loading: true,
    selectedSpecIndex: 0,
    selectedSpec: {},
    quantity: 1,
    maxQuantity: 99,
    totalText: '0.00',
    currentImage: 0,
    reviews: [],
    reviewTotal: 0,
    cartCount: 0
  },

  onLoad(options) {
    this.setData({ id: Number(options.id) })
    this.loadDetail()
    this.loadReviews()
    this.loadCartCount()
  },

  onShow() {
    this.loadCartCount()
  },

  async loadDetail() {
    try {
      const data = await request({ url: `/products/${this.data.id}`, silent: true })
      const product = mapProduct(data)
      const selectedSpec = product.specs[0] || { name: '标准杯', priceDelta: 0, priceText: product.priceText }
      const maxQuantity = Math.max(1, Math.min(99, product.stock || 99))
      this.setData({
        product,
        loading: false,
        selectedSpec,
        selectedSpecIndex: 0,
        maxQuantity,
        totalText: money(Number(selectedSpec.priceText || product.price) * this.data.quantity, true)
      })
      wx.setNavigationBarTitle({ title: product.name })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  async loadReviews() {
    try {
      const data = await request({
        url: `/products/${this.data.id}/reviews?page=1&pageSize=3`,
        silent: true
      })
      this.setData({
        reviews: (data.records || []).map(mapReview),
        reviewTotal: data.total || 0
      })
    } catch (e) {}
  },

  async loadCartCount() {
    if (!isLoggedIn()) {
      this.setData({ cartCount: 0 })
      return
    }
    try {
      const data = await request({ url: '/cart/count', silent: true })
      this.setData({ cartCount: data.count || 0 })
    } catch (e) {}
  },

  onSwiperChange(e) {
    this.setData({ currentImage: e.detail.current })
  },

  previewImage(e) {
    wx.previewImage({
      urls: this.data.product.images,
      current: e.currentTarget.dataset.src
    })
  },

  selectSpec(e) {
    const index = Number(e.currentTarget.dataset.index)
    const spec = this.data.product.specs[index]
    this.setData({
      selectedSpecIndex: index,
      selectedSpec: spec,
      totalText: money(Number(spec.priceText) * this.data.quantity, true)
    })
  },

  recalcTotal() {
    const spec = this.data.selectedSpec
    const unit = Number(spec.priceText || this.data.product.price)
    this.setData({ totalText: money(unit * this.data.quantity, true) })
  },

  minus() {
    if (this.data.quantity <= 1) return
    this.setData({ quantity: this.data.quantity - 1 }, () => this.recalcTotal())
  },

  plus() {
    if (this.data.quantity >= this.data.maxQuantity) return
    this.setData({ quantity: this.data.quantity + 1 }, () => this.recalcTotal())
  },

  async addToCart() {
    if (!ensureLogin()) return
    const { product, selectedSpec, quantity } = this.data
    try {
      await request({
        url: '/cart',
        method: 'POST',
        data: {
          productId: product.id,
          specId: selectedSpec.id || null,
          quantity
        }
      })
      wx.showToast({ title: '已加入购物车', icon: 'none' })
      this.loadCartCount()
    } catch (e) {}
  },

  buyNow() {
    if (!ensureLogin()) return
    const { product, selectedSpec, quantity } = this.data
    app.globalData.checkoutDirect = {
      items: [
        {
          productId: product.id,
          specId: selectedSpec.id || null,
          quantity
        }
      ],
      preview: [
        {
          productId: product.id,
          specId: selectedSpec.id || null,
          productName: product.name,
          productImage: product.image,
          specName: selectedSpec.name,
          price: selectedSpec.priceText,
          quantity,
          imageUrl: product.imageUrl
        }
      ]
    }
    wx.navigateTo({ url: '/pages/checkout/checkout' })
  },

  goCart() {
    wx.switchTab({ url: '/pages/cart/cart' })
  },

  onShareAppMessage() {
    const product = this.data.product
    return {
      title: product ? `${product.name} · GXUFL 咖啡厅` : 'GXUFL 咖啡厅',
      path: `/pages/detail/detail?id=${this.data.id}`
    }
  }
})
