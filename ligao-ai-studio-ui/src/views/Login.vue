<template>
  <div class="login-page">
    <!-- 烘焙暖色背景层（纯 CSS，无需图片） -->
    <div class="login-bg">
      <div class="bg-wash"></div>
      <span class="bg-blob blob-a"></span>
      <span class="bg-blob blob-b"></span>
      <span class="bg-blob blob-c"></span>
      <!-- 麦穗装饰 -->
      <svg class="bg-wheat wheat-left" viewBox="0 0 120 260" fill="none" aria-hidden="true">
        <g stroke="#7FB3DC" stroke-width="3" stroke-linecap="round">
          <path d="M60 250 C 58 190 58 120 62 40" />
          <g fill="#A3CDEB" stroke="none" opacity="0.75">
            <path d="M61 60 C 44 56 36 44 34 28 C 50 30 58 42 61 60 Z"/>
            <path d="M61 60 C 78 56 86 44 88 28 C 72 30 64 42 61 60 Z"/>
            <path d="M60 100 C 43 96 35 84 33 68 C 49 70 57 82 60 100 Z"/>
            <path d="M60 100 C 77 96 85 84 87 68 C 71 70 63 82 60 100 Z"/>
            <path d="M60 140 C 43 136 35 124 33 108 C 49 110 57 122 60 140 Z"/>
            <path d="M60 140 C 77 136 85 124 87 108 C 71 110 63 122 60 140 Z"/>
            <path d="M60 180 C 43 176 35 164 33 148 C 49 150 57 162 60 180 Z"/>
            <path d="M60 180 C 77 176 85 164 87 148 C 71 150 63 162 60 180 Z"/>
          </g>
        </g>
      </svg>
      <svg class="bg-wheat wheat-right" viewBox="0 0 120 260" fill="none" aria-hidden="true">
        <g stroke="#7FB3DC" stroke-width="3" stroke-linecap="round">
          <path d="M60 250 C 58 190 58 120 62 40" />
          <g fill="#A3CDEB" stroke="none" opacity="0.75">
            <path d="M61 60 C 44 56 36 44 34 28 C 50 30 58 42 61 60 Z"/>
            <path d="M61 60 C 78 56 86 44 88 28 C 72 30 64 42 61 60 Z"/>
            <path d="M60 100 C 43 96 35 84 33 68 C 49 70 57 82 60 100 Z"/>
            <path d="M60 100 C 77 96 85 84 87 68 C 71 70 63 82 60 100 Z"/>
            <path d="M60 140 C 43 136 35 124 33 108 C 49 110 57 122 60 140 Z"/>
            <path d="M60 140 C 77 136 85 124 87 108 C 71 110 63 122 60 140 Z"/>
          </g>
        </g>
      </svg>
      <div class="bg-overlay"></div>
    </div>

    <!-- 浮动登录卡片（靠右侧） -->
    <div class="login-panel">
      <div class="login-center">
        <div class="login-logo">
          <img src="/logo/ligao_logo.png" alt="立高食品 AI 中台" class="logo-image" />
          <h1 class="logo-title">立高食品 AI 中台</h1>
          <p class="logo-subtitle">烘焙行业的智能体平台</p>
        </div>

        <form class="login-form" @submit.prevent="handleLogin">
          <div class="input-wrap">
            <input
              v-model="form.username"
              type="text"
              class="form-input"
              :placeholder="t('login.placeholders.username')"
              :aria-label="t('login.fields.username')"
              autocomplete="username"
              required
            />
          </div>

          <div class="input-wrap">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="form-input form-input--has-eye"
              :placeholder="t('login.placeholders.password')"
              :aria-label="t('login.fields.password')"
              autocomplete="current-password"
              required
            />
            <button type="button" class="eye-btn" @click="showPassword = !showPassword">
              <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>

          <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

          <div class="login-options">
            <label class="remember-me">
              <input type="checkbox" v-model="rememberMe" />
              <span>{{ t('login.rememberMe') }}</span>
            </label>
          </div>

          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="!loading">{{ t('login.signIn') }}</span>
            <span v-else class="loading-dots">
              <span></span><span></span><span></span>
            </span>
          </button>

          <div class="login-links">
            <button type="button" class="link-btn" @click="showForgotHint = !showForgotHint">
              {{ t('login.forgotPassword') }}
            </button>
          </div>
          <div v-if="showForgotHint" class="forgot-hint">
            {{ t('login.forgotHint') }}
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { authApi } from '@/api/index'
import { useWorkspaceStore } from '@/stores/useWorkspaceStore'
import { useSystemSettingsStore } from '@/stores/useSystemSettingsStore'

const router = useRouter()
const { t } = useI18n()
const workspaceStore = useWorkspaceStore()
const systemSettingsStore = useSystemSettingsStore()
const loading = ref(false)
const showPassword = ref(false)
const errorMsg = ref('')
const rememberMe = ref(false)
const showForgotHint = ref(false)
const form = reactive({ username: '', password: '' })

const REMEMBER_KEY = 'ligao_remembered_username'

onMounted(() => {
  const saved = localStorage.getItem(REMEMBER_KEY)
  if (saved) {
    form.username = saved
    rememberMe.value = true
  }
})

