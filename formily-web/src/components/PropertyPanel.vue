<template>
  <div class="property-panel">
    <!-- 未选中组件状态 -->
    <div v-if="!selectedComponent" class="empty-state">
      <el-empty description="请选择一个组件进行编辑" />
    </div>
    
    <!-- 已选中组件状态 -->
    <div v-else class="property-content">
      <!-- 组件基本信息 -->
      <div class="component-basic-info">
        <h4>{{ selectedComponent.name }}</h4>
        <p class="component-type">类型：{{ selectedComponent.type }}</p>
      </div>
      
      <!-- 布局属性 -->
      <el-collapse v-model="activeCollapse" class="property-collapse">
        <el-collapse-item title="布局属性" name="layout">
          <div class="property-group">
            <!-- 位置属性 -->
            <div class="property-row">
              <div class="property-item">
                <label class="property-label">X 坐标</label>
                <el-input-number
                  v-model="layoutProps.left"
                  :min="0"
                  :step="editorState.gridSize || 1"
                  @change="handleLayoutChange('left')"
                  class="property-input"
                />
              </div>
              <div class="property-item">
                <label class="property-label">Y 坐标</label>
                <el-input-number
                  v-model="layoutProps.top"
                  :min="0"
                  :step="editorState.gridSize || 1"
                  @change="handleLayoutChange('top')"
                  class="property-input"
                />
              </div>
            </div>
            
            <!-- 尺寸属性 -->
            <div class="property-row">
              <div class="property-item">
                <label class="property-label">宽度</label>
                <el-input-number
                  v-model="layoutProps.width"
                  :min="10"
                  :step="editorState.gridSize || 1"
                  @change="handleLayoutChange('width')"
                  class="property-input"
                />
              </div>
              <div class="property-item">
                <label class="property-label">高度</label>
                <el-input-number
                  v-model="layoutProps.height"
                  :min="10"
                  :step="editorState.gridSize || 1"
                  @change="handleLayoutChange('height')"
                  class="property-input"
                />
              </div>
            </div>
          </div>
        </el-collapse-item>
        
        <!-- 样式属性 -->
        <el-collapse-item title="样式属性" name="style">
          <div class="property-group">
            <!-- 颜色属性行 -->
            <div class="property-row">
              <!-- 背景颜色 -->
              <div class="property-item">
                <label class="property-label">背景颜色</label>
                <el-color-picker
                  v-model="styleProps.backgroundColor"
                  show-alpha
                  @change="handleStyleChange('backgroundColor')"
                  class="property-input"
                />
              </div>
              
              <!-- 边框颜色 -->
              <div class="property-item">
                <label class="property-label">边框颜色</label>
                <el-color-picker
                  v-model="styleProps.borderColor"
                  show-alpha
                  @change="handleStyleChange('borderColor')"
                  class="property-input"
                />
              </div>
              
              <!-- 字体颜色 -->
              <div class="property-item">
                <label class="property-label">字体颜色</label>
                <el-color-picker
                  v-model="styleProps.color"
                  show-alpha
                  @change="handleStyleChange('color')"
                  class="property-input"
                />
              </div>
            </div>
            
            <!-- 边框半径 -->
            <div class="property-item">
              <label class="property-label">边框半径</label>
              <el-slider
                v-model="styleProps.borderRadius"
                :min="0"
                :max="20"
                @change="handleStyleChange('borderRadius')"
              />
              <span class="slider-value">{{ styleProps.borderRadius }}px</span>
            </div>
            
            <!-- 字体大小 -->
            <div class="property-item">
              <label class="property-label">字体大小</label>
              <el-input-number
                v-model="styleProps.fontSize"
                :min="8"
                :max="72"
                :step="1"
                @change="handleStyleChange('fontSize')"
                class="property-input"
              />
            </div>
          </div>
        </el-collapse-item>
        
        <!-- 内容属性 -->
        <el-collapse-item title="内容属性" name="content">
          <div class="property-group">
            <!-- 组件内容属性，根据组件类型动态生成 -->
            <div v-if="selectedComponent" class="property-item">
              <label class="property-label">组件类型</label>
              <el-input
                v-model="selectedComponent.type"
                disabled
                class="property-input"
              />
            </div>
            <div class="property-item">
              <label class="property-label">组件名称</label>
              <el-input
                v-model="contentProps.name"
                @input="handleContentChange('name')"
                class="property-input"
              />
            </div>

            <!-- Tabs 子组件：选择所在的标签页 -->
            <div v-if="parentTabOptions.length" class="property-item">
              <label class="property-label">所在标签页</label>
              <el-select
                v-model="contentProps.tabName"
                placeholder="请选择标签页"
                class="property-input"
                @change="handleContentChange('tabName')"
              >
                <el-option
                  v-for="opt in parentTabOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </div>

            <!-- Tabs 组件：编辑每个 Tab 的显示名称（label/title） -->
            <div v-if="isTabsComponent && tabsListKey" class="property-item">
              <label class="property-label">标签页名称</label>
              <div class="tabs-editor">
                <div
                  v-for="(tab, index) in contentProps[tabsListKey]"
                  :key="tab.name ?? index"
                  class="tab-editor-row"
                >
                  <el-input
                    v-model="tab[tabsTitleKey]"
                    placeholder="请输入标签页名称"
                    class="tab-title-input"
                    @input="emit('update-property', 'props', { [tabsListKey]: contentProps[tabsListKey] })"
                  />
                  <el-input
                    :model-value="tab.name"
                    disabled
                    class="tab-name-input"
                  />
                </div>
              </div>
            </div>
            
            <!-- 禁用、只读、必填属性行 -->
            <!-- 对于非按钮组件，禁用属性显示在这里 -->
            <div v-if="hasBooleanProps" class="property-row">
              <!-- 禁用 -->
              <div v-if="contentProps.hasOwnProperty('disabled')" class="property-item">
                <label class="property-label">{{ getPropertyLabel('disabled') }}</label>
                <el-switch
                  v-model="contentProps.disabled"
                  @change="handleContentChange('disabled')"
                />
              </div>
              
              <!-- 只读 -->
              <div v-if="contentProps.hasOwnProperty('readonly')" class="property-item">
                <label class="property-label">{{ getPropertyLabel('readonly') }}</label>
                <el-switch
                  v-model="contentProps.readonly"
                  @change="handleContentChange('readonly')"
                />
              </div>
              
              <!-- 必填 -->
              <div v-if="contentProps.hasOwnProperty('required')" class="property-item">
                <label class="property-label">{{ getPropertyLabel('required') }}</label>
                <el-switch
                  v-model="contentProps.required"
                  @change="handleContentChange('required')"
                />
              </div>
            </div>
            
            <!-- 加载中、圆角、圆形、朴素、链接属性行（每3个一行） -->
            <template v-if="hasButtonStyleProps">
              <!-- 第一行：加载中、圆角、圆形 -->
              <div class="property-row">
                <!-- 加载中 -->
                <div v-if="contentProps.hasOwnProperty('loading')" class="property-item">
                  <label class="property-label">{{ getPropertyLabel('loading') }}</label>
                  <el-switch
                    v-model="contentProps.loading"
                    @change="handleContentChange('loading')"
                  />
                </div>
                
                <!-- 圆角 -->
                <div v-if="contentProps.hasOwnProperty('round')" class="property-item">
                  <label class="property-label">{{ getPropertyLabel('round') }}</label>
                  <el-switch
                    v-model="contentProps.round"
                    @change="handleContentChange('round')"
                  />
                </div>
                
                <!-- 圆形 -->
                <div v-if="contentProps.hasOwnProperty('circle')" class="property-item">
                  <label class="property-label">{{ getPropertyLabel('circle') }}</label>
                  <el-switch
                    v-model="contentProps.circle"
                    @change="handleContentChange('circle')"
                  />
                </div>
              </div>
              
              <!-- 第二行：禁用、朴素、链接 -->
              <div class="property-row">
                <!-- 禁用 -->
                <!-- 只有按钮组件的禁用属性显示在这里 -->
                <div v-if="props.selectedComponent?.type === 'button' && contentProps.hasOwnProperty('disabled')" class="property-item">
                  <label class="property-label">{{ getPropertyLabel('disabled') }}</label>
                  <el-switch
                    v-model="contentProps.disabled"
                    @change="handleContentChange('disabled')"
                  />
                </div>
                
                <!-- 朴素 -->
                <div v-if="contentProps.hasOwnProperty('plain')" class="property-item">
                  <label class="property-label">{{ getPropertyLabel('plain') }}</label>
                  <el-switch
                    v-model="contentProps.plain"
                    @change="handleContentChange('plain')"
                  />
                </div>
                
                <!-- 链接 -->
                <div v-if="contentProps.hasOwnProperty('link')" class="property-item">
                  <label class="property-label">{{ getPropertyLabel('link') }}</label>
                  <el-switch
                    v-model="contentProps.link"
                    @change="handleContentChange('link')"
                  />
                </div>
              </div>
            </template>
            
            <!-- 其他动态属性编辑，根据组件props生成 -->
            <template v-for="(value, key) in contentProps" :key="key">
              <div v-if="key !== 'name' && key !== 'requestConfig' && key !== 'disabled' && key !== 'readonly' && key !== 'required' && !['loading', 'round', 'circle', 'plain', 'link'].includes(key)" class="property-item">
                <label class="property-label">{{ getPropertyLabel(key) }}</label>
                
                <!-- 文本类型 -->
                <el-input
                  v-if="typeof value === 'string'"
                  v-model="contentProps[key]"
                  @input="handleContentChange(key)"
                  class="property-input"
                />
                
                <!-- 数字类型 -->
                <el-input-number
                  v-else-if="typeof value === 'number'"
                  v-model="contentProps[key]"
                  :min="0"
                  @change="handleContentChange(key)"
                  class="property-input"
                />
                
                <!-- 布尔类型 -->
                <el-switch
                  v-else-if="typeof value === 'boolean'"
                  v-model="contentProps[key]"
                  @change="handleContentChange(key)"
                />
                
                <!-- 数组类型 -->
                <div v-else-if="Array.isArray(value)">
                  <!-- 简单数组（基本类型） -->
                  <el-select
                    v-if="isSimpleArray(value)"
                    v-model="contentProps[key]"
                    multiple
                    filterable
                    @change="handleContentChange(key)"
                    class="property-input"
                  >
                    <el-option
                      v-for="option in getArrayOptions(key)"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                  <!-- 复杂数组和对象暂时禁用编辑 -->
                  <el-input
                    v-else
                    :value="JSON.stringify(value)"
                    disabled
                    class="property-input"
                    type="textarea"
                    :rows="3"
                  />
                </div>
                
                <!-- 对象类型 -->
                <el-input
                  v-else-if="isObjectType(value)"
                  :value="JSON.stringify(value)"
                  disabled
                  class="property-input"
                  type="textarea"
                  :rows="3"
                />
              </div>
            </template>
          </div>
        </el-collapse-item>
        
        <!-- 请求信息 -->
        <el-collapse-item title="请求信息" name="request">
          <div class="property-group">
            <!-- 请求URL -->
            <div class="property-item">
              <label class="property-label">请求URL</label>
              <el-input
                v-model="requestConfig.url"
                placeholder="请输入请求URL，支持绝对路径与相对路径"
                @input="handleRequestConfigChange"
                class="property-input"
              />
            </div>
            
            <!-- 请求方式 -->
            <div class="property-item">
              <label class="property-label">请求方式</label>
              <el-select
                v-model="requestConfig.method"
                @change="handleRequestConfigChange"
                class="property-input"
              >
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </div>
            
            <!-- 请求参数 -->
            <div class="property-item">
              <div class="property-label">
                请求参数
                <el-button
                  type="primary"
                  size="small"
                  @click="addRequestParam"
                  style="margin-left: 12px"
                >
                  添加参数
                </el-button>
              </div>
              
              <!-- 参数列表 -->
            <div v-if="requestConfig.params && requestConfig.params.length > 0" class="request-params-list">
              <!-- 添加横向滚动容器 -->
              <div class="request-params-scroll-container">
                <div
                  v-for="(param, index) in requestConfig.params"
                  :key="param.id || index"
                  class="request-param-item"
                >
                  <div class="param-row">
                    <!-- 参数名称 -->
                    <el-input
                      v-model="param.name"
                      placeholder="参数名称"
                      @input="handleRequestConfigChange"
                      class="param-input param-name"
                    />
                    
                    <!-- 参数类型 -->
                    <el-select
                      v-model="param.type"
                      @change="handleRequestConfigChange"
                      class="param-input param-type"
                    >
                      <el-option label="String" value="string" />
                      <el-option label="Number" value="number" />
                      <el-option label="Boolean" value="boolean" />
                      <el-option label="Array" value="array" />
                      <el-option label="Object" value="object" />
                    </el-select>
                    
                    <!-- 参数值 -->
                    <el-input
                      v-model="param.value"
                      :placeholder="`参数值 (${param.type})`"
                      @input="handleRequestConfigChange"
                      class="param-input param-value"
                    />
                    
                    <!-- 是否必填 -->
                    <div class="param-required">
                      <el-checkbox
                        v-model="param.required"
                        @change="handleRequestConfigChange"
                      >
                        必填
                      </el-checkbox>
                    </div>
                    
                    <!-- 删除按钮 -->
                    <el-button
                      type="danger"
                      size="small"
                      @click="deleteRequestParam(index)"
                      class="param-delete"
                    >
                      删除
                    </el-button>
                  </div>
                  
                  <!-- 参数验证错误提示 -->
                  <div v-if="param.errors && param.errors.length > 0" class="param-errors">
                    <el-alert
                      v-for="(error, errIndex) in param.errors"
                      :key="errIndex"
                      :title="error"
                      type="error"
                      :closable="false"
                      show-icon
                      size="small"
                    />
                  </div>
                </div>
              </div>
            </div>
              
              <!-- 无参数提示 -->
              <div v-else class="no-params-tip">
                <el-empty description="暂无请求参数，点击添加参数按钮添加" />
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

