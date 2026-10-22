<template>
  <div class="login-shell">
    <section class="visual">
      <div class="visual-bg" :style="{ backgroundImage: `url(${hero})` }" />
      <div class="visual-shade" />
      <div class="visual-top">
        <BrandMark />
        <span class="tag">EST. 2026 · NANNING</span>
      </div>
      <div class="visual-copy">
        <p class="eyebrow">Merchant Console</p>
        <h1>每一杯咖啡，<br />都值得被认真对待。</h1>
        <p class="desc">
          从越南滴漏到苏门答腊麝香猫，<br />
          照看六国风味，也照看每一位到店的客人。
        </p>
      </div>
      <div class="visual-facts">
        <div>
          <strong>06</strong>
          <span>东南亚风味</span>
        </div>
        <div>
          <strong>01</strong>
          <span>校园门店</span>
        </div>
        <div>
          <strong>08:00</strong>
          <span>每日现制</span>
        </div>
      </div>
    </section>

    <section class="form-side">
      <div class="form-wrap">
        <div class="mobile-brand">
          <BrandMark />
        </div>
        <p class="eyebrow">Administrator</p>
        <h2>欢迎回来</h2>
        <p class="sub">登录商家后台，开始今天的经营。</p>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="onSubmit">
          <el-form-item label="管理员账号" prop="account">
            <el-input v-model="form.account" placeholder="请输入账号" size="large" autocomplete="username">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              autocomplete="current-password"
              @keyup.enter="onSubmit"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button class="submit" type="primary" size="large" :loading="loading" @click="onSubmit">
            进入后台
            <el-icon class="arrow"><Right /></el-icon>
          </el-button>
        </el-form>

        <div class="hint">
          <span>默认体验账号</span>
          <code>admin / admin123</code>
        </div>

        <p class="foot">GXUFL Coffee Atelier · 广西外国语学院咖啡厅</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Right } from '@element-plus/icons-vue'
import BrandMark from '@/components/BrandMark.vue'
import { adminLogin } from '@/api'
import { useAuthStore } from '@/stores/auth'
import hero from '@/assets/brand-hero.jpg'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ account: 'admin', password: 'admin123' })
const rules = {
  account: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await adminLogin(form)
    auth.setAuth(data)
    ElMessage.success(`欢迎回来，${data.user.nickname}`)
    router.replace(route.query.redirect || '/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(430px, 0.82fr);
  min-height: 100vh;
  background: var(--ink);
}

.visual {
  position: relative;
  overflow: hidden;
  padding: 46px 54px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.visual-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: saturate(0.8) contrast(1.05);
  transform: scale(1.03);
}

.visual-shade {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(7, 6, 5, 0.9) 0%, rgba(7, 6, 5, 0.45) 55%, rgba(7, 6, 5, 0.92) 100%),
    linear-gradient(0deg, #0b0908 0%, transparent 45%, rgba(7, 6, 5, 0.7) 100%);
}

.visual-top,
.visual-copy,
.visual-facts {
  position: relative;
  z-index: 1;
}

.visual-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tag {
  font-size: 9px;
  letter-spacing: 0.3em;
  color: rgba(243, 237, 228, 0.55);
}

.visual-copy {
  padding: 40px 0;
  max-width: 560px;
}

.visual-copy h1 {
  font-family: var(--serif);
  font-weight: 400;
  font-size: clamp(38px, 4.1vw, 62px);
  line-height: 1.22;
  margin: 18px 0 22px;
  letter-spacing: 0.01em;
  color: #fdf8f0;
  text-shadow: 0 2px 30px rgba(0, 0, 0, 0.45);
}

.visual-copy .desc {
  margin: 0;
  font-size: 13px;
  line-height: 2;
  color: rgba(243, 237, 228, 0.66);
  letter-spacing: 0.04em;
}

.visual-facts {
  display: flex;
  gap: 44px;
  padding-top: 26px;
  border-top: 1px solid rgba(200, 169, 126, 0.22);
}

.visual-facts div {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.visual-facts strong {
  font-family: var(--serif);
  font-size: 26px;
  font-weight: 400;
  color: var(--gold-bright);
}

.visual-facts span {
  font-size: 10px;
  letter-spacing: 0.18em;
  color: rgba(243, 237, 228, 0.45);
}

.form-side {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 48px;
  position: relative;
  background:
    radial-gradient(90% 60% at 100% 0%, rgba(200, 169, 126, 0.07), transparent 60%),
    var(--ink);
}

.form-side::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12%;
  bottom: 12%;
  width: 1px;
  background: linear-gradient(to bottom, transparent, rgba(200, 169, 126, 0.3), transparent);
}

.form-wrap {
  width: 100%;
  max-width: 368px;
  animation: fade-up 0.7s 0.1s ease both;
}

.mobile-brand {
  display: none;
  margin-bottom: 36px;
}

.form-wrap h2 {
  font-family: var(--serif);
  font-size: 40px;
  font-weight: 400;
  margin: 12px 0 8px;
}

.form-wrap .sub {
  margin: 0 0 30px;
  color: var(--muted);
  font-size: 12.5px;
}

.form-wrap :deep(.el-form-item) {
  margin-bottom: 22px;
}

.form-wrap :deep(.el-form-item__label) {
  font-size: 11px;
  letter-spacing: 0.1em;
  color: var(--muted);
  padding-bottom: 6px;
}

.form-wrap :deep(.el-input__wrapper) {
  height: 46px;
  background: rgba(255, 255, 255, 0.025) !important;
  border-radius: 11px;
}

.form-wrap :deep(.el-input__prefix) {
  color: var(--muted-2);
}

.submit {
  width: 100%;
  height: 48px;
  margin-top: 6px;
  border-radius: 999px;
  font-weight: 600;
  letter-spacing: 0.14em;
  background: linear-gradient(100deg, #b99261, #dcc099 55%, #c8a97e) !important;
  border: none !important;
  color: #15100c !important;
  box-shadow: 0 14px 34px rgba(200, 169, 126, 0.16);
}

.submit:hover {
  filter: brightness(1.06);
}

.arrow {
  margin-left: 8px;
}

.hint {
  margin-top: 24px;
  padding: 12px 14px;
  border: 1px dashed var(--line);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 11px;
  color: var(--muted-2);
  letter-spacing: 0.04em;
}

.hint code {
  color: var(--gold);
  font-family: var(--sans);
  font-size: 11px;
}

.foot {
  margin-top: 34px;
  font-size: 10px;
  color: var(--muted-2);
  letter-spacing: 0.06em;
  text-align: center;
}

@media (max-width: 980px) {
  .login-shell {
    grid-template-columns: 1fr;
  }
  .visual {
    display: none;
  }
  .form-side {
    min-height: 100vh;
    padding: 32px 24px;
  }
  .form-side::before {
    display: none;
  }
  .mobile-brand {
    display: block;
  }
}
</style>
