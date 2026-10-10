/**
 * Lucide 图标库（ISC License）— SVG 图标加载器 + 烘焙行业语义分类。
 *
 * 图标 SVG 收录于 `src/assets/lucide/`（来自 lucide-static v0.544.0），
 * 经 Vite `?raw` 预打包进产物：约 120 个图标、~35KB 原始体积，足够小，
 * 选择器可以直接全量渲染 + 本地搜索，无需懒加载。
 *
 * 图标方案：选中的图标以字符串 `lu:name`（kebab-case）持久化到
 * `ligao_agent.icon`。{@link SkillIcon} 渲染器识别该前缀并内联 SVG。
 * 旧版 `pi:`（pixelarticons）前缀已退役：遇到时回退渲染默认 bot 图标。
 *
 * 分类体系：面向立高烘焙业务线（烘焙食品 / 研发工艺 / 品控安全 /
 * 供应链 / 门店市场 / 通用智能），驱动选择器的语义筛选 chips 与
 * agentIconColor 的角色配色。
 */

// Eager glob — Vite resolves `?raw` text imports for SVGs like any asset.
const modules = import.meta.glob<string>(
  '../assets/lucide/*.svg',
  { query: '?raw', import: 'default', eager: true },
)

/**
 * Map from kebab-case icon name → raw SVG markup.
 * Built once at module load; ~120 entries.
 */
export const lucideIcons: Record<string, string> = (() => {
  const out: Record<string, string> = {}
  for (const [path, raw] of Object.entries(modules)) {
    const name = path.split('/').pop()!.replace(/\.svg$/, '')
    out[name] = raw
  }
  return out
})()

/**
 * Sorted list of icon names — drives the picker grid. Cached because the
 * picker reads it on every keystroke (search filter).
 */
export const lucideIconNames: string[] = Object.keys(lucideIcons).sort()

/** 图标缺省兜底：任何无法解析的图标值都渲染为 bot。 */
export const DEFAULT_ICON_NAME = 'bot'

/** 图标分类键 — 与 i18n `common.iconPicker.cat*` 一一对应。 */
export type IconCategory = 'baking' | 'rnd' | 'qc' | 'supply' | 'retail' | 'general'

/** 选择器分类 chips（'all' 表示全部）。 */
export const ICON_CATEGORY_KEYS: Array<'all' | IconCategory> = [
  'all', 'baking', 'rnd', 'qc', 'supply', 'retail', 'general',
]

/** 各分类包含的图标名 — 覆盖不到的图标一律归入 general。 */
const CATEGORY_MEMBERS: Record<Exclude<IconCategory, 'general'>, string[]> = {
  // 烘焙食品：成品、原料、餐饮食材
  baking: [
    'cake', 'cake-slice', 'croissant', 'cookie', 'dessert', 'wheat', 'sandwich',
    'ice-cream-cone', 'ice-cream-bowl', 'apple', 'cherry', 'citrus', 'grape',
    'candy', 'bean', 'egg', 'egg-fried', 'milk', 'coffee', 'cup-soda',
    'utensils', 'utensils-crossed', 'hand-platter',
  ],
  // 研发工艺：配方研发、工艺生产、设备
  rnd: [
    'chef-hat', 'flask-conical', 'flask-round', 'test-tube', 'test-tubes',
    'microscope', 'thermometer', 'scale', 'timer', 'lightbulb', 'factory',
    'settings', 'wrench', 'circuit-board', 'cpu', 'calculator', 'hard-hat',
  ],
  // 品控安全：检测、认证、HACCP
  qc: [
    'shield-check', 'gauge', 'search', 'file-search', 'clipboard-check',
    'list-checks', 'bug', 'snowflake',
  ],
  // 供应链：冷链物流、仓储、采购
  supply: [
    'truck', 'warehouse', 'package', 'car', 'car-front', 'globe', 'globe-2',
    'database', 'server', 'download', 'upload', 'link', 'cloud',
  ],
  // 门店市场：门店运营、零售、市场分析、培训
  retail: [
    'store', 'briefcase', 'presentation', 'trending-up', 'chart-bar',
    'chart-column', 'chart-line', 'chart-pie', 'target', 'flag', 'mail',
    'phone-call', 'messages-square', 'message-square', 'headphones',
    'graduation-cap', 'percent',
  ],
}

/** icon name → category 查找表（模块加载时构建一次）。 */
export const iconCategoryByName: Record<string, IconCategory> = (() => {
  const out: Record<string, IconCategory> = {}
  for (const [cat, names] of Object.entries(CATEGORY_MEMBERS)) {
    for (const n of names) out[n] = cat as IconCategory
  }
  return out
})()

/** 取图标的分类；未归类图标归入 general。 */
export function iconCategory(name: string): IconCategory {
  return iconCategoryByName[name] ?? 'general'
}

/**
 * Encode a lucide icon name into the persisted icon string.
 * Symmetric with {@link parseIconValue}.
 */
export function encodeLucideIcon(name: string): string {
  return `lu:${name}`
}

export type ParsedIcon =
  | { kind: 'lucide'; name: string; svg: string | null }
  | { kind: 'url'; url: string }
  | { kind: 'emoji'; value: string }
  | { kind: 'empty' }

/**
 * Plain-text icon glyph for hosts that can't render components — chiefly
 * `<select><option>`. Returns the emoji as-is, falls back for `lu:` /
 * URL icons (which would otherwise render as literal `lu:bot` text).
 */
export function plainTextIcon(value: string | null | undefined, fallback = '🤖'): string {
  const parsed = parseIconValue(value)
  return parsed.kind === 'emoji' ? parsed.value : fallback
}

/**
 * Decode a stored icon string into a renderable shape. Returns
 * {@code kind: 'empty'} for null/blank so callers can branch on a
 * single discriminator.
 */
export function parseIconValue(value: string | null | undefined): ParsedIcon {
  if (!value || !value.trim()) return { kind: 'empty' }
  const v = value.trim()
  if (v.startsWith('lu:')) {
    const name = v.slice(3)
    return { kind: 'lucide', name, svg: lucideIcons[name] ?? null }
  }
  // Legacy `pi:` (pixelarticons) values predate the Lucide icon set —
  // the old glyphs are gone, so render the default bot instead of a
  // broken placeholder.
  if (v.startsWith('pi:')) {
    return { kind: 'lucide', name: DEFAULT_ICON_NAME, svg: lucideIcons[DEFAULT_ICON_NAME] ?? null }
  }
  if (v.startsWith('http://') || v.startsWith('https://')) {
    return { kind: 'url', url: v }
  }
  // Site-absolute path (e.g. /skill-assets/<skill>/assets/<file>.png served
  // by the WebMvcConfig resource handler) — treat as a URL the browser will
  // resolve against the current host, so deployments don't need to hardcode
  // their public origin in SKILL.md.
  if (v.startsWith('/')) {
    return { kind: 'url', url: v }
  }
  // Anything else falls through as an emoji / text glyph. We don't
  // validate "is this really an emoji" — letting the user pick any
  // unicode glyph is intentionally flexible.
  return { kind: 'emoji', value: v }
}
