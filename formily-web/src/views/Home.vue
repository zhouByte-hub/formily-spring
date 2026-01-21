<template>
  <div class="home-container">
    <el-card class="welcome-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>欢迎使用 Formily 动态表单系统</h2>
        </div>
      </template>
      <div class="welcome-content">
        <p>这是一个基于 Vue 和 Formily 构建的动态表单管理系统，支持表单 Schema 的创建、编辑和管理，以及基于 Schema 的动态表单渲染。</p>
        <div class="feature-grid">
          <el-card class="feature-card" shadow="hover">
            <template #header>
              <div class="feature-header">
                <el-icon><Setting /></el-icon>
                <h3>Schema 管理</h3>
              </div>
            </template>
            <p>创建、编辑和管理表单 Schema，定义表单的结构和验证规则。</p>
            <el-button type="primary" @click="$router.push('/schemas')">
              <el-icon><ArrowRight /></el-icon>
              进入 Schema 管理
            </el-button>
          </el-card>
          <el-card class="feature-card" shadow="hover">
            <template #header>
              <div class="feature-header">
                <el-icon><Document /></el-icon>
                <h3>表单管理</h3>
              </div>
            </template>
            <p>基于 Schema 创建表单，查看和管理表单数据。</p>
            <el-button type="primary" @click="$router.push('/forms')">
              <el-icon><ArrowRight /></el-icon>
              进入表单管理
            </el-button>
          </el-card>
        </div>
      </div>
    </el-card>
    <div class="stats-section">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ schemaCount }}</div>
          <div class="stat-label">已创建 Schema</div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ formCount }}</div>
          <div class="stat-label">已创建表单</div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-number">{{ submissionCount }}</div>
          <div class="stat-label">表单提交数</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Setting, Document, ArrowRight } from '@element-plus/icons-vue'

// 统计数据
const schemaCount = ref(0)
const formCount = ref(0)
const submissionCount = ref(0)
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-content {
  padding: 20px 0;
}

.welcome-content p {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
  margin-bottom: 30px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.feature-card {
  height: 100%;
}

.feature-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.feature-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.feature-card p {
  margin: 20px 0;
  color: #606266;
  line-height: 1.6;
}

.feature-card .el-button {
  margin-top: 10px;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 16px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>