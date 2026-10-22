const { BASE_URL } = require('./config')

const TOKEN_KEY = 'coffee_token'
const USER_KEY = 'coffee_user'

let redirecting = false

const getToken = () => wx.getStorageSync(TOKEN_KEY) || ''

const getUser = () => wx.getStorageSync(USER_KEY) || null

const setAuth = (data) => {
  wx.setStorageSync(TOKEN_KEY, data.token)
  wx.setStorageSync(USER_KEY, data.user)
  const app = getApp()
  if (app) app.globalData.user = data.user
}

const clearAuth = () => {
  wx.removeStorageSync(TOKEN_KEY)
  wx.removeStorageSync(USER_KEY)
  const app = getApp()
  if (app) app.globalData.user = null
}

const isLoggedIn = () => !!getToken()

const goLogin = () => {
  if (redirecting) return
  const pages = getCurrentPages()
  const current = pages[pages.length - 1]
  if (current && current.route === 'pages/login/login') return
  redirecting = true
  wx.navigateTo({
    url: '/pages/login/login',
    complete: () => {
      setTimeout(() => {
        redirecting = false
      }, 600)
    }
  })
}

const ensureLogin = () => {
  if (isLoggedIn()) return true
  wx.showModal({
    title: '需要登录',
    content: '登录后即可点单、查看订单与评价。',
    confirmText: '去登录',
    cancelText: '先逛逛',
    confirmColor: '#C8A97E',
    success: (res) => {
      if (res.confirm) goLogin()
    }
  })
  return false
}

const request = ({ url, method = 'GET', data = {}, silent = false, loading = false }) => {
  return new Promise((resolve, reject) => {
    if (loading) {
      wx.showLoading({ title: '加载中', mask: true })
    }
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      timeout: 20000,
      header: {
        'content-type': 'application/json',
        ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {})
      },
      success: (res) => {
        const body = res.data
        if (res.statusCode === 401 || (body && body.code === 401)) {
          clearAuth()
          if (!silent) {
            wx.showToast({ title: '登录已过期', icon: 'none' })
          }
          goLogin()
          reject(new Error('UNAUTHORIZED'))
          return
        }
        if (res.statusCode >= 500) {
          if (!silent) wx.showToast({ title: '服务暂时不可用', icon: 'none' })
          reject(new Error('SERVER_ERROR'))
          return
        }
        if (body && body.code === 200) {
          resolve(body.data)
          return
        }
        const message = (body && body.message) || '请求失败'
        if (!silent) {
          wx.showToast({ title: message, icon: 'none' })
        }
        reject(new Error(message))
      },
      fail: (err) => {
        if (!silent) {
          wx.showToast({ title: '网络异常，请检查后端服务', icon: 'none' })
        }
        reject(err)
      },
      complete: () => {
        if (loading) wx.hideLoading()
      }
    })
  })
}

module.exports = {
  request,
  getToken,
  getUser,
  setAuth,
  clearAuth,
  isLoggedIn,
  ensureLogin,
  goLogin
}
