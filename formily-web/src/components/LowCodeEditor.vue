<template>
  <div class="lowcode-editor">
    <!-- 顶部工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="saveProject">
            <el-icon><ElementPlusIconsVue.Document /></el-icon>
            保存
          </el-button>
          <el-button @click="undo">
            <el-icon><ElementPlusIconsVue.Back /></el-icon>
            撤销
          </el-button>
          <el-button @click="redo">
            <el-icon><ElementPlusIconsVue.Right /></el-icon>
            重做
          </el-button>
          <el-divider direction="vertical" />
          <el-button @click="toggleGrid">
            <el-icon><ElementPlusIconsVue.Grid /></el-icon>
            网格线
          </el-button>
          <el-switch
            v-model="editorState.showGrid"
            size="small"
            inline-prompt
            active-text="显示"
            inactive-text="隐藏"
            class="switch-inline"
            @change="handleGridChange"
          />
      </div>
      <div class="toolbar-right">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>低代码编辑</el-breadcrumb-item>
          <el-breadcrumb-item>表单设计</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>
    
    <!-- 三栏布局 -->
    <div class="editor-content">
      <!-- 左侧组件库 -->
      <div class="left-panel" :class="{ 'collapsed': editorState.leftPanelCollapsed }">
        <div class="panel-header">
          <h3>组件库</h3>
          <el-button
            type="text"
            @click="toggleLeftPanel"
            class="collapse-btn"
          >
            <el-icon>
              <ElementPlusIconsVue.ArrowRight v-if="editorState.leftPanelCollapsed" />
              <ElementPlusIconsVue.ArrowLeft v-else />
            </el-icon>
          </el-button>
        </div>
        <ComponentLibrary
          @drag-start="handleDragStart"
        />
      </div>
      
      <!-- 中间画布 -->
      <div class="center-panel">
        <Canvas
          ref="canvasRef"
          :editor-state="editorState"
          @select-component="handleSelectComponent"
          @update-component="handleUpdateComponent"
          @add-component="handleAddComponent"
        />
      </div>
      
      <!-- 右侧属性面板 -->
      <div class="right-panel" :class="{ 'collapsed': editorState.rightPanelCollapsed }">
        <div class="panel-header">
          <h3>属性配置</h3>
          <el-button
            type="text"
            @click="toggleRightPanel"
            class="collapse-btn"
          >
            <el-icon>
              <ElementPlusIconsVue.ArrowLeft v-if="editorState.rightPanelCollapsed" />
              <ElementPlusIconsVue.ArrowRight v-else />
            </el-icon>
          </el-button>
        </div>
        <PropertyPanel
          :selected-component="selectedComponent"
          :editor-state="editorState"
          @update-property="handleUpdateProperty"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import ComponentLibrary from './ComponentLibrary.vue'
import Canvas from './Canvas.vue'
import PropertyPanel from './PropertyPanel.vue'
import api from '../services/api.js'

const LOWCODE_IMPORT_KEY = 'lowcode-editor-import'
const CURRENT_PROJECT_ID_KEY = 'current-project-id'

// 编辑器状态
const editorState = reactive({
  showGrid: true,
  gridSize: 20,
  leftPanelCollapsed: false,
  rightPanelCollapsed: false,
  components: [],
  history: [],
  historyIndex: -1,
  selectedComponentId: null
})

// 选中的组件
const selectedComponent = computed(() => {
  if (!editorState.selectedComponentId) return null
  return editorState.components.find(component => component.id === editorState.selectedComponentId)
})

// 画布引用
const canvasRef = ref(null)

// 初始化编辑器状态
const initEditorState = () => {
  // 从localStorage加载状态
  const savedState = localStorage.getItem('lowcode-editor-state')
  if (savedState) {
    try {
      const parsedState = JSON.parse(savedState)
      Object.assign(editorState, parsedState)
      ElMessage.success('已恢复上次编辑状态')
    } catch (error) {
      console.error('Failed to load saved state:', error)
      ElMessage.error('恢复状态失败')
    }
  }
}

// 保存编辑器状态
const saveEditorState = () => {
  const stateToSave = {
    showGrid: editorState.showGrid,
    gridSize: editorState.gridSize,
    leftPanelCollapsed: editorState.leftPanelCollapsed,
    rightPanelCollapsed: editorState.rightPanelCollapsed,
    components: editorState.components
  }
  localStorage.setItem('lowcode-editor-state', JSON.stringify(stateToSave))
}

