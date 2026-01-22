<template>
  <div class="formily-case">
    <FormProvider :form="form">
      <el-form label-position="left">
        <SchemaField :schema="caseSchema" />
      </el-form>
    </FormProvider>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onBeforeUnmount, reactive, shallowRef, watch } from 'vue'
import { createForm } from '@formily/core'
import { createSchemaField, FormProvider, RecursionField, useFieldSchema } from '@formily/vue'
import {
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElSelect,
  ElOption,
  ElRadioGroup,
  ElRadio,
  ElCheckboxGroup,
  ElCheckbox,
  ElSwitch,
  ElDatePicker,
  ElSlider,
  ElRate,
  ElTabs,
  ElTabPane,
  ElCard,
  ElButton,
  ElDivider,
  ElLink
} from 'element-plus'
import { http } from '../services/api.js'

const props = defineProps({
  // Backend schema value: { initial_values, datesources, schema }
  value: {
    type: Object,
    default: null
  }
})

const rawSchema = computed(() => props.value?.schema || { type: 'object', properties: {} })
const datasourceConfigs = computed(() => props.value?.datesources || props.value?.datasources || {})

const isPlainObject = (v) => v && typeof v === 'object' && !Array.isArray(v)

const clone = (obj) => JSON.parse(JSON.stringify(obj))

const uniqueKey = (base, used) => {
  const safe = (base || 'field').toString()
  if (!used.has(safe)) {
    used.add(safe)
    return safe
  }
  let i = 2
  while (used.has(`${safe}_${i}`)) i++
  const k = `${safe}_${i}`
  used.add(k)
  return k
}

const stripLayoutStyleShallow = (node) => {
  if (!isPlainObject(node)) return node
  const out = clone(node)
  for (const key of ['x-decorator-props', 'x-component-props']) {
    const propsObj = out[key]
    if (isPlainObject(propsObj) && isPlainObject(propsObj.style)) {
      const next = { ...propsObj }
      delete next.style
      out[key] = next
    }
  }
  return out
}

const isTabsNode = (node) => node?.type === 'void' && node?.['x-component'] === 'Tabs'
const isLeafVoidNode = (node) => {
  // For the "case" view we usually flatten layout wrappers, but we still want to show
  // leaf void components like Button/Link/Divider.
  const xc = node?.['x-component']
  return xc === 'Button' || xc === 'Link' || xc === 'Divider'
}

// Datasource payload cache keyed by datasource id.
// We attach this payload onto schema nodes (as `x-component-props.dataSource` / `enum`)
// so components can consume it directly.
const datasourceEnumMap = reactive({})
const datasourceLoadingMap = reactive({})
let disposed = false
onBeforeUnmount(() => {
  disposed = true
})

const getDatasourceId = (node) => {
  if (!isPlainObject(node)) return undefined
  // Be tolerant: historical typos and different placements exist in this project.
  const direct = node.datasource_id ?? node.datesource_id
  if (direct != null && direct !== '') return String(direct)
  const xcp = node['x-component-props']
  if (isPlainObject(xcp)) {
    const fromProps = xcp.datasource_id ?? xcp.datesource_id
    if (fromProps != null && fromProps !== '') return String(fromProps)
  }
  return undefined
}

// In the editor, each tab content is stored under `properties.tab_<name>` as a Card-like container.
const unwrapTabContainer = (node) => {
  if (!isPlainObject(node)) return null
  const xc = node?.['x-component']
  const props = node?.properties
  if (xc === 'Card' && isPlainObject(props)) return props
  if (isPlainObject(props)) return props
  return null
}

