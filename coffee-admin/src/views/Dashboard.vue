<template>
  <div v-loading="loading" class="page">
    <div class="metric-grid stagger">
      <div class="panel metric-card">
        <div class="metric-label">
          <span>今日营业额</span>
          <el-icon class="icon"><Coin /></el-icon>
        </div>
        <div class="metric-value">¥{{ formatMoney(stats.todayRevenue) }}</div>
        <div class="metric-foot">
          <i class="dot" />
          今日 {{ stats.todayOrders }} 笔有效订单
        </div>
      </div>

      <div class="panel metric-card">
        <div class="metric-label">
          <span>今日订单</span>
          <el-icon class="icon"><Tickets /></el-icon>
        </div>
        <div class="metric-value">
          {{ stats.todayOrders }}<span class="unit">单</span>
        </div>
        <div class="metric-foot">
          <i class="dot" />
          {{ stats.pendingOrders }} 单待处理
        </div>
      </div>

      <div class="panel metric-card">
        <div class="metric-label">
          <span>注册客人</span>
          <el-icon class="icon"><User /></el-icon>
        </div>
        <div class="metric-value">
          {{ stats.totalUsers }}<span class="unit">位</span>
        </div>
        <div class="metric-foot">
          <i class="dot" />
          今日新增 {{ stats.todayUsers }} 位
        </div>
      </div>

      <div class="panel metric-card">
        <div class="metric-label">
          <span>累计营业额</span>
          <el-icon class="icon"><TrendCharts /></el-icon>
        </div>
        <div class="metric-value">¥{{ formatCompactMoney(stats.totalRevenue) }}</div>
        <div class="metric-foot">
          <i class="dot" />
          累计 {{ stats.totalOrders }} 笔订单
        </div>
      </div>
    </div>

    <div class="dash-grid">
      <section class="panel">
        <div class="card-head">
          <div>
            <p class="eyebrow">Last 7 Days</p>
            <h3>营业额走势</h3>
          </div>
          <span class="hint">不含已取消订单</span>
        </div>
        <div ref="trendRef" class="chart-box" />
      </section>

      <section class="panel">
        <div class="card-head">
          <div>
            <p class="eyebrow">Order States</p>
            <h3>订单状态分布</h3>
          </div>
        </div>
        <div ref="statusRef" class="chart-box small" />
      </section>
    </div>

    <div class="dash-grid-bottom">
      <section class="panel">
        <div class="card-head">
          <div>
            <p class="eyebrow">Weekly Favorites</p>
            <h3>本周热销</h3>
          </div>
          <span class="hint">按杯数</span>
        </div>
        <div v-if="stats.topProducts.length" class="top-list">
          <div v-for="(item, index) in stats.topProducts" :key="item.name" class="top-item">
            <span class="top-rank">0{{ index + 1 }}</span>
            <div>
              <div class="top-name">
                <span>{{ item.name }}</span>
                <span>{{ item.quantity }} 杯</span>
              </div>
              <div class="top-bar">
                <i :style="{ width: barWidth(item.quantity) }" />
              </div>
            </div>
            <span class="top-amount">¥{{ formatMoney(item.amount) }}</span>
          </div>
        </div>
        <div v-else class="empty-hint">
          <span class="mark">“</span>
          本周还没有销售记录
        </div>
      </section>

      <section class="panel">
        <div class="card-head">
          <div>
            <p class="eyebrow">Latest Orders</p>
            <h3>最近订单</h3>
          </div>
          <el-button text class="more" @click="router.push('/orders')">
            全部订单
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="table-wrap">
          <el-table :data="stats.recentOrders" size="small">
            <el-table-column label="订单" min-width="140">
              <template #default="{ row }">
                <div class="mono order-no">{{ row.orderNo }}</div>
                <div class="time">{{ shortDate(row.createdAt) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="客人" min-width="110">
              <template #default="{ row }">
                <div class="guest">{{ row.userNickname || '—' }}</div>
                <div class="time">{{ maskPhone(row.userPhone) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="100">
              <template #default="{ row }">
                <span class="price">¥{{ formatMoney(row.totalAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <span class="status-pill" :class="`status-${row.status}`">
                  {{ ORDER_STATUS[row.status]?.label }}
                </span>
              </template>
            </el-table-column>
            <el-table-column width="74" align="right">
              <template #default="{ row }">
                <el-button
                  v-if="[0, 1, 2].includes(row.status)"
                  text
                  class="handle"
                  @click="router.push({ path: '/orders', query: { status: row.status } })"
                >
                  处理
                </el-button>
                <span v-else class="muted-dash">—</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Coin, Tickets, User, TrendCharts, ArrowRight } from '@element-plus/icons-vue'
import { getStats } from '@/api'
import {
  ORDER_STATUS,
  formatMoney,
  formatCompactMoney,
  shortDate,
  maskPhone
} from '@/utils/format'

const router = useRouter()
const loading = ref(true)
const trendRef = ref()
const statusRef = ref()
let trendChart = null
let statusChart = null

const stats = reactive({
  todayOrders: 0,
  todayRevenue: 0,
  todayUsers: 0,
  totalUsers: 0,
  totalOrders: 0,
  totalRevenue: 0,
  pendingOrders: 0,
  productCount: 0,
  trend: [],
  topProducts: [],
  statusDistribution: [],
  recentOrders: []
})

const maxQuantity = computed(() =>
  Math.max(1, ...stats.topProducts.map((item) => Number(item.quantity || 0)))
)

const barWidth = (quantity) => `${Math.max(6, (Number(quantity) / maxQuantity.value) * 100)}%`

const renderTrend = () => {
  if (!trendRef.value) return
  trendChart = trendChart || echarts.init(trendRef.value)
  const dates = stats.trend.map((item) => item.date.slice(5))
  const revenue = stats.trend.map((item) => Number(item.revenue || 0))
  const orders = stats.trend.map((item) => Number(item.orders || 0))

  trendChart.setOption({
    backgroundColor: 'transparent',
    grid: { left: 54, right: 24, top: 34, bottom: 34 },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#191512',
      borderColor: 'rgba(200,169,126,.22)',
      textStyle: { color: '#f3ede4', fontSize: 12 },
      axisPointer: { lineStyle: { color: 'rgba(200,169,126,.25)' } }
    },
    legend: {
      right: 24,
      top: 6,
      itemWidth: 10,
      itemHeight: 2,
      textStyle: { color: '#9c9287', fontSize: 10 },
      data: ['营业额', '订单数']
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: 'rgba(200,169,126,.14)' } },
      axisTick: { show: false },
      axisLabel: { color: '#6f665e', fontSize: 10 }
    },
    yAxis: [
      {
        type: 'value',
        splitLine: { lineStyle: { color: 'rgba(255,255,255,.04)' } },
        axisLabel: { color: '#6f665e', fontSize: 10 }
      },
      {
        type: 'value',
        splitLine: { show: false },
        axisLabel: { color: '#6f665e', fontSize: 10 }
      }
    ],
    series: [
      {
        name: '营业额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: revenue,
        lineStyle: { width: 2, color: '#c8a97e' },
        itemStyle: { color: '#e4c99f', borderColor: '#0b0908', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(200,169,126,.28)' },
            { offset: 1, color: 'rgba(200,169,126,0)' }
          ])
        }
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'none',
        data: orders,
        lineStyle: { width: 1, type: 'dashed', color: 'rgba(243,237,228,.28)' }
      }
    ]
  })
}

const renderStatus = () => {
  if (!statusRef.value) return
  statusChart = statusChart || echarts.init(statusRef.value)
  const colors = ['#c9a25f', '#9db4c0', '#8fa98a', '#8b8378', '#b26a5c']
  statusChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: '#191512',
      borderColor: 'rgba(200,169,126,.22)',
      textStyle: { color: '#f3ede4', fontSize: 12 }
    },
    legend: {
      bottom: 6,
      left: 'center',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: '#9c9287', fontSize: 10 },
      itemGap: 14
    },
    title: {
      text: String(stats.totalOrders),
      subtext: '累计订单',
      left: '49%',
      top: '35%',
      textAlign: 'center',
      textStyle: {
        color: '#f3ede4',
        fontFamily: 'Georgia, serif',
        fontSize: 30,
        fontWeight: 400
      },
      subtextStyle: { color: '#6f665e', fontSize: 10, letterSpacing: 2 }
    },
    series: [
      {
        type: 'pie',
        radius: ['57%', '76%'],
        center: ['50%', '43%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderColor: '#14100e',
          borderWidth: 3,
          borderRadius: 3
        },
        label: { show: false },
        data: stats.statusDistribution.map((item, index) => ({
          name: ORDER_STATUS[index]?.label || '未知',
          value: item.total,
          itemStyle: { color: colors[index] }
        }))
      }
    ]
  })
}

const load = async () => {
  loading.value = true
  try {
    const data = await getStats()
    Object.assign(stats, data)
    await nextTick()
    renderTrend()
    renderStatus()
  } finally {
    loading.value = false
  }
}

const onResize = () => {
  trendChart?.resize()
  statusChart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  statusChart?.dispose()
})
</script>

<style scoped>
.order-no {
  font-size: 11px;
  color: var(--text);
}

.time {
  font-size: 10px;
  color: var(--muted-2);
  margin-top: 3px;
}

.guest {
  font-size: 12px;
}

.more {
  color: var(--gold);
  font-size: 11px;
  letter-spacing: 0.04em;
}

.handle {
  color: var(--gold);
  font-size: 11px;
}

.muted-dash {
  color: var(--muted-2);
  font-size: 11px;
}

@media (max-width: 760px) {
  .dash-grid-bottom {
    grid-template-columns: 1fr;
  }
}
</style>
