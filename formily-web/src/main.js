import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'

// 引入Formily (使用正确的API)
import { createSchemaField, createForm } from '@formily/vue'

const app = createApp(App)

// 使用插件
app.use(ElementPlus)
app.use(router)

// 将Formily API挂载到全局
app.config.globalProperties.$formily = {
  createSchemaField,
  createForm
}

app.mount('#app')