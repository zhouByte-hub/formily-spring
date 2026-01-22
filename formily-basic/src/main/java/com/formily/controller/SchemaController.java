package com.formily.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.formily.model.Schema;
import com.formily.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema控制器
 * 提供RESTful API接口，实现Schema的CRUD操作
 */
@RestController
@RequestMapping("/api/schema")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    /**
     * 获取所有Schema
     * @return Schema列表
     */
    @GetMapping
    public ResponseEntity<List<Schema>> getAllSchemas() {
        try {
            List<Schema> schemas = schemaService.getAllSchemas();
            return ResponseEntity.ok(schemas);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据ID获取Schema
     * @param id Schema ID
     * @return Schema对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<Schema> getSchemaById(@PathVariable String id) {
        try {
            Schema schema = schemaService.getSchemaById(id);
            if (schema == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(schema);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 保存Schema（与前端设计方案一致：POST /api/schema，body: {id?, value}）
     * - id为空：创建并返回生成的id
     * - id不为空：存在则更新，不存在则按指定id创建
     */
    @PostMapping
    public ResponseEntity<Schema> createSchema(@RequestBody Schema schema) {
        try {
            boolean isCreate = schema.getId() == null || schema.getId().trim().isEmpty();
            Schema saved = schemaService.saveSchema(schema);
            return ResponseEntity.status(isCreate ? HttpStatus.CREATED : HttpStatus.OK).body(saved);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除Schema（前端使用 DELETE /api/schema，body: {id}）
     */
    @DeleteMapping(value = "/delete")
    public ResponseEntity<Void> deleteSchema(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        boolean deleted = schemaService.deleteSchema(id.trim());
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * 预览Schema：后端根据Schema值生成预览效果（返回HTML片段）
     * 前端：POST /api/schema/preview, body: { value: ... }
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, String>> previewSchema(@RequestBody Map<String, JsonNode> body) {
        JsonNode value = body == null ? null : body.get("value");
        if (value == null || value.isNull()) {
            return ResponseEntity.badRequest().build();
        }
        Map<String, String> resp = new HashMap<>();
        resp.put("html", SchemaPreviewRenderer.render(value));
        return ResponseEntity.ok(resp);
    }
}
