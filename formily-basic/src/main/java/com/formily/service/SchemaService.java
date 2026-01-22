package com.formily.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.formily.model.Schema;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;

/**
 * Schema服务类
 * 实现Schema的CRUD操作，使用文件系统存储Schema
 */
@Service
public class SchemaService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new SecureRandom();
    private final String schemaDir;
    private final String dataDir;

    public SchemaService() {
        this.schemaDir = resolveResourceDir("schema");
        this.dataDir = resolveResourceDir("data");
        // Ensure directories exist (we're persisting to filesystem, not classpath).
        try {
            Files.createDirectories(Paths.get(schemaDir));
            Files.createDirectories(Paths.get(dataDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    private static String resolveResourceDir(String subdir) {
        // Default to module-local path; fall back to repo-root layout when running from workspace root.
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path moduleLocal = cwd.resolve("src/main/resources").resolve(subdir);
        Path repoRootLayout = cwd.resolve("formily-basic/src/main/resources").resolve(subdir);
        Path resolved = Files.exists(moduleLocal.getParent()) ? moduleLocal : repoRootLayout;
        return resolved.toAbsolutePath().toString();
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
                attachInitialValuesFromData(schema);
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
        Schema schema = readSchemaFromFile(file);
        attachInitialValuesFromData(schema);
        return schema;
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
        ensureInitialValuesPopulated(schema);
        persistInitialValuesToData(schema);
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
        schema.setId(id);
        persistInitialValuesToData(schema);
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
        boolean deleted = file.delete();
        // Best-effort: keep schema/data 1:1 mapping.
        deleteSchemaData(id);
        return deleted;
    }

    /**
     * 从文件读取Schema
     * @param file 文件对象
     * @return Schema对象
     * @throws IOException IO异常
     */
    private Schema readSchemaFromFile(File file) throws IOException {
        // New format: {"id":"...","value":{...}}
        try {
            Schema schema = objectMapper.readValue(file, Schema.class);
            ensureDataFileForBackwardCompat(schema);
            return schema;
        } catch (MismatchedInputException ignored) {
            // Backward compatibility: historical files stored only the value JSON.
            JsonNode value = objectMapper.readTree(file);
            Schema schema = new Schema();
            String filename = file.getName();
            schema.setId(filename.endsWith(".json") ? filename.substring(0, filename.length() - 5) : filename);
            schema.setValue(value);
            ensureDataFileForBackwardCompat(schema);
            return schema;
        }
    }

    /**
     * 将Schema写入文件
     * @param schema Schema对象
     * @throws IOException IO异常
     */
    private void writeSchemaToFile(Schema schema) throws IOException {
        File file = new File(schemaDir, schema.getId() + ".json");
        Schema toWrite = new Schema();
        toWrite.setId(schema.getId());
        toWrite.setValue(stripInitialValues(schema.getValue()));
        try (FileWriter writer = new FileWriter(file)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, toWrite);
        }
    }

    /**
     * Upsert：如果提供了id则尝试更新，不存在则按指定id创建；未提供id则生成id创建。
     */
    public Schema saveSchema(Schema schema) throws IOException {
        if (schema.getId() == null || schema.getId().trim().isEmpty()) {
            return createSchema(schema);
        }
        File file = new File(schemaDir, schema.getId() + ".json");
        if (file.exists()) {
            return updateSchema(schema.getId(), schema);
        }
        // Create with client-provided id
        ensureInitialValuesPopulated(schema);
        persistInitialValuesToData(schema);
        writeSchemaToFile(schema);
        return schema;
    }

    public JsonNode getSchemaData(String id) throws IOException {
        File file = new File(dataDir, id + ".json");
        if (!file.exists()) return null;
        return objectMapper.readTree(file);
    }

    public void saveSchemaData(String id, JsonNode data) throws IOException {
        File file = new File(dataDir, id + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, data);
        }
    }

    public boolean deleteSchemaData(String id) {
        File file = new File(dataDir, id + ".json");
        return file.delete();
    }

    private void persistInitialValuesToData(Schema schema) throws IOException {
        if (schema == null || schema.getId() == null || schema.getId().trim().isEmpty()) return;
        JsonNode value = schema.getValue();
        if (value == null || !value.isObject()) return;
        JsonNode initialValues = value.get("initial_values");
        if (initialValues == null || initialValues.isNull()) return;
        saveSchemaData(schema.getId(), initialValues);
    }

    private void ensureInitialValuesPopulated(Schema schema) {
        if (schema == null) return;
        JsonNode value = schema.getValue();
        if (value == null || !value.isObject()) return;
        ObjectNode valueObj = (ObjectNode) value;

        ObjectNode initialObj;
        JsonNode existing = valueObj.get("initial_values");
        if (existing != null && existing.isObject()) {
            initialObj = (ObjectNode) existing;
        } else {
            initialObj = objectMapper.createObjectNode();
            valueObj.set("initial_values", initialObj);
        }

        JsonNode schemaNode =
                (valueObj.hasNonNull("schema") && valueObj.get("schema").isObject())
                        ? valueObj.get("schema")
                        : valueObj;

        // Fill missing/blank fields; keep any client-provided non-empty values.
        collectInitialValues(initialObj, schemaNode);
    }

    private void collectInitialValues(ObjectNode out, JsonNode schemaNode) {
        if (schemaNode == null || schemaNode.isNull()) return;

        // Formily schema commonly uses { properties: { ... } } as the container.
        if (schemaNode.has("properties") && schemaNode.get("properties").isObject()) {
            schemaNode.get("properties").fields()
                    .forEachRemaining(e -> collectField(out, e.getKey(), e.getValue()));
        }
    }

    private void collectField(ObjectNode out, String key, JsonNode field) {
        if (field == null || field.isNull()) return;

        String xComponent = field.hasNonNull("x-component") ? field.get("x-component").asText() : "";

        // Container nodes (Card/Tabs/...) usually carry child properties.
        if (field.has("properties") && field.get("properties").isObject()) {
            // Tabs often needs a current value (active pane).
            if ("Tabs".equals(xComponent)) {
                if (shouldFill(out.get(key))) {
                    out.put(key, randomTabsValue(field));
                }
            }
            field.get("properties").fields()
                    .forEachRemaining(e -> collectField(out, e.getKey(), e.getValue()));
            return;
        }

        // Leaf fields: generate value based on enum/type/component.
        if (!shouldFill(out.get(key))) {
            return;
        }

        JsonNode enumNode = field.get("enum");
        if (enumNode != null && enumNode.isArray() && enumNode.size() > 0) {
            JsonNode picked = enumNode.get(random.nextInt(enumNode.size()));
            JsonNode v = picked;
            if (picked.isObject() && picked.hasNonNull("value")) {
                v = picked.get("value");
            }
            if (v.isBoolean()) out.put(key, v.asBoolean());
            else if (v.isInt() || v.isLong()) out.put(key, v.asLong());
            else if (v.isNumber()) out.put(key, v.asDouble());
            else out.put(key, v.asText());
            return;
        }

        String type = field.hasNonNull("type") ? field.get("type").asText() : "";
        if ("boolean".equals(type) || "Switch".equals(xComponent)) {
            out.put(key, random.nextBoolean());
            return;
        }
        if ("number".equals(type) || "integer".equals(type) || "NumberPicker".equals(xComponent)) {
            out.put(key, 1 + random.nextInt(100));
            return;
        }
        if ("DateTimePicker".equals(xComponent)) {
            // ISO-8601 string is the safest default.
            long now = System.currentTimeMillis();
            long delta = (long) random.nextInt(30) * 24 * 60 * 60 * 1000L;
            out.put(key, new Date(now + delta).toInstant().toString());
            return;
        }

        // Default: string-ish value.
        out.put(key, key + "_" + randomAlphaNum(6));
    }

    private boolean shouldFill(JsonNode current) {
        if (current == null || current.isNull()) return true;
        if (current.isTextual()) return current.asText().trim().isEmpty();
        return false;
    }

    private String randomTabsValue(JsonNode field) {
        JsonNode props = field.hasNonNull("x-component-props") ? field.get("x-component-props") : null;
        JsonNode panes = props != null ? props.get("panes") : null;
        if (panes != null && panes.isArray() && panes.size() > 0) {
            JsonNode picked = panes.get(random.nextInt(panes.size()));
            if (picked.isObject() && picked.hasNonNull("name")) {
                return picked.get("name").asText();
            }
        }
        return "";
    }

    private String randomAlphaNum(int len) {
        final String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void attachInitialValuesFromData(Schema schema) throws IOException {
        if (schema == null || schema.getId() == null || schema.getId().trim().isEmpty()) return;
        JsonNode value = schema.getValue();
        if (value != null && value.isObject()) {
            JsonNode data = getSchemaData(schema.getId());
            // Keep response shape stable for the frontend: always provide initial_values.
            ((ObjectNode) value).set("initial_values", data == null ? objectMapper.createObjectNode() : data);
        }
    }

    private void ensureDataFileForBackwardCompat(Schema schema) throws IOException {
        if (schema == null || schema.getId() == null || schema.getId().trim().isEmpty()) return;
        File file = new File(dataDir, schema.getId() + ".json");
        if (file.exists()) return;
        JsonNode value = schema.getValue();
        if (value == null || !value.isObject()) return;
        JsonNode initialValues = value.get("initial_values");
        if (initialValues == null || initialValues.isNull()) return;
        saveSchemaData(schema.getId(), initialValues);
    }

    private JsonNode stripInitialValues(JsonNode value) {
        if (value != null && value.isObject()) {
            ObjectNode copy = ((ObjectNode) value).deepCopy();
            copy.remove("initial_values");
            return copy;
        }
        return value;
    }
}