const deepClone = (obj) => JSON.parse(JSON.stringify(obj))

const generateId = () => `component_${Math.random().toString(36).slice(2, 11)}`

const defaultMetaForType = (type) => {
  const map = {
    'input-text': { category: 'form', name: '文本输入框' },
    'input-number': { category: 'form', name: '数字输入框' },
    'input-password': { category: 'form', name: '密码框' },
    textarea: { category: 'form', name: '多行文本' },
    select: { category: 'form', name: '下拉列表' },
    radio: { category: 'form', name: '单选按钮' },
    checkbox: { category: 'form', name: '复选按钮' },
    switch: { category: 'form', name: '开关' },
    'datetime-picker': { category: 'form', name: '日期时间' },
    slider: { category: 'form', name: '滑块' },
    rate: { category: 'form', name: '评分' },
    tabs: { category: 'form', name: '标签页' },
    'form-container': { category: 'form', name: '表单容器' },
    panel: { category: 'basic', name: '面板' },
    collapse: { category: 'display', name: '折叠面板' }
  }
  return map[type] || { category: 'basic', name: type }
}

const defaultStyleForType = (type, depth, order) => {
  const left = 20 + depth * 30
  const top = 20 + order * 90
  const base = {
    left: `${left}px`,
    top: `${top}px`,
    position: 'absolute'
  }
  if (type === 'tabs') return { ...base, width: '700px', height: '360px' }
  if (type === 'panel' || type === 'form-container') return { ...base, width: '700px', height: '260px' }
  if (type === 'textarea') return { ...base, width: '420px', height: '120px' }
  if (type === 'slider') return { ...base, width: '320px', height: '60px' }
  return { ...base, width: '260px', height: '60px' }
}

const mapXComponentToEditorType = (node) => {
  const xc = node?.['x-component']
  switch (xc) {
    case 'Input':
      return 'input-text'
    case 'NumberPicker':
      return 'input-number'
    case 'Password':
      return 'input-password'
    case 'Textarea':
      return 'textarea'
    case 'Select':
      return 'select'
    case 'RadioGroup':
      return 'radio'
    case 'CheckboxGroup':
      return 'checkbox'
    case 'Switch':
      return 'switch'
    case 'DateTimePicker':
      return 'datetime-picker'
    case 'Slider':
      return 'slider'
    case 'Rate':
      return 'rate'
    case 'Tabs':
      return 'tabs'
    case 'Card':
      return 'panel'
    case 'Collapse':
      return 'collapse'
    default:
      return null
  }
}

