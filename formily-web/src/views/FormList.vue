<template>
  <div class="form-list-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>表单管理</h2>
        </div>
      </template>
      
      <!-- Schema 选择 -->
      <div class="schema-select-section">
        <el-card class="schema-select-card" shadow="hover">
          <template #header>
            <h3>选择表单 Schema</h3>
          </template>
          <el-form :model="selectForm" label-width="100px">
            <el-form-item label="Schema">
              <el-select
                v-model="selectForm.schemaId"
                placeholder="请选择一个 Schema 来创建表单"
                style="width: 100%"
                @change="handleSchemaChange"
              >
                <el-option
                  v-for="schema in schemas"
                  :key="schema.id"
                  :label="schema.name"
                  :value="schema.id"
                >
                  <div class="option-content">
                    <div class="option-name">{{ schema.name }}</div>
                    <div class="option-desc">{{ schema.description }}</div>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="表单标题">
              <el-input
                v-model="selectForm.formTitle"
                placeholder="请输入表单标题"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </el-card>
      </div>
      
      <!-- 动态表单渲染 -->
      <div v-if="selectedSchema" class="form-render-section">
        <FormRenderer
          :formily-data="selectedSchema.content"
          :form-title="selectForm.formTitle || selectedSchema.name"
          @submit="handleFormSubmit"
        />
      </div>
      <div v-else class="no-schema-selected">
        <el-empty description="请选择一个 Schema 来创建表单" />
      </div>
      
      <!-- 表单提交历史 -->
      <el-card v-if="formSubmissions.length > 0" class="submission-card" shadow="hover">
        <template #header>
          <h3>表单提交历史</h3>
        </template>
        <el-table
          :data="formSubmissions"
          style="width: 100%"
          border
        >
          <el-table-column prop="timestamp" label="提交时间" width="180" />
          <el-table-column prop="data" label="提交数据" show-overflow-tooltip>
            <template #default="scope">
              <pre class="submission-data">{{ JSON.stringify(scope.row.data, null, 2) }}</pre>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import api from '../services/api.js'
import FormRenderer from '../components/FormRenderer.vue'

// Schema列表
const schemas = ref([])

// Schema选择表单
const selectForm = ref({
  schemaId: '',
  formTitle: ''
})

// 选中的Schema
const selectedSchema = ref(null)

// 表单提交历史
const formSubmissions = ref([])

// 获取所有Schema
const fetchSchemas = async () => {
  try {
    const response = await api.schema.getAllSchemas()
    schemas.value = response.data
  } catch (error) {
    ElMessage.error('获取Schema列表失败')
    console.error('Error fetching schemas:', error)
  }
}

// 页面挂载时获取数据
onMounted(() => {
  fetchSchemas()
})

// Schema选择变化处理
const handleSchemaChange = async (schemaId) => {
  if (!schemaId) {
    selectedSchema.value = null
    return
  }
  
  try {
    const response = await api.schema.getSchemaById(schemaId)
    selectedSchema.value = response.data
    // 如果没有输入表单标题，自动使用Schema名称
    if (!selectForm.value.formTitle) {
      selectForm.value.formTitle = selectedSchema.value.name
    }
  } catch (error) {
    ElMessage.error('获取Schema详情失败')
    console.error('Error fetching schema:', error)
    selectedSchema.value = null
  }
}

// 表单提交处理
const handleFormSubmit = (formData) => {
  // 添加提交时间
  const submission = {
    timestamp: new Date().toLocaleString(),
    data: formData
  }
  
  // 添加到提交历史
  formSubmissions.value.unshift(submission)
  
  // 显示成功通知
  ElNotification({
    title: '提交成功',
    message: '表单数据已提交',
    type: 'success',
    duration: 3000
  })
  
  // 记录日志
  console.log('表单提交数据:', formData)
}
</script>

<style scoped>
.form-list-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.schema-select-section {
  margin-bottom: 20px;
}

.schema-select-card {
  margin-bottom: 20px;
}

.schema-select-card :deep(.el-card__header) {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.schema-select-card :deep(.el-card__header) h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.schema-select-card :deep(.el-card__body) {
  padding: 20px;
}

.option-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.option-name {
  font-weight: 600;
  color: #303133;
}

.option-desc {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.form-render-section {
  margin: 20px 0;
}

.no-schema-selected {
  padding: 40px 0;
  text-align: center;
}

.submission-card {
  margin-top: 30px;
}

.submission-card :deep(.el-card__header) {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.submission-card :deep(.el-card__header) h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.submission-card :deep(.el-card__body) {
  padding: 20px;
}

.submission-data {
  margin: 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  overflow-x: auto;
  max-height: 150px;
  overflow-y: auto;
}
</style>