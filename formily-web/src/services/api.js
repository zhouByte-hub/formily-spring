import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API基础URL
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
    // 新格式：{ "id": "" }
    return api.delete('/schema', { data: { id } })
  },
  // 预览Schema
  previewSchema(schema) {
    // 预览功能：后端根据Schema生成预览效果
    return api.post('/schema/preview', { value: schema })
  }
}

// 导出API
export default {
  schema: schemaApi
}