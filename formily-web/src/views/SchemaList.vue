<template>
  <div class="schema-list-container">
    <el-card class="schema-manage-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>Schema 管理</h2>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建 Schema
          </el-button>
        </div>
      </template>
      
      <!-- Schema 列表：每项固定 320x140，展示 Schema 效果 -->
      <div class="schema-cards" v-loading="loading">
        <el-empty v-if="!pagedSchemas.length && !loading" description="暂无 Schema" />
        <div v-else class="schema-grid">
          <el-card
            v-for="schema in pagedSchemas"
            :key="schema.id"
            class="schema-card"
            shadow="hover"
            :body-style="{ height: '100%', padding: '10px' }"
          >
            <div class="schema-card-actions">
              <el-button type="primary" size="small" link @click="handleEdit(schema)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="success" size="small" link @click="openPreviewDialog(schema)">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-button type="danger" size="small" link @click="handleDelete(schema.id)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>

            <div class="schema-card-preview" :title="schema.id">
              <FormilyPreview class="schema-card-preview-inner" :value="schema.value" />
            </div>
          </el-card>
        </div>
      </div>
      
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
    
    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="Schema 预览"
      width="60%"
      class="schema-preview-dialog"
      :before-close="handlePreviewClose"
    >
      <div class="preview-content">
        <FormilyPreview v-if="previewSchema" :value="previewSchema.value" />
        <el-empty v-else description="暂无预览内容" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import api from '../services/api.js'
import FormilyPreview from '../components/FormilyPreview.vue'

const router = useRouter()

const LOWCODE_STATE_KEY = 'lowcode-editor-state'
const LOWCODE_IMPORT_KEY = 'lowcode-editor-import'
const CURRENT_PROJECT_ID_KEY = 'current-project-id'

// 数据状态
const schemas = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const pagedSchemas = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return schemas.value.slice(start, start + pageSize.value)
})

// 预览对话框状态
const previewDialogVisible = ref(false)
const previewSchema = ref(null)

// 获取Schema列表
const fetchSchemas = async () => {
  loading.value = true
  try {
    const response = await api.schema.getAllSchemas()
    const list = Array.isArray(response.data) ? response.data : []
    schemas.value = list
    total.value = list.length
    const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
    if (currentPage.value > maxPage) currentPage.value = maxPage
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

const hasLowCodeEditorData = () => {
  const raw = localStorage.getItem(LOWCODE_STATE_KEY)
  if (!raw) return false
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed?.components) && parsed.components.length > 0
  } catch {
    return false
  }
}

const confirmOverwriteIfNeeded = async () => {
  if (!hasLowCodeEditorData()) return true
  try {
    await ElMessageBox.confirm(
      '低代码编辑器中已有内容，继续操作将覆盖原有数据，是否继续？',
      '提示',
      {
        confirmButtonText: '确认覆盖',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    return true
  } catch {
    return false
  }
}

const handleCreate = async () => {
  const ok = await confirmOverwriteIfNeeded()
  if (!ok) return

  localStorage.removeItem(LOWCODE_STATE_KEY)
  localStorage.removeItem(LOWCODE_IMPORT_KEY)
  localStorage.removeItem(CURRENT_PROJECT_ID_KEY)
  router.push('/')
}

const handleEdit = async (schema) => {
  const ok = await confirmOverwriteIfNeeded()
  if (!ok) return

  // Pass schema to the editor via localStorage, then redirect to LowCodeHome.
  localStorage.setItem(LOWCODE_IMPORT_KEY, JSON.stringify(schema))
  localStorage.setItem(CURRENT_PROJECT_ID_KEY, schema.id)
  router.push('/')
}

// 打开预览对话框
const openPreviewDialog = (schema) => {
  previewDialogVisible.value = true
  previewSchema.value = schema
}

// 关闭预览对话框
const handlePreviewClose = () => {
  previewDialogVisible.value = false
  previewSchema.value = null
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
  flex: 1;
  height: 100vh;
  min-height: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.schema-manage-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.schema-manage-card :deep(.el-card__body) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.schema-cards {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.schema-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 320px);
  gap: 16px;
  align-content: start;
}

.schema-card {
  width: 320px;
  height: 300px;
  position: relative;
  overflow: hidden;
}

.schema-card-actions {
  position: absolute;
  top: 6px;
  right: 8px;
  z-index: 2;
  display: flex;
  gap: 8px;
}

.schema-card-preview {
  height: 100%;
  overflow: hidden;
  padding-top: 18px; /* 给右上角操作按钮留空间 */
}

.schema-card-preview-inner {
  pointer-events: none;
}

/* Mini preview: compact + hide labels so users focus on "schema effect" */
.schema-card-preview :deep(.el-form-item__label) {
  display: none;
}

.schema-card-preview :deep(.el-form-item__content) {
  margin-left: 0 !important;
}

.schema-card-preview :deep(.el-form-item) {
  margin-bottom: 6px;
}

.schema-card-preview :deep(.formily-preview) {
  --preview-scale: 0.55;
  padding: 0;
  transform: scale(var(--preview-scale));
  transform-origin: top left;
  width: calc(100% / var(--preview-scale));
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.preview-content {
  min-height: 400px;
  height: 70vh;
  overflow: auto;
  padding: 20px;
  background-color: #f5f7fa;
}

:deep(.schema-preview-dialog.el-dialog) {
  /* Override global dialog styling that can clip absolute-positioned canvas nodes. */
  overflow: visible;
}

:deep(.schema-preview-dialog .el-dialog__body) {
  padding: 0;
}

:deep(.schema-preview-dialog .el-dialog__header) {
  padding: 10px;
  font-weight: bold;
}

.preview-content :deep(.formily-preview) {
  padding: 0;
}

.preview-content :deep(.formily-preview-canvas) {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
</style>
