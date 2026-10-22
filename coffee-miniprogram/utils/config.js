/**
 * 后端服务地址
 * - 微信开发者工具模拟器：127.0.0.1 可用
 * - 真机预览：将 DEV_BASE_URL 改为电脑局域网 IP，例如 http://192.168.1.20:8080/api
 * - 线上部署：改为你的 HTTPS 域名，并在小程序后台配置 request 合法域名
 */
const DEV_BASE_URL = 'http://127.0.0.1:8080/api'
const PROD_BASE_URL = ''

const BASE_URL = PROD_BASE_URL || DEV_BASE_URL

const resolveImage = (path) => {
  if (!path) return ''
  if (/^https?:\/\//i.test(path)) return path
  return `${BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
}

module.exports = {
  BASE_URL,
  resolveImage
}
