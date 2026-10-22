<template>
  <div class="shell" :class="{ collapsed }">
    <aside class="sidebar">
      <div class="aside-top">
        <BrandMark :compact="collapsed" />
        <button class="fold" :aria-label="collapsed ? '展开菜单' : '收起菜单'" @click="collapsed = !collapsed">
          <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
        </button>
      </div>

      <nav class="nav">
        <router-link
          v-for="item in navItems"
          :key="item.name"
          :to="item.path"
          class="nav-item"
          :class="{ active: route.name === item.name }"
        >
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span class="nav-label">{{ item.label }}</span>
          <span class="nav-dot" />
        </router-link>
      </nav>

      <div class="aside-foot">
        <div class="store-card">
          <span class="mark">N</span>
          <div v-if="!collapsed" class="store-copy">
            <strong>南宁 · 东盟校区</strong>
            <small>广西外国语学院咖啡厅</small>
          </div>
        </div>
        <p v-if="!collapsed" class="aside-note">Coffee, carefully made.</p>
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="head-left">
          <button class="mobile-menu ghost-icon-btn" aria-label="打开菜单" @click="drawerVisible = true">
            <el-icon><Menu /></el-icon>
          </button>
          <div class="head-copy">
            <p class="eyebrow">{{ eyebrow }}</p>
            <h1>{{ route.meta.title }}</h1>
            <p class="sub">{{ route.meta.subtitle }}</p>
          </div>
        </div>
        <div class="head-actions">
          <div class="date-block">
            <span>{{ dateText }}</span>
            <small><i class="live-dot" /> 门店营业中</small>
          </div>
          <el-dropdown trigger="click" @command="onCommand">
            <div class="admin-chip">
              <div class="avatar">{{ initial(auth.user?.nickname) }}</div>
              <div class="chip-copy">
                <strong>{{ auth.user?.nickname || '管理员' }}</strong>
                <small>Administrator</small>
              </div>
              <el-icon class="chev"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <el-drawer v-model="drawerVisible" direction="ltr" size="264px" :with-header="false">
      <div class="mobile-nav">
        <BrandMark />
        <nav class="mobile-links">
          <router-link
            v-for="item in navItems"
            :key="item.name"
            :to="item.path"
            class="mobile-link"
            :class="{ active: route.name === item.name }"
            @click="drawerVisible = false"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            {{ item.label }}
          </router-link>
        </nav>
        <p class="mobile-note">GXUFL Coffee · 南宁东盟校区</p>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Odometer,
  CoffeeCup,
  PriceTag,
  Tickets,
  User,
  ChatDotRound,
  Fold,
  Expand,
  ArrowDown,
  SwitchButton,
  Menu
} from '@element-plus/icons-vue'
import BrandMark from '@/components/BrandMark.vue'
import { useAuthStore } from '@/stores/auth'
import { initial } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(false)
const drawerVisible = ref(false)

const navItems = [
  { name: 'dashboard', path: '/dashboard', label: '经营概览', icon: Odometer },
  { name: 'products', path: '/products', label: '商品管理', icon: CoffeeCup },
  { name: 'categories', path: '/categories', label: '分类管理', icon: PriceTag },
  { name: 'orders', path: '/orders', label: '订单管理', icon: Tickets },
  { name: 'users', path: '/users', label: '用户管理', icon: User },
  { name: 'reviews', path: '/reviews', label: '评价管理', icon: ChatDotRound }
]

const eyebrow = computed(() => {
  const map = {
    dashboard: 'Overview',
    products: 'Menu',
    categories: 'Collections',
    orders: 'Orders',
    users: 'Guests',
    reviews: 'Voices'
  }
  return map[route.name] || 'Console'
})

const dateText = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
}).format(new Date())

const onCommand = (command) => {
  if (command === 'logout') {
    auth.clear()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}
</script>

<style scoped>
.shell {
  display: flex;
  min-height: 100vh;
  background:
    radial-gradient(70% 50% at 85% -10%, rgba(200, 169, 126, 0.075), transparent 60%),
    var(--ink);
}

.sidebar {
  width: 248px;
  flex-shrink: 0;
  border-right: 1px solid var(--line-soft);
  display: flex;
  flex-direction: column;
  padding: 26px 18px 22px;
  position: sticky;
  top: 0;
  height: 100vh;
  background: linear-gradient(180deg, rgba(20, 16, 14, 0.92), rgba(11, 9, 8, 0.96));
  transition: width 0.28s ease, padding 0.28s ease;
  z-index: 10;
}

.aside-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 2px 6px 28px;
}

.fold {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--muted-2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.fold:hover {
  color: var(--gold);
  border-color: var(--line);
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 44px;
  padding: 0 14px;
  border-radius: 10px;
  color: var(--muted);
  font-size: 13px;
  letter-spacing: 0.04em;
  transition: all 0.2s ease;
  overflow: hidden;
}

.nav-item:hover {
  color: var(--text);
  background: rgba(255, 255, 255, 0.028);
}

.nav-icon {
  font-size: 17px;
  flex-shrink: 0;
}

.nav-label {
  flex: 1;
  white-space: nowrap;
}

.nav-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: transparent;
}