const buildCaseProperties = (schemaNode, used) => {
  const out = {}
  if (!isPlainObject(schemaNode)) return out
  const propsNode = schemaNode.properties
  if (!isPlainObject(propsNode)) return out

  const hydrateEnumIfNeeded = (n) => {
    if (!isPlainObject(n)) return n
    const dsId = getDatasourceId(n)
    const payload = dsId ? datasourceEnumMap[dsId] : undefined
    if (dsId && payload != null) {
      // Formily passes `x-component-props` to component props; our components read `dataSource` first.
      const prev = n['x-component-props']
      const next = isPlainObject(prev) ? { ...prev } : {}
      next.dataSource = payload
      n['x-component-props'] = next
      // Also set `enum` for compatibility with any schema-based enum consumers.
      if (Array.isArray(payload)) n.enum = payload
    }
    return n
  }

  for (const key of Object.keys(propsNode)) {
    const node = propsNode[key]
    if (!isPlainObject(node)) continue

    // Keep Tabs (so the case UI can show tab switching), but drop the Card wrapper inside each tab.
    if (isTabsNode(node)) {
      const tabsNode = stripLayoutStyleShallow(node)
      const panes = Array.isArray(tabsNode?.['x-component-props']?.panes) ? tabsNode['x-component-props'].panes : []

      const nextTabs = {
        ...tabsNode,
        type: 'void',
        'x-component': 'Tabs',
        properties: {}
      }

      const tabsPropsNode = node.properties
      if (isPlainObject(tabsPropsNode)) {
        for (const pane of panes) {
          const tabKey = `tab_${pane?.name ?? ''}`
          const tabSchema = tabsPropsNode[tabKey]
          const tabProps = unwrapTabContainer(tabSchema)
          // Use `void` here so fields inside tabs keep the same (flat) value path as
          // the backend `initial_values` (which is not nested by tab containers).
          const tabObjSchema = { type: 'void', properties: {} }

          if (tabProps) {
            const usedInTab = new Set()
            for (const childKey of Object.keys(tabProps)) {
              const childNode = tabProps[childKey]
              if (!isPlainObject(childNode)) continue
              if (childNode.type === 'void' && !isTabsNode(childNode)) {
                Object.assign(tabObjSchema.properties, buildCaseProperties(childNode, usedInTab))
              } else if (isTabsNode(childNode)) {
                const k = uniqueKey(childKey, usedInTab)
                tabObjSchema.properties[k] = hydrateEnumIfNeeded(stripLayoutStyleShallow(childNode))
              } else {
                const k = uniqueKey(childKey, usedInTab)
                tabObjSchema.properties[k] = hydrateEnumIfNeeded(stripLayoutStyleShallow(childNode))
              }
            }
          }

          nextTabs.properties[tabKey] = tabObjSchema
        }
      }

      const k = uniqueKey(key, used)
      out[k] = nextTabs
      continue
    }

    // Flatten layout containers, but keep leaf void components (Button/Link/Divider) so they are visible.
    if (node.type === 'void' && !isLeafVoidNode(node)) {
      Object.assign(out, buildCaseProperties(node, used))
      continue
    }

    // Keep the field node itself (without layout style).
    const k = uniqueKey(key, used)
    out[k] = hydrateEnumIfNeeded(stripLayoutStyleShallow(node))
  }

  return out
}

const caseSchema = computed(() => {
  const used = new Set()
  return { type: 'object', properties: buildCaseProperties(rawSchema.value, used) }
})

const normalizeDatasourceUrl = (url) => {
  if (typeof url !== 'string') return ''
  // Our axios client has baseURL `/formily`. If config.url already starts with `/formily/`,
  // strip that prefix to avoid generating `/formily/formily/...`.
  return url.startsWith('/formily/') ? url.replace(/^\/formily/, '') : url
}

const normalizeDatasourceParams = (params) => {
  if (!params) return {}
  if (isPlainObject(params)) return params
  if (Array.isArray(params)) {
    const out = {}
    for (const p of params) {
      if (!isPlainObject(p)) continue
      const k = p.key ?? p.name
      const v = p.value
      if (k != null && k !== '') out[k] = v
    }
    return out
  }
  return {}
}

// Backend returns data that the target component can directly consume (e.g. Select expects [{label,value}, ...]).
const extractDatasourcePayload = (payload) => {
  if (Array.isArray(payload)) return payload
  if (isPlainObject(payload)) {
    if (Array.isArray(payload.data)) return payload.data
    if (Array.isArray(payload.list)) return payload.list
    if (Array.isArray(payload.items)) return payload.items
    if (Array.isArray(payload.records)) return payload.records
    // Some components may consume object payloads directly.
    return payload
  }
  return payload
}

const collectDatasourceIds = (node, acc) => {
  if (!isPlainObject(node)) return
  const dsId = getDatasourceId(node)
  if (dsId) acc.add(dsId)
  const propsNode = node.properties
  if (isPlainObject(propsNode)) {
    for (const k of Object.keys(propsNode)) collectDatasourceIds(propsNode[k], acc)
  }
  if (node.items) collectDatasourceIds(node.items, acc)
}

