package com.formily.controller;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 极简的预览渲染器：把 value.schema 转成可展示的HTML（不依赖前端框架）。
 * 目标是满足“预览弹窗展示效果并回显数据”的最小实现。
 */
final class SchemaPreviewRenderer {
    private SchemaPreviewRenderer() {}

    static String render(JsonNode value) {
        JsonNode schema = value.hasNonNull("schema") ? value.get("schema") : value;
        JsonNode initialValues = value.hasNonNull("initial_values") ? value.get("initial_values") : null;

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='formily-preview'>");
        sb.append("<style>");
        sb.append(".formily-preview{font-family:Arial,Helvetica,sans-serif;position:relative;min-height:600px;}");
        sb.append(".fp-item{margin:12px 0;box-sizing:border-box;}");
        sb.append(".fp-label{display:inline-block;margin:0 8px 0 0;color:#333;font-weight:600;}");
        sb.append(".fp-card{border:1px solid #e5e7eb;border-radius:8px;padding:12px;background:#fff;}");
        sb.append(".fp-card-h{margin:0 0 12px;font-size:16px;}");
        sb.append(".fp-item input,.fp-item textarea,.fp-item select{box-sizing:border-box;width:100%;padding:6px 8px;border:1px solid #d1d5db;border-radius:6px;}");
        sb.append("</style>");

        sb.append(renderSchemaNode(schema, initialValues));
        sb.append("</div>");
        return sb.toString();
    }

    private static String renderSchemaNode(JsonNode node, JsonNode initialValues) {
        if (node == null || node.isNull()) return "";

        // Root object schema
        if (node.has("properties") && node.get("properties").isObject()) {
            StringBuilder sb = new StringBuilder();
            node.get("properties").fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode child = entry.getValue();
                sb.append(renderField(key, child, initialValues));
            });
            return sb.toString();
        }

        // Fallback: show JSON
        return "<pre>" + escapeHtml(node.toString()) + "</pre>";
    }

    private static String renderField(String fieldKey, JsonNode field, JsonNode initialValues) {
        String title = field.hasNonNull("title") ? field.get("title").asText() : fieldKey;
        String xComponent = field.hasNonNull("x-component") ? field.get("x-component").asText() : "";
        JsonNode props = field.hasNonNull("x-component-props") ? field.get("x-component-props") : null;

        final String value =
                (initialValues != null && initialValues.has(fieldKey) && !initialValues.get(fieldKey).isNull())
                        ? initialValues.get(fieldKey).asText("")
                        : "";

        String styleAttr = styleAttr(field);

        // Card container (x-component: Card)
        if ("Card".equals(xComponent)) {
            String header = props != null && props.hasNonNull("header") ? props.get("header").asText() : title;
            String children = renderSchemaNode(field, initialValues);
            return "<div class='fp-item fp-card'" + styleAttr + "><div class='fp-card-h'>" + escapeHtml(header) + "</div>" + children + "</div>";
        }

        // Basic inputs
        if ("Input".equals(xComponent)) {
            String placeholder = props != null && props.hasNonNull("placeholder") ? props.get("placeholder").asText() : "";
            return wrap(
                    title,
                    "<input type='text' name='" + escapeHtml(fieldKey) + "' value='" + escapeHtml(value) + "' placeholder='" + escapeHtml(placeholder) + "' />",
                    styleAttr
            );
        }
        if ("NumberPicker".equals(xComponent)) {
            return wrap(title, "<input type='number' name='" + escapeHtml(fieldKey) + "' value='" + escapeHtml(value) + "' />", styleAttr);
        }
        if ("Password".equals(xComponent)) {
            String placeholder = props != null && props.hasNonNull("placeholder") ? props.get("placeholder").asText() : "";
            return wrap(
                    title,
                    "<input type='password' name='" + escapeHtml(fieldKey) + "' value='" + escapeHtml(value) + "' placeholder='" + escapeHtml(placeholder) + "' />",
                    styleAttr
            );
        }
        if ("Textarea".equals(xComponent)) {
            String placeholder = props != null && props.hasNonNull("placeholder") ? props.get("placeholder").asText() : "";
            return wrap(
                    title,
                    "<textarea name='" + escapeHtml(fieldKey) + "' placeholder='" + escapeHtml(placeholder) + "'>" + escapeHtml(value) + "</textarea>",
                    styleAttr
            );
        }
        if ("Select".equals(xComponent)) {
            StringBuilder options = new StringBuilder();
            if (field.has("enum") && field.get("enum").isArray()) {
                field.get("enum").forEach(opt -> {
                    String label = opt.hasNonNull("label") ? opt.get("label").asText() : opt.asText();
                    String v = opt.hasNonNull("value") ? opt.get("value").asText() : opt.asText();
                    boolean selected = v.equals(value);
                    options.append("<option value='")
                            .append(escapeHtml(v))
                            .append("'")
                            .append(selected ? " selected" : "")
                            .append(">")
                            .append(escapeHtml(label))
                            .append("</option>");
                });
            }
            return wrap(title, "<select name='" + escapeHtml(fieldKey) + "'>" + options + "</select>", styleAttr);
        }

        // Not fully supported: show JSON for the node
        return wrap(title, "<pre>" + escapeHtml(field.toString()) + "</pre>", styleAttr);
    }

    private static String wrap(String title, String html, String styleAttr) {
        return "<div class='fp-item'" + styleAttr + "><label class='fp-label'>" + escapeHtml(title) + "</label>" + html + "</div>";
    }

    private static String styleAttr(JsonNode field) {
        if (field == null || field.isNull()) return "";
        JsonNode props = field.hasNonNull("x-component-props") ? field.get("x-component-props") : null;
        JsonNode style = props != null && props.hasNonNull("style") ? props.get("style") : null;
        if (style == null || !style.isObject()) return "";

        StringBuilder css = new StringBuilder();
        style.fields().forEachRemaining(e -> {
            String key = e.getKey();
            JsonNode v = e.getValue();
            if (v == null || v.isNull()) return;
            String val = v.isTextual() ? v.asText() : v.toString();
            if (val == null || val.isEmpty()) return;
            css.append(camelToKebab(key)).append(':').append(escapeHtmlAttr(val)).append(';');
        });

        if (css.length() == 0) return "";
        return " style='" + css + "'";
    }

    private static String camelToKebab(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                out.append('-').append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String escapeHtmlAttr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