// 定义属性
const props = defineProps({
  selectedComponent: {
    type: Object,
    default: null
  },
  // Used for cross-component config (e.g. tabs child selecting its parent tab)
  editorState: {
    type: Object,
    default: null
  }
})

// 定义事件
const emit = defineEmits(['update-property'])

// 编辑器状态，用于获取网格大小
const editorState = {
  gridSize: 20 // 默认网格大小
}

// 折叠面板激活状态
const activeCollapse = ref(['layout', 'style', 'content'])

// 布局属性
const layoutProps = ref({
  left: 0,
  top: 0,
  width: 0,
  height: 0
})

// 样式属性
const styleProps = ref({
  backgroundColor: '#ffffff',
  borderColor: '#dcdfe6',
  borderRadius: 4,
  fontSize: 14,
  color: '#303133'
})

// 内容属性
const contentProps = ref({})

// 计算属性：判断是否有禁用、只读、必填属性
// 对于非按钮组件，禁用属性需要和只读、必填属性显示在同一行
const hasBooleanProps = computed(() => {
  return contentProps.value.hasOwnProperty('disabled') || 
         contentProps.value.hasOwnProperty('readonly') || 
         contentProps.value.hasOwnProperty('required')
})

// 计算属性：判断是否有按钮样式属性（加载中、圆角、圆形、朴素、链接）
// 只有按钮组件的禁用属性显示在这里，其他组件的禁用属性显示在禁用、只读、必填属性行中
const hasButtonStyleProps = computed(() => {
  // 检查是否是按钮组件
  const isButtonComponent = props.selectedComponent?.type === 'button'
  
  // 只有按钮组件的禁用属性包含在按钮样式属性中
  const includeDisabled = isButtonComponent && contentProps.value.hasOwnProperty('disabled')
  
  return includeDisabled || 
         contentProps.value.hasOwnProperty('loading') || 
         contentProps.value.hasOwnProperty('round') || 
         contentProps.value.hasOwnProperty('circle') || 
         contentProps.value.hasOwnProperty('plain') || 
         contentProps.value.hasOwnProperty('link')
})