const importFormilyValueToEditorComponents = (value) => {
  const components = []
  const initialValues = value?.initial_values || {}
  const rootSchema = value?.schema
  if (!rootSchema || typeof rootSchema !== 'object') return components

  const getNodeStyleFromSchemaNode = (node) => {
    const s1 = node?.['x-decorator-props']?.style
    const s2 = node?.['x-component-props']?.style
    return (s1 && typeof s1 === 'object') ? s1 : (s2 && typeof s2 === 'object') ? s2 : null
  }

  const makeComponent = ({ type, key, node, parentId, tabName, depth, order }) => {
    const meta = defaultMetaForType(type)
    const xp = node?.['x-component-props'] || {}
    const importedStyle = getNodeStyleFromSchemaNode(node)
    const baseStyle = defaultStyleForType(type, depth, order)
    const comp = {
      id: generateId(),
      type,
      name: meta.name,
      description: meta.name,
      category: meta.category,
      icon: '',
      parentId: parentId || undefined,
      props: {
        name: key,
        label: node?.title || meta.name,
        required: !!node?.required,
        disabled: false,
        readonly: false,
        defaultValue: initialValues?.[key]
      },
      // Restore saved layout when present; fall back to a sensible default for legacy schemas.
      style: importedStyle ? { ...baseStyle, ...importedStyle } : baseStyle
    }

    if (tabName) comp.props.tabName = tabName

    if (xp?.placeholder) comp.props.placeholder = xp.placeholder
    if (typeof xp?.rows === 'number') comp.props.rows = xp.rows
    if (typeof xp?.min === 'number') comp.props.min = xp.min
    if (typeof xp?.max === 'number') comp.props.max = xp.max

    if ((type === 'select' || type === 'radio' || type === 'checkbox') && Array.isArray(node?.enum)) {
      comp.props.options = node.enum.map((o) => ({ label: o?.label, value: o?.value }))
    }

    if (type === 'switch') {
      comp.props.defaultValue = !!initialValues?.[key]
    }

    if (type === 'checkbox') {
      comp.props.defaultValue = Array.isArray(initialValues?.[key]) ? initialValues?.[key] : []
    }

    if (type === 'tabs') {
      const panes = Array.isArray(xp?.panes) ? xp.panes : []
      comp.props.label = node?.title || comp.props.label
      comp.props.activeName = panes?.[0]?.name || '1'
      comp.props.type = 'card'
      comp.props.closable = false
      comp.props.addable = false
      comp.props.editable = false
      comp.props.disabled = false
      comp.props.tabs = panes.map((p) => ({
        name: `${p?.name ?? ''}`,
        label: `${p?.label ?? ''}`,
        content: `${p?.content ?? ''}`
      }))
    }

    if (type === 'panel') {
      comp.props.title = xp?.header || node?.title || comp.props.label || '面板'
      comp.props.border = true
    }

    // Clean up label/defaultValue for void containers
    if (node?.type === 'void') {
      delete comp.props.defaultValue
      delete comp.props.placeholder
    }

    return comp
  }

  const walk = ({ key, node, parentId, tabName, depth, order, tabChildOrderRef }) => {
    if (!node || typeof node !== 'object') return
    const type = mapXComponentToEditorType(node)
    if (!type) return

    if (type === 'tabs') {
      const tabsComp = makeComponent({ type, key, node, parentId, tabName, depth, order })
      components.push(tabsComp)

      const panes = Array.isArray(tabsComp.props.tabs) ? tabsComp.props.tabs : []
      const propsNode = node?.properties || {}
      for (const pane of panes) {
        const paneName = pane?.name
        const tabKey = `tab_${paneName}`
        const tabContainer = propsNode?.[tabKey]
        const tabFields = tabContainer?.properties || {}

        let localOrder = 0
        for (const childKey of Object.keys(tabFields)) {
          walk({
            key: childKey,
            node: tabFields[childKey],
            parentId: tabsComp.id,
            tabName: paneName,
            depth: depth + 1,
            order: tabChildOrderRef ? tabChildOrderRef.value++ : localOrder++,
            tabChildOrderRef
          })
        }
      }
      return
    }

    const comp = makeComponent({ type, key, node, parentId, tabName, depth, order })
    components.push(comp)

    // Parse container children (Card/panel)
    const childProps = node?.properties
    if (childProps && typeof childProps === 'object' && (type === 'panel' || type === 'form-container' || type === 'collapse')) {
      let localOrder = 0
      for (const childKey of Object.keys(childProps)) {
        walk({
          key: childKey,
          node: childProps[childKey],
          parentId: comp.id,
          tabName: null,
          depth: depth + 1,
          order: localOrder++,
          tabChildOrderRef
        })
      }
    }
  }

  const rootProps = rootSchema?.properties || {}
  const tabChildOrderRef = { value: 0 }
  let rootOrder = 0
  for (const key of Object.keys(rootProps)) {
    walk({
      key,
      node: rootProps[key],
      parentId: null,
      tabName: null,
      depth: 0,
      order: rootOrder++,
      tabChildOrderRef
    })
  }
  return components
}

const applyImportedSchemaIfPresent = () => {
  const raw = localStorage.getItem(LOWCODE_IMPORT_KEY)
  if (!raw) return false
  try {
    const schemaItem = JSON.parse(raw)
    const value = schemaItem?.value
    const imported = importFormilyValueToEditorComponents(value)

    // Replace the current editor content.
    editorState.components = imported
    editorState.selectedComponentId = null
    editorState.history = []
    editorState.historyIndex = -1

    // Persist for refresh, then clear the one-shot import payload.
    saveEditorState()
    localStorage.removeItem(LOWCODE_IMPORT_KEY)

    if (schemaItem?.id) {
      localStorage.setItem(CURRENT_PROJECT_ID_KEY, schemaItem.id)
    }

    ElMessage.success('已加载 Schema，可继续编辑')
    return true
  } catch (e) {
    console.error('Failed to apply imported schema:', e)
    localStorage.removeItem(LOWCODE_IMPORT_KEY)
    return false
  }
}