const findDatasourceConfig = (cfgs, id) => {
  if (!cfgs) return undefined
  // Preferred format: map/object keyed by datasource id.
  if (isPlainObject(cfgs) && isPlainObject(cfgs[id])) return cfgs[id]
  // Tolerate array format: [{id, ...}, ...]
  if (Array.isArray(cfgs)) {
    return cfgs.find((x) => isPlainObject(x) && String(x.id ?? x.datasource_id ?? x.datesource_id) === String(id))
  }
  return undefined
}

const ensureDatasourcesLoaded = async () => {
  const cfgs = datasourceConfigs.value
  if (!isPlainObject(cfgs) && !Array.isArray(cfgs)) return

  const ids = new Set()
  collectDatasourceIds(rawSchema.value, ids)
  if (!ids.size) return

  await Promise.all(
    Array.from(ids).map(async (id) => {
      if (datasourceEnumMap[id]) return
      if (datasourceLoadingMap[id]) return
      const cfg = findDatasourceConfig(cfgs, id)
      if (!isPlainObject(cfg) || !cfg.url) return

      datasourceLoadingMap[id] = true
      try {
        const method = (cfg.method || 'GET').toUpperCase()
        const url = normalizeDatasourceUrl(cfg.url)
        const params = normalizeDatasourceParams(cfg.params)

        const res =
          method === 'GET'
            ? await http.get(url, { params })
            : await http.request({ method, url, data: params })

        if (disposed) return
        // Backend response is expected to be directly consumable by the target component.
        datasourceEnumMap[id] = extractDatasourcePayload(res?.data)
      } catch (e) {
        // Keep the form usable even if a datasource fails.
        console.error('Error fetching datasource:', id, e)
      } finally {
        datasourceLoadingMap[id] = false
      }
    })
  )
}

watch([rawSchema, datasourceConfigs], () => {
  ensureDatasourcesLoaded()
}, { immediate: true })

const pickEnum = (node) => {
  const enums = Array.isArray(node?.enum) ? node.enum : []
  const first = enums[0]
  return first ? first.value ?? first.label : undefined
}

const generateExampleValues = (schemaObj) => {
  const values = {}
  const walk = (node) => {
    if (!isPlainObject(node)) return
    const propsNode = node.properties
    if (isPlainObject(propsNode)) {
      for (const key of Object.keys(propsNode)) {
        const child = propsNode[key]
        if (!isPlainObject(child)) continue
        if (isPlainObject(child.properties)) {
          walk(child)
          continue
        }
        if (child.type === 'void') continue

        const xc = child?.['x-component']
        if (xc === 'Switch') values[key] = true
        else if (xc === 'CheckboxGroup') {
          const v = pickEnum(child)
          values[key] = v !== undefined ? [v] : []
        } else if (xc === 'RadioGroup' || xc === 'Select') values[key] = pickEnum(child) ?? ''
        else if (xc === 'NumberPicker' || xc === 'Slider' || xc === 'Rate') values[key] = 1
        else if (xc === 'DateTimePicker') values[key] = new Date()
        else if (xc === 'Password') values[key] = '******'
        else values[key] = '示例数据'
      }
    }
  }

  walk(schemaObj)
  return values
}

const form = shallowRef(createForm({ values: {} }))

watch(
  [() => caseSchema.value, () => props.value?.initial_values],
  ([s]) => {
    const exampleValues = generateExampleValues(s)
    const preset = isPlainObject(props.value?.initial_values) ? props.value.initial_values : {}
    form.value = createForm({ values: { ...exampleValues, ...preset } })
  },
  { immediate: true }
)

const FormItem = defineComponent({
  name: 'FormItem',
  setup(_p, { slots }) {
    const fieldSchemaRef = useFieldSchema()
    return () =>
      h(
        ElFormItem,
        { label: fieldSchemaRef.value?.title, class: 'formily-case-item' },
        { default: () => slots.default?.() }
      )
  }
})

