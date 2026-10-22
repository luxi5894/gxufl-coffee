<template>
  <div class="page">
    <div class="panel">
      <div class="toolbar">
        <div>
          <p class="eyebrow">Guests</p>
          <h3 class="panel-title">客人名录</h3>
        </div>
        <div class="spacer" />
        <el-input
          v-model="query.keyword"
          class="search-input"
          placeholder="搜索昵称或手机号"
          clearable
          @keyup.enter="reload"
          @clear="reload"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" row-key="id">
          <el-table-column label="客人" min-width="220">
            <template #default="{ row }">
              <div class="guest-cell">
                <span class="avatar">{{ initial(row.nickname) }}</span>
                <div>
                  <div class="name">{{ row.nickname }}</div>
                  <div class="sub mono">{{ maskPhone(row.phone) }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="完整手机号" min-width="150">
            <template #default="{ row }">
              <span class="mono muted">{{ row.phone }}</span>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" min-width="170">
            <template #default="{ row }">
              <span class="muted">{{ row.createdAt }}</span>
            </template>
          </el-table-column>
          <el-table-column label="账号状态" width="130" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status === 1"
                @change="(val) => onToggle(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="会员身份" width="120">
            <template #default>
              <span class="member-tag">校园客人</span>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!loading && !list.length" class="empty-hint">
          <span class="mark">“</span>
          还没有匹配的客人
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { confirmAction } from '@/utils/confirm'
import { getUsers, updateUserStatus } from '@/api'
import { initial, maskPhone } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, pageSize: 10 })

const load = async () => {
  loading.value = true
  try {
    const data = await getUsers(query)
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

const onToggle = async (row, value) => {
  const status = value ? 1 : 0
  if (!status) {
    const ok = await confirmAction(`停用「${row.nickname}」后，该账号将无法登录与下单。`, '停用账号', {
      confirmButtonText: '停用'
    })
    if (!ok) return
  }
  await updateUserStatus(row.id, status)
  row.status = status
  ElMessage.success(status ? '账号已启用' : '账号已停用')
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

.guest-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1px solid var(--line);
  background: var(--gold-dim);
  color: var(--gold);
  font-family: var(--serif);
  font-size: 17px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.name {
  font-size: 13px;
}

.sub {
  font-size: 10px;
  color: var(--muted-2);
  margin-top: 3px;
}

.member-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 999px;
  border: 1px solid var(--line-soft);
  color: var(--muted);
  font-size: 10px;
  letter-spacing: 0.04em;
}
</style>
