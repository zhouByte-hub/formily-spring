<template>
  <div class="canvas-container">
    <!-- 画布背景 -->
    <div 
      class="canvas"
      ref="canvasRef"
      @dragover.prevent
      @dragenter.prevent
      @drop="handleDrop"
      @click="handleCanvasClick"
      :style="canvasStyle"
    >
      <!-- 网格线 -->
      <div 
        v-if="editorState.showGrid" 
        class="grid"
        :style="gridStyle"
      ></div>
      
      <!-- 拖拽预览虚影 -->
      <div 
        v-if="isDragging"
        class="drag-preview"
        :style="dragPreviewStyle"
      >
        <el-icon class="preview-icon">{{ dragPreviewComponent?.icon }}</el-icon>
        <div class="preview-text">{{ dragPreviewComponent?.name }}</div>
      </div>
      
      <!-- 组件直接渲染在画布上（无额外父容器包装） -->
      <template
        v-for="component in editorState.components"
        :key="component.id"
      >
        <!-- 表单组件 -->
        <template v-if="component.category === 'form'">
          <!-- 文本输入框 -->
          <el-form-item v-if="component.type === 'input-text'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-input 
                  v-model="component.props.defaultValue" 
                  :placeholder="component.props.placeholder" 
                  :disabled="component.props.disabled" 
                  :readonly="component.props.readonly" 
                  :maxlength="component.props.maxLength" 
                  :show-word-limit="component.props.showWordLimit" 
                />
              </el-form-item>
              
              <!-- 数字输入框 -->
              <el-form-item v-else-if="component.type === 'input-number'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-input-number 
                  v-model="component.props.defaultValue" 
                  :placeholder="component.props.placeholder" 
                  :disabled="component.props.disabled" 
                  :min="component.props.min" 
                  :max="component.props.max" 
                  :step="component.props.step" 
                  :precision="component.props.precision" 
                  :controls-position="component.props.controlsPosition" 
                />
              </el-form-item>
              
              <!-- 密码框 -->
              <el-form-item v-else-if="component.type === 'input-password'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-input 
                  v-model="component.props.defaultValue" 
                  :placeholder="component.props.placeholder" 
                  :disabled="component.props.disabled" 
                  :show-password="component.props.showPassword" 
                  :maxlength="component.props.maxLength" 
                  :show-word-limit="component.props.showWordLimit" 
                  type="password"
                />
              </el-form-item>
              
              <!-- 多行文本框 -->
              <el-form-item v-else-if="component.type === 'textarea'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-input 
                  v-model="component.props.defaultValue" 
                  :placeholder="component.props.placeholder" 
                  :disabled="component.props.disabled" 
                  :rows="component.props.rows" 
                  :maxlength="component.props.maxLength" 
                  :show-word-limit="component.props.showWordLimit" 
                  :resize="component.props.resize" 
                  type="textarea"
                />
              </el-form-item>
              
              <!-- 下拉列表框 -->
              <el-form-item v-else-if="component.type === 'select'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-select 
                  v-model="component.props.defaultValue" 
                  :placeholder="component.props.placeholder" 
                  :disabled="component.props.disabled" 
                  :multiple="component.props.multiple" 
                  :filterable="component.props.filterable" 
                >
                  <el-option 
                    v-for="option in component.props.options" 
                    :key="option.value" 
                    :label="option.label" 
                    :value="option.value" 
                  />
                </el-select>
              </el-form-item>
              
              <!-- 单选按钮 -->
              <el-form-item v-else-if="component.type === 'radio'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-radio-group 
                  v-model="component.props.defaultValue" 
                  :disabled="component.props.disabled" 
                  :direction="component.props.direction" 
                >
                  <el-radio 
                    v-for="option in component.props.options" 
                    :key="option.value" 
                    :label="option.value" 
                  >{{ option.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <!-- 复选按钮 -->
              <el-form-item v-else-if="component.type === 'checkbox'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-checkbox-group 
                  v-model="component.props.defaultValue" 
                  :disabled="component.props.disabled" 
                  :direction="component.props.direction" 
                >
                  <el-checkbox 
                    v-for="option in component.props.options" 
                    :key="option.value" 
                    :label="option.value" 
                  >{{ option.label }}</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <!-- 开关按钮 -->
              <el-form-item v-else-if="component.type === 'switch'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-switch 
                  v-model="component.props.defaultValue" 
                  :disabled="component.props.disabled" 
                  :active-text="component.props.activeText" 
                  :inactive-text="component.props.inactiveText" 
                  :active-color="component.props.activeColor" 
                  :inactive-color="component.props.inactiveColor" 
                />
              </el-form-item>
              
              <!-- 日期时间选择器 -->
              <el-form-item v-else-if="component.type === 'datetime-picker'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-date-picker 
                  v-model="component.props.defaultValue" 
                  :placeholder="component.props.placeholder" 
                  :disabled="component.props.disabled" 
                  :type="component.props.type" 
                  :format="component.props.format" 
                  :clearable="component.props.clearable" 
                  :editable="component.props.editable" 
                />
              </el-form-item>
              
              <!-- 滑块 -->
              <el-form-item v-else-if="component.type === 'slider'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-slider 
                  v-model="component.props.defaultValue" 
                  :disabled="component.props.disabled" 
                  :min="component.props.min" 
                  :max="component.props.max" 
                  :step="component.props.step" 
                  :show-input="component.props.showInput" 
                  :range="component.props.range" 
                  :vertical="component.props.vertical" 
                />
              </el-form-item>
              
              <!-- 评分 -->
              <el-form-item v-else-if="component.type === 'rate'" :label="component.props.label" label-position="left" class="canvas-form-item" :style="component.style" @click.stop="handleComponentClick(component)">
                <el-rate 
                  v-model="component.props.defaultValue" 
                  :disabled="component.props.disabled" 
                  :max="component.props.max" 
                  :allow-half="component.props.allowHalf" 
                  :show-text="component.props.showText" 
                  :text-color="component.props.textColor" 
                  :active-color="component.props.activeColor" 
                />
              </el-form-item>
              
              <!-- 表单容器 -->
              <el-form v-else-if="component.type === 'form-container'" 
                :label-position="component.props.labelPosition" 
                :label-width="component.props.labelWidth" 
                :label-suffix="component.props.labelSuffix" 
                :inline="component.props.inline" 
                :size="component.props.size" 
                :disabled="component.props.disabled" 
                class="canvas-form-container"
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <div class="form-container-placeholder">表单容器</div>
              </el-form>
              
              <!-- 标签页 -->
              <el-tabs v-else-if="component.type === 'tabs'" 
                v-model="component.props.activeName"
                :type="component.props.type"
                :closable="component.props.closable"
                :addable="component.props.addable"
                :editable="component.props.editable"
                :disabled="component.props.disabled"
                :style="component.style"
                @click.stop="handleComponentClick(component)"
                @tab-add="handleTabAdd(component)"
                @tab-remove="handleTabRemove(component, $event)"
              >
                <el-tab-pane 
                  v-for="tab in component.props.tabs" 
                  :key="tab.name" 
                  :label="tab.label" 
                  :name="tab.name"
                >
                  <!-- 渲染tab内部的子组件 -->
                  <div class="tab-content" 
                    @dragover.prevent
                    @drop="handleTabDrop(component, tab, $event)"
                    @keydown.delete="handleKeyDown($event, component)"
                  >
                    <!-- 查找该tab下的所有子组件 -->
                    <template 
                      v-for="child in editorState.components.filter(c => 
                        c.parentId === component.id && c.props.tabName === tab.name
                      )" 
                      :key="child.id"
                    >
                      <!-- 递归渲染子组件 -->
                      <!-- 表单组件 -->
                      <template v-if="child.category === 'form'">
                        <!-- 文本输入框 -->
                        <el-form-item v-if="child.type === 'input-text'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-input 
                                v-model="child.props.defaultValue" 
                                :placeholder="child.props.placeholder" 
                                :disabled="child.props.disabled" 
                                :readonly="child.props.readonly" 
                                :maxlength="child.props.maxLength" 
                                :show-word-limit="child.props.showWordLimit" 
                              />
                            </el-form-item>
                            
                            <!-- 数字输入框 -->
                            <el-form-item v-else-if="child.type === 'input-number'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-input-number 
                                v-model="child.props.defaultValue" 
                                :placeholder="child.props.placeholder" 
                                :disabled="child.props.disabled" 
                                :min="child.props.min" 
                                :max="child.props.max" 
                                :step="child.props.step" 
                                :precision="child.props.precision" 
                                :controls-position="child.props.controlsPosition" 
                              />
                            </el-form-item>
                            
                            <!-- 密码框 -->
                            <el-form-item v-else-if="child.type === 'input-password'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-input 
                                v-model="child.props.defaultValue" 
                                :placeholder="child.props.placeholder" 
                                :disabled="child.props.disabled" 
                                :show-password="child.props.showPassword" 
                                :maxlength="child.props.maxLength" 
                                :show-word-limit="child.props.showWordLimit" 
                                type="password"
                              />
                            </el-form-item>
                            
                            <!-- 多行文本框 -->
                            <el-form-item v-else-if="child.type === 'textarea'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-input 
                                v-model="child.props.defaultValue" 
                                :placeholder="child.props.placeholder" 
                                :disabled="child.props.disabled" 
                                :rows="child.props.rows" 
                                :maxlength="child.props.maxLength" 
                                :show-word-limit="child.props.showWordLimit" 
                                :resize="child.props.resize" 
                                type="textarea"
                              />
                            </el-form-item>
                            
                            <!-- 下拉列表框 -->
                            <el-form-item v-else-if="child.type === 'select'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-select 
                                v-model="child.props.defaultValue" 
                                :placeholder="child.props.placeholder" 
                                :disabled="child.props.disabled" 
                                :multiple="child.props.multiple" 
                                :filterable="child.props.filterable" 
                              >
                                <el-option 
                                  v-for="option in child.props.options" 
                                  :key="option.value" 
                                  :label="option.label" 
                                  :value="option.value" 
                                />
                              </el-select>
                            </el-form-item>
                            
                            <!-- 单选按钮 -->
                            <el-form-item v-else-if="child.type === 'radio'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-radio-group 
                                v-model="child.props.defaultValue" 
                                :disabled="child.props.disabled" 
                                :direction="child.props.direction" 
                              >
                                <el-radio 
                                  v-for="option in child.props.options" 
                                  :key="option.value" 
                                  :label="option.value" 
                                >{{ option.label }}</el-radio>
                              </el-radio-group>
                            </el-form-item>
                            
                            <!-- 复选按钮 -->
                            <el-form-item v-else-if="child.type === 'checkbox'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-checkbox-group 
                                v-model="child.props.defaultValue" 
                                :disabled="child.props.disabled" 
                                :direction="child.props.direction" 
                              >
                                <el-checkbox 
                                  v-for="option in child.props.options" 
                                  :key="option.value" 
                                  :label="option.value" 
                                >{{ option.label }}</el-checkbox>
                              </el-checkbox-group>
                            </el-form-item>
                            
                            <!-- 开关按钮 -->
                            <el-form-item v-else-if="child.type === 'switch'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-switch 
                                v-model="child.props.defaultValue" 
                                :disabled="child.props.disabled" 
                                :active-text="child.props.activeText" 
                                :inactive-text="child.props.inactiveText" 
                                :active-color="child.props.activeColor" 
                                :inactive-color="child.props.inactiveColor" 
                              />
                            </el-form-item>
                            
                            <!-- 日期时间选择器 -->
                            <el-form-item v-else-if="child.type === 'datetime-picker'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-date-picker 
                                v-model="child.props.defaultValue" 
                                :placeholder="child.props.placeholder" 
                                :disabled="child.props.disabled" 
                                :type="child.props.type" 
                                :format="child.props.format" 
                                :clearable="child.props.clearable" 
                                :editable="child.props.editable" 
                              />
                            </el-form-item>
                            
                            <!-- 滑块 -->
                            <el-form-item v-else-if="child.type === 'slider'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-slider 
                                v-model="child.props.defaultValue" 
                                :disabled="child.props.disabled" 
                                :min="child.props.min" 
                                :max="child.props.max" 
                                :step="child.props.step" 
                                :show-input="child.props.showInput" 
                                :range="child.props.range" 
                                :vertical="child.props.vertical" 
                              />
                            </el-form-item>
                            
                            <!-- 评分 -->
                            <el-form-item v-else-if="child.type === 'rate'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-rate 
                                v-model="child.props.defaultValue" 
                                :disabled="child.props.disabled" 
                                :max="child.props.max" 
                                :allow-half="child.props.allowHalf" 
                                :show-text="child.props.showText" 
                                :text-color="child.props.textColor" 
                                :active-color="child.props.activeColor" 
                              />
                            </el-form-item>
                          </template>
                          
                          <!-- 基础组件 -->
                          <template v-else-if="child.category === 'basic'">
                            <!-- 按钮 -->
                            <el-button v-if="child.type === 'button'" 
                              :type="child.props.type" 
                              :size="child.props.size" 
                              :disabled="child.props.disabled" 
                              :loading="child.props.loading" 
                              :round="child.props.round" 
                              :circle="child.props.circle" 
                              :plain="child.props.plain" 
                              :link="child.props.link" 
                              :style="child.style"
                              @click.stop="handleComponentClick(child)"
                            >
                              {{ child.props.label }}
                            </el-button>
                          </template>
                        </template>
                      </div>
                    </el-tab-pane>
                  </el-tabs>
            </template>
            
            <!-- 基础组件 -->
            <template v-else-if="component.category === 'basic'">
              <!-- 按钮 -->
              <el-button v-if="component.type === 'button'" 
                :type="component.props.type" 
                :size="component.props.size" 
                :disabled="component.props.disabled" 
                :loading="component.props.loading" 
                :round="component.props.round" 
                :circle="component.props.circle" 
                :plain="component.props.plain" 
                :link="component.props.link" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                {{ component.props.label }}
              </el-button>
              
              <!-- 标签 -->
              <el-tag v-else-if="component.type === 'tag'" 
                :type="component.props.type" 
                :size="component.props.size" 
                :closable="component.props.closable" 
                :effect="component.props.effect" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                {{ component.props.label }}
              </el-tag>
              
              <!-- 链接 -->
              <el-link v-else-if="component.type === 'link'" 
                :type="component.props.type" 
                :underline="component.props.underline" 
                :disabled="component.props.disabled" 
                :href="component.props.href" 
                :target="component.props.target" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                {{ component.props.text }}
              </el-link>
              
              <!-- 加载指示器 -->
              <div v-else-if="component.type === 'loading'" class="loading-container" :style="component.style">
                <el-loading :fullscreen="false" :text="component.props.text" :color="component.props.color" :background="component.props.background" />
              </div>
              
              <!-- 提示 -->
              <el-tooltip v-else-if="component.type === 'tooltip'" 
                :content="component.props.content" 
                :placement="component.props.placement" 
                :effect="component.props.effect" 
                :style="component.style"
              >
                <div class="tooltip-trigger">悬停查看提示</div>
              </el-tooltip>
              
              <!-- 弹出气泡 -->
              <el-popover v-else-if="component.type === 'popover'" 
                :title="component.props.title" 
                :content="component.props.content" 
                :placement="component.props.placement" 
                :trigger="component.props.trigger" 
                :effect="component.props.effect" 
                :style="component.style"
              >
                <template #reference>
                  <div class="popover-trigger">点击查看气泡</div>
                </template>
              </el-popover>
              
              <!-- 进度条 -->
              <el-progress v-else-if="component.type === 'progress'" 
                :percentage="component.props.percentage" 
                :type="component.props.type" 
                :stroke-width="component.props.strokeWidth" 
                :color="component.props.color" 
                :show-text="component.props.showText" 
                :status="component.props.status" 
                :width="component.props.width" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              />
              
              <!-- 分割线 -->
              <el-divider v-else-if="component.type === 'divider'" 
                :orientation="component.props.orientation" 
                :content-position="component.props.contentPosition" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >{{ component.props.content }}</el-divider>
              
              <!-- 面板 -->
              <el-card v-else-if="component.type === 'panel'" 
                :title="component.props.title" 
                :border="component.props.border" 
                :body-style="{ padding: '16px' }"
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <div class="panel-content">面板内容</div>
              </el-card>
            </template>
            
            <!-- 数据展示组件 -->
            <template v-else-if="component.category === 'display'">
              <!-- 表格 -->
              <el-table v-if="component.type === 'table'" 
                :data="component.props.data" 
                :stripe="component.props.stripe" 
                :border="component.props.border" 
                :height="component.props.height" 
                :max-height="component.props.maxHeight" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <el-table-column 
                  v-for="column in component.props.columns" 
                  :key="column.prop" 
                  :prop="column.prop" 
                  :label="column.label" 
                  :width="column.width" 
                />
              </el-table>
              
              <!-- 列表 -->
              <el-list v-else-if="component.type === 'list'" 
                :data="component.props.data" 
                :border="component.props.border" 
                :size="component.props.size" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <template #default="{ item }">
                  <el-list-item :title="item.title">{{ item.desc }}</el-list-item>
                </template>
              </el-list>
              
              <!-- 描述列表 -->
              <el-descriptions v-else-if="component.type === 'descriptions'" 
                :title="component.props.title" 
                :border="component.props.border" 
                :column="component.props.column" 
                :size="component.props.size" 
                :colon="component.props.colon" 
                :label-position="component.props.labelPosition" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <el-descriptions-item 
                  v-for="(value, key) in component.props.data" 
                  :key="key" 
                  :label="key" 
                >{{ value }}</el-descriptions-item>
              </el-descriptions>
              
              <!-- 树形控件 -->
              <el-tree v-else-if="component.type === 'tree'" 
                :data="component.props.data" 
                :node-key="component.props.nodeKey" 
                :default-expand-all="component.props.defaultExpandAll" 
                :default-expanded-keys="component.props.defaultExpandedKeys" 
                :default-checked-keys="component.props.defaultCheckedKeys" 
                :default-selected-keys="component.props.defaultSelectedKeys" 
                :highlight-current="component.props.highlightCurrent" 
                :expand-on-click-node="component.props.expandOnClickNode" 
                :style="component.style"
                @node-click="handleComponentClick(component)"
              />
              
              <!-- 折叠面板 -->
              <el-collapse v-else-if="component.type === 'collapse'" 
                v-model="component.props.activeNames" 
                :border="component.props.border" 
                :accordion="component.props.accordion" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <el-collapse-item 
                  v-for="item in component.props.items" 
                  :key="item.name" 
                  :title="item.title" 
                  :name="item.name" 
                >{{ item.content }}</el-collapse-item>
              </el-collapse>
              
              <!-- 标签页 -->
              <el-tabs v-else-if="component.type === 'tabs'" 
                v-model="component.props.activeName" 
                :type="component.props.type" 
                :closable="component.props.closable" 
                :addable="component.props.addable" 
                :editable="component.props.editable"
                :style="component.style"
                @tab-click="handleComponentClick(component)"
                @tab-add="handleTabAdd(component)"
                @tab-remove="handleTabRemove(component, $event)"
              >
                <el-tab-pane 
                  v-for="item in component.props.items" 
                  :key="item.name" 
                  :label="item.title" 
                  :name="item.name" 
                >
                  <!-- 渲染tab内部的子组件 -->
                  <div class="tab-content" 
                    @dragover.prevent
                    @drop="handleTabDrop(component, item, $event)"
                    @keydown.delete="handleKeyDown($event, component)"
                  >
                    <!-- 查找该tab下的所有子组件 -->
                    <template 
                      v-for="child in editorState.components.filter(c => 
                        c.parentId === component.id && c.props.tabName === item.name
                      )" 
                      :key="child.id"
                    >
                      <!-- 递归渲染子组件 -->
                      <!-- 表单组件 -->
                      <template v-if="child.category === 'form'">
                        <!-- 文本输入框 -->
                        <el-form-item v-if="child.type === 'input-text'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-input 
                                v-model="child.props.defaultValue" 
                                :placeholder="child.props.placeholder" 
                                :disabled="child.props.disabled" 
                                :readonly="child.props.readonly" 
                                :maxlength="child.props.maxLength" 
                                :show-word-limit="child.props.showWordLimit" 
                              />
                            </el-form-item>
                            
                            <!-- 数字输入框 -->
                            <el-form-item v-else-if="child.type === 'input-number'" :label="child.props.label" label-position="left" class="canvas-form-item" :style="child.style" @click.stop="handleComponentClick(child)">
                              <el-input-number 
                                v-model="child.props.defaultValue" 
                                :placeholder="child.props.placeholder" 
                                :disabled="child.props.disabled" 
                                :min="child.props.min" 
                                :max="child.props.max" 
                                :step="child.props.step" 
                                :precision="child.props.precision" 
                                :controls-position="child.props.controlsPosition" 
                              />
                            </el-form-item>
                          </template>
                          
                          <!-- 基础组件 -->
                          <template v-else-if="child.category === 'basic'">
                            <!-- 按钮 -->
                            <el-button v-if="child.type === 'button'" 
                              :type="child.props.type" 
                              :size="child.props.size" 
                              :disabled="child.props.disabled" 
                              :loading="child.props.loading" 
                              :round="child.props.round" 
                              :circle="child.props.circle" 
                              :plain="child.props.plain" 
                              :link="child.props.link" 
                              :style="child.style"
                              @click.stop="handleComponentClick(child)"
                            >
                              {{ child.props.label }}
                            </el-button>
                          </template>
                        </template>
                      </div>
                    </el-tab-pane>
                  </el-tabs>
              
              <!-- 轮播图 -->
              <el-carousel v-else-if="component.type === 'carousel'" 
                :height="component.props.height" 
                :autoplay="component.props.autoplay" 
                :interval="component.props.interval" 
                :indicator-position="component.props.indicatorPosition" 
                :arrow="component.props.arrow" 
                :loop="component.props.loop" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <el-carousel-item 
                  v-for="(item, index) in component.props.items" 
                  :key="index" 
                >
                  <img :src="item.url" :alt="item.alt" style="width: 100%; height: 100%; object-fit: cover;">
                </el-carousel-item>
              </el-carousel>
              
              <!-- 统计卡片 -->
              <el-statistic v-else-if="component.type === 'stat-card'" 
                :title="component.props.title" 
                :value="component.props.value" 
                :precision="0" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <template #prefix>
                  <span v-if="component.props.icon">{{ component.props.icon }}</span>
                </template>
                <template #suffix>
                  <span>{{ component.props.unit }}</span>
                  <span class="trend" :class="component.props.trend">
                    {{ component.props.trendValue }} <el-icon>{{ component.props.trend === 'up' ? 'ArrowUp' : 'ArrowDown' }}</el-icon>
                  </span>
                </template>
                <template #extra>
                  <span class="description">{{ component.props.description }}</span>
                </template>
              </el-statistic>
              
              <!-- 时间轴 -->
              <el-timeline v-else-if="component.type === 'timeline'" 
                :direction="component.props.direction" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <el-timeline-item 
                  v-for="(item, index) in component.props.items" 
                  :key="index" 
                  :timestamp="item.timestamp" 
                  :placement="component.props.placement" 
                >
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.description }}</p>
                </el-timeline-item>
              </el-timeline>
              
              <!-- 面包屑 -->
              <el-breadcrumb v-else-if="component.type === 'breadcrumb'" 
                :separator="component.props.separator" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <el-breadcrumb-item 
                  v-for="(item, index) in component.props.items" 
                  :key="index" 
                  :to="item.path" 
                >{{ item.name }}</el-breadcrumb-item>
              </el-breadcrumb>
              
              <!-- 分页器 -->
              <el-pagination v-else-if="component.type === 'pagination'" 
                v-model:current-page="component.props.currentPage" 
                v-model:page-size="component.props.pageSize" 
                :page-sizes="component.props.pageSizes" 
                :layout="component.props.layout" 
                :total="component.props.total" 
                :background="component.props.background" 
                :small="component.props.small" 
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              />
            </template>
            
            <!-- 布局组件 -->
            <template v-else-if="component.category === 'layout'">
              <!-- 布局容器 -->
              <el-layout v-if="component.type === 'layout'" 
                :direction="component.props.direction" 
                class="canvas-layout"
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <div class="layout-placeholder">布局容器</div>
              </el-layout>
              
              <!-- 行 -->
              <el-row v-else-if="component.type === 'row'" 
                :gutter="component.props.gutter" 
                :type="component.props.type" 
                :justify="component.props.justify" 
                :align="component.props.align" 
                :tag="component.props.tag" 
                class="canvas-row"
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <div class="row-placeholder">行布局</div>
              </el-row>
              
              <!-- 列 -->
              <el-col v-else-if="component.type === 'column'" 
                :span="component.props.span" 
                :offset="component.props.offset" 
                :push="component.props.push" 
                :pull="component.props.pull" 
                :xs="component.props.xs" 
                :sm="component.props.sm" 
                :md="component.props.md" 
                :lg="component.props.lg" 
                :xl="component.props.xl" 
                :xxl="component.props.xxl" 
                :tag="component.props.tag" 
                class="canvas-col"
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <div class="col-placeholder">列布局</div>
              </el-col>
              
              <!-- 分栏布局 -->
              <el-aside v-else-if="component.type === 'aside'" 
                :width="component.props.width" 
                class="canvas-aside"
                :style="component.style"
                @click.stop="handleComponentClick(component)"
              >
                <div class="aside-placeholder">侧边栏</div>
              </el-aside>
              
              <!-- 弹性盒子 -->
              <div v-else-if="component.type === 'flex'" 
                class="canvas-flex" 
                :style="{
                  ...component.style,
                  display: 'flex',
                  flexDirection: component.props.direction,
                  justifyContent: component.props.justify,
                  alignItems: component.props.align,
                  flexWrap: component.props.wrap,
                  gap: `${component.props.gap}px`
                }"
                @click.stop="handleComponentClick(component)"
              >
                <div class="flex-placeholder">弹性盒子</div>
              </div>
              
              <!-- 容器 -->
              <div v-else-if="component.type === 'container'" 
                class="canvas-container-component" 
                :style="{
                  ...component.style,
                  padding: component.props.padding,
                  margin: component.props.margin,
                  backgroundColor: component.props.backgroundColor,
                  borderRadius: component.props.borderRadius,
                  border: component.props.border,
                  boxShadow: component.props.boxShadow
                }"
                @click.stop="handleComponentClick(component)"
              >
                <div class="container-placeholder">容器</div>
              </div>
              
              <!-- 侧边栏 -->
              <div v-else-if="component.type === 'sidebar'" 
                class="canvas-sidebar" 
                :style="{
                  ...component.style,
                  width: component.props.width,
                  backgroundColor: component.props.backgroundColor,
                  color: component.props.textColor,
                  borderBottom: component.props.collapsed ? 'none' : component.props.borderBottom
                }"
                @click.stop="handleComponentClick(component)"
              >
                <div class="sidebar-placeholder">侧边栏</div>
              </div>
              
              <!-- 顶部导航 -->
              <el-header v-else-if="component.type === 'header'" 
                class="canvas-header"
                :style="{
                  ...component.style,
                  height: component.props.height,
                  backgroundColor: component.props.backgroundColor,
                  borderBottom: component.props.borderBottom
                }"
                @click.stop="handleComponentClick(component)"
              >
                <div class="header-content">
                  <span v-if="component.props.logo" class="logo">{{ component.props.logo }}</span>
                  <span class="title">{{ component.props.title }}</span>
                </div>
              </el-header>
              
              <!-- 底部 -->
              <el-footer v-else-if="component.type === 'footer'" 
                class="canvas-footer"
                :style="{
                  ...component.style,
                  height: component.props.height,
                  backgroundColor: component.props.backgroundColor,
                  borderTop: component.props.borderTop
                }"
                @click.stop="handleComponentClick(component)"
              >
                <div class="footer-content">{{ component.props.text }}</div>
              </el-footer>
              
              <!-- 网格 -->
              <div v-else-if="component.type === 'grid'" 
                class="canvas-grid" 
                :style="{
                  ...component.style,
                  display: 'grid',
                  gridTemplateColumns: `repeat(${component.props.columns}, 1fr)`,
                  gap: `${component.props.gap}px`,
                  gridAutoRows: component.props.autoRows
                }"
                @click.stop="handleComponentClick(component)"
              >
                <div class="grid-item" v-for="i in component.props.columns" :key="i"></div>
              </div>
            </template>
            
            <!-- 默认显示 -->
            <template v-else>
              <div class="default-component" :style="component.style" @click.stop="handleComponentClick(component)">{{ component.name }}</div>
            </template>
          </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// Props