// 将编辑器组件转换为Formily Schema（按设计方案的组件Schema模板生成）
const convertToFormilySchema = (componentDatasourceMap = new Map()) => {
  const SCHEMA_TEMPLATES = {
    // Form
    'input-text': {
      type: 'string',
      title: 'Input',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Input',
      'x-component-props': { placeholder: 'Please input' }
    },
    'input-number': {
      type: 'number',
      title: 'Number',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'NumberPicker',
      'x-component-props': { min: 0 }
    },
    'input-password': {
      type: 'string',
      title: 'Password',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Password',
      'x-component-props': { placeholder: 'Password' }
    },
    textarea: {
      type: 'string',
      title: 'Textarea',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Textarea',
      'x-component-props': { placeholder: '...' }
    },
    select: {
      type: 'string',
      title: 'Select',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Select',
      enum: [{ label: 'A', value: 'a' }, { label: 'B', value: 'b' }]
    },
    radio: {
      type: 'string',
      title: 'Radio',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'RadioGroup',
      enum: [{ label: 'Yes', value: 'yes' }, { label: 'No', value: 'no' }]
    },
    checkbox: {
      type: 'array',
      title: 'Checkbox',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'CheckboxGroup',
      enum: [{ label: 'A', value: 'a' }, { label: 'B', value: 'b' }]
    },
    switch: {
      type: 'boolean',
      title: 'Switch',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Switch'
    },
    'datetime-picker': {
      type: 'string',
      title: 'DateTime',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'DateTimePicker'
    },
    slider: {
      type: 'number',
      title: 'Slider',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Slider',
      'x-component-props': { min: 0, max: 100 }
    },
    rate: {
      type: 'number',
      title: 'Rate',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Rate'
    },
    'form-container': {
      type: 'void',
      'x-component': 'Card',
      'x-component-props': { header: 'Form Container' }
    },

    // Basic
    button: { type: 'void', 'x-component': 'Button', 'x-component-props': { text: 'Click me' } },
    tag: { type: 'void', 'x-component': 'Tag', 'x-component-props': { text: 'Tag' } },
    link: { type: 'void', 'x-component': 'Link', 'x-component-props': { text: 'Link', href: '#' } },
    loading: { type: 'void', 'x-component': 'LoadingIndicator', 'x-component-props': { rows: 3, animated: true } },
    alert: { type: 'void', 'x-component': 'Alert', 'x-component-props': { title: 'Info', type: 'info', description: 'Message' } },
    tooltip: { type: 'void', 'x-component': 'Tooltip', 'x-component-props': { text: 'Hover', content: 'Tooltip content' } },
    popover: { type: 'void', 'x-component': 'Popover', 'x-component-props': { text: 'Click', title: 'Popover', content: 'Popover content' } },
    progress: { type: 'void', 'x-component': 'Progress', 'x-component-props': { percentage: 50 } },
    divider: { type: 'void', 'x-component': 'Divider' },
    panel: { type: 'void', 'x-component': 'Card', 'x-component-props': { header: 'Panel' } },

    // Display
    table: {
      type: 'void',
      'x-component': 'Table',
      'x-component-props': {
        columns: [{ prop: 'name', label: 'Name' }],
        data: [{ name: 'Row 1' }, { name: 'Row 2' }]
      }
    },
    list: { type: 'void', 'x-component': 'List', 'x-component-props': { items: ['Item 1', 'Item 2'] } },
    descriptions: {
      type: 'void',
      'x-component': 'Descriptions',
      'x-component-props': { title: 'Descriptions', items: [{ label: 'A', value: '1' }, { label: 'B', value: '2' }] }
    },
    tree: {
      type: 'void',
      'x-component': 'Tree',
      'x-component-props': { data: [{ label: 'Node 1', children: [{ label: 'Child 1' }] }] }
    },
    collapse: {
      type: 'void',
      'x-component': 'Collapse',
      'x-component-props': { items: [{ name: '1', title: 'Title 1', content: 'Content 1' }] }
    },
    tabs: {
      type: 'void',
      'x-component': 'Tabs',
      'x-component-props': {
        panes: [
          { name: 'a', label: 'Tab A', content: 'Content A' },
          { name: 'b', label: 'Tab B', content: 'Content B' }
        ]
      }
    },
    carousel: { type: 'void', 'x-component': 'Carousel', 'x-component-props': { items: ['Slide 1', 'Slide 2'], height: '160px' } },
    statistic: { type: 'void', 'x-component': 'Statistic', 'x-component-props': { title: 'Orders', value: 1024 } },
    timeline: { type: 'void', 'x-component': 'Timeline', 'x-component-props': { items: [{ timestamp: '2026-01-01', content: 'Event' }] } },
    breadcrumb: { type: 'void', 'x-component': 'Breadcrumb', 'x-component-props': { items: [{ text: 'Home' }, { text: 'Page' }] } },
    pagination: { type: 'void', 'x-component': 'Pagination', 'x-component-props': { total: 100, pageSize: 10 } }
  }

  const components = editorState.components || []

  const parsePx = (v) => {
    if (typeof v === 'number') return v
    if (typeof v !== 'string') return 0
    const m = v.match(/-?\\d+(?:\\.\\d+)?/)
    return m ? Number(m[0]) : null
  }

  const rectOf = (c) => {
    const left = parsePx(c?.style?.left) ?? 0
    const top = parsePx(c?.style?.top) ?? 0
    const width = parsePx(c?.style?.width) ?? 0
    const height = parsePx(c?.style?.height) ?? 0
    return { left, top, right: left + width, bottom: top + height, area: Math.max(width, 0) * Math.max(height, 0) }
  }

  const contains = (outer, inner) =>
    inner.left >= outer.left && inner.top >= outer.top && inner.right <= outer.right && inner.bottom <= outer.bottom

  const isImplicitContainer = (type) => type === 'form-container' || type === 'panel'

  // 如果组件被摆放在容器(如Card)内部，则自动视为嵌套（无需显式parentId）
  const implicitParentById = new Map()
  const containers = components.filter((c) => isImplicitContainer(c.type))
  for (const child of components) {
    if (child.parentId) continue
    if (isImplicitContainer(child.type)) continue
    const childRect = rectOf(child)
    let best = null
    for (const container of containers) {
      if (container.id === child.id) continue
      const r = rectOf(container)
      if (r.area <= 0) continue
      if (contains(r, childRect)) {
        if (!best || r.area < best.area) best = { id: container.id, area: r.area }
      }
    }
    if (best) implicitParentById.set(child.id, best.id)
  }

  const parentIdOf = (c) => c.parentId || implicitParentById.get(c.id) || null
  const childrenByParent = new Map()
  for (const c of components) {
    const pid = parentIdOf(c)
    if (!pid) continue
    if (!childrenByParent.has(pid)) childrenByParent.set(pid, [])
    childrenByParent.get(pid).push(c)
  }

  const sanitizeKey = (k) => (k || '').toString().trim().replace(/[^a-zA-Z0-9_$]/g, '_')
  const resolveFieldKey = (component, used) => {
    const preferred = sanitizeKey(component?.props?.name)
    const base = preferred || `field_${sanitizeKey(component.id)}`
    let key = base
    let i = 2
    while (used.has(key)) {
      key = `${base}_${i++}`
    }
    used.add(key)
    return key
  }

  const applyPropsToSchema = (component, node) => {
    const p = component?.props || {}
    if (p.label && node.title) node.title = p.label
    if (typeof p.required === 'boolean') node.required = p.required

    // Ensure props object exists when needed.
    if (!node['x-component-props'] && node['x-component'] && node.type === 'void') {
      node['x-component-props'] = {}
    }
    if (node['x-component-props'] && typeof node['x-component-props'] === 'object') {
      if (p.placeholder) node['x-component-props'].placeholder = p.placeholder
      if (typeof p.min === 'number') node['x-component-props'].min = p.min
      if (typeof p.max === 'number') node['x-component-props'].max = p.max
      if (p.text) node['x-component-props'].text = p.text
      if (p.href) node['x-component-props'].href = p.href
      if (p.title && node['x-component'] === 'Card') node['x-component-props'].header = p.title
      if (p.header && node['x-component'] === 'Card') node['x-component-props'].header = p.header
      if (typeof p.rows === 'number') node['x-component-props'].rows = p.rows
      if (typeof p.animated === 'boolean') node['x-component-props'].animated = p.animated
      if (typeof p.percentage === 'number') node['x-component-props'].percentage = p.percentage
    }

    // enum from options
    if ((component.type === 'select' || component.type === 'radio' || component.type === 'checkbox') && Array.isArray(p.options)) {
      node.enum = p.options.map((o) => ({ label: o.label, value: o.value }))
    }

    // Tabs panes from editor props
    if (component.type === 'tabs' && Array.isArray(p.tabs)) {
      node['x-component-props'] = node['x-component-props'] || {}
      node['x-component-props'].panes = p.tabs.map((t) => ({ name: t.name, label: t.label, content: t.content }))
    }

    // Collapse items from editor props
    if (component.type === 'collapse' && Array.isArray(p.items)) {
      node['x-component-props'] = node['x-component-props'] || {}
      node['x-component-props'].items = p.items.map((it) => ({ name: it.name, title: it.title, content: it.content }))
    }
  }

  const attachStyleToSchema = (node, styleObj) => {
    if (!styleObj) return
    // Layout (position/left/top/width/height) should be applied on the outer wrapper:
    // - for form fields, that's the decorator (FormItem) so label + content move together.
    // - for void/container nodes, that's the component itself.
    if (node['x-decorator']) {
      const existing =
        node['x-decorator-props'] && typeof node['x-decorator-props'] === 'object' ? node['x-decorator-props'] : {}
      node['x-decorator-props'] = { ...existing, style: styleObj }
    } else {
      const existing =
        node['x-component-props'] && typeof node['x-component-props'] === 'object' ? node['x-component-props'] : {}
      node['x-component-props'] = { ...existing, style: styleObj }
    }
  }

  const buildNode = (component) => {
    const template = SCHEMA_TEMPLATES[component.type] || { type: 'void', 'x-component': 'div' }
    const node = deepClone(template)
    applyPropsToSchema(component, node)

    const dsId = componentDatasourceMap.get(component.id)
    if (dsId) node.datesource_id = dsId

    // Persist layout to schema so backend preview matches the editor.
    // NOTE: keep the same coordinate system as the editor; do not rebase child coords here.
    attachStyleToSchema(node, { ...component.style })

    const children = childrenByParent.get(component.id) || []
    const isContainer = component.type === 'form-container' || component.type === 'panel' || component.type === 'tabs' || component.type === 'collapse'
    if (children.length && isContainer) {
      node.properties = node.properties || {}

      if (component.type === 'tabs' && Array.isArray(component?.props?.tabs)) {
        // Put children into each tab container (Card), keyed by tabName.
        const tabs = component.props.tabs
        for (const tab of tabs) {
          const tabKey = sanitizeKey(`tab_${tab.name}`) || `tab_${tab.name}`
          const tabContainer = deepClone(SCHEMA_TEMPLATES['form-container'])
          tabContainer['x-component-props'] = { header: tab.label || `Tab ${tab.name}` }
          tabContainer.properties = {}

          const tabChildren = children.filter((c) => c?.props?.tabName === tab.name)
          const used = new Set()
          for (const ch of tabChildren) {
            const ck = resolveFieldKey(ch, used)
            tabContainer.properties[ck] = buildNode(ch)
          }
          node.properties[tabKey] = tabContainer
        }
      } else {
        const used = new Set()
        for (const ch of children) {
          const ck = resolveFieldKey(ch, used)
          node.properties[ck] = buildNode(ch)
        }
      }
    }
    return node
  }

  const schema = { type: 'object', properties: {} }
  const roots = components.filter((c) => !parentIdOf(c))
  const used = new Set()
  for (const c of roots) {
    const k = resolveFieldKey(c, used)
    schema.properties[k] = buildNode(c)
  }
  return schema
}