const isTabsComponent = computed(() => props.selectedComponent?.type === 'tabs')

const tabsListKey = computed(() => {
  if (!props.selectedComponent) return null
  // form tabs vs display tabs
  if (Array.isArray(contentProps.value?.tabs)) return 'tabs'
  if (Array.isArray(contentProps.value?.items)) return 'items'
  return null
})

const tabsTitleKey = computed(() => (tabsListKey.value === 'items' ? 'title' : 'label'))

const parentTabsComponent = computed(() => {
  const id = props.selectedComponent?.parentId
  const list = props.editorState?.components
  if (!id || !Array.isArray(list)) return null
  const parent = list.find(c => c.id === id)
  return parent?.type === 'tabs' ? parent : null
})

const parentTabOptions = computed(() => {
  const parent = parentTabsComponent.value
  if (!parent) return []
  const tabs = Array.isArray(parent.props?.tabs) ? parent.props.tabs : null
  const items = Array.isArray(parent.props?.items) ? parent.props.items : null
  const list = tabs || items || []
  const titleKey = tabs ? 'label' : 'title'
  return list.map((t) => ({
    label: t?.[titleKey] ?? t?.name ?? '',
    value: t?.name
  })).filter(o => o.value !== undefined && o.value !== null)
})

// 请求配置
const requestConfig = ref({
  url: '',
  method: 'GET',
  params: []
})

