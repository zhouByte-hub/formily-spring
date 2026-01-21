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
          <el-switch
            v-model="editorState.showGrid"
            size="small"
            inline-prompt
            active-text="显示"
            inactive-text="隐藏"
            class="switch-inline"
          />
        </el-button>
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

// 将编辑器组件转换为Formily Schema
const convertToFormilySchema = () => {
  console.log('开始转换Schema，当前组件列表:', JSON.stringify(editorState.components.map(c => ({
    id: c.id,
    type: c.type,
    parentId: c.parentId,
    tabName: c.props.tabName
  })), null, 2))
  
  // 初始化Formily schema结构
  const schema = {
    type: 'object',
    properties: {},
    required: []
  }
  
  // 遍历所有组件，生成嵌套的schema
  editorState.components.forEach(component => {
    // 如果是根组件（没有父组件）
    if (!component.parentId) {
      console.log(`处理根组件: ${component.id}, type: ${component.type}`)
      
      // 生成字段名
      const fieldName = `field_${component.id.replace(/-/g, '_')}`
      
      // 处理tabs组件
      if (component.type === 'tabs') {
        // 初始化tabs schema
        const tabsSchema = {
          type: 'object',
          title: component.props.label || component.name || '标签页',
          'x-component': 'ElTabs',
          'x-component-props': {
            type: component.props.type || 'card',
            closable: component.props.closable || false,
            addable: component.props.addable || false,
            editable: component.props.editable || false,
            disabled: component.props.disabled || false,
            'onUpdate:modelValue': 'onTabsChange'
          },
          properties: {}
        }
        
        // 添加所有tab页
        component.props.tabs.forEach(tab => {
          tabsSchema.properties[`tab${tab.name}`] = {
            type: 'object',
            'x-component': 'ElTabPane',
            'x-component-props': {
              label: tab.label,
              name: tab.name
            },
            properties: {}
          }
        })
        
        // 查找该tabs组件的所有子组件
        const tabChildren = editorState.components.filter(child => child.parentId === component.id)
        console.log(`找到 ${tabChildren.length} 个tabs子组件`)
        
        // 将子组件添加到对应的tab中
        tabChildren.forEach(child => {
          const tabName = child.props.tabName
          console.log(`处理子组件: ${child.id}, tabName: ${tabName}`)
          
          if (tabName) {
            const tabKey = `tab${tabName}`
            if (tabsSchema.properties[tabKey]) {
              // 生成子组件字段名
              const childFieldName = `field_${child.id.replace(/-/g, '_')}`
              
              // 初始化子组件schema
              const childSchema = {
                type: 'string',
                title: child.props.label || child.name || '未命名字段',
                'x-component': '',
                'x-component-props': {
                  ...child.props,
                  style: child.style
                },
                properties: {}
              }
              
              // 设置子组件的x-component
              switch (child.type) {
                case 'input-text':
                  childSchema['x-component'] = 'ElInput'
                  break
                case 'input-number':
                  childSchema.type = 'number'
                  childSchema['x-component'] = 'ElInputNumber'
                  break
                case 'button':
                  childSchema['x-component'] = 'ElButton'
                  break
                case 'rate':
                  childSchema['x-component'] = 'ElRate'
                  break
                default:
                  childSchema['x-component'] = 'div'
              }
              
              // 添加到对应的tab中
              tabsSchema.properties[tabKey].properties[childFieldName] = childSchema
              console.log(`已将子组件 ${child.id} 添加到 tab ${tabKey}`)
            }
          }
        })
        
        // 将tabs schema添加到主schema
        schema.properties[fieldName] = tabsSchema
      } else {
        // 非tabs根组件，使用默认处理
        const fieldSchema = {
          type: 'string',
          title: component.props.label || component.name || '未命名字段',
          'x-component': '',
          'x-component-props': {
            ...component.props,
            style: component.style
          },
          properties: {}
        }
        
        // 设置x-component
        switch (component.type) {
          case 'input-text':
            fieldSchema['x-component'] = 'ElInput'
            break
          case 'input-number':
            fieldSchema.type = 'number'
            fieldSchema['x-component'] = 'ElInputNumber'
            break
          case 'button':
            fieldSchema['x-component'] = 'ElButton'
            break
          case 'rate':
            fieldSchema.type = 'number'
            fieldSchema['x-component'] = 'ElRate'
            break
          default:
            fieldSchema['x-component'] = 'div'
        }
        
        schema.properties[fieldName] = fieldSchema
      }
    } else {
      console.log(`跳过非根组件: ${component.id}, parentId: ${component.parentId}`)
    }
  })
  
  console.log('转换后的Schema:', JSON.stringify(schema, null, 2))
  return schema
}

// 生成完整的Formily格式数据
const generateFormilyData = () => {
  // 生成initial_values
  const initialValues = {}
  
  // 生成datasources
  const datasources = {}
  
  // 生成schema
  const schema = convertToFormilySchema()
  
  // 遍历所有组件，生成initial_values和datasources映射
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
    
    // 处理datasources：获取请求信息
    if (component.props.requestConfig) {
      const { url, method, params } = component.props.requestConfig
      if (url) {
        // 生成固定的数据源ID，使用组件ID确保唯一性和可追溯性
        const datasourceId = `datasource_${component.id}`
        
        // 保存数据源
        datasources[datasourceId] = {
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
  
  // 将数据源ID添加到对应的schema中
  // 查找组件对应的schema节点并添加datesource_id
  editorState.components.forEach(component => {
    const datasourceId = componentDatasourceMap.get(component.id)
    if (datasourceId) {
      // 在schema中查找对应的节点并添加datesource_id
      const fieldName = `field_${component.id.replace(/-/g, '_')}`
      if (schema.properties[fieldName]) {
        schema.properties[fieldName]['datesource_id'] = datasourceId
      }
    }
  })
  
  // 构建完整的Formily格式数据
  const formilyData = {
    initial_values: initialValues,
    datasources: datasources,
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
      localStorage.setItem('current-project-id', response.data.id || Date.now().toString())
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
  // 移除当前索引之后的历史
  editorState.history = editorState.history.slice(0, editorState.historyIndex + 1)
  // 保存当前状态
  editorState.history.push(JSON.stringify(editorState.components))
  // 限制历史记录数量
  if (editorState.history.length > 50) {
    editorState.history.shift()
  } else {
    editorState.historyIndex++
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
  } else {
    ElMessage.info('已经是最新状态')
  }
}

// 切换网格显示
const toggleGrid = () => {
  editorState.showGrid = !editorState.showGrid
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
    recordHistory() // 记录历史
    editorState.components[index] = updatedComponent
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
  
  recordHistory() // 记录历史
  editorState.components.push(newComponent)
  editorState.selectedComponentId = newComponent.id
  saveEditorState()
}

// 处理属性更新
const handleUpdateProperty = (propertyName, value) => {
  if (!selectedComponent.value) return
  
  recordHistory() // 记录历史
  
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
    
    saveEditorState()
  }
}

// 页面挂载时初始化
onMounted(() => {
  initEditorState()
  // 初始化历史记录
  recordHistory()
})
</script>

<style scoped>
.lowcode-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f5f7fa;
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
}

/* 左侧面板 */
.left-panel {
  width: 280px;
  background-color: #ffffff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
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