// 生成完整的Formily格式数据
const generateFormilyData = () => {
  // 生成initial_values
  const initialValues = {}
  
  // 生成datesources（与设计方案字段名一致）
  const datesources = {}

  // 遍历所有组件，生成initial_values和datesources映射
  const componentDatasourceMap = new Map() // 组件ID到数据源ID的映射
  
  editorState.components.forEach(component => {
    // 处理initial_values：保存属性配置面板中内容属性部分中的“组件名称项”
    // 无论组件类型，只要有name属性就保存
    if (component.props.name) {
      // 使用组件props中的name属性（即属性配置面板中内容属性部分中的“组件名称项”）作为key
      const fieldKey = component.props.name
      // 使用默认值作为initial_value
      initialValues[fieldKey] = component.props.defaultValue || ''
    }
    
    // 处理datesources：获取请求信息
    if (component.props.requestConfig) {
      const { url, method, params } = component.props.requestConfig
      if (url) {
        // 生成固定的数据源ID，使用组件ID确保唯一性和可追溯性
        const datasourceId = `datasource_${component.id}`
        
        // 保存数据源
        datesources[datasourceId] = {
          id: datasourceId, // 数据源ID与键名一致
          method: method || 'GET',
          url: url,
          params: params || []
        }
        
        // 记录组件与数据源的映射关系
        componentDatasourceMap.set(component.id, datasourceId)
      }
    }
  })

  // 生成schema（会把datesource_id写入对应组件节点）
  const schema = convertToFormilySchema(componentDatasourceMap)
  
  // 构建完整的Formily格式数据
  const formilyData = {
    initial_values: initialValues,
    datesources: datesources,
    schema: schema
  }
  
  return formilyData
}

