<template>
  <div class="page">
    <div class="panel">
      <div class="toolbar orders-toolbar">
        <div class="status-filter">
          <button
            v-for="tab in statusTabs"
            :key="tab.value"
            type="button"
            class="pill"
            :class="{ active: statusTab === tab.value }"
            @click="switchStatus(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>
        <div class="spacer" />
        <el-input
          v-model="query.keyword"
          class="search-input"
          placeholder="订单号 / 昵称 / 手机号"
          clearable
          @keyup.enter="reload"
          @clear="reload"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <button class="ghost-icon-btn" title="刷新" @click="load">
          <el-icon><Refresh /></el-icon>
        </button>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" row-key="id">
          <el-table-column label="订单" min-width="170">
            <template #default="{ row }">
              <div class="mono order-no">{{ row.orderNo }}</div>
              <div class="time">{{ shortDate(row.createdAt) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="客人" min-width="130">
            <template #default="{ row }">
              <div class="guest">{{ row.userNickname || '—' }}</div>
              <div class="time">{{ maskPhone(row.userPhone) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="商品" min-width="220">
            <template #default="{ row }">
              <div class="items-cell">
                <div class="thumb-stack">
                  <img
                    v-for="(item, index) in row.items.slice(0, 3)"
                    :key="item.id"
                    :src="assetUrl(item.productImage)"
                    :style="{ zIndex: 3 - index }"
                    alt=""
                  />
                </div>
                <div class="items-copy">
                  <div class="items-name">{{ itemsSummary(row) }}</div>
                  <div class="time">共 {{ row.itemCount }} 件</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{ row }">
              <span class="price">¥{{ formatMoney(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="取餐号" width="94">
            <template #default="{ row }">
              <span class="pickup-code">{{ row.pickupCode }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="104">
            <template #default="{ row }">
              <span class="status-pill" :class="`status-${row.status}`">
                {{ ORDER_STATUS[row.status]?.label }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="right">
            <template #default="{ row }">
              <el-button
                v-if="ORDER_STATUS[row.status]?.next !== null"
                text
                class="link-btn"
                @click="advance(row)"
              >
                {{ ORDER_STATUS[row.status].action }}
              </el-button>
              <el-button
                v-if="[0, 1].includes(row.status)"
                text
                class="link-btn danger"
                @click="cancel(row)"
              >
                取消
              </el-button>
              <el-button text class="link-btn subtle" @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!loading && !list.length" class="empty-hint">
          <span class="mark">“</span>
          这个状态下暂时没有订单
        </div>
      </div>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="load"
        />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" title="订单详情" size="420px" destroy-on-close>
      <div v-if="current" class="order-detail">
        <div class="detail-meta">
          <div class="meta-item">
            <label>订单号</label>
            <span class="mono">{{ current.orderNo }}</span>
          </div>
          <div class="meta-item">
            <label>下单时间</label>
            <span>{{ current.createdAt }}</span>
          </div>
          <div class="meta-item">
            <label>客人</label>
            <span>{{ current.userNickname || '—' }} · {{ maskPhone(current.userPhone) }}</span>
          </div>
          <div class="meta-item">
            <label>取餐号</label>
            <span class="pickup-code">{{ current.pickupCode }}</span>
          </div>
          <div class="meta-item" v-if="current.remark">
            <label>备注</label>
            <span>{{ current.remark }}</span>
          </div>
          <div class="meta-item">
            <label>状态</label>
            <span class="status-pill" :class="`status-${current.status}`">
              {{ ORDER_STATUS[current.status]?.label }}
            </span>
          </div>
        </div>

        <div class="detail-items">
          <div v-for="item in current.items" :key="item.id" class="detail-item">
            <img :src="assetUrl(item.productImage)" :alt="item.productName" />
            <div class="info">
              <div class="name">{{ item.productName }}</div>
              <div class="spec">{{ item.specName || '标准规格' }} · ¥{{ formatMoney(item.price) }}</div>
            </div>
            <span class="muted">×{{ item.quantity }}</span>
          </div>
        </div>

        <div class="detail-total">
          <span class="label">合计</span>
          <span class="value">¥{{ formatMoney(current.totalAmount) }}</span>
        </div>

        <div class="drawer-actions">
          <el-button
            v-if="[0, 1].includes(current.status)"
            @click="cancel(current, true)"
          >
            取消订单
          </el-button>
          <el-button
            v-if="ORDER_STATUS[current.status]?.next !== null"
            type="primary"
            @click="advance(current, true)"
          >
            {{ ORDER_STATUS[current.status].action }}
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { confirmAction } from '@/utils/confirm'
import { getOrders, getOrder, updateOrderStatus } from '@/api'
import { ORDER_STATUS, assetUrl, formatMoney, shortDate, maskPhone } from '@/utils/format'

const route = useRoute()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const statusTabs = [
  { label: '全部订单', value: 'all' },
  { label: '待制作', value: '0' },
  { label: '制作中', value: '1' },
  { label: '待取餐', value: '2' },
  { label: '已完成', value: '3' },
  { label: '已取消', value: '4' }
]
const statusTab = ref(route.query.status !== undefined ? String(route.query.status) : 'all')
const query = reactive({ keyword: '', page: 1, pageSize: 10 })

const drawerVisible = ref(false)
const current = ref(null)

const itemsSummary = (row) => {
  if (!row.items?.length) return '—'
  const first = row.items[0]
  return row.items.length > 1 ? `${first.productName} 等 ${row.items.length} 款` : first.productName
}

const buildParams = () => ({
  ...query,
  status: statusTab.value === 'all' ? undefined : Number(statusTab.value)
})

const load = async () => {
  loading.value = true
  try {
    const data = await getOrders(buildParams())
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const reload = () => {
  query.page = 1
  load()
}

const switchStatus = (value) => {
  statusTab.value = value
  reload()
}

const advance = async (row, fromDrawer = false) => {
  const next = ORDER_STATUS[row.status]?.next
  if (next === null || next === undefined) return
  const label = ORDER_STATUS[next].label
  const ok = await confirmAction(`将订单 ${row.orderNo} 更新为「${label}」？`, '更新订单', {
    type: 'info',
    confirmButtonText: '确认更新'
  })
  if (!ok) return
  await updateOrderStatus(row.id, next)
  ElMessage.success(`订单已更新为${label}`)
  await load()
  if (fromDrawer || drawerVisible.value) {
    current.value = await getOrder(row.id)
  }
}

const cancel = async (row, fromDrawer = false) => {
  const ok = await confirmAction(`确定取消订单 ${row.orderNo}？库存将自动回补。`, '取消订单', {
    confirmButtonText: '取消订单'
  })
  if (!ok) return
  await updateOrderStatus(row.id, 4)
  ElMessage.success('订单已取消')
  await load()
  if (fromDrawer || drawerVisible.value) {
    current.value = await getOrder(row.id)
  }
}

const openDetail = async (row) => {
  current.value = await getOrder(row.id)
  drawerVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.orders-toolbar {
  padding-top: 18px;
  padding-bottom: 16px;
}

.status-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 3px;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
}

.pill {
  border: none;
  background: transparent;
  color: var(--muted);
  font-size: 11px;
  letter-spacing: 0.04em;
  padding: 7px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pill:hover {
  color: var(--text);
}

.pill.active {
  background: var(--gold-dim);
  color: var(--gold-bright);
  box-shadow: inset 0 0 0 1px rgba(200, 169, 126, 0.18);
}

.order-no {
  font-size: 11px;
}

.time {
  font-size: 10px;
  color: var(--muted-2);
  margin-top: 3px;
}

.guest {
  font-size: 12px;
}

.items-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.thumb-stack {
  display: flex;
  flex-shrink: 0;
}

.thumb-stack img {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  object-fit: cover;
  border: 2px solid var(--panel);
  margin-left: -10px;
}

.thumb-stack img:first-child {
  margin-left: 0;
}

.items-name {
  font-size: 12px;
  color: var(--text);
  max-width: 170px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pickup-code {
  font-family: var(--serif);
  font-size: 20px;
  color: var(--gold-bright);
  letter-spacing: 0.08em;
}

.link-btn {
  color: var(--gold);
  font-size: 12px;
  padding: 0 4px;
}

.link-btn.danger {
  color: var(--wine);
}

.link-btn.subtle {
  color: var(--muted);
}

.drawer-actions {
  display: flex;
  gap: 10px;
  margin-top: 24px;
}

.drawer-actions .el-button {
  flex: 1;
}
</style>
