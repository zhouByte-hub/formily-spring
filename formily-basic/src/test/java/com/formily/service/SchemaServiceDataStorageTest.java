package com.formily.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.formily.model.Schema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SchemaServiceDataStorageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String previousUserDir;

    private SchemaService newServiceAt(Path baseDir) throws IOException {
        // SchemaService resolves its storage path from user.dir; in tests we point it at a temp workspace.
        previousUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", baseDir.toString());
        Files.createDirectories(baseDir.resolve("src/main/resources"));
        return new SchemaService();
    }

    @AfterEach
    void restoreUserDir() {
        if (previousUserDir != null) {
            System.setProperty("user.dir", previousUserDir);
        }
    }

    @Test
    void saveSchema_persistsInitialValuesToResourcesData_andStripsFromSchemaFile(@TempDir Path tmp) throws Exception {
        SchemaService service = newServiceAt(tmp);

        ObjectNode initial = objectMapper.createObjectNode();
        initial.put("name", "alice");
        initial.put("age", 18);

        ObjectNode value = objectMapper.createObjectNode();
        value.set("initial_values", initial);
        value.set("schema", objectMapper.createObjectNode().put("type", "object"));

        Schema schema = new Schema();
        schema.setId("abc");
        schema.setValue(value);

        service.saveSchema(schema);

        Path schemaFile = tmp.resolve("src/main/resources/schema/abc.json");
        assertTrue(Files.exists(schemaFile), "schema file should be written");

        JsonNode schemaJson = objectMapper.readTree(schemaFile.toFile());
        assertFalse(schemaJson.get("value").has("initial_values"), "schema file should not contain initial_values");

        Path dataFile = tmp.resolve("src/main/resources/data/abc.json");
        assertTrue(Files.exists(dataFile), "data file should be written");
        JsonNode dataJson = objectMapper.readTree(dataFile.toFile());
        assertEquals(initial, dataJson);

        Schema read = service.getSchemaById("abc");
        assertNotNull(read);
        assertNotNull(read.getValue());
        assertTrue(read.getValue().has("initial_values"), "GET should re-attach initial_values from data file");
        assertEquals(initial, read.getValue().get("initial_values"));
    }

    @Test
    void getSchemaById_migratesInitialValuesFromLegacySchemaFileIntoDataFile(@TempDir Path tmp) throws Exception {
        SchemaService service = newServiceAt(tmp);

        ObjectNode initial = objectMapper.createObjectNode();
        initial.put("k", "v");

        ObjectNode value = objectMapper.createObjectNode();
        value.set("initial_values", initial);
        value.set("schema", objectMapper.createObjectNode().put("type", "object"));

        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.put("id", "legacy");
        wrapper.set("value", value);

        Path schemaFile = tmp.resolve("src/main/resources/schema/legacy.json");
        Files.createDirectories(schemaFile.getParent());
        Files.writeString(schemaFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(wrapper));

        // No data file yet.
        Path dataFile = tmp.resolve("src/main/resources/data/legacy.json");
        assertFalse(Files.exists(dataFile));

        Schema read = service.getSchemaById("legacy");
        assertNotNull(read);

        assertTrue(Files.exists(dataFile), "legacy initial_values should be migrated to data file");
        JsonNode dataJson = objectMapper.readTree(dataFile.toFile());
        assertEquals(initial, dataJson);
    }

    @Test
    void saveSchema_generatesRandomInitialValuesWhenAbsent(@TempDir Path tmp) throws Exception {
        SchemaService service = newServiceAt(tmp);

        ObjectNode schemaNode = objectMapper.createObjectNode();
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("name", objectMapper.createObjectNode().put("type", "string").put("x-component", "Input"));
        properties.set("age", objectMapper.createObjectNode().put("type", "number").put("x-component", "NumberPicker"));
        properties.set("enabled", objectMapper.createObjectNode().put("type", "boolean").put("x-component", "Switch"));
        ObjectNode select = objectMapper.createObjectNode().put("type", "string").put("x-component", "Select");
        select.set("enum", objectMapper.readTree("[{\"label\":\"A\",\"value\":\"a\"},{\"label\":\"B\",\"value\":\"b\"}]"));
        properties.set("level", select);
        schemaNode.set("properties", properties);

        ObjectNode value = objectMapper.createObjectNode();
        value.set("schema", schemaNode);

        Schema schema = new Schema();
        schema.setId("gen");
        schema.setValue(value);

        service.saveSchema(schema);

        Path dataFile = tmp.resolve("src/main/resources/data/gen.json");
        assertTrue(Files.exists(dataFile), "data file should be created for generated initial_values");
        JsonNode data = objectMapper.readTree(dataFile.toFile());

        assertTrue(data.hasNonNull("name"));
        assertTrue(data.hasNonNull("age"));
        assertTrue(data.hasNonNull("enabled"));
        assertTrue(data.hasNonNull("level"));
        assertTrue(data.get("level").asText().equals("a") || data.get("level").asText().equals("b"));

        Schema read = service.getSchemaById("gen");
        assertNotNull(read);
        assertNotNull(read.getValue());
        assertTrue(read.getValue().has("initial_values"));
        assertTrue(read.getValue().get("initial_values").has("name"));
    }

    @Test
    void createSchema_fillsBlankInitialValues(@TempDir Path tmp) throws Exception {
        SchemaService service = newServiceAt(tmp);

        ObjectNode schemaNode = objectMapper.createObjectNode();
        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("name", objectMapper.createObjectNode().put("type", "string").put("x-component", "Input"));
        properties.set("age", objectMapper.createObjectNode().put("type", "number").put("x-component", "NumberPicker"));
        schemaNode.set("properties", properties);

        ObjectNode initial = objectMapper.createObjectNode();
        initial.put("name", ""); // blank should be filled
        initial.putNull("age");  // null should be filled

        ObjectNode value = objectMapper.createObjectNode();
        value.set("schema", schemaNode);
        value.set("initial_values", initial);

        Schema schema = new Schema();
        schema.setValue(value);

        Schema created = service.createSchema(schema);
        assertNotNull(created.getId());

        JsonNode data = service.getSchemaData(created.getId());
        assertNotNull(data);
        assertTrue(data.hasNonNull("name"));
        assertFalse(data.get("name").asText().trim().isEmpty());
        assertTrue(data.hasNonNull("age"));
    }
}