// 保存项目
const saveProject = async () => {
  try {
    // 保存到localStorage
    saveEditorState()
    
    // 生成完整的Formily格式数据
    const formilyData = generateFormilyData()
    
    // 获取当前项目的ID（从localStorage或其他地方获取）
    // 这里假设我们已经有一个项目ID存储在localStorage中
    const projectId = localStorage.getItem('current-project-id')
    
    let response
    if (projectId) {
      // 更新项目
      response = await api.schema.updateSchema(projectId, formilyData)
    } else {
      // 创建新项目
      response = await api.schema.createSchema(formilyData)
      // 保存新项目ID到localStorage
      if (response?.data?.id) {
        localStorage.setItem('current-project-id', response.data.id)
      }
    }
    
    ElMessage.success(projectId ? '项目已更新到后端' : '项目已保存到后端')
    console.log('保存成功，响应:', response.data)
    console.log('生成的Formily数据:', JSON.stringify(formilyData, null, 2))
    console.log('浏览器存储的组件:', JSON.stringify(editorState.components, null, 2))
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查网络连接或后端服务')
  }
}

// 记录历史
const recordHistory = () => {
  // Drop redo branch, then push the *current* snapshot.
  editorState.history = editorState.history.slice(0, editorState.historyIndex + 1)
  editorState.history.push(JSON.stringify(editorState.components))
  editorState.historyIndex = editorState.history.length - 1
  if (editorState.history.length > 50) {
    editorState.history.shift()
    editorState.historyIndex = editorState.history.length - 1
  }
}

