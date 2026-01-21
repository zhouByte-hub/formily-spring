<template>
  <div class="schema-list-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>Schema 管理</h2>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            新建 Schema
          </el-button>
        </div>
      </template>
      
      <!-- Schema 列表 -->
      <el-table
        v-loading="loading"
        :data="schemas"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
        <el-table-column prop="name" label="名称" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="success" size="small" @click="openPreviewDialog(scope.row)">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- Schema 表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="60%"
      :before-close="handleClose"
    >
      <SchemaForm
        ref="schemaFormRef"
        :schema="currentSchema"
        @submit="handleSubmit"
      />
    </el-dialog>
    
    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="Schema 预览"
      width="80%"
      :before-close="handlePreviewClose"
    >
      <div v-loading="previewLoading" class="preview-content">
        <div v-if="previewHtml" class="preview-html" v-html="previewHtml"></div>
        <el-empty v-else description="暂无预览内容" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import api from '../services/api.js'
import SchemaForm from '../components/SchemaForm.vue'

// 数据状态
const schemas = ref([])
const loading = ref(false)
const selectedSchemas = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('新建 Schema')
const currentSchema = ref({})
const schemaFormRef = ref(null)

// 预览对话框状态
const previewDialogVisible = ref(false)
const previewLoading = ref(false)
const previewHtml = ref('')
const previewSchema = ref(null)

// 获取Schema列表
const fetchSchemas = async () => {
  loading.value = true
  try {
    const response = await api.schema.getAllSchemas()
    schemas.value = response.data
    total.value = response.data.length
  } catch (error) {
    ElMessage.error('获取Schema列表失败')
    console.error('Error fetching schemas:', error)
  } finally {
    loading.value = false
  }
}

// 页面挂载时获取数据
onMounted(() => {
  fetchSchemas()
})

// 打开创建对话框
const openCreateDialog = () => {
  dialogTitle.value = '新建 Schema'
  currentSchema.value = {}
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (schema) => {
  dialogTitle.value = '编辑 Schema'
  currentSchema.value = { ...schema }
  dialogVisible.value = true
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  if (schemaFormRef.value) {
    schemaFormRef.value.resetForm()
  }
}

// 打开预览对话框
const openPreviewDialog = async (schema) => {
  previewDialogVisible.value = true
  previewLoading.value = true
  previewSchema.value = schema
  
  try {
    // 调用预览API
    const response = await api.schema.previewSchema(schema.content)
    // 后端返回预览效果
    previewHtml.value = response.data.html || ''
  } catch (error) {
    ElMessage.error('预览加载失败')
    console.error('Error previewing schema:', error)
    previewHtml.value = ''
  } finally {
    previewLoading.value = false
  }
}

// 关闭预览对话框
const handlePreviewClose = () => {
  previewDialogVisible.value = false
  previewHtml.value = ''
  previewSchema.value = null
}

// 提交表单
const handleSubmit = async (formData) => {
  try {
    if (formData.id) {
      // 更新Schema
      await api.schema.updateSchema(formData.id, formData)
      ElMessage.success('Schema更新成功')
    } else {
      // 创建Schema
      await api.schema.createSchema(formData)
      ElMessage.success('Schema创建成功')
    }
    // 关闭对话框并刷新数据
    dialogVisible.value = false
    fetchSchemas()
  } catch (error) {
    ElMessage.error(formData.id ? 'Schema更新失败' : 'Schema创建失败')
    console.error('Error submitting schema:', error)
  }
}

// 处理删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个Schema吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.schema.deleteSchema(id)
    ElMessage.success('Schema删除成功')
    fetchSchemas()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('Schema删除失败')
      console.error('Error deleting schema:', error)
    }
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedSchemas.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
}
</script>

<style scoped>
.schema-list-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.preview-content {
  min-height: 400px;
  padding: 20px;
}

.preview-html {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  background-color: #f5f7fa;
}
</style>