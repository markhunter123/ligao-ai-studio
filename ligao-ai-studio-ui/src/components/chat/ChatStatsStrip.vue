<template>
  <div
    v-if="metrics.hasData"
    class="chat-stats-strip"
    :class="{ 'is-live': metrics.isLive }"
  >
    <span v-if="counts" class="stats-group">{{ counts }}</span>
    <span v-if="elapsed" class="stats-group">{{ elapsed }}</span>
    <span v-if="speed" class="stats-group">{{ speed }}</span>
    <span v-if="tokens" class="stats-group">{{ tokens }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ChatMetrics } from '@/composables/chat/useChatMetrics'

interface Props {
  metrics: ChatMetrics
}

const props = defineProps<Props>()
const { t } = useI18n()

function formatDuration(ms: number): string {
  const s = ms / 1000
  if (s < 60) return `${Math.round(s * 10) / 10}s`
  const whole = Math.round(s)
  return `${Math.floor(whole / 60)}m${whole % 60}s`
}

function formatTokens(n: number): string {
  if (n < 1000) return String(n)
  if (n < 1_000_000) {
    const scaled = n / 1000
    return scaled >= 100 ? `${Math.round(scaled)}K` : `${Math.round(scaled * 10) / 10}K`
  }
  const scaled = n / 1_000_000
  return scaled >= 100 ? `${Math.round(scaled)}M` : `${Math.round(scaled * 10) / 10}M`
}

function formatTokensPerSecond(tps: number): string {
  const clamped = Math.max(0, tps)
  return clamped >= 10 ? String(Math.round(clamped)) : String(Math.round(clamped * 10) / 10)
}

const counts = computed(() =>
  props.metrics.assistantCount > 0
    ? t('chat.stats.counts', {
        turns: props.metrics.assistantCount,
        steps: props.metrics.toolCallCount,
      })
    : '',
)

const elapsed = computed(() =>
  props.metrics.elapsedMs > 0
    ? t('chat.stats.elapsed', { duration: formatDuration(props.metrics.elapsedMs) })
    : '',
)

const speed = computed(() =>
  props.metrics.tokensPerSecond != null && props.metrics.tokensPerSecond > 0
    ? t('chat.stats.tokensPerSecond', { throughput: formatTokensPerSecond(props.metrics.tokensPerSecond) })
    : '',
)

const tokens = computed(() => {
  const { promptTokens, completionTokens } = props.metrics
  if (promptTokens > 0 || completionTokens > 0) {
    return t('chat.stats.tokens', {
      input: formatTokens(promptTokens),
      output: formatTokens(completionTokens),
    })
  }
  return ''
})
</script>

<style scoped>
.chat-stats-strip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--mc-text-tertiary, #94a3b8);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1 1 auto;
  min-width: 0;
  justify-content: flex-end;
}

.stats-group:not(:last-child)::after {
  content: '|';
  margin-left: 6px;
  color: var(--mc-border, #e2e8f0);
  opacity: 0.7;
}

.chat-stats-strip.is-live .stats-group {
  color: var(--mc-text-secondary, #64748b);
}

/* Hide on very narrow viewports so the model hint and length counter stay readable. */
@media (max-width: 640px) {
  .chat-stats-strip {
    display: none;
  }
}
</style>
