<template>
  <div class="page">
    <div class="panel">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          class="search-input"
          placeholder="搜索商品名称、标签..."
          clearable
          @keyup.enter="reload"
          @clear="reload"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="query.categoryId" class="filter-select" placeholder="全部分类" clearable @change="reload">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <div class="spacer" />
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          新增商品
        </el-button>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" row-key="id">
          <el-table-column label="商品" min-width="260">
            <template #default="{ row }">
              <div class="product-cell">
                <img class="product-thumb" :src="assetUrl(row.image)" :alt="row.name" />
                <div>
                  <div class="product-name">{{ row.name }}</div>
                  <div class="product-sub">{{ row.subtitle || row.categoryName }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="分类" width="100">
            <template #default="{ row }">
              <span class="muted">{{ row.categoryName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="价格" width="100">
            <template #default="{ row }">
              <span class="price">¥{{ formatMoney(row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="库存" width="80">
            <template #default="{ row }">
              <span :class="{ low: row.stock <= 10 }">{{ row.stock }}</span>
            </template>
          </el-table-column>
          <el-table-column label="销量" width="80">
            <template #default="{ row }">
              <span class="mono muted">{{ row.sales }}</span>
            </template>
          </el-table-column>
          <el-table-column label="评分" width="80">
            <template #default="{ row }">
              <span class="rating">★ {{ Number(row.rating).toFixed(1) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="推荐" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.recommended === 1"
                @change="(val) => onToggle(row, 'recommended', val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="上架" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status === 1"
                @change="(val) => onToggle(row, 'status', val)"
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
          暂时没有找到商品
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

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑商品' : '新增商品'"
      width="760px"
      destroy-on-close
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <p class="form-section-title">基础信息</p>
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" maxlength="40" placeholder="例如：越南蛋咖啡" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="副标题">
              <el-input v-model="form.subtitle" maxlength="60" placeholder="一句有画面的描述" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="所属分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%">
                <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="售价（元）" prop="price">
              <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价（元）">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="18">
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="0" :max="99999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序（越小越前）">
              <el-input-number v-model="form.sort" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="推荐">
              <el-switch v-model="form.recommended" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="上架">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>

        <p class="form-section-title">视觉与风味</p>
        <el-form-item label="商品图片（第一张为主图）">
          <div class="uploader-grid">
            <div v-for="(img, index) in form.images" :key="img + index" class="image-tile">
              <img :src="assetUrl(img)" alt="" />
              <span v-if="index === 0" class="main-tag">主图</span>
              <button type="button" class="remove" @click="removeImage(index)">×</button>
            </div>
            <el-upload
              :show-file-list="false"
              accept="image/*"
              :http-request="handleUpload"
              class="upload-tile-wrap"
            >
              <div class="image-tile add" :class="{ uploading: uploadLoading }">
                <el-icon><Plus /></el-icon>
                <span>{{ uploadLoading ? '上传中' : '上传图片' }}</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="标签">
              <el-select
                v-model="form.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入标签"
                style="width: 100%"
              >
                <el-option v-for="tag in tagOptions" :key="tag" :label="tag" :value="tag" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="温度">
              <el-select
                v-model="form.temperature"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="例如：热饮 / 冰饮"
                style="width: 100%"
              >
                <el-option label="热饮" value="热饮" />
                <el-option label="冰饮" value="冰饮" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="产地">
              <el-input v-model="form.origin" placeholder="例如：越南 · 岘港" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工艺">
              <el-input v-model="form.process" placeholder="例如：滴漏萃取 + 蛋黄奶沫" />
            </el-form-item>
          </el-col>
        </el-row>

        <p class="form-section-title">规格与加价</p>
        <div class="spec-row" v-for="(spec, index) in form.specs" :key="index">
          <el-input v-model="spec.name" placeholder="规格名称，例如：大杯 473ml" />
          <el-input-number v-model="spec.priceDelta" :min="0" :precision="2" :step="1" style="width: 100%" />
          <button type="button" class="spec-remove" @click="form.specs.splice(index, 1)">
            <el-icon><Delete /></el-icon>
          </button>
        </div>
        <button type="button" class="spec-add" @click="addSpec">+ 添加规格</button>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存商品</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Delete } from '@element-plus/icons-vue'
import { confirmAction } from '@/utils/confirm'
import {
  getProducts,
  getCategories,
  createProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus,
  updateProductRecommended,
  uploadImage
} from '@/api'
import { assetUrl, formatMoney } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const query = reactive({ keyword: '', categoryId: null, page: 1, pageSize: 10 })

const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const saving = ref(false)
const uploadLoading = ref(false)
const tagOptions = ['招牌', '热销', '珍品', '经典', '人气', '特色']

const createForm = () => ({
  name: '',
  subtitle: '',
  categoryId: null,
  price: 25,
  originalPrice: null,
  stock: 200,
  sort: 0,
  recommended: 0,
  status: 1,
  description: '',
  images: [],
  tags: [],
  temperature: [],
  origin: '',
  process: '',
  specs: [{ name: '标准杯', priceDelta: 0 }]
})

const form = reactive(createForm())
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const data = await getProducts(query)
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

const loadCategories = async () => {
  categories.value = await getCategories()
}

const openCreate = () => {
  editingId.value = null
  Object.assign(form, createForm())
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    subtitle: row.subtitle || '',
    categoryId: row.categoryId,
    price: Number(row.price),
    originalPrice: row.originalPrice ? Number(row.originalPrice) : null,
    stock: row.stock,
    sort: row.sort || 0,
    recommended: row.recommended ?? 0,
    status: row.status ?? 1,
    description: row.description || '',
    images: [...(row.images || [])],
    tags: [...(row.tags || [])],
    temperature: [...(row.temperature || [])],
    origin: row.origin || '',
    process: row.process || '',
    specs: (row.specs || []).map((spec) => ({
      id: spec.id,
      name: spec.name,
      priceDelta: Number(spec.priceDelta || 0)
    }))
  })
  if (!form.images.length && row.image) form.images = [row.image]
  if (!form.specs.length) form.specs = [{ name: '标准杯', priceDelta: 0 }]
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const addSpec = () => form.specs.push({ name: '', priceDelta: 0 })

const removeImage = (index) => {
  form.images.splice(index, 1)
}

const handleUpload = async ({ file }) => {
  uploadLoading.value = true
  try {
    const data = await uploadImage(file)
    form.images.push(data.url)
    ElMessage.success('图片上传成功')
  } finally {
    uploadLoading.value = false
  }
}

const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = {
      name: form.name,
      subtitle: form.subtitle,
      categoryId: form.categoryId,
      price: form.price,
      originalPrice: form.originalPrice,
      stock: form.stock,
      sort: form.sort,
      recommended: form.recommended,
      status: form.status,
      description: form.description,
      images: form.images,
      image: form.images[0] || '',
      tags: form.tags,
      temperature: form.temperature,
      origin: form.origin,
      process: form.process,
      specs: form.specs.filter((spec) => spec.name && spec.name.trim())
    }
    if (editingId.value) {
      await updateProduct(editingId.value, payload)
      ElMessage.success('商品已更新')
    } else {
      await createProduct(payload)
      ElMessage.success('商品已创建')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const onToggle = async (row, field, value) => {
  try {
    const status = value ? 1 : 0
    if (field === 'status') {
      await updateProductStatus(row.id, status)
      row.status = status
    } else {
      await updateProductRecommended(row.id, status)
      row.recommended = status
    }
    ElMessage.success('状态已更新')
  } catch (error) {
    // interceptor already reports the error
  }
}

const remove = async (row) => {
  const ok = await confirmAction(
    `确定删除「${row.name}」吗？相关评价与购物车记录也会一并清理。`,
    '删除商品',
    { confirmButtonText: '删除' }
  )
  if (!ok) return
  await deleteProduct(row.id)
  ElMessage.success('已删除')
  if (list.value.length === 1 && query.page > 1) query.page -= 1
  load()
}

const onDialogClosed = () => {
  Object.assign(form, createForm())
}

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.low {
  color: var(--wine);
}

.rating {
  color: var(--gold);
  font-size: 12px;
}

.link-btn {
  color: var(--gold);
  font-size: 12px;
  padding: 0 4px;
}

.link-btn.danger {
  color: var(--wine);
}

.uploader-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-tile {
  width: 92px;
  height: 92px;
  border-radius: 10px;
  border: 1px solid var(--line-soft);
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.02);
}

.image-tile img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-tile.add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: var(--muted-2);
  font-size: 10px;
  cursor: pointer;
  border-style: dashed;
  transition: all 0.2s;
}

.image-tile.add:hover {
  color: var(--gold);
  border-color: var(--gold);
  background: var(--gold-dim);
}

.image-tile.add .el-icon {
  font-size: 18px;
}

.main-tag {
  position: absolute;
  left: 5px;
  bottom: 5px;
  font-size: 9px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(11, 9, 8, 0.8);
  color: var(--gold);
  letter-spacing: 0.05em;
}

.remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: none;
  background: rgba(11, 9, 8, 0.78);
  color: #f3ede4;
  line-height: 1;
  cursor: pointer;
  font-size: 13px;
}

.spec-remove {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  border: 1px solid var(--line-soft);
  background: transparent;
  color: var(--muted-2);
  cursor: pointer;
}

.spec-remove:hover {
  color: var(--wine);
  border-color: rgba(178, 106, 92, 0.4);
}
</style>
