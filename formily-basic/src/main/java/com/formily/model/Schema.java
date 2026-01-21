package com.formily.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Schema实体类
 * 用于表示表单Schema的数据结构
 */
public class Schema {
    private String id;
    private JsonNode value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JsonNode getValue() {
        return value;
    }

    public void setValue(JsonNode value) {
        this.value = value;
    }
}