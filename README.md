# GXUFL Coffee Atelier · 广西外国语学院咖啡厅点单系统

一个校园咖啡厅的全栈点单系统：**微信原生小程序 + Spring Boot 后端 + Vue 3 商家管理后台**。

以「东南亚六国咖啡」为品牌主题，视觉延续深咖啡黑与香槟金的安静奢华风格；小程序端覆盖从浏览、选规格、购物车、下单到取餐与评价的完整闭环，管理端覆盖商品、分类、订单、用户、评价与经营数据。

---

## 技术栈

| 端 | 技术 |
|---|---|
| 微信小程序 | 微信原生（WXML / WXSS / JS）、自定义导航栏、自定义 TabBar |
| 后端 | Java 17+、Spring Boot 3.2、MyBatis-Plus 3.5、MySQL 8.0+、JWT、BCrypt |
| 管理后台 | Vue 3、Vite 5、Element Plus、ECharts、Pinia、Vue Router |
| 数据库 | MySQL 8.0+（已使用 MySQL 9.7 实测） |

## 目录结构

```
.
├── coffee-miniprogram/       微信原生小程序
│   ├── pages/                首页 / 点单 / 购物车 / 订单 / 我的 / 详情 / 结算 / 登录 / 评价 / 关于
│   ├── components/nav-bar/   沉浸式自定义导航栏
│   ├── custom-tab-bar/       暗金风格自定义 TabBar
│   └── utils/                请求封装、配置、格式化
├── coffee-server/            Spring Boot 后端
│   └── src/main/
│       ├── java/com/gxufl/coffee/
│       │   ├── controller/   用户端 & 管理端 REST API
│       │   ├── service/      业务逻辑（订单/购物车/商品/评价/统计）
│       │   ├── entity/       MyBatis-Plus 实体
│       │   ├── mapper/       数据访问
│       │   ├── dto/          请求与视图对象
│       │   ├── security/     JWT 与登录上下文
│       │   └── config/       跨域、拦截器、静态资源
│       └── resources/
│           ├── application.yml
│           └── static/images/ 品牌与商品图片
├── coffee-admin/             Vue 3 商家管理后台
│   └── src/
│       ├── views/            概览 / 商品 / 分类 / 订单 / 用户 / 评价
│       ├── layouts/          暗金侧边栏布局
│       ├── api/              Axios 封装
│       └── styles/           主题与 Element Plus 覆盖
├── docs/database.sql         建库、建表与种子数据
├── project.config.json       微信开发者工具项目配置
└── README.md
```

---

## 快速开始

### 1. 初始化 MySQL

```bash
mysql -u root -p < docs/database.sql
```

脚本会创建 `coffee_shop` 数据库、8 张业务表，并写入 6 款咖啡、分类、规格、示例用户/订单/评价。

### 2. 启动后端

```bash
cd coffee-server
mvn spring-boot:run
```

默认地址：`http://127.0.0.1:8080/api`

如数据库账号不同，可通过环境变量覆盖：

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL 主机 |
| `DB_PORT` | `3306` | MySQL 端口 |
| `DB_NAME` | `coffee_shop` | 数据库名 |
| `DB_USERNAME` | `root` | 用户名 |
| `DB_PASSWORD` | `root` | 密码 |
| `SERVER_PORT` | `8080` | 后端端口 |
| `JWT_SECRET` | 内置开发密钥 | 生产环境务必替换 |
| `UPLOAD_DIR` | `./uploads` | 图片上传目录 |

示例：

```bash
DB_PASSWORD=你的密码 mvn spring-boot:run
```

打包运行：

```bash
mvn -DskipTests package
java -jar target/coffee-server-1.0.0.jar
```

### 3. 启动管理后台

```bash
cd coffee-admin
npm install
npm run dev
```

访问 `http://localhost:5173`。Vite 已将 `/api` 代理到 `http://127.0.0.1:8080`。

生产构建：

```bash
npm run build
# 产物在 coffee-admin/dist
```

### 4. 运行微信小程序

