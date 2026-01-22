<template>
  <div class="formily-preview">
    <FormProvider :form="form">
      <!-- Use a plain relative-positioned canvas wrapper so absolute coords match the editor canvas. -->
      <div class="formily-preview-canvas" :style="{ minHeight: `${canvasMinHeight}px` }">
        <SchemaField :schema="schema" />
      </div>
    </FormProvider>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, shallowRef, watch } from 'vue'
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

const props = defineProps({
  // Backend schema value: { initial_values, datesources, schema }
  value: {
    type: Object,
    default: null
  }
})

const schema = computed(() => props.value?.schema || { type: 'object', properties: {} })

const form = shallowRef(
  createForm({
    values: props.value?.initial_values || {}
  })
)

watch(
  () => props.value,
  (val) => {
    form.value = createForm({ values: val?.initial_values || {} })
  }
)

const toPx = (v) => {
  if (typeof v === 'number' && Number.isFinite(v)) return v
  if (typeof v !== 'string') return NaN
  const m = v.trim().match(/-?\d+(\.\d+)?/)
  return m ? Number(m[0]) : NaN
}

const getNodeStyleFromSchemaNode = (node) => {
  const s1 = node?.['x-decorator-props']?.style
  const s2 = node?.['x-component-props']?.style
  return (s1 && typeof s1 === 'object') ? s1 : (s2 && typeof s2 === 'object') ? s2 : null
}

const collectMaxBottom = (node, acc) => {
  if (!node || typeof node !== 'object') return

  const style = getNodeStyleFromSchemaNode(node)
  if (style && style.position === 'absolute') {
    const top = toPx(style.top)
    const height = toPx(style.height)
    const fallbackHeight = 60
    if (Number.isFinite(top)) {
      acc.maxBottom = Math.max(acc.maxBottom, top + (Number.isFinite(height) ? height : fallbackHeight))
    }
  }

  const props = node.properties
  if (props && typeof props === 'object') {
    for (const key of Object.keys(props)) {
      collectMaxBottom(props[key], acc)
    }
  }

  // Handle array schemas if present.
  if (node.items) collectMaxBottom(node.items, acc)
}

const canvasMinHeight = computed(() => {
  const acc = { maxBottom: 0 }
  collectMaxBottom(schema.value, acc)
  // Add a bit of breathing room at the bottom for absolutely-positioned nodes.
  return Math.max(240, Math.ceil(acc.maxBottom + 40))
})

const getNodeStyle = (fieldSchema) => {
  const s1 = fieldSchema?.['x-decorator-props']?.style
  const s2 = fieldSchema?.['x-component-props']?.style
  return (s1 && typeof s1 === 'object') ? s1 : (s2 && typeof s2 === 'object') ? s2 : null
}

const FormItem = defineComponent({
  name: 'FormItem',
  setup(_p, { slots }) {
    const fieldSchemaRef = useFieldSchema()
    return () =>
      h(
        ElFormItem,
        {
          label: fieldSchemaRef.value?.title,
          class: 'formily-preview-item',
          // Keep the same absolute positioning surface as the editor canvas.
          style: getNodeStyle(fieldSchemaRef.value) || undefined
        },
        { default: () => slots.default?.() }
      )
  }
})