// 撤销
const undo = () => {
  if (editorState.historyIndex > 0) {
    editorState.historyIndex--
    const historyState = JSON.parse(editorState.history[editorState.historyIndex])
    editorState.components = historyState
    // 清除选中状态
    editorState.selectedComponentId = null
    saveEditorState()
  } else {
    ElMessage.info('已经是最早状态')
  }
}

// 重做
const redo = () => {
  if (editorState.historyIndex < editorState.history.length - 1) {
    editorState.historyIndex++
    const historyState = JSON.parse(editorState.history[editorState.historyIndex])
    editorState.components = historyState
    // 清除选中状态
    editorState.selectedComponentId = null
    saveEditorState()
  } else {
    ElMessage.info('已经是最新状态')
  }
}

// 切换网格显示
const toggleGrid = () => {
  editorState.showGrid = !editorState.showGrid
  saveEditorState()
}

const handleGridChange = () => {
  saveEditorState()
}

// 切换左侧面板
const toggleLeftPanel = () => {
  editorState.leftPanelCollapsed = !editorState.leftPanelCollapsed
  saveEditorState()
}

// 切换右侧面板
const toggleRightPanel = () => {
  editorState.rightPanelCollapsed = !editorState.rightPanelCollapsed
  saveEditorState()
}

// 处理拖拽开始
const handleDragStart = (componentType) => {
  // 可以在这里添加拖拽开始的逻辑
}

// 处理组件选择
const handleSelectComponent = (componentId) => {
  editorState.selectedComponentId = componentId
}

// 处理组件更新
const handleUpdateComponent = (updatedComponent) => {
  console.log('=== Update Component Debug ===')
  console.log('更新的组件:', updatedComponent)
  console.log('更新组件的parentId:', updatedComponent.parentId)
  console.log('更新组件的tabName:', updatedComponent.props.tabName)
  console.log('=============================')
  
  const index = editorState.components.findIndex(c => c.id === updatedComponent.id)
  if (index !== -1) {
    editorState.components[index] = updatedComponent
    recordHistory() // 记录历史（记录变更后的状态，支持 redo）
    saveEditorState()
  }
}