const props = defineProps({
  editorState: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['select-component', 'update-component', 'add-component'])

// Refs
const canvasRef = ref(null)

// 响应式数据
const selectedComponentId = ref(null)
const isDragging = ref(false)
const dragPreviewComponent = ref(null)
const dragOffset = ref({ x: 0, y: 0 })
const resizeStartPos = ref({ x: 0, y: 0 })
const resizeStartSize = ref({ width: 0, height: 0 })
const resizeMode = ref('')

// 计算属性
const canvasStyle = computed(() => ({
  position: 'relative',
  width: '100%',
  height: '100%',
  backgroundColor: '#ffffff',
  overflow: 'hidden',
  cursor: 'default'
}))

const gridStyle = computed(() => ({
  position: 'absolute',
  top: '0',
  left: '0',
  width: '100%',
  height: '100%',
  backgroundImage: `linear-gradient(#ebeef5 1px, transparent 1px), linear-gradient(90deg, #ebeef5 1px, transparent 1px)`,
  backgroundSize: `${props.editorState.gridSize}px ${props.editorState.gridSize}px`
}))

const dragPreviewStyle = computed(() => ({
  position: 'absolute',
  left: `${dragOffset.value.x}px`,
  top: `${dragOffset.value.y}px`,
  width: '100px',
  height: '60px',
  backgroundColor: 'rgba(64, 158, 255, 0.1)',
  border: 'none',
  borderRadius: '4px',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  pointerEvents: 'none',
  zIndex: '1000'
}))

// 生成唯一ID
const generateId = () => {
  return 'component_' + Math.random().toString(36).substr(2, 9)
}



// 吸附到网格
const snapToGrid = (value) => {
  const gridSize = props.editorState.gridSize
  return Math.round(value / gridSize) * gridSize
}

// 获取鼠标在画布中的位置
const getMousePosInCanvas = (event) => {
  if (!canvasRef.value) return { x: 0, y: 0 }
  const rect = canvasRef.value.getBoundingClientRect()
  return {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
}

// 处理拖拽开始（从组件库拖拽到画布）
const handleDragOver = (event) => {
  event.preventDefault()
  event.dataTransfer.dropEffect = 'copy'
  
  // 更新拖拽预览位置
  const pos = getMousePosInCanvas(event)
  dragOffset.value = {
    x: snapToGrid(pos.x - 50), // 居中显示
    y: snapToGrid(pos.y - 30)  // 居中显示
  }
  
  // 如果还没有拖拽预览组件，尝试解析拖拽数据
  if (!dragPreviewComponent.value) {
    try {
      const componentData = event.dataTransfer.getData('application/json')
      if (componentData) {
        dragPreviewComponent.value = JSON.parse(componentData)
        isDragging.value = true
      }
    } catch (error) {
      console.error('Failed to parse drag data:', error)
    }
  }
}

// 处理放置
const handleDrop = (event) => {
  event.preventDefault()
  isDragging.value = false
  
  try {
    // 获取拖拽的组件数据
    const componentData = event.dataTransfer.getData('application/json')
    if (!componentData) return
    
    const componentTemplate = JSON.parse(componentData)
    const pos = getMousePosInCanvas(event)
    
    // 创建新组件
    const newComponent = {
      id: generateId(),
      ...componentTemplate,
      style: {
        ...componentTemplate.style,
        left: `${snapToGrid(pos.x - 50)}px`, // 居中放置
        top: `${snapToGrid(pos.y - 30)}px`   // 居中放置
      }
    }
    
    // 添加组件
    emit('add-component', newComponent)
  } catch (error) {
    console.error('Failed to handle drop:', error)
  } finally {
    // 清除拖拽预览
    dragPreviewComponent.value = null
    dragOffset.value = { x: 0, y: 0 }
  }
}

// 处理画布点击（取消选中）
const handleCanvasClick = () => {
  selectedComponentId.value = null
  emit('select-component', null)
}

// 处理组件点击
const handleComponentClick = (component) => {
  selectedComponentId.value = component.id
  emit('select-component', component.id)
}

// 处理组件拖拽开始
const handleComponentDragStart = (event, component) => {
  event.dataTransfer.effectAllowed = 'move'
  event.dataTransfer.setData('text/plain', component.id)
  selectedComponentId.value = component.id
  emit('select-component', component.id)
}

// 处理组件内放置
const handleComponentDrop = (event, targetComponent) => {
  event.preventDefault()
  const draggedComponentId = event.dataTransfer.getData('text/plain')
  if (draggedComponentId === targetComponent.id) return
  
  // 这里可以实现组件嵌套逻辑
  console.log('Drop component', draggedComponentId, 'into', targetComponent.id)
}

// 处理添加新tab
const handleTabAdd = (component) => {
  // 生成新的tab名称
  const newTabIndex = component.props.tabs.length + 1
  const newTab = {
    label: `标签${newTabIndex}`,
    name: `${newTabIndex}`,
    content: `标签${newTabIndex}内容`
  }
  
  // 添加到tabs列表
  component.props.tabs.push(newTab)
  // 设置为当前激活的tab
  component.props.activeName = newTab.name
  
  emit('update-component', component)
}

// 处理删除tab
const handleTabRemove = (component, tabName) => {
  // 过滤掉要删除的tab
  component.props.tabs = component.props.tabs.filter(tab => tab.name !== tabName)
  
  // 删除该tab下的所有子组件
  const tabChildComponents = editorState.components.filter(c => 
    c.parentId === component.id && c.props.tabName === tabName
  )
  
  tabChildComponents.forEach(child => {
    const index = editorState.components.findIndex(c => c.id === child.id)
    if (index !== -1) {
      editorState.components.splice(index, 1)
    }
  })
  
  // 如果删除的是当前激活的tab，切换到第一个tab
  if (component.props.activeName === tabName && component.props.tabs.length > 0) {
    component.props.activeName = component.props.tabs[0].name
  }
  
  emit('update-component', component)
}

// 处理tab内容区域的拖拽放置
const handleTabDrop = (component, tab, event) => {
  event.preventDefault()
  
  try {
    // 获取拖拽的组件数据
    const componentData = event.dataTransfer.getData('application/json')
    if (!componentData) return
    
    const componentTemplate = JSON.parse(componentData)
    const pos = getMousePosInCanvas(event)
    
    console.log('=== Tab Drop Debug ===')
    console.log('拖拽的组件模板:', componentTemplate)
    console.log('目标组件ID:', component.id)
    console.log('目标Tab名称:', tab.name)
    
    // 创建新组件，设置父组件ID和所属tab名称
    const newComponent = {
      id: generateId(),
      ...componentTemplate,
      parentId: component.id,
      props: {
        ...componentTemplate.props,
        tabName: tab.name
      },
      style: {
        ...componentTemplate.style,
        left: `${snapToGrid(pos.x - 50)}px`, // 居中放置
        top: `${snapToGrid(pos.y - 30)}px`   // 居中放置
      }
    }
    
    console.log('创建的新组件:', newComponent)
    console.log('新组件的parentId:', newComponent.parentId)
    console.log('新组件的tabName:', newComponent.props.tabName)
    console.log('====================')
    
    // 添加组件
    emit('add-component', newComponent)
  } catch (error) {
    console.error('Failed to handle tab drop:', error)
  }
}

// 处理键盘删除事件
const handleKeyDown = (event, component) => {
  // 确保是删除键且有选中的组件
  if (event.key === 'Delete' && selectedComponentId.value) {
    // 查找选中的组件
    const index = editorState.components.findIndex(c => c.id === selectedComponentId.value)
    if (index !== -1) {
      // 删除组件
      editorState.components.splice(index, 1)
      // 取消选中
      selectedComponentId.value = null
      emit('select-component', null)
    }
  }
}

// 处理组件鼠标按下（开始拖拽）
const handleComponentMouseDown = (event, component) => {
  selectedComponentId.value = component.id
  emit('select-component', component.id)
}

// 处理调整大小开始
const handleResizeStart = (event, component, mode) => {
  event.stopPropagation()
  resizeMode.value = mode
  resizeStartPos.value = getMousePosInCanvas(event)
  resizeStartSize.value = {
    width: parseInt(component.style.width),
    height: parseInt(component.style.height)
  }
  
  // 添加全局事件监听
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', handleResizeEnd)
}

// 处理调整大小
const handleResize = (event) => {
  if (!resizeMode.value) return
  
  const pos = getMousePosInCanvas(event)
  const deltaX = pos.x - resizeStartPos.value.x
  const deltaY = pos.y - resizeStartPos.value.y
  
  let newWidth = resizeStartSize.value.width
  let newHeight = resizeStartSize.value.height
  let newLeft = parseInt(editorState.components.find(c => c.id === selectedComponentId.value)?.style.left) || 0
  let newTop = parseInt(editorState.components.find(c => c.id === selectedComponentId.value)?.style.top) || 0
  
  // 根据调整模式计算新的尺寸和位置
  switch (resizeMode.value) {
    case 'top-left':
      newWidth = Math.max(50, resizeStartSize.value.width - deltaX)
      newHeight = Math.max(30, resizeStartSize.value.height - deltaY)
      newLeft = resizeStartPos.value.x - (resizeStartSize.value.width - newWidth)
      newTop = resizeStartPos.value.y - (resizeStartSize.value.height - newHeight)
      break
    case 'top':
      newHeight = Math.max(30, resizeStartSize.value.height - deltaY)
      newTop = resizeStartPos.value.y - (resizeStartSize.value.height - newHeight)
      break
    case 'top-right':
      newWidth = Math.max(50, resizeStartSize.value.width + deltaX)
      newHeight = Math.max(30, resizeStartSize.value.height - deltaY)
      newTop = resizeStartPos.value.y - (resizeStartSize.value.height - newHeight)
      break
    case 'left':
      newWidth = Math.max(50, resizeStartSize.value.width - deltaX)
      newLeft = resizeStartPos.value.x - (resizeStartSize.value.width - newWidth)
      break
    case 'right':
      newWidth = Math.max(50, resizeStartSize.value.width + deltaX)
      break
    case 'bottom-left':
      newWidth = Math.max(50, resizeStartSize.value.width - deltaX)
      newHeight = Math.max(30, resizeStartSize.value.height + deltaY)
      newLeft = resizeStartPos.value.x - (resizeStartSize.value.width - newWidth)
      break
    case 'bottom':
      newHeight = Math.max(30, resizeStartSize.value.height + deltaY)
      break
    case 'bottom-right':
      newWidth = Math.max(50, resizeStartSize.value.width + deltaX)
      newHeight = Math.max(30, resizeStartSize.value.height + deltaY)
      break
  }
  
  // 吸附到网格
  newWidth = snapToGrid(newWidth)
  newHeight = snapToGrid(newHeight)
  newLeft = snapToGrid(newLeft)
  newTop = snapToGrid(newTop)
  
  // 更新组件样式
  const component = editorState.components.find(c => c.id === selectedComponentId.value)
  if (component) {
    console.log('=== Resize Component Debug ===')
    console.log('调整大小的组件:', component)
    console.log('调整大小前的parentId:', component.parentId)
    console.log('调整大小前的tabName:', component.props.tabName)
    console.log('==============================')
    
    component.style = {
      ...component.style,
      width: `${newWidth}px`,
      height: `${newHeight}px`,
      left: `${newLeft}px`,
      top: `${newTop}px`
    }
    
    console.log('调整大小后的组件:', component)
    console.log('调整大小后的parentId:', component.parentId)
    console.log('调整大小后的tabName:', component.props.tabName)
    console.log('==============================')
    
    emit('update-component', component)
  }
}

// 处理调整大小结束
const handleResizeEnd = () => {
  resizeMode.value = ''
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', handleResizeEnd)
}

// 生命周期钩子
onMounted(() => {
  // 添加全局拖拽事件监听
  document.addEventListener('dragover', handleDragOver)
  document.addEventListener('dragenter', (event) => {
    if (event.target === canvasRef.value) {
      isDragging.value = true
    }
  })
  document.addEventListener('dragleave', (event) => {
    if (event.target === canvasRef.value) {
      isDragging.value = false
    }
  })
  document.addEventListener('dragend', () => {
    isDragging.value = false
    dragPreviewComponent.value = null
    dragOffset.value = { x: 0, y: 0 }
  })
  
  // 添加全局键盘事件监听，用于删除选中的组件
  document.addEventListener('keydown', handleGlobalKeyDown)
})

onUnmounted(() => {
  // 移除全局事件监听
  document.removeEventListener('dragover', handleDragOver)
  document.removeEventListener('resize', handleResize)
  document.removeEventListener('mouseup', handleResizeEnd)
  // 移除全局键盘事件监听
  document.removeEventListener('keydown', handleGlobalKeyDown)
})

// 处理全局键盘事件
const handleGlobalKeyDown = (event) => {
  // 确保是删除键且有选中的组件
  if (event.key === 'Delete' && selectedComponentId.value) {
    // 查找选中的组件
    const index = editorState.components.findIndex(c => c.id === selectedComponentId.value)
    if (index !== -1) {
      // 删除组件
      editorState.components.splice(index, 1)
      // 取消选中
      selectedComponentId.value = null
      emit('select-component', null)
    }
  }
}
</script>

<style scoped>
.canvas-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: #f5f7fa;
  padding: 20px;
}

.canvas {
  position: relative;
  width: 100%;
  min-height: 800px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  overflow: hidden;
}

/* 网格线 */
.grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

/* 拖拽预览虚影 */
.drag-preview {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0.8;
}

.preview-icon {
  font-size: 24px;
  color: #409eff;
  margin-bottom: 4px;
}

.preview-text {
  font-size: 12px;
  color: #409eff;
  font-weight: 500;
}

/* 组件容器 */
.components-container {
  position: relative;
  width: 100%;
  height: 100%;
}

/* 组件包装器 */
.component-wrapper {
  position: absolute;
  border: none;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.8);
  transition: all 0.2s ease;
  cursor: move;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  user-select: none;
  z-index: 10;
}

