const { request, setAuth } = require('../../utils/request')
const { resolveImage } = require('../../utils/config')

Page({
  data: {
    heroUrl: resolveImage('/images/brand-hero.jpg'),
    mode: 'login',
    nickname: '',
    phone: '',
    password: '',
    confirmPwd: '',
    showPassword: false,
    submitting: false
  },

  switchMode(e) {
    const mode = e.currentTarget.dataset.mode
    if (mode === this.data.mode) return
    this.setData({ mode })
  },

  onNickname(e) {
    this.setData({ nickname: e.detail.value })
  },

  onPhone(e) {
    this.setData({ phone: e.detail.value })
  },

  onPassword(e) {
    this.setData({ password: e.detail.value })
  },

  onConfirm(e) {
    this.setData({ confirmPwd: e.detail.value })
  },

  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword })
  },

  async submit() {
    if (this.data.submitting) return
    const { mode, nickname, phone, password, confirmPwd } = this.data

    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '请输入 11 位手机号', icon: 'none' })
      return
    }
    if (!password || password.length < 6) {
      wx.showToast({ title: '密码至少 6 位', icon: 'none' })
      return
    }
    if (mode === 'register') {
      if (!nickname.trim()) {
        wx.showToast({ title: '请输入昵称', icon: 'none' })
        return
      }
      if (password !== confirmPwd) {
        wx.showToast({ title: '两次密码不一致', icon: 'none' })
        return
      }
    }

    this.setData({ submitting: true })
    try {
      const data = await request({
        url: mode === 'login' ? '/auth/login' : '/auth/register',
        method: 'POST',
        data: mode === 'login' ? { phone, password } : { nickname: nickname.trim(), phone, password }
      })
      setAuth(data)
      wx.showToast({ title: mode === 'login' ? '登录成功' : '注册成功', icon: 'success' })
      setTimeout(() => {
        const pages = getCurrentPages()
        if (pages.length > 1) {
          wx.navigateBack()
        } else {
          wx.switchTab({ url: '/pages/index/index' })
        }
      }, 700)
    } catch (e) {
      this.setData({ submitting: false })
    }
  }
})