// 处理添加组件
const handleAddComponent = (newComponent) => {
  console.log('=== Add Component Debug ===')
  console.log('添加的新组件:', newComponent)
  console.log('新组件的parentId:', newComponent.parentId)
  console.log('新组件的tabName:', newComponent.props.tabName)
  console.log('===========================')
  
  editorState.components.push(newComponent)
  editorState.selectedComponentId = newComponent.id
  recordHistory() // 记录历史（记录变更后的状态，支持 redo）
  saveEditorState()
}

// 处理属性更新
const handleUpdateProperty = (propertyName, value) => {
  if (!selectedComponent.value) return
  
  // 更新组件属性
  const component = editorState.components.find(c => c.id === selectedComponent.value.id)
  if (component) {
    if (propertyName === 'props' && typeof value === 'object') {
      // 批量更新props属性（新的事件格式）
      // 保留 tabName 和 parentId，避免被覆盖
      const preservedProps = {
        tabName: component.props.tabName,
        parentId: component.parentId
      }
      Object.assign(component.props, value)
      // 恢复保留的属性
      if (preservedProps.tabName !== undefined) {
        component.props.tabName = preservedProps.tabName
      }
      if (preservedProps.parentId !== undefined) {
        component.parentId = preservedProps.parentId
      }
    } else {
      // 原有逻辑：检查属性是属于props还是style
      const styleProperties = ['width', 'height', 'left', 'top', 'backgroundColor', 'borderColor', 'borderRadius', 'fontSize', 'color']
      
      if (styleProperties.includes(propertyName)) {
        // 样式属性
        if (['width', 'height', 'left', 'top'].includes(propertyName)) {
          // 尺寸和位置属性，带px单位
          component.style[propertyName] = `${value}px`
        } else {
          // 其他样式属性
          component.style[propertyName] = value
        }
        // 对于文本组件，特殊处理字体大小和颜色到props
        if (component.type === 'text') {
          if (propertyName === 'fontSize') {
            component.props.fontSize = value
          } else if (propertyName === 'color') {
            component.props.color = value
          }
        }
      } else {
        // 单个props属性更新（原有格式）
        component.props[propertyName] = value
      }
    }
    recordHistory() // 记录历史（记录变更后的状态，支持 redo）
    saveEditorState()
  }
}

// 页面挂载时初始化
onMounted(() => {
  const imported = applyImportedSchemaIfPresent()
  if (!imported) initEditorState()
  // 初始化历史记录
  recordHistory()
})
</script>

<style scoped>
.lowcode-editor {
  display: flex;
  flex-direction: column;
  height: 100vh; /* constrain the editor to viewport height so side panels can scroll */
  background-color: #f5f7fa;
  overflow: hidden;
}

/* 顶部工具栏 */
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 50px;
  background-color: #ffffff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.switch-inline {
  margin-left: 10px;
}

/* 三栏布局 */
.editor-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0; /* allow inner panels to scroll within a flex layout */
}

/* 左侧面板 */
.left-panel {
  width: 280px;
  background-color: #ffffff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  min-height: 0;
}

.left-panel.collapsed {
  width: 48px;
}

/* 右侧面板 */
.right-panel {
  width: 320px;
  background-color: #ffffff;
  border-left: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  min-height: 0;
}

.right-panel.collapsed {
  width: 48px;
}

/* 面板头部 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
  height: 50px;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.collapse-btn {
  padding: 4px;
}

/* 中间画布 */
.center-panel {
  flex: 1;
  background-color: #f5f7fa;
  overflow: auto;
  position: relative;
  min-height: 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .left-panel {
    width: 240px;
  }
  
  .right-panel {
    width: 280px;
  }
}

@media (max-width: 768px) {
  .left-panel {
    position: absolute;
    left: 0;
    top: 50px;
    bottom: 0;
    z-index: 100;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.1);
  }
  
  .right-panel {
    position: absolute;
    right: 0;
    top: 50px;
    bottom: 0;
    z-index: 100;
    box-shadow: -2px 0 12px rgba(0, 0, 0, 0.1);
  }
  
  .left-panel:not(.collapsed),
  .right-panel:not(.collapsed) {
    width: 280px;
  }
}
</style>
