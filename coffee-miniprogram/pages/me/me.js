const { request, getUser, isLoggedIn, clearAuth } = require('../../utils/request')
const { maskPhone, money } = require('../../utils/format')

const app = getApp()

const stroke = '%23C8A97E'
const icon = (paths) =>
  `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='${stroke}' stroke-width='1.25' stroke-linecap='round' stroke-linejoin='round'%3E${paths}%3C/svg%3E`

const RECEIPT = "%3Cpath d='M6 3h12v18l-3-2-3 2-3-2-3 2z'/%3E%3Cpath d='M9 8h6M9 12h6'/%3E"
const CHAT = "%3Cpath d='M4 5h16v11H9l-4 3z'/%3E%3Cpath d='M8 9h8M8 12h5'/%3E"
const CART = "%3Cpath d='M4 5h2l2.2 10.2h9.4L20 8H7'/%3E%3Ccircle cx='9.4' cy='19' r='1.2'/%3E%3Ccircle cx='17' cy='19' r='1.2'/%3E"
const INFO = "%3Ccircle cx='12' cy='12' r='8.5'/%3E%3Cpath d='M12 10.8V17'/%3E%3Cpath d='M12 7.4v.2'/%3E"
const PHONE = "%3Cpath d='M6 3h3l2 5-2.5 1.5a13 13 0 0 0 6 6L16 13l5 2v3a2 2 0 0 1-2.2 2A17 17 0 0 1 4 5.2 2 2 0 0 1 6 3z'/%3E"

Page({
  data: {
    loggedIn: false,
    user: {},
    avatarChar: '·',
    phoneMasked: '',
    stats: { orderCount: 0, completedCount: 0, totalSpentText: '0.00' },
    cartCount: 0,
    menuItems: [
      {
        key: 'orders',
        label: '我的订单',
        sub: '查看制作与取餐进度',
        icon: icon(RECEIPT)
      },
      {
        key: 'reviews',
        label: '我的评价',
        sub: '管理你写下的口碑',
        icon: icon(CHAT)
      },
      {
        key: 'cart',
        label: '购物车',
        sub: '已选的咖啡都在这里',
        icon: icon(CART)
      },
      {
        key: 'about',
        label: '关于咖啡厅',
        sub: '品牌故事与六国地图',
        icon: icon(INFO)
      },
      {
        key: 'phone',
        label: '联系门店',
        sub: '0771-8888888',
        icon: icon(PHONE)
      }
    ]
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar) {
      tabBar.setData({ selected: 4 })
      tabBar.refresh()
    }
    const loggedIn = isLoggedIn()
    const user = getUser() || {}
    this.setData({
      loggedIn,
      user,
      avatarChar: user.nickname ? user.nickname.slice(0, 1) : '·',
      phoneMasked: maskPhone(user.phone)
    })
    if (loggedIn) {
      this.loadStats()
      this.loadCartCount()
    } else {
      this.setData({
        stats: { orderCount: 0, completedCount: 0, totalSpentText: '0.00' },
        cartCount: 0
      })
    }
  },

  async loadStats() {
    try {
      const data = await request({ url: '/users/me/stats', silent: true })
      this.setData({
        stats: {
          orderCount: data.orderCount || 0,
          completedCount: data.completedCount || 0,
          totalSpentText: money(data.totalSpent, true)
        }
      })
    } catch (e) {}
  },

  async loadCartCount() {
    try {
      const data = await request({ url: '/cart/count', silent: true })
      this.setData({ cartCount: data.count || 0 })
    } catch (e) {}
  },

  onMenu(e) {
    const key = e.currentTarget.dataset.key
    if (key === 'orders') {
      wx.switchTab({ url: '/pages/orders/orders' })
      return
    }
    if (key === 'cart') {
      wx.switchTab({ url: '/pages/cart/cart' })
      return
    }
    if (key === 'reviews') {
      wx.navigateTo({ url: '/pages/review/review?tab=mine' })
      return
    }
    if (key === 'about') {
      wx.navigateTo({ url: '/pages/about/about' })
      return
    }
    if (key === 'phone') {
      wx.makePhoneCall({ phoneNumber: '07718888888' })
    }
  },

  editNickname() {
    const current = this.data.user.nickname || ''
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: current || '输入新的昵称',
      confirmColor: '#C8A97E',
      success: async (res) => {
        if (!res.confirm) return
        const nickname = (res.content || '').trim()
        if (!nickname) {
          wx.showToast({ title: '昵称不能为空', icon: 'none' })
          return
        }
        if (nickname.length > 20) {
          wx.showToast({ title: '昵称最多 20 个字符', icon: 'none' })
          return
        }
        try {
          const user = await request({
            url: '/auth/me',
            method: 'PUT',
            data: { nickname }
          })
          wx.setStorageSync('coffee_user', user)
          app.globalData.user = user
          this.setData({
            user,
            avatarChar: user.nickname.slice(0, 1)
          })
          wx.showToast({ title: '昵称已更新', icon: 'none' })
        } catch (e) {}
      }
    })
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '退出后购物车与订单仍会保留，下次登录可继续查看。',
      confirmText: '退出',
      confirmColor: '#C8A97E',
      success: (res) => {
        if (!res.confirm) return
        clearAuth()
        app.globalData.cartCount = 0
        this.setData({
          loggedIn: false,
          user: {},
          avatarChar: '·',
          phoneMasked: '',
          stats: { orderCount: 0, completedCount: 0, totalSpentText: '0.00' },
          cartCount: 0
        })
        const tabBar = this.getTabBar && this.getTabBar()
        if (tabBar) tabBar.refresh()
        wx.showToast({ title: '已退出登录', icon: 'none' })
      }
    })
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  }
})
