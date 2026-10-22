const { getUser } = require('./utils/request')

App({
  globalData: {
    user: null,
    cartCount: 0,
    checkout: null,
    checkoutDirect: null,
    lastOrder: null
  },

  onLaunch() {
    this.globalData.user = getUser()
  }
})
