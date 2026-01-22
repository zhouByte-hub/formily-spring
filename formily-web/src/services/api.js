import axios from 'axios'

// 创建axios实例
const api = axios.create({
  // Prefer same-origin `/api` (works in prod behind a gateway/proxy).
  // For local dev, configure Vite proxy to forward `/api` to the backend.
  baseURL: '/api',
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// Schema相关API
const schemaApi = {
  // 获取所有Schema
  getAllSchemas() {
    return api.get('/schema')
  },
  // 根据ID获取Schema
  getSchemaById(id) {
    return api.get(`/schema/${id}`)
  },
  // 创建Schema
  createSchema(schema) {
    // 新格式：{ "value": "" }
    return api.post('/schema', { value: schema })
  },
  // 更新Schema
  updateSchema(id, schema) {
    // 新格式：{ "id": "", "value": "" }
    return api.post('/schema', { id, value: schema })
  },
  // 删除Schema
  deleteSchema(id) {
    // Backend API: GET /api/schema/delete?id=SchemaID
    return api.get('/schema/delete', { params: { id } })
  },
  // 预览Schema（若后端提供预览接口可启用）
  // previewSchema(schema) {
  //   return api.post('/schema/preview', { value: schema })
  // }
}

// 导出API
export default {
  schema: schemaApi
}