.component-wrapper:hover {
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.component-wrapper.selected {
  border: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  z-index: 100;
}

/* 组件内容 */
.component-content {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 组件类型标签已移除，改为直接渲染组件 */

/* 表单组件样式 */
.canvas-form-item {
  width: 100%;
  margin: 0 !important;
  padding: 0 8px;
  display: flex !important;
  flex-direction: row !important;
  align-items: center !important;
  gap: 8px;
}

/* 确保Element Plus的form-item内部元素也水平排列 */
.canvas-form-item :deep(.el-form-item__content) {
  margin-left: 0 !important;
  padding-left: 0 !important;
}

.form-container-placeholder {
  padding: 16px;
  color: #909399;
  text-align: center;
}

.canvas-form-container {
  width: 100%;
  height: 100%;
  padding: 16px;
  overflow: hidden;
}

/* 基础组件样式 */
.loading-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tooltip-trigger,
.popover-trigger {
  padding: 8px 16px;
  background: #f5f7fa;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.panel-content {
  padding: 16px 0;
  color: #606266;
}

/* 数据展示组件样式 */
.canvas-table {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.trend {
  margin-left: 8px;
  font-size: 12px;
}

.trend.up {
  color: #67c23a;
}

.trend.down {
  color: #f56c6c;
}

.description {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 布局组件样式 */
.canvas-layout,
.canvas-row,
.canvas-col,
.canvas-aside,
.canvas-flex,
.canvas-container-component,
.canvas-sidebar,
.canvas-header,
.canvas-footer,
.canvas-grid {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.layout-placeholder,
.row-placeholder,
.col-placeholder,
.aside-placeholder,
.flex-placeholder,
.container-placeholder,
.sidebar-placeholder {
  color: #909399;
  text-align: center;
  padding: 16px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  padding: 0 20px;
}

.logo {
  font-weight: bold;
  font-size: 18px;
  color: #409eff;
}

.title {
  font-size: 16px;
  color: #303133;
}

.footer-content {
  width: 100%;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.grid-item {
  background: #f0f9ff;
  border: none;
  border-radius: 4px;
  min-height: 50px;
}

/* 默认组件样式 */
.default-component {
  font-size: 14px;
  color: #606266;
  text-align: center;
  padding: 16px;
  background: rgba(245, 247, 250, 0.8);
  border-radius: 4px;
  border: none;
}

/* 组件操作手柄 */
.component-handles {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  pointer-events: none;
}

.handle {
  position: absolute;
  width: 8px;
  height: 8px;
  background-color: #409eff;
  border: 1px solid #ffffff;
  border-radius: 50%;
  cursor: pointer;
  pointer-events: auto;
  box-shadow: 0 0 2px rgba(0, 0, 0, 0.3);
}

/* 手柄位置 */
.handle.top-left {
  top: 0;
  left: 0;
  cursor: nw-resize;
}

.handle.top {
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  cursor: n-resize;
}

.handle.top-right {
  top: 0;
  right: 0;
  cursor: ne-resize;
}

.handle.left {
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  cursor: w-resize;
}

.handle.right {
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  cursor: e-resize;
}

.handle.bottom-left {
  bottom: 0;
  left: 0;
  cursor: sw-resize;
}

.handle.bottom {
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  cursor: s-resize;
}

.handle.bottom-right {
  bottom: 0;
  right: 0;
  cursor: se-resize;
}
</style>