1. 用微信开发者工具「导入项目」，目录选择本仓库根目录（`project.config.json` 已指向 `coffee-miniprogram/`）。
2. 在「详情 → 本地设置」勾选 **不校验合法域名、web-view、TLS 版本以及 HTTPS 证书**。
3. 修改 [utils/config.js](coffee-miniprogram/utils/config.js) 中的 `DEV_BASE_URL`：
   - 模拟器：`http://127.0.0.1:8080/api`
   - 真机预览：改成电脑局域网 IP，例如 `http://192.168.1.20:8080/api`
4. 编译运行，后端需保持运行。

> 线上部署时请将 `PROD_BASE_URL` 改为你的 HTTPS 域名，并在微信公众平台配置 request 合法域名。

---

## 默认账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 后台管理员 | `admin` | `admin123` |
| 小程序体验用户 | `13800000000` | `123456` |

> 管理员账号直接写入 `sys_user` 表，登录入口为 `/api/auth/admin/login`；普通用户登录入口为 `/api/auth/login`。密码均使用 BCrypt 存储。

---

## 功能清单

### 小程序端

- 沉浸式首页：品牌 Hero、六国分类、主理人推荐、本季咖啡
- 点单页：服务端关键词搜索、分类筛选、规格选择、快速加购
- 商品详情：图集预览、规格与加价、数量、咖啡档案、近期评价
- 购物车：勾选、改数量、移除、清空、合计、结算
- 确认订单：门店信息、商品明细、备注、到店支付
- 订单中心：状态筛选、取餐号、取消订单、确认取餐、去评价、再来一杯
- 评价：写评价、我的评价、展示状态
- 我的：用户信息、累计订单/消费、购物车角标、修改昵称、退出登录
- 登录注册：真实接口、JWT、登录态持久化、401 自动跳转

### 管理后台

- 经营概览：今日营业额/订单/新增用户、累计数据、7 日趋势、状态分布、本周热销、最近订单
- 商品管理：搜索、分类筛选、图片上传、规格与加价、库存、推荐位、上下架
- 分类管理：六国分类的新增、编辑、排序、启停
- 订单管理：状态筛选、关键词检索、详情抽屉、开始制作 → 制作完成 → 完成取餐、取消回补库存
- 用户管理：搜索、注册信息、账号启停
- 评价管理：关键词/状态筛选、卡片列表、展示与隐藏、删除
- 全部页面采用暗金主题、响应式布局，并适配窄屏抽屉导航

### 后端

- JWT 无状态鉴权，区分 `USER` / `ADMIN`
- BCrypt 密码加密
- 服务端计算价格与总价，库存与销量事务扣减
- 取消订单自动回补库存与销量
- 商品评价后自动重算评分
- 图片上传、静态资源托管、统一响应结构与异常处理
- 管理端统计接口（今日 / 7 日趋势 / 热销 / 状态分布）

---

## API 概览

统一响应：`{ "code": 200, "message": "success", "data": ... }`，鉴权接口需携带 `Authorization: Bearer <token>`。

