Component({
  properties: {
    title: { type: String, value: '' },
    subtitle: { type: String, value: '' },
    showBack: { type: Boolean, value: false },
    overlay: { type: Boolean, value: false },
    transparent: { type: Boolean, value: false }
  },

  data: {
    statusBarHeight: 20,
    navHeight: 44,
    totalHeight: 64
  },

  lifetimes: {
    attached() {
      const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
      const statusBarHeight = info.statusBarHeight || 20
      let navHeight = 44
      try {
        const menu = wx.getMenuButtonBoundingClientRect()
        if (menu && menu.height) {
          navHeight = (menu.top - statusBarHeight) * 2 + menu.height
        }
      } catch (e) {
        navHeight = 44
      }
      this.setData({
        statusBarHeight,
        navHeight,
        totalHeight: statusBarHeight + navHeight
      })
    }
  },

  methods: {
    onBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        wx.navigateBack()
      } else {
        wx.switchTab({ url: '/pages/index/index' })
      }
    }
  }
})
