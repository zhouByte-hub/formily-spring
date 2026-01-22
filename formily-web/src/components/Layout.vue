<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-content">
        <h1 class="logo">Formily 动态表单案例</h1>
        
        <!-- 顶部菜单 -->
        <el-menu
          :default-active="activeMenu"
          class="top-menu"
          mode="horizontal"
          router
        >
          <el-menu-item index="/">
            <template #icon>
              <el-icon><HomeFilled /></el-icon>
            </template>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/schemas">
            <template #icon>
              <el-icon><Setting /></el-icon>
            </template>
            <span>Schema管理</span>
          </el-menu-item>
        </el-menu>
        
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar size="small" :src="userAvatar"></el-avatar>
              <span>管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <!-- 主体内容区 -->
    <main class="app-main">
      <!-- 主内容区 -->
      <section class="main-content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, Setting } from '@element-plus/icons-vue'

// 用户头像
// 避免依赖外网资源（网络受限/离线场景）
const userAvatar = ref('')

// 获取当前路由
const route = useRoute()

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.path || '/'
})
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 顶部导航栏 */
.app-header {
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  transition: all 0.3s ease;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 32px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  gap: 24px;
}

.logo {
  font-size: 22px;
  font-weight: 600;
  color: #409eff;
  margin: 0;
  letter-spacing: 0.5px;
  transition: color 0.3s ease;
  white-space: nowrap;
}

.logo:hover {
  color: #66b1ff;
}

/* 顶部菜单 */
.top-menu {
  flex: 1;
  border: none;
  background-color: transparent;
  box-shadow: none;
  height: 100%;
  align-items: center;
}

.top-menu .el-menu-item {
  border-radius: 6px;
  margin: 0 8px;
  height: 100%;
  line-height: 64px;
  transition: all 0.3s ease;
  background-color: transparent !important;
  border-bottom: 3px solid transparent;
}

.el-menu--horizontal.el-menu {
    border-bottom: 0px !important;
}

.top-menu .el-menu-item:hover,
.top-menu .el-menu-item.is-active {
  background-color: #d6ebff !important;
  height: 40px;
  border-bottom: 0px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  white-space: nowrap;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background-color: #f0f9ff;
}

/* 主体内容区 */
.app-main {
  flex: 1;
  margin-top: 84px;
  padding: 0 20px;
  max-width: 1400px;
  width: 100%;
  margin-left: auto;
  margin-right: auto;
}

/* 主内容区 */
.main-content {
  width: 100%;
  background-color: #f5f7fa;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  padding: 0;
  min-height: calc(100vh - 124px);
  height: 100%;
  transition: all 0.3s ease;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-content {
    padding: 0 24px;
  }
  
  .app-main {
    padding: 0 16px;
  }
  
  .main-content {
    padding: 24px;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    gap: 16px;
  }
  
  .app-main {
    margin-top: 74px;
  }
  
  .main-content {
    padding: 16px;
    min-height: calc(100vh - 114px);
  }
  
  .logo {
    font-size: 18px;
  }
  
  .top-menu {
    overflow-x: auto;
  }
  
  .top-menu .el-menu-item {
    margin: 0 4px;
    padding: 0 12px;
  }
  
  .user-info span {
    display: none;
  }
}
</style>