// 监听选中组件变化，更新属性值
watch(
  () => props.selectedComponent?.id, // 只监听组件id变化，避免循环更新
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      const newComponent = props.selectedComponent
      if (newComponent) {
        // 更新布局属性
        layoutProps.value = {
          left: parseInt(newComponent.style.left) || 0,
          top: parseInt(newComponent.style.top) || 0,
          width: parseInt(newComponent.style.width) || 0,
          height: parseInt(newComponent.style.height) || 0
        }
        
        // 更新样式属性
        styleProps.value = {
          backgroundColor: newComponent.style.backgroundColor || '#ffffff',
          borderColor: newComponent.style.border?.color || '#dcdfe6',
          borderRadius: parseInt(newComponent.style.borderRadius) || 4,
          fontSize: parseInt(newComponent.style.fontSize) || 14,
          color: newComponent.style.color || '#303133'
        }
        
        // 更新内容属性
        contentProps.value = { ...newComponent.props }
        
        // 更新请求配置
        requestConfig.value = {
          url: newComponent.props.requestConfig?.url || '',
          method: newComponent.props.requestConfig?.method || 'GET',
          params: newComponent.props.requestConfig?.params || []
        }
      }
    }
  },
  { immediate: true }
)

// 处理布局属性变化
const handleLayoutChange = (propertyName) => {
  if (!props.selectedComponent) return
  
  const value = layoutProps.value[propertyName]
  emit('update-property', propertyName, value)
}

