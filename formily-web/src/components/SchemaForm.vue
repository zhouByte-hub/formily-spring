<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-width="100px"
    class="schema-form"
  >
    <!-- Schema 内容（与设计方案一致：{ id?, value }，其中 value 包含 initial_values / datesources / schema） -->
    <el-card class="form-section" shadow="never">
      <template #header>
        <h3>Schema Value</h3>
      </template>
      <el-form-item v-if="formData.id" label="ID">
        <el-input v-model="formData.id" disabled />
      </el-form-item>
      <el-form-item label="JSON" prop="valueText">
        <el-input
          v-model="formData.valueText"
          placeholder="请输入JSON格式的 value（包含 initial_values / datesources / schema）"
          type="textarea"
          :rows="15"
          spellcheck="false"
          @input="handleJsonInput"
        />
        <div class="json-help">
          <p>示例 Schema：</p>
          <pre class="json-example">
{
  "initial_values": {},
  "datesources": {},
  "schema": { "type": "object", "properties": {} }
}
          </pre>
        </div>
      </el-form-item>
    </el-card>
    
    <!-- 操作按钮 -->
    <div class="form-actions">
      <el-button @click="resetForm">重置</el-button>
      <el-button type="primary" @click="submitForm">提交</el-button>
    </div>
  </el-form>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 组件属性
const props = defineProps({
  schema: {
    type: Object,
    default: () => ({})
  }
})

// 事件
const emit = defineEmits(['submit', 'reset'])

// 表单引用
const formRef = ref(null)

// 表单数据
const formData = ref({
  id: '',
  valueText: ''
})

const defaultValue = () => ({ initial_values: {}, datesources: {}, schema: { type: 'object', properties: {} } })

// 表单验证规则
const rules = {
  valueText: [
    { required: true, message: '请输入value内容', trigger: 'blur' },
    { validator: validateJson, trigger: 'blur' }
  ]
}

// 监听props变化，更新表单数据
watch(
  () => props.schema,
  (newSchema) => {
    if (newSchema) {
      const value = newSchema.value && Object.keys(newSchema.value).length ? newSchema.value : defaultValue()
      formData.value = { id: newSchema.id || '', valueText: JSON.stringify(value, null, 2) }
    } else {
      resetForm()
    }
  },
  { immediate: true, deep: true }
)

// 页面挂载时初始化
onMounted(() => {
  if (props.schema) {
    const value = props.schema.value && Object.keys(props.schema.value).length ? props.schema.value : defaultValue()
    formData.value = { id: props.schema.id || '', valueText: JSON.stringify(value, null, 2) }
  }
})

// JSON验证
const validateJson = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入JSON内容'))
    return
  }
  try {
    const parsed = JSON.parse(value)
    if (!parsed || typeof parsed !== 'object') {
      callback(new Error('value必须是JSON对象'))
      return
    }
    if (!Object.prototype.hasOwnProperty.call(parsed, 'initial_values') ||
        !Object.prototype.hasOwnProperty.call(parsed, 'datesources') ||
        !Object.prototype.hasOwnProperty.call(parsed, 'schema')) {
      callback(new Error('value必须包含 initial_values / datesources / schema'))
      return
    }
    callback()
  } catch (error) {
    callback(new Error('请输入有效的JSON格式'))
  }
}

// 处理JSON输入
const handleJsonInput = (value) => {
  try {
    JSON.parse(value)
    formRef.value?.clearValidate('valueText')
  } catch (error) {
    // 保持验证错误状态
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.value = {
    id: '',
    valueText: JSON.stringify(defaultValue(), null, 2)
  }
  emit('reset')
}

// 提交表单
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      try {
        const value = JSON.parse(formData.value.valueText)
        emit('submit', { id: formData.value.id || '', value })
      } catch (error) {
        ElMessage.error('JSON格式错误，请检查后重试')
      }
    } else {
      return false
    }
  })
}

// 暴露方法
defineExpose({
  resetForm,
  submitForm
})
</script>

<style scoped>
.schema-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-section {
  margin-bottom: 20px;
}

.form-section :deep(.el-card__header) {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.form-section :deep(.el-card__header) h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-section :deep(.el-card__body) {
  padding: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.json-help {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.json-help p {
  margin: 0 0 10px 0;
  font-weight: 600;
  color: #303133;
}

.json-example {
  margin: 0;
  padding: 10px;
  background-color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