async function handleLogin() {
  if (!form.username || !form.password) return
  loading.value = true
  errorMsg.value = ''
  try {
    const res: any = await authApi.login(form)
    const data = res.data || res
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', String(data.id || '1'))
    localStorage.setItem('username', data.username || form.username)
    localStorage.setItem('role', data.role || 'user')
    // Remember username
    if (rememberMe.value) {
      localStorage.setItem(REMEMBER_KEY, form.username)
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }
    systemSettingsStore.load()
    try {
      await workspaceStore.fetchWorkspaces()
    } catch {
    }
    const target = workspaceStore.can('view:dashboard') ? '/dashboard' : '/chat'
    router.push(target)
  } catch (e: any) {
    errorMsg.value = typeof e === 'string' ? e : t('login.failed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== 立高品牌蓝背景：冰蓝底 + 柔光色斑 + 麦穗装饰 ===== */
.login-page {
  position: relative;
  min-height: 100vh;
  width: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #F5FAFE 0%, #E8F2FA 45%, #D8E9F6 100%);
}

/* 背景层：铺满整个视口 */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

/* 底层暖色渐变晕染 */
.bg-wash {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(900px 600px at 18% 20%, rgba(26, 140, 220, 0.30), transparent 65%),
    radial-gradient(800px 560px at 82% 78%, rgba(16, 56, 79, 0.22), transparent 62%),
    radial-gradient(520px 380px at 55% 45%, rgba(255, 255, 255, 0.55), transparent 70%);
}

/* 焦糖色柔光圆形 */
.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
}

.blob-a {
  width: 420px;
  height: 420px;
  left: -120px;
  top: -140px;
  background: radial-gradient(circle, #8FC6EE 0%, transparent 70%);
}

.blob-b {
  width: 520px;
  height: 520px;
  right: -160px;
  bottom: -200px;
  background: radial-gradient(circle, #1470B8 0%, transparent 70%);
}

.blob-c {
  width: 260px;
  height: 260px;
  left: 34%;
  bottom: 6%;
  background: radial-gradient(circle, #A8D4F2 0%, transparent 72%);
  opacity: 0.65;
}

/* 麦穗装饰 */
.bg-wheat {
  position: absolute;
  width: 130px;
  height: 280px;
  opacity: 0.5;
}

.wheat-left {
  left: 6%;
  bottom: 4%;
  transform: rotate(-8deg);
}

.wheat-right {
  right: 26%;
  top: 5%;
  transform: rotate(16deg) scale(0.8);
  opacity: 0.35;
}

/* 淡淡的压暗遮罩，让右侧登录卡片更清晰 */
.bg-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to right,
    rgba(16, 56, 79, 0) 0%,
    rgba(16, 56, 79, 0.04) 55%,
    rgba(16, 56, 79, 0.10) 100%
  );
}

/* ===== 浮动登录卡片（右侧偏上区域） ===== */
.login-panel {
  position: absolute;
  top: 18%;
  right: 12%;
  width: 400px;
  max-width: calc(100vw - 48px);
  background: rgba(255, 252, 245, 0.93);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(26, 140, 220, 0.30);
  border-radius: 20px;
  padding: 40px 36px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.28);
  animation: fadeUp 0.6s ease-out both;
  z-index: 2;
}

.login-center {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
}

.login-logo {
  text-align: center;
}

.logo-image {
  display: block;
  margin: 0 auto 14px;
  width: 76px;
  height: auto;
  object-fit: contain;
}

.logo-title {
  font-size: 25px;
  font-weight: 800;
  color: #10384F;
  margin: 0;
  letter-spacing: -0.02em;
}

.logo-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin: 4px 0 0;
  letter-spacing: 0.02em;
}

.login-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  padding: 13px 16px;
  border: 1.8px solid #E2E8F0;
  border-radius: 12px;
  font-size: 15px;
  color: #1A1A2E;
  background: #F8FAFC;
  outline: none;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.form-input:-webkit-autofill,
.form-input:-webkit-autofill:hover,
.form-input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0 30px #F8FAFC inset !important;
  -webkit-text-fill-color: #1A1A2E !important;
  transition: background-color 5000s ease-in-out 0s;
}

.form-input--has-eye {
  padding-right: 44px;
}

.form-input:focus {
  border-color: #1266AA;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(18, 102, 170, 0.15);
}

.eye-btn {
  position: absolute;
  right: 12px;
  width: 28px;
  height: 28px;
  border: none;
  background: none;
  cursor: pointer;
  color: #9CA3AF;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.eye-btn:hover {
  color: #1266AA;
}

.error-msg {
  padding: 10px 14px;
  background: #FFF1F0;
  border: 1px solid #FF4D4F;
  border-radius: 10px;
  font-size: 13px;
  color: #D4380D;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #1A8CDC, #1266AA);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  margin-top: 4px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(18, 102, 170, 0.35);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6B7280;
  cursor: pointer;
  user-select: none;
}

.remember-me input[type="checkbox"] {
  width: 15px;
  height: 15px;
  accent-color: #1266AA;
  cursor: pointer;
}

.login-links {
  text-align: center;
}

.link-btn {
  background: none;
  border: none;
  color: #1266AA;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  text-decoration: none;
}

.link-btn:hover {
  text-decoration: underline;
  color: #0D548C;
}

.forgot-hint {
  margin-top: 4px;
  padding: 10px 14px;
  background: #FEF3C7;
  border: 1px solid #F59E0B;
  border-radius: 10px;
  font-size: 13px;
  color: #92400E;
  text-align: center;
  line-height: 1.5;
}

.loading-dots {
  display: flex;
  gap: 5px;
  align-items: center;
}

.loading-dots span {
  width: 6px;
  height: 6px;
  background: white;
  border-radius: 50%;
  animation: bounce 1.2s infinite;
}

.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-5px); }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 小屏适配：登录卡片水平居中、垂直偏上 ===== */
@media (max-width: 640px) {
  .login-panel {
    left: 50%;
    right: auto;
    top: 12%;
    transform: translateX(-50%);
    width: calc(100vw - 40px);
    padding: 32px 24px;
  }
}
</style>
