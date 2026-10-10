<template>
  <div class="mc-page-shell">
    <div class="mc-page-frame enterprise-frame">
      <div class="mc-page-inner enterprise-shell">
        <header class="mc-page-header">
          <div>
            <div class="mc-page-kicker">{{ t('enterprise.eyebrow') }}</div>
            <h1 class="mc-page-title">{{ t('enterprise.title') }}</h1>
            <p class="mc-page-desc">{{ t('enterprise.subtitle') }}</p>
          </div>
        </header>

        <nav class="enterprise-tabs">
      <button
        v-for="tab in tabs" :key="tab.key"
        class="enterprise-tab" :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span class="tab-label">{{ tab.label }}</span>
        <span v-if="tab.count != null" class="tab-count">{{ tab.count }}</span>
      </button>
    </nav>

    <section class="enterprise-body">
      <Overview v-if="activeTab === 'overview'" />
      <Approvals v-else-if="activeTab === 'approvals'" />
      <Audit v-else-if="activeTab === 'audit'" />
    </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Overview from './Overview.vue'
import Approvals from './Approvals.vue'
import Audit from './Audit.vue'

const { t } = useI18n()
type TabKey = 'overview' | 'approvals' | 'audit'
const activeTab = ref<TabKey>('overview')

const tabs = computed<{ key: TabKey; label: string; count: number | null }[]>(() => [
  { key: 'overview', label: t('enterprise.tabs.overview'), count: null },
  { key: 'approvals', label: t('enterprise.tabs.approvals'), count: 0 },
  { key: 'audit', label: t('enterprise.tabs.audit'), count: null },
])
</script>

<style scoped>
/* The frame card stays fixed; each tab manages its own internal scroll,
   so suppress the global frame-level scrollbar here. */
.enterprise-frame {
  display: flex;
  overflow: hidden;
}

.enterprise-shell {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  gap: 18px;
  /* Constrain the inner body so each tab can decide how to overflow:
     vertical-stack pages scroll the body itself, three-pane grid pages
     keep each pane scrolling independently. Without this, content that
     exceeds the viewport falls outside the layout box and is unreachable. */
  overflow: hidden;
}

.enterprise-tabs {
  display: inline-flex;
  align-self: flex-start;
  padding: 4px;
  background: var(--mc-bg-muted);
  border: 1px solid var(--mc-border-light);
  border-radius: 14px;
  gap: 2px;
}
.enterprise-tab {
  border: none;
  background: none;
  padding: 8px 14px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--mc-text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.15s;
}
.enterprise-tab:hover { color: var(--mc-text-primary); }
.enterprise-tab.active {
  background: var(--mc-bg-elevated);
  color: var(--mc-primary);
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.tab-count {
  font-size: 11px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 999px;
  background: var(--mc-primary-bg);
  color: var(--mc-primary-hover);
}
.enterprise-tab.active .tab-count { background: var(--mc-primary); color: white; }

/* The body fills the remaining height and gives its single child a strict
   height contract via flex. Page roots must declare `flex: 1; min-height: 0;`
   so they consume that height and can choose their own overflow strategy. */
.enterprise-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