const Input = defineComponent({
  name: 'Input',
  inheritAttrs: false,
  props: { value: {}, placeholder: {}, disabled: {}, readOnly: {}, type: {} },
  emits: ['change'],
  setup(p, { emit }) {
    return () =>
      h(ElInput, {
        modelValue: p.value,
        placeholder: p.placeholder,
        disabled: p.disabled,
        readonly: p.readOnly,
        type: p.type,
        'onUpdate:modelValue': (v) => emit('change', v)
      })
  }
})

const Password = defineComponent({
  name: 'Password',
  inheritAttrs: false,
  props: { value: {}, placeholder: {}, disabled: {}, readOnly: {} },
  emits: ['change'],
  setup(p, { emit }) {
    return () =>
      h(ElInput, {
        modelValue: p.value,
        placeholder: p.placeholder,
        disabled: p.disabled,
        readonly: p.readOnly,
        type: 'password',
        showPassword: true,
        'onUpdate:modelValue': (v) => emit('change', v)
      })
  }
})

const Textarea = defineComponent({
  name: 'Textarea',
  inheritAttrs: false,
  props: { value: {}, placeholder: {}, disabled: {}, readOnly: {}, rows: {} },
  emits: ['change'],
  setup(p, { emit }) {
    return () =>
      h(ElInput, {
        modelValue: p.value,
        placeholder: p.placeholder,
        disabled: p.disabled,
        readonly: p.readOnly,
        type: 'textarea',
        rows: p.rows,
        'onUpdate:modelValue': (v) => emit('change', v)
      })
  }
})

const NumberPicker = defineComponent({
  name: 'NumberPicker',
  inheritAttrs: false,
  props: { value: {}, min: {}, max: {}, disabled: {} },
  emits: ['change'],
  setup(p, { emit }) {
    return () =>
      h(ElInputNumber, {
        modelValue: p.value,
        min: p.min,
        max: p.max,
        disabled: p.disabled,
        'onUpdate:modelValue': (v) => emit('change', v)
      })
  }
})

const Select = defineComponent({
  name: 'Select',
  inheritAttrs: false,
  props: { value: {}, placeholder: {}, disabled: {}, dataSource: {}, enum: {} },
  emits: ['change'],
  setup(p, { emit }) {
    const options = computed(() => (Array.isArray(p.dataSource) ? p.dataSource : Array.isArray(p.enum) ? p.enum : []))
    return () =>
      h(
        ElSelect,
        { modelValue: p.value, placeholder: p.placeholder, disabled: p.disabled, 'onUpdate:modelValue': (v) => emit('change', v) },
        {
          default: () =>
            options.value.map((o, idx) =>
              h(ElOption, { key: o?.value ?? o?.label ?? idx, label: o?.label, value: o?.value, disabled: o?.disabled })
            )
        }
      )
  }
})

const RadioGroup = defineComponent({
  name: 'RadioGroup',
  inheritAttrs: false,
  props: { value: {}, disabled: {}, dataSource: {}, enum: {} },
  emits: ['change'],
  setup(p, { emit }) {
    const options = computed(() => (Array.isArray(p.dataSource) ? p.dataSource : Array.isArray(p.enum) ? p.enum : []))
    return () =>
      h(
        ElRadioGroup,
        { modelValue: p.value, disabled: p.disabled, 'onUpdate:modelValue': (v) => emit('change', v) },
        {
          default: () =>
            options.value.map((o, idx) =>
              h(
                ElRadio,
                { key: o?.value ?? o?.label ?? idx, value: o?.value ?? o?.label ?? idx, disabled: o?.disabled },
                { default: () => o?.label ?? '' }
              )
            )
        }
      )
  }
})

const CheckboxGroup = defineComponent({
  name: 'CheckboxGroup',
  inheritAttrs: false,
  props: { value: {}, disabled: {}, dataSource: {}, enum: {} },
  emits: ['change'],
  setup(p, { emit }) {
    const options = computed(() => (Array.isArray(p.dataSource) ? p.dataSource : Array.isArray(p.enum) ? p.enum : []))
    return () =>
      h(
        ElCheckboxGroup,
        { modelValue: p.value, disabled: p.disabled, 'onUpdate:modelValue': (v) => emit('change', v) },
        {
          default: () =>
            options.value.map((o, idx) =>
              h(
                ElCheckbox,
                { key: o?.value ?? o?.label ?? idx, label: o?.value ?? o?.label ?? idx, disabled: o?.disabled },
                { default: () => o?.label ?? '' }
              )
            )
        }
      )
  }
})

