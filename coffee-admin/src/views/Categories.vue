<template>
  <div class="page">
    <div class="panel">
      <div class="toolbar">
        <div>
          <p class="eyebrow">Collections</p>
          <h3 class="panel-title">菜单分类</h3>
        </div>
        <div class="spacer" />
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          新增分类
        </el-button>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" row-key="id">
          <el-table-column label="分类" min-width="200">
            <template #default="{ row }">
              <div class="category-cell">
                <span class="icon-badge">{{ row.icon || row.name.slice(0, 1) }}</span>
                <span>{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="英文名" min-width="160">
            <template #default="{ row }">
              <span class="muted mono">{{ row.nameEn || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column label="状态" width="110" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status === 1"
                @change="(val) => onToggle(row, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="right">
            <template #default="{ row }">
              <el-button text class="link-btn" @click="openEdit(row)">编辑</el-button>
              <el-button text class="link-btn danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!loading && !list.length" class="empty-hint">
          <span class="mark">“</span>
          还没有分类，先创建第一个吧
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="460px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" maxlength="20" placeholder="例如：越南" />
        </el-form-item>
        <el-form-item label="英文名">
          <el-input v-model="form.nameEn" maxlength="40" placeholder="例如：VIETNAM" />
        </el-form-item>
        <el-form-item label="图标文字">
          <el-input v-model="form.icon" maxlength="4" placeholder="例如：VN" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { confirmAction } from '@/utils/confirm'
import {
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref()
const form = reactive({ name: '', nameEn: '', icon: '', sort: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    list.value = await getCategories()
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = null
  Object.assign(form, { name: '', nameEn: '', icon: '', sort: list.value.length + 1, status: 1 })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    nameEn: row.nameEn || '',
    icon: row.icon || '',
    sort: row.sort ?? 0,
    status: row.status ?? 1
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, form)
      ElMessage.success('分类已更新')
    } else {
      await createCategory(form)
      ElMessage.success('分类已创建')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const onToggle = async (row, value) => {
  await updateCategory(row.id, { ...row, status: value ? 1 : 0 })
  row.status = value ? 1 : 0
  ElMessage.success('状态已更新')
}

const remove = async (row) => {
  const ok = await confirmAction(`确定删除分类「${row.name}」吗？`, '删除分类', {
    confirmButtonText: '删除'
  })
  if (!ok) return
  await deleteCategory(row.id)
  ElMessage.success('已删除')
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

.category-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.icon-badge {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  border: 1px solid var(--line-soft);
  background: var(--gold-dim);
  color: var(--gold);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  letter-spacing: 0.06em;
}

.link-btn {
  color: var(--gold);
  font-size: 12px;
  padding: 0 4px;
}

.link-btn.danger {
  color: var(--wine);
}
</style>
