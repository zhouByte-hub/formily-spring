<template>
  <div class="form-renderer">
    <h2>{{ formTitle }}</h2>
    <el-card class="form-card" shadow="hover">
      <form @submit.prevent="handleSubmit">
        <!-- 使用 Formily 渲染表单 -->
        <el-form :model="formData" :rules="validationRules" ref="formRef" label-width="100px">
          <div v-for="(property, key) in actualSchema.properties" :key="key" class="form-item">
            <!-- 文本输入框 -->
            <el-form-item
              v-if="property.type === 'string'"
              :label="property.title || key"
              :prop="key"
              :required="property.required"
            >
              <el-input
                v-model="formData[key]"
                :placeholder="property.description || `请输入${property.title || key}`"
                :maxlength="property.maxLength"
                :show-word-limit="!!property.maxLength"
                :type="property.format || 'text'"
              />
            </el-form-item>
            
            <!-- 数字输入框 -->
            <el-form-item
              v-else-if="property.type === 'number' || property.type === 'integer'"
              :label="property.title || key"
              :prop="key"
              :required="property.required"
            >
              <el-input-number
                v-model="formData[key]"
                :placeholder="property.description || `请输入${property.title || key}`"
                :min="property.minimum"
                :max="property.maximum"
                :step="property.multipleOf || 1"
              />
            </el-form-item>
            
            <!-- 布尔值选择框 -->
            <el-form-item
              v-else-if="property.type === 'boolean'"
              :label="property.title || key"
              :prop="key"
            >
              <el-switch v-model="formData[key]" />
            </el-form-item>
            
            <!-- 下拉选择框 -->
            <el-form-item
              v-else-if="property.type === 'string' && property.enum"
              :label="property.title || key"
              :prop="key"
              :required="property.required"
            >
              <el-select
                v-model="formData[key]"
                :placeholder="property.description || `请选择${property.title || key}`"
              >
                <el-option
                  v-for="option in property.enum"
                  :key="option"
                  :label="option"
                  :value="option"
                />
              </el-select>
            </el-form-item>
            
            <!-- 日期选择器 -->
            <el-form-item
              v-else-if="property.format === 'date'"
              :label="property.title || key"
              :prop="key"
              :required="property.required"
            >
              <el-date-picker
                v-model="formData[key]"
                type="date"
                :placeholder="property.description || `请选择${property.title || key}`"
                style="width: 100%"
              />
            </el-form-item>
            
            <!-- 日期时间选择器 -->
            <el-form-item
              v-else-if="property.format === 'datetime'"
              :label="property.title || key"
              :prop="key"
              :required="property.required"
            >
              <el-date-picker
                v-model="formData[key]"
                type="datetime"
                :placeholder="property.description || `请选择${property.title || key}`"
                style="width: 100%"
              />
            </el-form-item>
            
            <!-- 未支持的类型 -->
            <el-form-item
              v-else
              :label="property.title || key"
              :prop="key"
            >
              <div class="unsupported-type">
                暂不支持的类型: {{ property.type }}
              </div>
            </el-form-item>
          </div>
          
          <!-- 提交按钮 -->
          <div class="form-actions">
            <el-button type="primary" native-type="submit" :loading="submitting">
              <el-icon v-if="submitting"><Loading /></el-icon>
              {{ submitText }}
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </div>
        </el-form>
      </form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

// 组件属性
const props = defineProps({
  schema: {
    type: Object,
    default: () => ({
      type: 'object',
      properties: {}
    })
  },
  formTitle: {
    type: String,
    default: '动态表单'
  },
  submitText: {
    type: String,
    default: '提交'
  },
  // 新增：支持完整的Formily格式数据
  formilyData: {
    type: Object,
    default: null
  }
})

// 事件
const emit = defineEmits(['submit', 'reset'])

// 表单引用
const formRef = ref(null)

// 表单数据
const formData = ref({})

// 计算属性：获取实际的schema和initial_values
const actualSchema = computed(() => {
  // 如果提供了完整的formilyData，使用其中的schema
  if (props.formilyData && props.formilyData.schema) {
    return props.formilyData.schema
  }
  // 否则使用schema属性
  return props.schema
})

const actualInitialValues = computed(() => {
  // 如果提供了完整的formilyData，使用其中的initial_values
  if (props.formilyData && props.formilyData.initial_values) {
    return props.formilyData.initial_values
  }
  // 否则返回空对象
  return {}
})

// 初始化表单数据
const initFormData = () => {
  // 使用initial_values初始化表单数据
  formData.value = { ...actualInitialValues.value }
}

// 监听initial_values变化，更新表单数据
watch(
  () => actualInitialValues.value,
  (newValues) => {
    formData.value = { ...newValues }
  },
  { deep: true, immediate: true }
)

// 提交状态
const submitting = ref(false)

// 根据Schema生成验证规则
const validationRules = computed(() => {
  const rules = {}
  if (!actualSchema.value.properties) return rules
  
  for (const [key, property] of Object.entries(actualSchema.value.properties)) {
    rules[key] = []
    
    // 必填验证
    if (property.required) {
      rules[key].push({
        required: true,
        message: `请输入${property.title || key}`,
        trigger: 'blur'
      })
    }
    
    // 类型验证
    if (property.type === 'number' || property.type === 'integer') {
      rules[key].push({
        type: 'number',
        message: `${property.title || key}必须是数字`,
        trigger: 'blur'
      })
    }
    
    // 字符串长度验证
    if (property.type === 'string') {
      if (property.minLength) {
        rules[key].push({
          min: property.minLength,
          message: `${property.title || key}长度不能少于${property.minLength}个字符`,
          trigger: 'blur'
        })
      }
      if (property.maxLength) {
        rules[key].push({
          max: property.maxLength,
          message: `${property.title || key}长度不能超过${property.maxLength}个字符`,
          trigger: 'blur'
        })
      }
    }
    
    // 数值范围验证
    if (property.type === 'number' || property.type === 'integer') {
      if (property.minimum !== undefined) {
        rules[key].push({
          min: property.minimum,
          message: `${property.title || key}不能小于${property.minimum}`,
          trigger: 'blur'
        })
      }
      if (property.maximum !== undefined) {
        rules[key].push({
          max: property.maximum,
          message: `${property.title || key}不能大于${property.maximum}`,
          trigger: 'blur'
        })
      }
    }
  }
  
  return rules
})

// 重置表单
const resetForm = () => {
  formData.value = {}
  if (formRef.value) {
    formRef.value.resetFields()
  }
  emit('reset')
}

// 提交表单
const handleSubmit = () => {
  if (formRef.value) {
    formRef.value.validate((valid) => {
      if (valid) {
        submitting.value = true
        // 触发提交事件
        emit('submit', { ...formData.value })
        // 延迟重置，显示提交状态
        setTimeout(() => {
          submitting.value = false
          resetForm()
        }, 1000)
      } else {
        ElMessage.error('表单验证失败，请检查输入')
        return false
      }
    })
  }
}

// 暴露方法
defineExpose({
  resetForm,
  formData
})
</script>

<style scoped>
.form-renderer {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-renderer h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.form-card {
  margin-top: 20px;
}

.form-item {
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.unsupported-type {
  padding: 10px;
  background-color: #f56c6c;
  color: white;
  border-radius: 4px;
  text-align: center;
}
</style>