.nav-item.active {
  color: var(--gold-bright);
  background: linear-gradient(90deg, rgba(200, 169, 126, 0.14), rgba(200, 169, 126, 0.02));
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 2px;
  border-radius: 2px;
  background: var(--gold);
}

.nav-item.active .nav-dot {
  background: var(--gold);
}

.aside-foot {
  border-top: 1px solid var(--line-soft);
  padding-top: 18px;
}

.store-card {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 0 8px;
}

.store-card .mark {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px solid var(--line);
  color: var(--gold);
  font-family: var(--serif);
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.store-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  white-space: nowrap;
}

.store-copy strong {
  font-size: 11px;
  font-weight: 500;
  color: var(--text);
}

.store-copy small {
  font-size: 9px;
  letter-spacing: 0.06em;
  color: var(--muted-2);
}

.aside-note {
  margin: 16px 8px 0;
  font-family: var(--serif);
  font-size: 10px;
  font-style: italic;
  color: var(--muted-2);
  letter-spacing: 0.04em;
  white-space: nowrap;
}

.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 9;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 34px 18px;
  background: linear-gradient(180deg, rgba(11, 9, 8, 0.94), rgba(11, 9, 8, 0.72) 70%, transparent);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid transparent;
}

.head-left {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.mobile-menu {
  display: none;
  margin-bottom: 8px;
}

.head-copy h1 {
  margin: 7px 0 5px;
  font-family: var(--serif);
  font-size: 32px;
  font-weight: 400;
  line-height: 1;
}

.head-copy .sub {
  margin: 0;
  font-size: 12px;
  color: var(--muted-2);
  letter-spacing: 0.02em;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 22px;
}

.date-block {
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 11px;
  color: var(--muted);
  letter-spacing: 0.04em;
}

.date-block small {
  font-size: 10px;
  color: var(--muted-2);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
}

.live-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--sage);
  box-shadow: 0 0 0 3px rgba(143, 169, 138, 0.12);
}

.admin-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px 6px 6px;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  outline: none;
}

.admin-chip:hover {
  border-color: var(--line);
  background: rgba(255, 255, 255, 0.02);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(145deg, #b99261, #dcc099);
  color: #15100c;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--serif);
  font-size: 15px;
  flex-shrink: 0;
}

.chip-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1;
}

.chip-copy strong {
  font-size: 11px;
  font-weight: 500;
}

.chip-copy small {
  font-size: 8px;
  color: var(--muted-2);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.chev {
  color: var(--muted-2);
  font-size: 12px;
}

.content {
  flex: 1;
  min-height: 0;
}

.collapsed .sidebar {
  width: 82px;
  padding-left: 14px;
  padding-right: 14px;
}

.collapsed .aside-top {
  flex-direction: column;
  gap: 14px;
}

.collapsed .nav-item {
  justify-content: center;
  padding: 0;
}

.collapsed .nav-label,
.collapsed .nav-dot {
  display: none;
}

.collapsed .store-card {
  justify-content: center;
  padding: 0;
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.mobile-nav {
  height: 100%;
  padding: 26px 18px;
  background: var(--panel);
  display: flex;
  flex-direction: column;
}

.mobile-links {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 30px;
  flex: 1;
}

.mobile-link {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 44px;
  padding: 0 14px;
  border-radius: 10px;
  color: var(--muted);
  font-size: 13px;
  letter-spacing: 0.04em;
}

.mobile-link.active {
  color: var(--gold-bright);
  background: linear-gradient(90deg, rgba(200, 169, 126, 0.14), rgba(200, 169, 126, 0.02));
}

.mobile-note {
  font-family: var(--serif);
  font-size: 11px;
  color: var(--muted-2);
  letter-spacing: 0.05em;
  border-top: 1px solid var(--line-soft);
  padding-top: 16px;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
}

@media (max-width: 1100px) {
  .sidebar {
    width: 82px;
    padding-left: 14px;
    padding-right: 14px;
  }
  .sidebar .nav-label,
  .sidebar .nav-dot,
  .sidebar :deep(.word),
  .sidebar .store-copy,
  .sidebar .aside-note {
    display: none;
  }
  .aside-top {
    flex-direction: column;
    gap: 14px;
  }
  .nav-item {
    justify-content: center;
    padding: 0;
  }
  .store-card {
    justify-content: center;
    padding: 0;
  }
  .fold {
    display: none;
  }
}

@media (max-width: 760px) {
  .topbar {
    padding: 18px 16px 12px;
    align-items: flex-start;
  }
  .mobile-menu {
    display: inline-flex;
  }
  .date-block {
    display: none;
  }
  .head-copy h1 {
    font-size: 26px;
  }
  .head-actions {
    gap: 10px;
  }
  .chip-copy {
    display: none;
  }
  .sidebar {
    display: none;
  }
}
</style>
