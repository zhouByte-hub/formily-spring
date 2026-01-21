<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-width="100px"
    class="schema-form"
  >
    <!-- Schema 基本信息 -->
    <el-card class="form-section" shadow="never">
      <template #header>
        <h3>基本信息</h3>
      </template>
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入Schema名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          placeholder="请输入Schema描述"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-card>
    
    <!-- Schema 内容 -->
    <el-card class="form-section" shadow="never">
      <template #header>
        <h3>Schema 内容</h3>
      </template>
      <el-form-item label="JSON" prop="content">
        <el-input
          v-model="formData.content"
          placeholder="请输入JSON格式的Schema内容"
          type="textarea"
          :rows="15"
          monaco-editor
          spellcheck="false"
          @input="handleJsonInput"
        />
        <div class="json-help">
          <p>示例 Schema：</p>
          <pre class="json-example">
{
  "type": "object",
  "properties": {
    "name": {
      "type": "string",
      "title": "姓名",
      "required": true
    },
    "age": {
      "type": "number",
      "title": "年龄",
      "required": true
    }
  }
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
const emit = defineEmits(['submit'])

// 表单引用
const formRef = ref(null)

// 表单数据
const formData = ref({
  name: '',
  description: '',
  content: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入Schema名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述长度不能超过 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入Schema内容', trigger: 'blur' },
    { validator: validateJson, trigger: 'blur' }
  ]
}

// 监听props变化，更新表单数据
watch(
  () => props.schema,
  (newSchema) => {
    if (newSchema) {
      formData.value = {
        id: newSchema.id || '',
        name: newSchema.name || '',
        description: newSchema.description || '',
        content: JSON.stringify(newSchema.content || {}, null, 2)
      }
    } else {
      resetForm()
    }
  },
  { immediate: true, deep: true }
)

// 页面挂载时初始化
onMounted(() => {
  if (props.schema) {
    formData.value = {
      id: props.schema.id || '',
      name: props.schema.name || '',
      description: props.schema.description || '',
      content: JSON.stringify(props.schema.content || {}, null, 2)
    }
  }
})

// JSON验证
const validateJson = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入JSON内容'))
    return
  }
  try {
    JSON.parse(value)
    callback()
  } catch (error) {
    callback(new Error('请输入有效的JSON格式'))
  }
}

// 处理JSON输入
const handleJsonInput = (value) => {
  try {
    JSON.parse(value)
    formRef.value.clearValidate('content')
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
    name: '',
    description: '',
    content: ''
  }
  emit('reset')
}

// 提交表单
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      try {
        // 解析JSON内容
        const content = JSON.parse(formData.value.content)
        // 准备提交数据
        const submitData = {
          ...formData.value,
          content
        }
        // 触发提交事件
        emit('submit', submitData)
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