### 用户端

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/api/auth/register` | - | 注册 |
| POST | `/api/auth/login` | - | 登录 |
| GET | `/api/auth/me` | 用户 | 当前用户 |
| PUT | `/api/auth/me` | 用户 | 修改昵称/头像 |
| GET | `/api/categories` | - | 分类列表 |
| GET | `/api/products` | - | 商品分页/搜索/分类 |
| GET | `/api/products/recommended` | - | 推荐商品 |
| GET | `/api/products/{id}` | - | 商品详情（含规格） |
| GET | `/api/products/{id}/reviews` | - | 商品评价 |
| GET | `/api/cart` | 用户 | 购物车 |
| POST | `/api/cart` | 用户 | 加入购物车 |
| PUT | `/api/cart/{id}` | 用户 | 修改数量 |
| DELETE | `/api/cart/{id}` | 用户 | 移除 |
| DELETE | `/api/cart` | 用户 | 清空 |
| GET | `/api/cart/count` | 用户 | 购物车数量 |
| POST | `/api/orders` | 用户 | 创建订单（购物车 ID 或直接商品） |
| GET | `/api/orders` | 用户 | 我的订单 |
| GET | `/api/orders/{id}` | 用户 | 订单详情 |
| POST | `/api/orders/{id}/cancel` | 用户 | 取消订单 |
| POST | `/api/orders/{id}/confirm` | 用户 | 确认取餐 |
| POST | `/api/reviews` | 用户 | 发布评价 |
| GET | `/api/reviews/mine` | 用户 | 我的评价 |
| GET | `/api/users/me/stats` | 用户 | 个人订单统计 |

### 管理端（需 `ADMIN`）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/admin/login` | 管理员登录 |
| GET | `/api/admin/stats` | 经营概览 |
| GET/POST | `/api/admin/products` | 商品列表 / 新增 |
| GET/PUT/DELETE | `/api/admin/products/{id}` | 商品详情 / 编辑 / 删除 |
| PUT | `/api/admin/products/{id}/status` | 上下架 |
| PUT | `/api/admin/products/{id}/recommend` | 推荐位 |
| GET/POST | `/api/admin/categories` | 分类列表 / 新增 |
| PUT/DELETE | `/api/admin/categories/{id}` | 编辑 / 删除 |
| GET | `/api/admin/orders` | 订单列表 |
| GET | `/api/admin/orders/{id}` | 订单详情 |
| PUT | `/api/admin/orders/{id}/status` | 更新订单状态 |
| GET | `/api/admin/users` | 用户列表 |
| PUT | `/api/admin/users/{id}/status` | 启用/停用用户 |
| GET | `/api/admin/reviews` | 评价列表 |
| PUT | `/api/admin/reviews/{id}/status` | 展示/隐藏 |
| DELETE | `/api/admin/reviews/{id}` | 删除评价 |
| POST | `/api/admin/upload` | 上传商品图片 |

订单状态：`0 待制作` → `1 制作中` → `2 待取餐` → `3 已完成`；`4 已取消`。

---

## 本次重构修复与优化

- 统一请求配置，移除页面内散落的 `127.0.0.1` 硬编码
- 用真实 JWT 登录替换原有的「本地缓存假登录」
- 搜索改为服务端检索，修复原先只过滤已加载分页的问题
- 下拉刷新同时刷新推荐与列表，并正确结束刷新态
- 「立即购买」接入规格、数量、结算与下单，不再是空 Toast
- 新增购物车、订单、取餐、评价完整业务闭环
- 修复「我的」页重复导航栏、硬编码留言数与登录态不一致
- 统一图片地址解析，兼容本地上传与远程图片
- 补齐加载骨架、空状态、错误提示、登录失效跳转
- 后台采用统一的暗金设计语言，并完成桌面/窄屏适配

---

## 设计说明

- **色彩**：深咖啡黑 `#0B0908`、香槟金 `#C8A97E`、暖白 `#F4EEE6`
- **字体**：英文展示使用 Didot / Times 系，正文使用 PingFang / Avenir Next
- **质感**：克制留白、细金线、毛玻璃底栏、暗角与肌理噪点
- **后台**：侧边品牌印记 + 衬线数字 + 低饱和状态色，与小程序保持同一套视觉语言

---

## 部署提示

- 后端默认上下文路径为 `/api`，静态图片位于 `/api/images/**`，上传图片位于 `/api/uploads/**`
- 生产环境请修改 `JWT_SECRET`、数据库密码，并关闭不必要的跨域来源
- 建议 Nginx 托管 `coffee-admin/dist`，并将 `/api` 反向代理到 Spring Boot
- 当前支付方式为「到店支付」，未接入微信支付；如需线上支付，可在订单状态机中增加支付态
- 小程序上线前需配置 HTTPS 域名与 request 合法域名，并将 `utils/config.js` 切换到生产地址

---

GXUFL Coffee Atelier · Coffee, carefully made.