const Switch = defineComponent({
  name: 'Switch',
  inheritAttrs: false,
  props: { value: {}, disabled: {} },
  emits: ['change'],
  setup(p, { emit }) {
    return () => h(ElSwitch, { modelValue: p.value, disabled: p.disabled, 'onUpdate:modelValue': (v) => emit('change', v) })
  }
})

const Button = defineComponent({
  name: 'Button',
  inheritAttrs: false,
  props: { text: {}, type: {}, disabled: {} },
  setup(p) {
    return () => h(ElButton, { type: p.type || 'primary', disabled: p.disabled }, { default: () => p.text ?? '' })
  }
})

const Link = defineComponent({
  name: 'Link',
  inheritAttrs: false,
  props: { text: {}, href: {}, target: {}, disabled: {} },
  setup(p) {
    return () =>
      h(
        ElLink,
        { href: p.href, target: p.target, disabled: p.disabled, type: 'primary', underline: false },
        { default: () => p.text ?? '' }
      )
  }
})

const Divider = defineComponent({
  name: 'Divider',
  inheritAttrs: false,
  setup() {
    return () => h(ElDivider)
  }
})

const DateTimePicker = defineComponent({
  name: 'DateTimePicker',
  inheritAttrs: false,
  props: { value: {}, disabled: {} },
  emits: ['change'],
  setup(p, { emit }) {
    return () => h(ElDatePicker, { modelValue: p.value, type: 'datetime', disabled: p.disabled, 'onUpdate:modelValue': (v) => emit('change', v) })
  }
})

const Slider = defineComponent({
  name: 'Slider',
  inheritAttrs: false,
  props: { value: {}, min: {}, max: {}, disabled: {} },
  emits: ['change'],
  setup(p, { emit }) {
    return () => h(ElSlider, { modelValue: p.value, min: p.min, max: p.max, disabled: p.disabled, 'onUpdate:modelValue': (v) => emit('change', v) })
  }
})

const Rate = defineComponent({
  name: 'Rate',
  inheritAttrs: false,
  props: { value: {}, disabled: {} },
  emits: ['change'],
  setup(p, { emit }) {
    return () => h(ElRate, { modelValue: p.value, disabled: p.disabled, 'onUpdate:modelValue': (v) => emit('change', v) })
  }
})

const Card = defineComponent({
  name: 'Card',
  props: { header: {} },
  setup(p, { slots }) {
    return () => h(ElCard, {}, { header: () => h('div', {}, p.header), default: () => slots.default?.() })
  }
})

const Tabs = defineComponent({
  name: 'Tabs',
  props: { panes: { type: Array, default: () => [] } },
  setup(p) {
    const fieldSchemaRef = useFieldSchema()
    const activeName = shallowRef(p?.panes?.[0]?.name)
    watch(
      () => p.panes,
      (panes) => {
        const next = panes?.[0]?.name
        if (!panes?.some((x) => x?.name === activeName.value)) activeName.value = next
      },
      { deep: true, immediate: true }
    )
    const tabKeyOf = (name) => `tab_${name}`
    return () =>
      h(
        ElTabs,
        { modelValue: activeName.value, 'onUpdate:modelValue': (v) => (activeName.value = v) },
        {
          default: () =>
            p.panes.map((pane, idx) => {
              const tabKey = tabKeyOf(pane?.name)
              const tabSchema = fieldSchemaRef.value?.properties?.[tabKey]
              return h(
                ElTabPane,
                { key: pane?.name ?? idx, label: pane?.label, name: pane?.name },
                tabSchema
                  ? {
                      default: () => h(RecursionField, { schema: tabSchema, name: tabKey })
                    }
                  : undefined
              )
            })
        }
      )
  }
})

const { SchemaField } = createSchemaField({
  components: {
    FormItem,
    Input,
    Password,
    Textarea,
    NumberPicker,
    Select,
    RadioGroup,
    CheckboxGroup,
    Switch,
    Button,
    Link,
    Divider,
    DateTimePicker,
    Slider,
    Rate,
    Tabs,
    Card
  }
})
</script>

<style scoped>
.formily-case {
  background-color: #ffffff;
  padding: 16px;
  border-radius: 8px;
}

.formily-case :deep(.formily-case-item) {
  margin-bottom: 12px;
}
</style>