const Input = defineComponent({
  name: 'Input',
  inheritAttrs: false,
  props: {
    value: {},
    placeholder: {},
    disabled: {},
    readOnly: {},
    type: {}
  },
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
  props: {
    value: {},
    placeholder: {},
    disabled: {},
    readOnly: {}
  },
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
  props: {
    value: {},
    placeholder: {},
    disabled: {},
    readOnly: {},
    rows: {}
  },
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
  props: {
    value: {},
    min: {},
    max: {},
    disabled: {}
  },
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
  props: {
    value: {},
    placeholder: {},
    disabled: {},
    dataSource: {},
    enum: {}
  },
  emits: ['change'],
  setup(p, { emit }) {
    const options = computed(() => (Array.isArray(p.dataSource) ? p.dataSource : Array.isArray(p.enum) ? p.enum : []))
    return () =>
      h(
        ElSelect,
        {
          modelValue: p.value,
          placeholder: p.placeholder,
          disabled: p.disabled,
          'onUpdate:modelValue': (v) => emit('change', v)
        },
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
  props: {
    value: {},
    disabled: {},
    dataSource: {},
    enum: {}
  },
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
  props: {
    value: {},
    disabled: {},
    dataSource: {},
    enum: {}
  },
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
  props: {
    value: {},
    disabled: {}
  },
  emits: ['change'],
  setup(p, { emit }) {
    return () => h(ElSwitch, { modelValue: p.value, disabled: p.disabled, 'onUpdate:modelValue': (v) => emit('change', v) })
  }
})

const DateTimePicker = defineComponent({
  name: 'DateTimePicker',
  inheritAttrs: false,
  props: {
    value: {},
    disabled: {}
  },
  emits: ['change'],
  setup(p, { emit }) {
    return () =>
      h(ElDatePicker, {
        modelValue: p.value,
        type: 'datetime',
        disabled: p.disabled,
        'onUpdate:modelValue': (v) => emit('change', v)
      })
  }
})

const Slider = defineComponent({
  name: 'Slider',
  inheritAttrs: false,
  props: {
    value: {},
    min: {},
    max: {},
    disabled: {}
  },
  emits: ['change'],
  setup(p, { emit }) {
    return () =>
      h(ElSlider, {
        modelValue: p.value,
        min: p.min,
        max: p.max,
        disabled: p.disabled,
        'onUpdate:modelValue': (v) => emit('change', v)
      })
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

const Button = defineComponent({
  name: 'Button',
  inheritAttrs: false,
  props: { text: {}, type: {}, disabled: {}, style: {} },
  setup(p) {
    // Void component: style is usually stored in `x-component-props.style`.
    return () =>
      h(
        'div',
        { style: p.style && typeof p.style === 'object' ? p.style : undefined },
        { default: () => h(ElButton, { type: p.type || 'primary', disabled: p.disabled, style: { width: '100%', height: '100%' } }, { default: () => p.text ?? '' }) }
      )
  }
})

const Link = defineComponent({
  name: 'Link',
  inheritAttrs: false,
  props: { text: {}, href: {}, target: {}, disabled: {}, style: {} },
  setup(p) {
    return () =>
      h(
        'div',
        { style: p.style && typeof p.style === 'object' ? p.style : undefined },
        { default: () => h(ElLink, { href: p.href, target: p.target, disabled: p.disabled, type: 'primary', underline: false }, { default: () => p.text ?? '' }) }
      )
  }
})

const Divider = defineComponent({
  name: 'Divider',
  inheritAttrs: false,
  props: { style: {} },
  setup(p) {
    return () => h('div', { style: p.style && typeof p.style === 'object' ? p.style : undefined }, { default: () => h(ElDivider) })
  }
})

const Card = defineComponent({
  name: 'Card',
  props: {
    header: {}
  },
  setup(p, { slots }) {
    const fieldSchemaRef = useFieldSchema()
    const nodeStyle = computed(() => getNodeStyle(fieldSchemaRef.value))
    return () =>
      h(
        ElCard,
        {
          // Match the editor: card itself carries the absolute positioning.
          style: nodeStyle.value || undefined
        },
        {
          header: () => h('div', {}, p.header),
          default: () => slots.default?.()
        }
      )
  }
})

const Tabs = defineComponent({
  name: 'Tabs',
  props: {
    panes: {
      type: Array,
      default: () => []
    }
  },
  setup(p) {
    const fieldSchemaRef = useFieldSchema()
    const nodeStyle = computed(() => getNodeStyle(fieldSchemaRef.value))
    const activeName = shallowRef(p?.panes?.[0]?.name)
    watch(
      () => p.panes,
      (panes) => {
        const next = panes?.[0]?.name
        if (!panes?.some((x) => x?.name === activeName.value)) {
          activeName.value = next
        }
      },
      { deep: true, immediate: true }
    )

    const tabKeyOf = (name) => `tab_${name}`

    return () =>
      h(
        ElTabs,
        {
          // Match the editor: tabs itself carries the absolute positioning.
          style: nodeStyle.value || undefined,
          modelValue: activeName.value,
          'onUpdate:modelValue': (v) => (activeName.value = v)
        },
        {
          default: () =>
            p.panes.map((pane, idx) => {
              const tabKey = tabKeyOf(pane?.name)
              const tabSchema = fieldSchemaRef.value?.properties?.[tabKey]
              const unwrapCard =
                tabSchema &&
                tabSchema['x-component'] === 'Card' &&
                tabSchema.properties &&
                typeof tabSchema.properties === 'object'

              return h(
                ElTabPane,
                { key: pane?.name ?? idx, label: pane?.label, name: pane?.name },
                tabSchema
                  ? {
                      default: () =>
                        unwrapCard
                          ? h(RecursionField, { schema: { type: 'object', properties: tabSchema.properties }, name: tabKey })
                          : h(RecursionField, { schema: tabSchema, name: tabKey })
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
    DateTimePicker,
    Slider,
    Rate,
    Button,
    Link,
    Divider,
    Tabs,
    Card
  }
})
</script>

<style scoped>
.formily-preview {
  padding: 0;
}

.formily-preview-canvas {
  position: relative;
  background-color: #ffffff;
  border-radius: 8px;
  overflow: visible;
}

.formily-preview :deep(.formily-preview-item) {
  width: 100%;
  margin: 0 !important;
  padding: 0 8px;
  display: flex !important;
  flex-direction: row !important;
  align-items: center !important;
  gap: 8px;
  box-sizing: border-box;
}

.formily-preview :deep(.formily-preview-item .el-form-item__content) {
  margin-left: 0 !important;
  padding-left: 0 !important;
  flex: 1;
  min-width: 0;
}

.formily-preview :deep(.el-form-item) {
  box-sizing: border-box;
}
</style>
