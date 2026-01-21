package com.formily.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formily.model.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Schema服务类
 * 实现Schema的CRUD操作，使用文件系统存储Schema
 */
@Service
public class SchemaService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String schemaDir;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SchemaService() {
        // 使用绝对路径，确保schema目录存在
        String userDir = System.getProperty("user.dir");
        this.schemaDir = userDir + "/src/main/resources/schema";
        // 确保目录存在
        try {
            Files.createDirectories(Paths.get(schemaDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create schema directory", e);
        }
    }

    /**
     * 获取所有Schema
     * @return Schema列表
     * @throws IOException IO异常
     */
    public List<Schema> getAllSchemas() throws IOException {
        List<Schema> schemas = new ArrayList<>();
        File[] files = new File(schemaDir).listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                Schema schema = readSchemaFromFile(file);
                schemas.add(schema);
            }
        }
        return schemas;
    }

    /**
     * 根据ID获取Schema
     * @param id Schema ID
     * @return Schema对象
     * @throws IOException IO异常
     */
    public Schema getSchemaById(String id) throws IOException {
        File file = new File(schemaDir, id + ".json");
        if (!file.exists()) {
            return null;
        }
        return readSchemaFromFile(file);
    }

    /**
     * 创建Schema
     * @param schema Schema对象
     * @return 创建后的Schema
     * @throws IOException IO异常
     */
    public Schema createSchema(Schema schema) throws IOException {
        // 生成唯一ID
        String id = UUID.randomUUID().toString().replace("-", "");
        schema.setId(id);
        // 写入文件
        writeSchemaToFile(schema);
        return schema;
    }

    /**
     * 更新Schema
     * @param id Schema ID
     * @param schema Schema对象
     * @return 更新后的Schema
     * @throws IOException IO异常
     */
    public Schema updateSchema(String id, Schema schema) throws IOException {
        File file = new File(schemaDir, id + ".json");
        if (!file.exists()) {
            return null;
        }
        // 保留原有ID和创建时间
        Schema existingSchema = readSchemaFromFile(file);
        schema.setId(id);
        // 写入文件
        writeSchemaToFile(schema);
        return schema;
    }

    /**
     * 删除Schema
     * @param id Schema ID
     * @return 是否删除成功
     */
    public boolean deleteSchema(String id) {
        File file = new File(schemaDir, id + ".json");
        return file.delete();
    }

    /**
     * 从文件读取Schema
     * @param file 文件对象
     * @return Schema对象
     * @throws IOException IO异常
     */
    private Schema readSchemaFromFile(File file) throws IOException {
        return objectMapper.readValue(file, Schema.class);
    }

    /**
     * 将Schema写入文件
     * @param schema Schema对象
     * @throws IOException IO异常
     */
    private void writeSchemaToFile(Schema schema) throws IOException {
        File file = new File(schemaDir, schema.getId() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            objectMapper.writeValue(writer, schema.getValue());
        }
    }
}