// 处理样式属性变化
const handleStyleChange = (propertyName) => {
  if (!props.selectedComponent) return
  
  const value = styleProps.value[propertyName]
  
  // 根据属性名确定是直接更新props还是style
  if (propertyName === 'fontSize') {
    emit('update-property', 'fontSize', value)
  } else if (propertyName === 'color') {
    emit('update-property', 'color', value)
  } else {
    // 其他样式属性通过style更新
    emit('update-property', propertyName, value)
  }
}

// 辅助函数：将属性名转换为友好的标签
const getPropertyLabel = (propKey) => {
  const labelMap = {
    // 通用属性
    'defaultValue': '默认值',
    'placeholder': '占位符',
    'disabled': '禁用',
    'readonly': '只读',
    'required': '必填',
    'label': '标签',
    'name': '名称',
    'type': '类型',
    'size': '尺寸',
    'width': '宽度',
    'height': '高度',
    'color': '颜色',
    'backgroundColor': '背景色',
    'borderRadius': '边框圆角',
    'fontSize': '字体大小',
    'direction': '方向',
    'orientation': '方向',
    'placement': '位置',
    'effect': '效果',
    
    // 表单组件属性
    'maxLength': '最大长度',
    'showWordLimit': '显示字数限制',
    'min': '最小值',
    'max': '最大值',
    'step': '步长',
    'precision': '精度',
    'controlsPosition': '控制按钮位置',
    'showPassword': '显示密码',
    'multiple': '多选',
    'filterable': '可过滤',
    'remote': '远程搜索',
    'options': '选项',
    'activeText': '激活文本',
    'inactiveText': '非激活文本',
    'activeColor': '激活颜色',
    'inactiveColor': '非激活颜色',
    'format': '格式化',
    'clearable': '可清空',
    'editable': '可编辑',
    'showInput': '显示输入框',
    'range': '范围选择',
    'vertical': '垂直',
    'allowHalf': '允许半选',
    'showText': '显示文本',
    'textColor': '文本颜色',
    'labelPosition': '标签位置',
    'labelWidth': '标签宽度',
    'labelSuffix': '标签后缀',
    'inline': '行内',
    'rules': '验证规则',
    'model': '数据模型',
    
    // 基础组件属性
    'loading': '加载中',
    'round': '圆角',
    'circle': '圆形',
    'plain': '朴素',
    'link': '链接',
    'icon': '图标',
    'closable': '可关闭',
    'disableTransitions': '禁用过渡',
    'hit': '高亮',
    'content': '内容',
    'title': '标题',
    'trigger': '触发方式',
    'transition': '过渡效果',
    'percentage': '百分比',
    'strokeWidth': '线宽',
    'showText': '显示文本',
    'status': '状态',
    'duration': '持续时间',
    'strokeLinecap': '线帽',
    'contentPosition': '内容位置',
    'border': '边框',
    'shadow': '阴影',
    
    // 数据展示组件属性
    'data': '数据',
    'columns': '列',
    'stripe': '斑马纹',
    'fit': '自适应',
    'showHeader': '显示表头',
    'highlightCurrentRow': '高亮当前行',
    'currentRowKey': '当前行键',
    'rowKey': '行键',
    'emptyText': '空文本',
    'pagination': '分页',
    'direction': '方向',
    'activeNames': '激活名称',
    'accordion': '手风琴',
    'activeName': '激活名称',
    'closable': '可关闭',
    'addable': '可添加',
    'editable': '可编辑',
    'height': '高度',
    'autoplay': '自动播放',
    'interval': '间隔',
    'indicatorPosition': '指示器位置',
    'arrow': '箭头',
    'loop': '循环',
    'unit': '单位',
    'trend': '趋势',
    'trendValue': '趋势值',
    'description': '描述',
    'timestamp': '时间戳',
    'separator': '分隔符',
    'items': '项目',
    'total': '总数',
    'pageSize': '每页条数',
    'currentPage': '当前页',
    'layout': '布局',
    'pageSizes': '每页条数选项',
    'background': '背景',
    'small': '小型',
    
    // 布局组件属性
    'gutter': '间距',
    'justify': '对齐方式',
    'align': '垂直对齐',
    'tag': '标签',
    'span': '跨度',
    'offset': '偏移',
    'push': '右移',
    'pull': '左移',
    'xs': '超小屏幕',
    'sm': '小屏幕',
    'md': '中等屏幕',
    'lg': '大屏幕',
    'xl': '超大屏幕',
    'xxl': '超超大屏幕',
    'wrap': '换行',
    'gap': '间隙',
    'autoplay': '自动播放',
    'interval': '间隔',
    'indicatorPosition': '指示器位置',
    'arrow': '箭头',
    'loop': '循环',
    'borderBottom': '下边框',
    'borderTop': '上边框',
    'collapsed': '折叠',
    'collapseTransition': '折叠过渡',
    'autoRows': '自动行高',
    'columns': '列数'
  }
  
  return labelMap[propKey] || propKey
}

