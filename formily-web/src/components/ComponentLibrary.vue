<template>
  <div class="component-library">
    <!-- 组件分类标签 -->
    <div class="category-tabs">
      <el-tabs 
        v-model="activeCategory" 
        @tab-change="handleCategoryChange"
        type="card"
        size="large"
        class="custom-tabs"
      >
        <el-tab-pane
          v-for="category in categories"
          :key="category.value"
          :label="category.label"
          :name="category.value"
        />
      </el-tabs>
    </div>
    
    <!-- 组件列表 -->
    <div class="component-list">
      <div
        v-for="component in filteredComponents"
        :key="component.type"
        class="component-item"
        draggable="true"
        @dragstart="handleDragStart($event, component)"
        @dragover.prevent
        @dragenter.prevent
      >
        <!-- 组件预览 -->
        <div class="component-preview">
          <!-- 真实组件预览 -->
          <div class="real-preview" :class="`preview-${component.type}`">
            <!-- 根据组件类型显示不同的预览 -->
            <!-- 文本输入框 -->
            <template v-if="component.type === 'input-text'">
              <el-input size="small" placeholder="文本" />
            </template>
            <!-- 数字输入框 -->
            <template v-else-if="component.type === 'input-number'">
              <el-input-number size="small" :min="0" :max="100" />
            </template>
            <!-- 密码输入框 -->
            <template v-else-if="component.type === 'input-password'">
              <el-input size="small" type="password" placeholder="密码" />
            </template>
            <!-- 多行文本框 -->
            <template v-else-if="component.type === 'textarea'">
              <el-input size="small" type="textarea" :rows="2" placeholder="多行文本" />
            </template>
            <!-- 下拉列表框 -->
            <template v-else-if="component.type === 'select'">
              <el-select size="small" placeholder="选择" style="width: 100%">
                <el-option label="选项1" value="1" />
              </el-select>
            </template>
            <!-- 单选按钮 -->
            <template v-else-if="component.type === 'radio'">
              <el-radio-group size="small">
                <el-radio value="1">选项</el-radio>
              </el-radio-group>
            </template>
            <!-- 复选按钮 -->
            <template v-else-if="component.type === 'checkbox'">
              <el-checkbox size="small">选项</el-checkbox>
            </template>
            <!-- 开关按钮 -->
            <template v-else-if="component.type === 'switch'">
              <el-switch size="small" />
            </template>
            <!-- 日期时间选择器 -->
            <template v-else-if="component.type === 'datetime-picker'">
              <el-date-picker size="small" type="datetime" placeholder="日期" />
            </template>
            <!-- 滑块 -->
            <template v-else-if="component.type === 'slider'">
              <el-slider size="small" :min="0" :max="100" :model-value="50" />
            </template>
            <!-- 评分 -->
            <template v-else-if="component.type === 'rate'">
              <el-rate size="small" :model-value="3" />
            </template>
            <!-- 表单容器 -->
            <template v-else-if="component.type === 'form-container'">
              <div class="preview-form-container">
                <div class="form-label">表单</div>
                <el-input size="mini" placeholder="表单项" />
              </div>
            </template>
            <!-- 按钮 -->
            <template v-else-if="component.type === 'button'">
              <el-button size="small" type="primary">按钮</el-button>
            </template>
            <!-- 标签 -->
            <template v-else-if="component.type === 'tag'">
              <el-tag size="small" type="primary">标签</el-tag>
            </template>
            <!-- 标签页 -->
            <template v-else-if="component.type === 'tabs'">
              <el-tabs size="small" v-model="activeTab">
                <el-tab-pane label="标签1" name="1">内容1</el-tab-pane>
                <el-tab-pane label="标签2" name="2">内容2</el-tab-pane>
              </el-tabs>
            </template>
            <!-- 链接 -->
            <template v-else-if="component.type === 'link'">
              <el-link type="primary">链接</el-link>
            </template>
            <!-- 加载指示器 -->
            <template v-else-if="component.type === 'loading'">
              <el-icon size="small"><Loading /></el-icon>
            </template>
            <!-- 提示 -->
            <template v-else-if="component.type === 'tooltip'">
              <el-tooltip content="提示" placement="top">
                <el-button size="small">提示</el-button>
              </el-tooltip>
            </template>
            <!-- 弹出气泡 -->
            <template v-else-if="component.type === 'popover'">
              <el-popover content="气泡内容" placement="top" trigger="hover">
                <template #reference>
                  <el-button size="small">气泡</el-button>
                </template>
              </el-popover>
            </template>
            <!-- 进度条 -->
            <template v-else-if="component.type === 'progress'">
              <el-progress size="small" :percentage="50" />
            </template>
            <!-- 分割线 -->
            <template v-else-if="component.type === 'divider'">
              <el-divider />
            </template>
            <!-- 面板 -->
            <template v-else-if="component.type === 'panel'">
              <el-card size="small" :body-style="{ padding: '8px' }">
                <div class="panel-title">面板</div>
              </el-card>
            </template>
            <!-- 表格 -->
            <template v-else-if="component.type === 'table'">
              <el-table size="small" :data="[{ name: '行1' }]" style="width: 100%">
                <el-table-column prop="name" label="列1" width="60" />
                <el-table-column prop="name" label="列2" width="60" />
              </el-table>
            </template>
            <!-- 列表 -->
            <template v-else-if="component.type === 'list'">
              <el-list size="small" :data="[{ title: '列表项' }]">
                <el-list-item slot="default" prop="title" />
              </el-list>
            </template>
            <!-- 描述列表 -->
            <template v-else-if="component.type === 'descriptions'">
              <el-descriptions size="small" :column="2" :data="[{ label: '标签', value: '值' }]" />
            </template>
            <!-- 树形控件 -->
            <template v-else-if="component.type === 'tree'">
              <el-tree size="small" :data="[{ label: '节点', children: [{ label: '子节点' }] }]" :indent="12" />
            </template>
            <!-- 折叠面板 -->
            <template v-else-if="component.type === 'collapse'">
              <el-collapse size="small">
                <el-collapse-item title="面板" name="1">
                  <div style="font-size: 12px;">内容</div>
                </el-collapse-item>
              </el-collapse>
            </template>
            <!-- 标签页 -->
            <template v-else-if="component.type === 'tabs'">
              <el-tabs size="small">
                <el-tab-pane label="标签1"><div style="font-size: 12px;">内容</div></el-tab-pane>
              </el-tabs>
            </template>
            <!-- 轮播图 -->
            <template v-else-if="component.type === 'carousel'">
              <el-carousel height="60px" indicator-position="none">
                <el-carousel-item>
                  <div class="carousel-item" style="background: #409eff;"></div>
                </el-carousel-item>
              </el-carousel>
            </template>
            <!-- 统计卡片 -->
            <template v-else-if="component.type === 'stat-card'">
              <div class="stat-card-preview">
                <div class="stat-value">1234</div>
                <div class="stat-label">统计</div>
              </div>
            </template>
            <!-- 时间轴 -->
            <template v-else-if="component.type === 'timeline'">
              <el-timeline>
                <el-timeline-item size="small" />
              </el-timeline>
            </template>
            <!-- 面包屑 -->
            <template v-else-if="component.type === 'breadcrumb'">
              <el-breadcrumb separator="/" size="small">
                <el-breadcrumb-item>首页</el-breadcrumb-item>
                <el-breadcrumb-item>页面</el-breadcrumb-item>
              </el-breadcrumb>
            </template>
            <!-- 分页器 -->
            <template v-else-if="component.type === 'pagination'">
              <el-pagination size="small" layout="prev, pager, next" :total="100" :page-size="10" />
            </template>
            <!-- 布局容器 -->
            <template v-else-if="component.type === 'layout'">
              <div class="layout-preview">
                <div class="layout-header"></div>
                <div class="layout-content"></div>
              </div>
            </template>
            <!-- 行 -->
            <template v-else-if="component.type === 'row'">
              <el-row>
                <el-col :span="8"><div class="col-preview"></div></el-col>
                <el-col :span="8"><div class="col-preview"></div></el-col>
              </el-row>
            </template>
            <!-- 列 -->
            <template v-else-if="component.type === 'column'">
              <div class="col-preview"></div>
            </template>
            <!-- 分栏布局 -->
            <template v-else-if="component.type === 'aside'">
              <div class="aside-preview"></div>
            </template>
            <!-- 弹性盒子 -->
            <template v-else-if="component.type === 'flex'">
              <div class="flex-preview">
                <div class="flex-item"></div>
                <div class="flex-item"></div>
              </div>
            </template>
            <!-- 容器 -->
            <template v-else-if="component.type === 'container'">
              <div class="container-preview">容器</div>
            </template>
            <!-- 侧边栏 -->
            <template v-else-if="component.type === 'sidebar'">
              <div class="sidebar-preview">
                <div class="sidebar-item"></div>
                <div class="sidebar-item"></div>
              </div>
            </template>
            <!-- 顶部导航 -->
            <template v-else-if="component.type === 'header'">
              <div class="header-preview">
                <div class="header-title">导航</div>
              </div>
            </template>
            <!-- 底部 -->
            <template v-else-if="component.type === 'footer'">
              <div class="footer-preview">底部</div>
            </template>
            <!-- 网格 -->
            <template v-else-if="component.type === 'grid'">
              <div class="grid-preview">
                <div class="grid-item"></div>
                <div class="grid-item"></div>
                <div class="grid-item"></div>
                <div class="grid-item"></div>
              </div>
            </template>
            <!-- 默认显示 -->
            <template v-else>
              <el-icon size="small"><Box /></el-icon>
            </template>
          </div>
        </div>
        
        <!-- 组件描述 -->
        <div class="component-description">
          <div class="component-desc-text">{{ component.description }}</div>
        </div>
        

        

      </div>
      
      <!-- 空状态 -->
      <div v-if="filteredComponents.length === 0" class="empty-state">
        <el-empty 
          description="未找到匹配的组件" 
          :image-size="100"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Loading, Box } from '@element-plus/icons-vue'


