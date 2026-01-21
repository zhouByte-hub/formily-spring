package com.formily.controller;

import com.formily.model.Schema;
import com.formily.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

    /**
     * Schema控制器
 * 提供RESTful API接口，实现Schema的CRUD操作
 */
@RestController
@RequestMapping("/api/schemas")
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
     * 创建Schema
     * @param schema Schema对象
     * @return 创建后的Schema
     */
    @PostMapping
    public ResponseEntity<Schema> createSchema(@RequestBody Schema schema) {
        try {
            Schema createdSchema = schemaService.createSchema(schema);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchema);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更新Schema
     * @param id Schema ID
     * @param schema Schema对象
     * @return 更新后的Schema
     */
    @PutMapping("/{id}")
    public ResponseEntity<Schema> updateSchema(@PathVariable String id, @RequestBody Schema schema) {
        try {
            Schema updatedSchema = schemaService.updateSchema(id, schema);
            if (updatedSchema == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedSchema);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 删除Schema
     * @param id Schema ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchema(@PathVariable String id) {
        boolean deleted = schemaService.deleteSchema(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}