// 辅助函数：判断是否为对象类型
const isObjectType = (value) => {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

// 辅助函数：判断是否为简单数组（仅包含基本类型）
const isSimpleArray = (value) => {
  if (!Array.isArray(value)) return false
  return value.every(item => typeof item === 'string' || typeof item === 'number' || typeof item === 'boolean')
}

// 辅助函数：判断是否为复杂数组（包含对象）
const isComplexArray = (value) => {
  if (!Array.isArray(value)) return false
  return value.length > 0 && value.every(item => typeof item === 'object' && item !== null)
}

// 辅助函数：获取数组选项（示例，实际根据组件类型动态生成）
const getArrayOptions = (key) => {
  // 这里可以根据key返回不同的选项
  return []
}

// 处理内容属性变化
const handleContentChange = (propertyName) => {
  if (!props.selectedComponent) return
  
  const value = contentProps.value[propertyName]
  emit('update-property', 'props', { [propertyName]: value })
}

// 处理请求配置变化
const handleRequestConfigChange = () => {
  if (!props.selectedComponent) return
  
  // 验证请求参数
  validateRequestParams()
  
  emit('update-property', 'props', { requestConfig: requestConfig.value })
}

// 添加请求参数
const addRequestParam = () => {
  requestConfig.value.params.push({
    id: `param_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
    name: '',
    type: 'string',
    value: '',
    required: false,
    errors: []
  })
  
  handleRequestConfigChange()
}

// 删除请求参数
const deleteRequestParam = (index) => {
  requestConfig.value.params.splice(index, 1)
  handleRequestConfigChange()
}

// 验证请求参数
const validateRequestParams = () => {
  requestConfig.value.params.forEach(param => {
    param.errors = []
    
    // 验证参数名称
    if (!param.name.trim()) {
      param.errors.push('参数名称不能为空')
    }
    
    // 验证参数值（如果是必填项）
    if (param.required && !param.value && param.value !== 0 && param.value !== false) {
      param.errors.push('必填参数值不能为空')
    }
    
    // 验证参数类型对应的格式
    if (param.type === 'number' && param.value && isNaN(Number(param.value))) {
      param.errors.push('参数值必须是数字')
    }
    
    if (param.type === 'boolean' && param.value && !['true', 'false'].includes(param.value.toLowerCase())) {
      param.errors.push('参数值必须是true或false')
    }
  })
}
</script>

<style scoped>
.property-panel {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0; /* enable vertical scrolling inside a flex column */
  overflow: hidden;
}

/* 空状态 */
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

/* 属性内容区 */
.property-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.tabs-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tab-editor-row {
  display: grid;
  grid-template-columns: 1fr 110px;
  gap: 10px;
  align-items: center;
}

.tab-title-input {
  width: 100%;
}

.tab-name-input {
  width: 100%;
}

/* 组件基本信息 */
.component-basic-info {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: none;
}

.component-basic-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.component-type {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

/* 折叠面板 */
.property-collapse {
  border: none;
}

.property-collapse :deep(.el-collapse-item__header) {
  padding: 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
}

.property-collapse :deep(.el-collapse-item__content) {
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

/* 属性组 */
.property-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 属性行 */
.property-row {
  display: flex;
  gap: 16px;
}

/* 属性项 */
.property-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 属性标签 */
.property-label {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

/* 属性输入框 */
.property-input {
  width: 100%;
}

/* 滑块值 */
.slider-value {
  display: inline-block;
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
  min-width: 40px;
}

/* 请求参数样式 */
.request-params-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 请求参数滚动容器 */
.request-params-scroll-container {
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
  padding-bottom: 8px; /* 为滚动条留出空间 */
}

/* 自定义滚动条样式 */
.request-params-scroll-container::-webkit-scrollbar {
  height: 6px;
}

.request-params-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.request-params-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.request-params-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.request-param-item {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  min-width: 100%; /* 确保每个参数项至少占满容器宽度 */
}

.param-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-width: 800px; /* 设置最小宽度，确保所有元素都能完整显示 */
}

.param-input {
  flex: 1;
  min-width: 120px; /* 设置最小宽度，防止输入框被过度挤压 */
}

.param-name {
  flex: 2;
}

.param-type {
  flex: 1;
  min-width: 150px; /* 为选择框设置更大的最小宽度 */
}

.param-value {
  flex: 3;
}

.param-required {
  display: flex;
  align-items: center;
  min-width: 80px;
}

.param-delete {
  min-width: 80px;
}

/* 滚动条样式 */
.property-content::-webkit-scrollbar {
  width: 6px;
}

.property-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.property-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.property-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
