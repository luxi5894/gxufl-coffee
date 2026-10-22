const { resolveImage } = require('../../utils/config')

Page({
  data: {
    heroUrl: resolveImage('/images/brand-hero.jpg'),
    origins: [
      { code: 'VN', name: '越南', en: 'VIETNAM', coffee: '蛋咖啡 · 滴漏咖啡' },
      { code: 'TH', name: '泰国', en: 'THAILAND', coffee: '泰式冰咖啡 · 小豆蔻咖啡' },
      { code: 'ID', name: '印度尼西亚', en: 'INDONESIA', coffee: '麝香猫 · 苏门答腊曼特宁' },
      { code: 'MY', name: '马来西亚', en: 'MALAYSIA', coffee: '南洋白咖啡 · 槟城拉茶咖啡' },
      { code: 'SG', name: '新加坡', en: 'SINGAPORE', coffee: 'Kopi-C · Kopi-O' },
      { code: 'PH', name: '菲律宾', en: 'PHILIPPINES', coffee: '巴拉克咖啡 · Liberica' }
    ]
  }
})
