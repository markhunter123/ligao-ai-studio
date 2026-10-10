import {
  computed,
  onBeforeUnmount,
  reactive,
  ref,
  watch,
  type Ref,
} from 'vue'
import type { Message } from '@/types'

/** Fine-grained pre-token lifecycle stage mirrored from useChat. */
export interface ChatMetricsLifecycleStage {
  stage: 'connecting' | 'started' | 'context_prepared' | 'llm_request_sent' | 'streaming'
  detail?: any
  since: number
}

export interface UseChatMetricsOptions {
  messages: Ref<Message[]>
  isGenerating: Ref<boolean>
  lifecycleStage: Ref<ChatMetricsLifecycleStage | null>
}

export interface ChatMetrics {
  /** Wall time for the current turn (or the last finalized turn). */
  elapsedMs: number
  /** Live decode throughput, or the finalized speed for the last turn. */
  tokensPerSecond: number | null
  /** Sum of prompt-side tokens seen across the conversation. */
  promptTokens: number
  /** Sum of completion tokens seen across the conversation. */
  completionTokens: number
  /** Total tool-call / iteration segments across the conversation. */
  toolCallCount: number
  /** Number of assistant messages in the conversation. */
  assistantCount: number
  /** True when at least one displayable metric exists. */
  hasData: boolean
  /** True while the model is still generating. */
  isLive: boolean
}

/**
 * Derive a compact agent-run statistics strip from the message list and the
 * streaming lifecycle. Mirrors the reference StatsLine intent but only uses
 * data the target frontend already receives (tokens from SSE done events /
 * message fields, tool-call segments, and wall-clock elapsed time computed
 * locally). Metrics with no data source (cache hit, TTFT, backend timings)
 * are intentionally omitted rather than faked.
 */
export function useChatMetrics(options: UseChatMetricsOptions): ChatMetrics {
  const { messages, isGenerating, lifecycleStage } = options

  const now = ref(Date.now())
  let timer: ReturnType<typeof setInterval> | null = null

  function startTick() {
    if (timer) return
    now.value = Date.now()
    timer = setInterval(() => {
      now.value = Date.now()
    }, 200)
  }

  function stopTick() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  // Tick while generating or while a recent final value is still warm.
  watch(
    () => isGenerating.value || lifecycleStage.value != null,
    (active) => {
      if (active) startTick()
      else stopTick()
    },
    { immediate: true },
  )

  onBeforeUnmount(() => {
    stopTick()
  })

  let turnStartAt: number | null = null
  let firstTokenAt: number | null = null
  let finalElapsedMs: number | null = null
  let finalCompletionTokens = 0
  let finalTokensPerSecond: number | null = null

  function finalizeTurn() {
    if (turnStartAt == null || finalElapsedMs != null) return
    const endedAt = Date.now()
    finalElapsedMs = endedAt - turnStartAt
    finalCompletionTokens = currentAssistantCompletionTokens.value
    if (firstTokenAt && finalCompletionTokens > 0 && finalElapsedMs > 0) {
      finalTokensPerSecond = finalCompletionTokens / (finalElapsedMs / 1000)
    }
  }

  function resetTurn() {
    turnStartAt = null
    firstTokenAt = null
    finalElapsedMs = null
    finalCompletionTokens = 0
    finalTokensPerSecond = null
  }

  watch(
    () => lifecycleStage.value,
    (newVal, oldVal) => {
      if (newVal && !oldVal) {
        // New turn started.
        resetTurn()
        turnStartAt = newVal.since || Date.now()
      }
      if (newVal?.stage === 'streaming' && firstTokenAt == null) {
        firstTokenAt = newVal.since || Date.now()
      }
      if (!newVal && oldVal) {
        // Turn finalized — freeze the figures so the strip settles.
        finalizeTurn()
      }
    },
  )

  // Fallback for reconnecting to a backend stream that is already running
  // (useChat does not re-emit a lifecycleStage in that path).
  watch(
    () => isGenerating.value,
    (newVal, oldVal) => {
      if (newVal && !oldVal && turnStartAt == null) {
        resetTurn()
        turnStartAt = Date.now()
      }
      if (!newVal && oldVal) {
        finalizeTurn()
      }
    },
  )

  const assistantMessages = computed(() =>
    messages.value.filter((m) => m.role === 'assistant'),
  )

  const currentAssistant = computed(() => {
    const list = assistantMessages.value
    return list.length > 0 ? list[list.length - 1] : null
  })

  const currentAssistantCompletionTokens = computed(
    () => currentAssistant.value?.completionTokens ?? 0,
  )

  const promptTokens = computed(() =>
    messages.value.reduce((sum, m) => sum + (m.promptTokens || 0), 0),
  )

  const completionTokens = computed(() =>
    messages.value.reduce((sum, m) => sum + (m.completionTokens || 0), 0),
  )

  const toolCallCount = computed(() => {
    let count = 0
    for (const m of messages.value) {
      if (m.role !== 'assistant') continue
      const segments = m.metadata?.segments
      if (Array.isArray(segments)) {
        count += segments.filter((s: any) => s.type === 'tool_call').length
      } else if (Array.isArray(m.metadata?.toolCalls)) {
        count += m.metadata.toolCalls.length
      }
    }
    return count
  })

  const assistantCount = computed(() => assistantMessages.value.length)

  const elapsedMs = computed(() => {
    if (isGenerating.value && turnStartAt) {
      return Math.max(0, now.value - turnStartAt)
    }
    if (finalElapsedMs != null) return finalElapsedMs
    return 0
  })

  const tokensPerSecond = computed(() => {
    if (isGenerating.value && firstTokenAt && currentAssistantCompletionTokens.value > 0) {
      const ms = Math.max(1, now.value - firstTokenAt)
      return currentAssistantCompletionTokens.value / (ms / 1000)
    }
    if (finalTokensPerSecond != null) return finalTokensPerSecond
    if (!isGenerating.value && finalElapsedMs && finalCompletionTokens > 0) {
      return finalCompletionTokens / (finalElapsedMs / 1000)
    }
    return null
  })

  const hasData = computed(() =>
    assistantCount.value > 0 &&
    (
      promptTokens.value > 0 ||
      completionTokens.value > 0 ||
      toolCallCount.value > 0 ||
      elapsedMs.value > 0
    ),
  )

  return reactive({
    elapsedMs,
    tokensPerSecond,
    promptTokens,
    completionTokens,
    toolCallCount,
    assistantCount,
    hasData,
    isLive: isGenerating,
  }) as ChatMetrics
}

export default useChatMetrics
