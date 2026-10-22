const { request, isLoggedIn } = require('../utils/request')

const inactive = '%238E857B'
const active = '%23C8A97E'

const icon = (paths, color) =>
  `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='${color}' stroke-width='1.25' stroke-linecap='round' stroke-linejoin='round'%3E${paths}%3C/svg%3E`

const HOME = "%3Cpath d='M4 10.8 12 4l8 6.8V20H4z'/%3E%3Cpath d='M9.5 20v-6h5v6'/%3E"
const CUP = "%3Cpath d='M5 9h11v5.4a5.5 5.5 0 0 1-11 0z'/%3E%3Cpath d='M16 10.6h1.3a2.3 2.3 0 0 1 0 4.6H16'/%3E%3Cpath d='M4 21h13'/%3E"
const CART = "%3Cpath d='M4 5h2l2.2 10.2h9.4L20 8H7'/%3E%3Ccircle cx='9.4' cy='19' r='1.2'/%3E%3Ccircle cx='17' cy='19' r='1.2'/%3E"
const ORDER = "%3Cpath d='M6 3h12v18l-3-2-3 2-3-2-3 2z'/%3E%3Cpath d='M9 8h6M9 12h6'/%3E"
const USER = "%3Ccircle cx='12' cy='8' r='3.4'/%3E%3Cpath d='M5 20c.8-3.4 3.4-5.2 7-5.2s6.2 1.8 7 5.2'/%3E"

Component({
  data: {
    selected: 0,
    cartCount: 0,
    tabs: [
      {
        path: '/pages/index/index',
        text: '首页',
        key: 'home',
        icon: icon(HOME, inactive),
        activeIcon: icon(HOME, active)
      },
      {
        path: '/pages/menu/menu',
        text: '点单',
        key: 'menu',
        featured: true,
        icon: icon(CUP, inactive),
        activeIcon: icon(CUP, active)
      },
      {
        path: '/pages/cart/cart',
        text: '购物车',
        key: 'cart',
        icon: icon(CART, inactive),
        activeIcon: icon(CART, active)
      },
      {
        path: '/pages/orders/orders',
        text: '订单',
        key: 'orders',
        icon: icon(ORDER, inactive),
        activeIcon: icon(ORDER, active)
      },
      {
        path: '/pages/me/me',
        text: '我的',
        key: 'me',
        icon: icon(USER, inactive),
        activeIcon: icon(USER, active)
      }
    ]
  },

  lifetimes: {
    attached() {
      this.refresh()
    }
  },

  methods: {
    onSwitch(e) {
      const { index, path } = e.currentTarget.dataset
      if (index === this.data.selected) return
      wx.switchTab({ url: path })
    },

    refresh() {
      if (!isLoggedIn()) {
        this.setData({ cartCount: 0 })
        return
      }
      request({ url: '/cart/count', silent: true })
        .then((data) => {
          this.setData({ cartCount: data.count || 0 })
        })
        .catch(() => {})
    }
  }
})