// 组件分类
const categories = [
  { label: '基础组件', value: 'basic' },
  { label: '表单组件', value: 'form' },
  { label: '数据展示', value: 'display' }
]

// 组件列表数据
const components = [
  // 表单组件
  {
    group: 'Form',
    type: 'Input',
    label: '文本输入框 Input',
    schema: {
      type: 'string',
      title: 'Input',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Input',
      'x-component-props': {
        placeholder: '请输入文本',
        disabled: false,
        readonly: false,
        maxLength: 100,
        showWordLimit: false
      }
    },
    type: 'input-text',
    name: '文本输入框',
    description: '文本输入',
    category: 'form',
    icon: '<EditPen />',
    props: {
      placeholder: '请输入文本',
      defaultValue: '',
      disabled: false, readonly: false, required: false,
      label: '文本输入',
      maxLength: 100,
      showWordLimit: false
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'center'
    }
  },
  {
    group: 'Form',
    type: 'NumberPicker',
    label: '数字输入框 Number',
    schema: {
      type: 'number',
      title: 'Number',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'NumberPicker',
      'x-component-props': {
        min: 0,
        max: 100,
        step: 1,
        precision: 0,
        controlsPosition: 'right'
      }
    },
    type: 'input-number',
    name: '数字输入框',
    description: '数字输入',
    category: 'form',
    icon: '<Operation />',
    props: {
      placeholder: '请输入数字',
      defaultValue: 0,
      disabled: false, readonly: false, required: false,
      label: '数字输入',
      min: -Infinity,
      max: Infinity,
      step: 1,
      precision: 0,
      controlsPosition: 'right'
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'center'
    }
  },
  {
    group: 'Form',
    type: 'Password',
    label: '密码框 Password',
    schema: {
      type: 'string',
      title: 'Password',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Password',
      'x-component-props': {
        placeholder: 'Password',
        showPassword: false,
        maxLength: 20,
        showWordLimit: false
      }
    },
    type: 'input-password',
    name: '密码框',
    description: '密码输入',
    category: 'form',
    icon: '<Lock />',
    props: {
      placeholder: '请输入密码',
      defaultValue: '',
      disabled: false, readonly: false, required: false,
      label: '密码输入',
      showPassword: false,
      maxLength: 20,
      showWordLimit: false
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'center'
    }
  },
  {
    group: 'Form',
    type: 'Textarea',
    label: '多行文本 Textarea',
    schema: {
      type: 'string',
      title: 'Textarea',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Textarea',
      'x-component-props': {
        placeholder: '...',
        rows: 4,
        maxLength: 500,
        showWordLimit: false,
        resize: 'both'
      }
    },
    type: 'textarea',
    name: '多行文本框',
    description: '多行文本',
    category: 'form',
    icon: '<Document />',
    props: {
      placeholder: '请输入文本',
      defaultValue: '',
      disabled: false, readonly: false, required: false,
      label: '多行文本',
      rows: 4,
      maxLength: 500,
      showWordLimit: false,
      resize: 'both'
    },
    style: {
      width: '200px',
      height: '120px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'flex-start'
    }
  },
  {
    group: 'Form',
    type: 'Select',
    label: '下拉列表 Select',
    schema: {
      type: 'string',
      title: 'Select',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Select'
    },
    type: 'select',
    name: '下拉列表框',
    description: '下拉选择',
    category: 'form',
    icon: '<CaretBottom />',
    props: {
      placeholder: '请选择',
      defaultValue: '',
      disabled: false, readonly: false, required: false,
      label: '下拉选择',
      // Do not ship hardcoded options; options should come from request (datesource).
      options: [],
      multiple: false,
      filterable: false,
      remote: false,
      requestConfig: {
        url: '',
        method: 'GET',
        params: []
      }
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'center'
    }
  },
  {
    group: 'Form',
    type: 'RadioGroup',
    label: '单选 Radio',
    schema: {
      type: 'string',
      title: 'Radio',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'RadioGroup',
      'enum': [
        { label: 'Yes', value: 'yes' },
        { label: 'No', value: 'no' }
      ]
    },
    type: 'radio',
    name: '单选按钮',
    description: '单选按钮',
    category: 'form',
    icon: '<Radio />',
    props: {
      defaultValue: '',
      disabled: false, readonly: false, required: false,
      label: '单选按钮',
      options: [
        { label: '选项1', value: 'option1' },
        { label: '选项2', value: 'option2' }
      ],
      direction: 'horizontal'
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'center'
    }
  },
  {
    group: 'Form',
    type: 'CheckboxGroup',
    label: '复选 Checkbox',
    schema: {
      type: 'array',
      title: 'Checkbox',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'CheckboxGroup',
      'enum': [
        { label: 'A', value: 'a' },
        { label: 'B', value: 'b' }
      ]
    },
    type: 'checkbox',
    name: '复选按钮',
    description: '复选按钮',
    category: 'form',
    icon: '<Checkbox />',
    props: {
      defaultValue: [],
      disabled: false, readonly: false, required: false,
      label: '复选按钮',
      options: [
        { label: '选项1', value: 'option1' },
        { label: '选项2', value: 'option2' },
        { label: '选项3', value: 'option3' }
      ],
      direction: 'horizontal'
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'center'
    }
  },
  {
    group: 'Form',
    type: 'Switch',
    label: '开关 Switch',
    schema: {
      type: 'boolean',
      title: 'Switch',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Switch'
    },
    type: 'switch',
    name: '开关按钮',
    description: '开关',
    category: 'form',
    icon: '<SwitchButton />',
    props: {
      defaultValue: false,
      disabled: false, readonly: false, required: false,
      label: '开关',
      activeText: '',
      inactiveText: '',
      activeColor: '#409eff',
      inactiveColor: '#dcdfe6'
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      gap: '12px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    group: 'Form',
    type: 'DateTimePicker',
    label: '日期时间 Datetime',
    schema: {
      type: 'string',
      title: 'DateTime',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'DateTimePicker'
    },
    type: 'datetime-picker',
    name: '日期时间选择器',
    description: '日期时间',
    category: 'form',
    icon: '<Calendar />',
    props: {
      placeholder: '请选择日期时间',
      defaultValue: '',
      disabled: false, readonly: false, required: false,
      label: '日期时间选择',
      type: 'datetime',
      format: 'YYYY-MM-DD HH:mm:ss',
      clearable: true,
      editable: true
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'center'
    }
  },
  {
    group: 'Form',
    type: 'Slider',
    label: '滑块 Slider',
    schema: {
      type: 'number',
      title: 'Slider',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Slider',
      'x-component-props': {
        min: 0,
        max: 100
      }
    },
    type: 'slider',
    name: '滑块',
    description: '滑块',
    category: 'form',
    icon: '<Position />',
    props: {
      defaultValue: 0,
      disabled: false, readonly: false, required: false,
      label: '滑块',
      min: 0,
      max: 100,
      step: 1,
      showInput: false,
      showInputControls: true,
      range: false,
      vertical: false
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'center'
    }
  },
  {
    group: 'Form',
    type: 'rate',
    label: '评分 Rate',
    schema: {
      type: 'number',
      title: 'Rate',
      required: false,
      'x-decorator': 'FormItem',
      'x-component': 'Rate'
    },
    name: '评分',
    description: '评分',
    category: 'form',
    icon: '<Star />',
    props: {
      defaultValue: 0,
      disabled: false, readonly: false, required: false,
      label: '评分',
      max: 5,
      allowHalf: false,
      showText: false,
      textColor: '#ff9900',
      inactiveColor: '#e4e7ed',
      activeColor: '#ffd04b'
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      alignItems: 'center'
    }
  },
  {
    group: 'Form',
    type: 'FormContainer',
    label: '表单容器 Form Container',
    schema: {
      type: 'void',
      'x-component': 'Card',
      'x-component-props': {
        header: 'Form Container'
      }
    },
    type: 'form-container',
    name: '表单容器',
    description: '表单容器',
    category: 'form',
    icon: '<DocumentChecked />',
    props: {
      labelPosition: 'left',
      labelWidth: '80px',
      labelSuffix: ':',
      inline: false,
      rules: {},
      model: {},
      size: 'medium',
      disabled: false
    },
    style: {
      width: '400px',
      height: '300px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '20px',
      borderRadius: '8px',
      backgroundColor: '#ffffff',
      boxShadow: '0 2px 12px 0 rgba(0, 0, 0, 0.1)'
    }
  },
  { 
    type: 'tabs',
    name: '标签页',
    description: '标签页组件',
    category: 'form',
    icon: '<Collection />',
    props: {
      activeName: '1',
      type: 'card',
      closable: true,
      addable: true,
      editable: true,
      disabled: false,
      label: '标签页',
      tabs: [
        { label: '标签1', name: '1', content: '标签1内容' },
        { label: '标签2', name: '2', content: '标签2内容' }
      ]
    },
    style: {
      width: '300px',
      height: '200px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'column',
      gap: '8px',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      border: '1px solid #ebeef5'
    },
    schema: {
      type: 'object',
      title: '标签页',
      'x-component': 'ElTabs',
      'x-component-props': {
        type: 'card',
        closable: true,
        addable: true,
        editable: true
      },
      properties: {
        tab1: {
          type: 'object',
          'x-component': 'ElTabPane',
          'x-component-props': {
            label: '标签1',
            name: '1'
          },
          properties: {}
        },
        tab2: {
          type: 'object',
          'x-component': 'ElTabPane',
          'x-component-props': {
            label: '标签2',
            name: '2'
          },
          properties: {}
        }
      }
    }
  },
  
  // 基础组件
  {
    type: 'button',
    name: '按钮',
    description: '按钮',
    category: 'basic',
    icon: '<Button />',
    props: {
      label: '按钮',
      type: 'primary',
      size: 'medium',
      disabled: false,
      loading: false,
      round: false,
      circle: false,
      plain: false,
      link: false,
      icon: '',
      requestConfig: {
        url: '',
        method: 'GET',
        params: []
      }
    },
    style: {
      width: '100px',
      height: '40px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }
  },
  {
    type: 'tag',
    name: '标签',
    description: '标签',
    category: 'basic',
    icon: '<Promotion />',
    props: {
      label: '标签',
      type: 'primary',
      size: 'medium',
      closable: false,
      disableTransitions: false,
      hit: false,
      effect: 'light'
    },
    style: {
      width: '80px',
      height: '32px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }
  },
  {
    type: 'link',
    name: '链接',
    description: '链接',
    category: 'basic',
    icon: '<Link />',
    props: {
      text: '链接',
      href: '#',
      target: '_self',
      type: 'primary',
      underline: true,
      disabled: false,
      requestConfig: {
        url: '',
        method: 'GET',
        params: []
      }
    },
    style: {
      width: '80px',
      height: '32px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }
  },
  {
    type: 'loading',
    name: '加载指示器',
    description: '加载',
    category: 'basic',
    icon: '<Loading />',
    props: {
      text: '加载中',
      size: 'medium',
      color: '#409eff',
      fullscreen: false,
      spinner: 'el-icon-loading',
      background: 'rgba(255, 255, 255, 0.9)'
    },
    style: {
      width: '120px',
      height: '80px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      padding: '16px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'tooltip',
    name: '提示',
    description: '提示',
    category: 'basic',
    icon: '<InfoFilled />',
    props: {
      content: '提示内容',
      placement: 'top',
      effect: 'dark',
      transition: 'el-fade-in-linear',
      showAfter: 0,
      hideAfter: 200,
      autoClose: true,
      manual: false,
      popperClass: '',
      popperOptions: {}
    },
    style: {
      width: '80px',
      height: '32px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'popover',
    name: '弹出气泡',
    description: '气泡',
    category: 'basic',
    icon: '<QuestionFilled />',
    props: {
      title: '气泡标题',
      content: '气泡内容',
      placement: 'top',
      effect: 'dark',
      trigger: 'click',
      transition: 'el-fade-in-linear',
      popperClass: '',
      popperOptions: {}
    },
    style: {
      width: '100px',
      height: '40px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'progress',
    name: '进度条',
    description: '进度条',
    category: 'basic',
    icon: '<DataAnalysis />',
    props: {
      percentage: 50,
      type: 'line',
      strokeWidth: 6,
      color: '#409eff',
      showText: true,
      status: '',
      duration: 3000,
      width: 126,
      strokeLinecap: 'round'
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '16px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'divider',
    name: '分割线',
    description: '分割线',
    category: 'basic',
    icon: '<Remove />',
    props: {
      orientation: 'horizontal',
      contentPosition: 'center',
      direction: 'horizontal'
    },
    style: {
      width: '200px',
      height: '20px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }
  },
  {
    type: 'panel',
    name: '面板',
    description: '面板',
    category: 'basic',
    icon: '<Notebook />',
    props: {
      title: '面板标题',
      border: false,
      shadow: 'none'
    },
    style: {
      width: '200px',
      height: '120px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '16px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  
  // 数据展示组件
  {
    type: 'table',
    name: '表格',
    description: '表格',
    category: 'display',
    icon: '<Grid />',
    props: {
      data: [],
      columns: [
        { prop: 'name', label: '名称', width: 180 },
        { prop: 'value', label: '值', width: 180 },
        { prop: 'desc', label: '描述' }
      ],
      height: '',
      maxHeight: '',
      stripe: false,
      border: false,
      fit: true,
      showHeader: true,
      highlightCurrentRow: false,
      currentRowKey: '',
      rowKey: '',
      emptyText: '暂无数据',
      pagination: false,
      requestConfig: {
        url: '',
        method: 'GET',
        params: []
      }
    },
    style: {
      width: '400px',
      height: '200px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'list',
    name: '列表',
    description: '列表',
    category: 'display',
    icon: '<List />',
    props: {
      data: [
        { title: '列表项1', desc: '描述内容1' },
        { title: '列表项2', desc: '描述内容2' },
        { title: '列表项3', desc: '描述内容3' }
      ],
      border: true,
      size: 'default'
    },
    style: {
      width: '300px',
      height: '150px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'descriptions',
    name: '描述列表',
    description: '描述列表',
    category: 'display',
    icon: '<DocumentCopy />',
    props: {
      title: '描述列表',
      border: false,
      column: 3,
      size: 'default',
      colon: true,
      labelPosition: 'top',
      data: {
        name: '名称',
        value: '值',
        desc: '描述内容'
      }
    },
    style: {
      width: '300px',
      height: '120px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'tree',
    name: '树形控件',
    description: '树形',
    category: 'display',
    icon: '<Tree />',
    props: {
      data: [
        {
          label: '一级节点1',
          children: [
            { label: '二级节点1-1' },
            { label: '二级节点1-2' }
          ]
        },
        {
          label: '一级节点2',
          children: [
            { label: '二级节点2-1' },
            { label: '二级节点2-2' }
          ]
        }
      ],
      emptyText: '暂无数据',
      nodeKey: '',
      defaultExpandAll: false,
      defaultExpandedKeys: [],
      defaultCheckedKeys: [],
      defaultSelectedKeys: [],
      highlightCurrent: false,
      expandOnClickNode: true,
      checkOnClickNode: false
    },
    style: {
      width: '300px',
      height: '200px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'collapse',
    name: '折叠面板',
    description: '折叠面板',
    category: 'display',
    icon: '<CaretRight />',
    props: {
      activeNames: ['1'],
      border: true,
      accordion: false,
      items: [
        { title: '面板1', name: '1', content: '面板1内容' },
        { title: '面板2', name: '2', content: '面板2内容' },
        { title: '面板3', name: '3', content: '面板3内容' }
      ]
    },
    style: {
      width: '300px',
      height: '200px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'tabs',
    name: '标签页',
    description: '标签页',
    category: 'display',
    icon: '<Collection />',
    props: {
      activeName: '1',
      type: 'card',
      closable: false,
      addable: false,
      editable: false,
      items: [
        { title: '标签1', name: '1', content: '标签1内容' },
        { title: '标签2', name: '2', content: '标签2内容' },
        { title: '标签3', name: '3', content: '标签3内容' }
      ]
    },
    style: {
      width: '300px',
      height: '200px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '8px',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'carousel',
    name: '轮播图',
    description: '轮播图',
    category: 'display',
    icon: '<Picture />',
    props: {
      height: '150px',
      autoplay: true,
      interval: 3000,
      indicatorPosition: 'bottom',
      arrow: 'hover',
      loop: true,
      items: [
        { url: 'https://picsum.photos/300/150?random=1', alt: '轮播图1' },
        { url: 'https://picsum.photos/300/150?random=2', alt: '轮播图2' },
        { url: 'https://picsum.photos/300/150?random=3', alt: '轮播图3' }
      ]
    },
    style: {
      width: '300px',
      height: '150px',
      left: '0px',
      top: '0px',
      position: 'absolute',

      borderRadius: '4px',
      overflow: 'hidden'
    }
  },
  {
    type: 'stat-card',
    name: '统计卡片',
    description: '统计卡片',
    category: 'display',
    icon: '<TrendCharts />',
    props: {
      title: '统计标题',
      value: '1234',
      unit: '',
      icon: '',
      trend: 'up',
      trendValue: '+10%',
      description: '环比增长'
    },
    style: {
      width: '200px',
      height: '120px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '16px',
      border: '1px solid #dcdfe6',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      boxShadow: '0 2px 12px 0 rgba(0, 0, 0, 0.1)'
    }
  },
  {
    type: 'timeline',
    name: '时间轴',
    description: '时间轴',
    category: 'display',
    icon: '<Timer />',
    props: {
      items: [
        { timestamp: '2024-01-01', title: '事件1', description: '详细描述1' },
        { timestamp: '2024-01-02', title: '事件2', description: '详细描述2' },
        { timestamp: '2024-01-03', title: '事件3', description: '详细描述3' }
      ],
      direction: 'vertical',
      placement: 'right'
    },
    style: {
      width: '300px',
      height: '200px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '8px',
      border: '1px solid #dcdfe6',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'breadcrumb',
    name: '面包屑',
    description: '面包屑',
    category: 'display',
    icon: '<CaretRight />',
    props: {
      separator: '/',
      items: [
        { name: '首页', path: '/' },
        { name: '分类', path: '/category' },
        { name: '详情', path: '/detail' }
      ]
    },
    style: {
      width: '200px',
      height: '40px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      padding: '8px',
      border: '1px solid #dcdfe6',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'pagination',
    name: '分页器',
    description: '分页',
    category: 'display',
    icon: '<SwitchButton />',
    props: {
      total: 100,
      pageSize: 10,
      currentPage: 1,
      layout: 'total, sizes, prev, pager, next, jumper',
      pageSizes: [10, 20, 50, 100],
      background: false,
      small: false
    },
    style: {
      width: '300px',
      height: '50px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '8px',
      border: '1px solid #dcdfe6',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  
  // 布局组件
  {
    type: 'layout',
    name: '布局容器',
    description: '布局容器',
    category: 'layout',
    icon: '<Operation />',
    props: {
      direction: 'vertical'
    },
    style: {
      width: '400px',
      height: '300px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'column',
      border: '1px solid #dcdfe6',
      borderRadius: '4px',
      backgroundColor: '#ffffff',
      overflow: 'hidden'
    }
  },
  {
    type: 'row',
    name: '行',
    description: '行',
    category: 'layout',
    icon: '<Grid />',
    props: {
      gutter: 0,
      type: '',
      justify: 'start',
      align: 'top',
      tag: 'div'
    },
    style: {
      width: '400px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      flexDirection: 'row',
      gap: '8px',
      padding: '8px',

      borderRadius: '4px',
      backgroundColor: '#f0f9ff'
    }
  },
  {
    type: 'column',
    name: '列',
    description: '列',
    category: 'layout',
    icon: '<Grid />',
    props: {
      span: 24,
      offset: 0,
      push: 0,
      pull: 0,
      xs: '',
      sm: '',
      md: '',
      lg: '',
      xl: '',
      xxl: '',
      tag: 'div'
    },
    style: {
      width: '200px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '8px',

      borderRadius: '4px',
      backgroundColor: '#f0f9ff'
    }
  },
  {
    type: 'aside',
    name: '分栏布局',
    description: '分栏',
    category: 'layout',
    icon: '<Menu />',
    props: {
      width: '200px'
    },
    style: {
      width: '200px',
      height: '200px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '8px',

      borderRadius: '4px',
      backgroundColor: '#fdf6ec'
    }
  },
  {
    type: 'flex',
    name: '弹性盒子',
    description: '弹性盒子',
    category: 'layout',
    icon: '<Operation />',
    props: {
      direction: 'row',
      justify: 'flex-start',
      align: 'stretch',
      wrap: 'nowrap',
      gap: 0
    },
    style: {
      width: '300px',
      height: '100px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      gap: '8px',
      padding: '8px',

      borderRadius: '4px',
      backgroundColor: '#fef0f0'
    }
  },
  {
    type: 'container',
    name: '容器',
    description: '容器',
    category: 'layout',
    icon: '<Box />',
    props: {
      padding: '16px',
      margin: '0px',
      backgroundColor: '#ffffff',
      borderRadius: '4px',
      border: '1px solid #dcdfe6',
      boxShadow: '0 2px 8px 0 rgba(0, 0, 0, 0.08)'
    },
    style: {
      width: '200px',
      height: '120px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      padding: '16px',
      border: '1px solid #dcdfe6',
      borderRadius: '4px',
      backgroundColor: '#ffffff'
    }
  },
  {
    type: 'sidebar',
    name: '侧边栏',
    description: '侧边栏',
    category: 'layout',
    icon: '<Menu />',
    props: {
      width: '200px',
      backgroundColor: '#304156',
      textColor: '#bfcbd9',
      activeTextColor: '#409eff',
      collapsed: false,
      collapseTransition: true,
      items: [
        { index: '1', title: '菜单1', icon: 'el-icon-menu' },
        { index: '2', title: '菜单2', icon: 'el-icon-document', children: [
          { index: '2-1', title: '子菜单1' },
          { index: '2-2', title: '子菜单2' }
        ]}
      ]
    },
    style: {
      width: '200px',
      height: '200px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      background: '#304156',
      border: '1px solid #dcdfe6',
      borderRadius: '4px',
      overflow: 'hidden'
    }
  },
  {
    type: 'header',
    name: '顶部导航',
    description: '顶部导航',
    category: 'layout',
    icon: '<Grid />',
    props: {
      height: '60px',
      backgroundColor: '#ffffff',
      borderBottom: '1px solid #dcdfe6',
      title: '顶部导航',
      logo: ''
    },
    style: {
      width: '400px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 20px',
      background: '#ffffff',
      border: '1px solid #dcdfe6',
      borderRadius: '4px 4px 0 0'
    }
  },
  {
    type: 'footer',
    name: '底部',
    description: '底部',
    category: 'layout',
    icon: '<Grid />',
    props: {
      height: '60px',
      backgroundColor: '#ffffff',
      borderTop: '1px solid #dcdfe6',
      text: '© 2024 Footer Content'
    },
    style: {
      width: '400px',
      height: '60px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: '#ffffff',
      border: '1px solid #dcdfe6',
      borderRadius: '0 0 4px 4px'
    }
  },
  {
    type: 'grid',
    name: '网格',
    description: '网格',
    category: 'layout',
    icon: '<Grid />',
    props: {
      columns: 4,
      gap: 8,
      autoRows: 'minmax(100px, auto)'
    },
    style: {
      width: '400px',
      height: '200px',
      left: '0px',
      top: '0px',
      position: 'absolute',
      display: 'grid',
      gridTemplateColumns: 'repeat(4, 1fr)',
      gap: '8px',
      padding: '8px',

      borderRadius: '4px',
      backgroundColor: '#f5f7fa'
    }
  }
]

// 响应式数据
const activeCategory = ref('basic')
const activeTab = ref('1') // 用于标签页预览

// 计算属性：筛选后的组件列表
const filteredComponents = computed(() => {
  return components.filter(component => {
    // 仅根据分类筛选
    return component.category === activeCategory.value
  })
})

// 事件定义
const emit = defineEmits(['drag-start'])

// 处理拖拽开始
const handleDragStart = (event, component) => {
  // 设置拖拽数据
  event.dataTransfer.effectAllowed = 'copy'
  event.dataTransfer.setData('application/json', JSON.stringify(component))
  // 发送拖拽开始事件
  emit('drag-start', component.type)
  // 添加拖拽样式
  event.target.classList.add('dragging')
  // 拖拽结束时移除样式
  event.target.addEventListener('dragend', () => {
    event.target.classList.remove('dragging')
  }, { once: true })
}

// 处理分类切换
const handleCategoryChange = () => {
  // 滚动到顶部
  document.querySelector('.component-list')?.scrollTo(0, 0)
}
</script>

<style scoped>
/* 主容器 */
.component-library {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0; /* enable vertical scrolling inside a flex column */
  overflow: hidden;
  background: #f5f7fa;
}

/* 分类标签 */
.category-tabs {
  padding: 0 16px;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
}

.custom-tabs {
  --el-tabs-header-height: 40px;
  --el-tabs-content-height: 0;
}

.custom-tabs :deep(.el-tabs__nav-wrap) {
  margin-bottom: 0;
}

.custom-tabs :deep(.el-tabs__nav) {
  border-bottom: none;
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 13px;
  font-weight: 500;
  padding: 0 16px;
  color: #606266;
  transition: all 0.3s ease;
  margin-right: 4px;
}

.custom-tabs :deep(.el-tabs__item:hover) {
  color: #409eff;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  border-bottom-color: #409eff;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  background-color: #409eff;
  height: 2px;
}

/* 组件列表 */
.component-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 组件项 */
.component-item {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 16px;
  border: none;
  border-radius: 8px;
  cursor: move;
  transition: all 0.2s ease;
  background: #ffffff;
  user-select: none;
  min-height: 60px;
  gap: 16px;
}

.component-item:hover {
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.component-item.dragging {
  opacity: 0.8;
  transform: rotate(3deg);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 组件描述 */
.component-description {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.component-desc-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 组件预览 */
.component-preview {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
  overflow: hidden;
}

/* 真实组件预览 */
.real-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  overflow: hidden;
  box-sizing: border-box;
}

/* 确保所有预览组件不超出父容器 */
.real-preview :deep(*) {
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
  box-sizing: border-box;
}

/* 文本预览 */
.preview-text {
  width: 40px;
  height: 24px;
  background: #ffffff;
  border: none;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 图片预览 */
.preview-image {
  width: 48px;
  height: 48px;
  background: linear-gradient(45deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 表单项预览 */
.preview-form-item {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 复选框预览 */
.preview-checkbox {
  width: 100%;
  display: flex;
  justify-content: center;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 单选框预览 */
.preview-radio {
  width: 100%;
  display: flex;
  justify-content: center;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 表格预览 */
.preview-table {
  width: 100%;
  background: #ffffff;
  border: none;
  border-radius: 4px;
  overflow: hidden;
  max-width: 100%;
  max-height: 100%;
}

.table-header {
  display: flex;
  background: #f5f7fa;
  border-bottom: none;
}

.table-row {
  display: flex;
}

.table-cell {
  flex: 1;
  padding: 4px 8px;
  font-size: 10px;
  border-right: none;
}

.table-cell:last-child {
  border-right: none;
}

/* 图表预览 */
.preview-chart {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  background: #ffffff;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 48px;
  width: 100%;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.chart-bar {
  flex: 1;
  background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
  border-radius: 2px 2px 0 0;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 容器预览 */
.preview-container {
  width: 100%;
  height: 100%;
  background: #ffffff;
  border: none;
  border-radius: 8px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 网格预览 */
.preview-grid {
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 4px;
  padding: 8px;
  background: #ffffff;
  border: none;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.grid-item {
  background: #f0f9ff;
  border-radius: 3px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 表单容器预览 */
.preview-form-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.form-label {
  font-size: 12px;
  color: #606266;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 统计卡片预览 */
.stat-card-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  background: #ffffff;
  border-radius: 4px;
  padding: 8px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.stat-label {
  font-size: 12px;
  color: #606266;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 轮播图预览 */
.carousel-item {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 布局预览 */
.layout-preview {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.layout-header {
  height: 20px;
  background: #409eff;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.layout-content {
  flex: 1;
  background: #f0f9ff;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 列预览 */
.col-preview {
  width: 100%;
  height: 30px;
  background: #409eff;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 分栏预览 */
.aside-preview {
  width: 100%;
  height: 100%;
  background: #304156;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 弹性盒子预览 */
.flex-preview {
  width: 100%;
  height: 100%;
  display: flex;
  gap: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.flex-item {
  flex: 1;
  background: #409eff;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 容器预览 */
.container-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f9ff;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 侧边栏预览 */
.sidebar-preview {
  width: 100%;
  height: 100%;
  background: #304156;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.sidebar-item {
  height: 20px;
  background: #409eff;
  border-radius: 4px;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 顶部导航预览 */
.header-preview {
  width: 100%;
  height: 100%;
  background: #ffffff;
  border: none;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

.header-title {
  font-size: 12px;
  color: #606266;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 底部预览 */
.footer-preview {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  border: none;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #606266;
  max-width: 100%;
  max-height: 100%;
  overflow: hidden;
}

/* 面板标题 */
.panel-title {
  font-size: 12px;
  color: #606266;
  text-align: center;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 滚动条样式 */
.component-list::-webkit-scrollbar {
  width: 6px;
}

.component-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.component-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.component-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .component-list {
    gap: 8px;
    padding: 12px;
  }
  
  .component-item {
    padding: 12px;
    min-height: 50px;
    gap: 12px;
  }
  
  .component-preview {
    width: 50px;
    height: 50px;
  }
  
  .category-tabs {
    padding: 0 12px;
  }
}
</style>
