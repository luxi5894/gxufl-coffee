<template>
  <div class="page">
    <div class="panel">
      <div class="toolbar">
        <div>
          <p class="eyebrow">Voices</p>
          <h3 class="panel-title">客人评价</h3>
        </div>
        <div class="spacer" />
        <el-input
          v-model="query.keyword"
          class="search-input"
          placeholder="搜索昵称或评价内容"
          clearable
          @keyup.enter="reload"
          @clear="reload"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="query.status" class="filter-select" placeholder="全部状态" clearable @change="reload">
          <el-option label="展示中" :value="1" />
          <el-option label="已隐藏" :value="0" />
        </el-select>
      </div>

      <div v-loading="loading" class="review-grid" v-if="list.length">
        <article
          v-for="row in list"
          :key="row.id"
          class="review-card"
          :class="{ 'review-hidden': row.status === 0 }"
        >
          <header class="review-head">
            <span class="review-avatar">{{ initial(row.nickname) }}</span>
            <div class="review-user">
              {{ row.nickname }}
              <small>{{ row.createdAt }}</small>
            </div>
            <div class="stars">
              <span v-for="star in 5" :key="star" :class="{ off: star > row.rating }">★</span>
            </div>
          </header>

          <div class="review-product">
            <img :src="assetUrl(row.productImage)" alt="" />
            <span>{{ row.productName }}</span>
          </div>

          <p class="review-content">{{ row.content }}</p>

          <footer class="review-foot">
            <el-switch
              :model-value="row.status === 1"
              active-text="展示"
              @change="(val) => toggle(row, val)"
            />
            <button class="delete-link" @click="remove(row)">
              <el-icon><Delete /></el-icon>
              删除
            </button>
          </footer>
        </article>
      </div>

      <div v-else-if="!loading" class="empty-hint">
        <span class="mark">“</span>
        还没有收到评价
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'
import { confirmAction } from '@/utils/confirm'
import { getReviews, updateReviewStatus, deleteReview } from '@/api'
import { assetUrl, initial } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: null, page: 1, pageSize: 9 })

const load = async () => {
  loading.value = true
  try {
    const data = await getReviews(query)
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

const toggle = async (row, value) => {
  const status = value ? 1 : 0
  await updateReviewStatus(row.id, status)
  row.status = status
  ElMessage.success(status ? '评价已展示' : '评价已隐藏')
}

const remove = async (row) => {
  const ok = await confirmAction('删除后不可恢复，确定删除这条评价吗？', '删除评价', {
    confirmButtonText: '删除'
  })
  if (!ok) return
  await deleteReview(row.id)
  ElMessage.success('已删除')
  if (list.value.length === 1 && query.page > 1) query.page -= 1
  load()
}

onMounted(load)
</script>

<style scoped>
.panel-title {
  font-family: var(--serif);
  font-size: 22px;
  font-weight: 400;
  margin: 6px 0 0;
}

.delete-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: transparent;
  border: none;
  color: var(--muted-2);
  font-size: 11px;
  cursor: pointer;
  transition: color 0.2s;
}

.delete-link:hover {
  color: var(--wine);
}

@media (max-width: 760px) {
  .review-grid {
    grid-template-columns: 1fr;
    padding: 14px;
  }
}
</style>
