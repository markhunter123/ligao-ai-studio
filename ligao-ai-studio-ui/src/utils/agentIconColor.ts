/**
 * 数字员工图标的角色配色。
 *
 * Lucide SVG 以 `stroke="currentColor"` 渲染，外层元素设置 `color: ...`
 * 即可给图标着色而不必改动 SVG 本体。仅用于 agent 场景（卡片、选择器、
 * 聊天头部）；技能 / 工具保持默认中性色。
 *
 * 配色体系锚定立高品牌蓝 #1266AA（hsl 207,72%,38%），按烘焙业务角色
 * 划分五个色相族 —— 同族低饱和、跨族可区分，与品牌蓝同处一个明度区间，
 * 不抢占主色的视觉权重：
 * - 烘焙研发 — 麦穗金（品牌金点缀 #df9c35 同族）
 * - 品控安全 — 焙茶绿（食品安全联想）
 * - 供应链 — 冰川蓝（冷链联想，偏主色的冷调）
 * - 门店市场 — 面包焙棕（面包壳联想的暖调）
 * - 通用智能 — 立高蓝（品牌主色）
 * - 规划协同 — 靛蓝（冷静的任务感）
 */

const BRAND_FALLBACK = 'var(--mc-primary)'

/** 图标名（不含 `lu:` 前缀）→ CSS 颜色 */
const ICON_COLOR_MAP: Record<string, string> = {
  // 烘焙研发 / 配方 — 麦穗金
  'chef-hat': 'hsl(38, 78%, 44%)',
  'wheat': 'hsl(38, 78%, 44%)',
  'flask-conical': 'hsl(38, 78%, 44%)',
  'flask-round': 'hsl(38, 78%, 44%)',
  'test-tube': 'hsl(38, 78%, 44%)',
  'test-tubes': 'hsl(38, 78%, 44%)',
  'lightbulb': 'hsl(38, 78%, 44%)',
  'cake': 'hsl(38, 78%, 44%)',
  'croissant': 'hsl(38, 78%, 44%)',
  'cookie': 'hsl(38, 78%, 44%)',

  // 品控安全 / 检测 — 焙茶绿
  'microscope': 'hsl(174, 45%, 34%)',
  'shield-check': 'hsl(174, 45%, 34%)',
  'gauge': 'hsl(174, 45%, 34%)',
  'scale': 'hsl(174, 45%, 34%)',
  'thermometer': 'hsl(174, 45%, 34%)',
  'hard-hat': 'hsl(174, 45%, 34%)',
  'snowflake': 'hsl(174, 45%, 34%)',
  'bug': 'hsl(174, 45%, 34%)',
  'search': 'hsl(174, 45%, 34%)',
  'file-search': 'hsl(174, 45%, 34%)',

  // 供应链 / 冷链 — 冰川蓝
  'truck': 'hsl(205, 68%, 42%)',
  'warehouse': 'hsl(205, 68%, 42%)',
  'package': 'hsl(205, 68%, 42%)',
  'car': 'hsl(205, 68%, 42%)',
  'car-front': 'hsl(205, 68%, 42%)',
  'globe': 'hsl(205, 68%, 42%)',
  'globe-2': 'hsl(205, 68%, 42%)',
  'cloud': 'hsl(205, 68%, 42%)',

  // 门店市场 / 零售 — 面包焙棕
  'store': 'hsl(16, 62%, 46%)',
  'briefcase': 'hsl(16, 62%, 46%)',
  'presentation': 'hsl(16, 62%, 46%)',
  'trending-up': 'hsl(16, 62%, 46%)',
  'chart-bar': 'hsl(16, 62%, 46%)',
  'chart-column': 'hsl(16, 62%, 46%)',
  'chart-line': 'hsl(16, 62%, 46%)',
  'chart-pie': 'hsl(16, 62%, 46%)',
  'target': 'hsl(16, 62%, 46%)',
  'flag': 'hsl(16, 62%, 46%)',
  'percent': 'hsl(16, 62%, 46%)',

  // 客户沟通 / 培训 — 陶土橙
  'headphones': 'hsl(20, 68%, 48%)',
  'message-square': 'hsl(20, 68%, 48%)',
  'messages-square': 'hsl(20, 68%, 48%)',
  'phone-call': 'hsl(20, 68%, 48%)',
  'mail': 'hsl(20, 68%, 48%)',
  'graduation-cap': 'hsl(20, 68%, 48%)',

  // 规划 / 任务 — 靛蓝
  'clipboard-check': 'hsl(232, 38%, 52%)',
  'clipboard-list': 'hsl(232, 38%, 52%)',
  'list-checks': 'hsl(232, 38%, 52%)',
  'calendar': 'hsl(232, 38%, 52%)',
  'calendar-check': 'hsl(232, 38%, 52%)',
  'notebook-pen': 'hsl(232, 38%, 52%)',
  'sticky-note': 'hsl(232, 38%, 52%)',
  'bookmark': 'hsl(232, 38%, 52%)',

  // 系统基础设施 — 石板灰蓝
  'cpu': 'hsl(215, 30%, 44%)',
  'server': 'hsl(215, 30%, 44%)',
  'database': 'hsl(215, 30%, 44%)',
  'network': 'hsl(215, 30%, 44%)',
  'terminal': 'hsl(215, 30%, 44%)',
  'code': 'hsl(215, 30%, 44%)',
  'code-xml': 'hsl(215, 30%, 44%)',
  'key-round': 'hsl(215, 30%, 44%)',
  'lock': 'hsl(215, 30%, 44%)',
  'circuit-board': 'hsl(215, 30%, 44%)',
  'factory': 'hsl(215, 30%, 44%)',
  'settings': 'hsl(215, 30%, 44%)',
  'wrench': 'hsl(215, 30%, 44%)',
  'calculator': 'hsl(215, 30%, 44%)',

  // 通用 / 友好助手 — 立高蓝（品牌主色）
  'bot': 'hsl(207, 72%, 38%)',
  'sparkles': 'hsl(207, 72%, 38%)',
  'brain': 'hsl(207, 72%, 38%)',
  'brain-circuit': 'hsl(207, 72%, 38%)',
  'atom': 'hsl(207, 72%, 38%)',
  'activity': 'hsl(207, 72%, 38%)',
  'zap': 'hsl(207, 72%, 38%)',
  'rocket': 'hsl(207, 72%, 38%)',
  'user': 'hsl(207, 72%, 38%)',
  'users': 'hsl(207, 72%, 38%)',
  'user-cog': 'hsl(207, 72%, 38%)',
}

/**
 * Return the accent color for a stored icon string. Returns the brand
 * primary as a CSS variable for emoji / URL / unknown icons so callers
 * can apply the color unconditionally.
 */
export function agentIconColor(iconValue: string | null | undefined): string {
  if (!iconValue) return BRAND_FALLBACK
  const v = iconValue.trim()
  if (!v.startsWith('lu:')) return BRAND_FALLBACK
  const name = v.slice(3)
  return ICON_COLOR_MAP[name] || BRAND_FALLBACK
}
