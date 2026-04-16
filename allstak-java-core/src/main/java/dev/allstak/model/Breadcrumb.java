package dev.allstak.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * Represents a breadcrumb — a small piece of contextual information
 * that is captured before an error occurs. Breadcrumbs are attached
 * to error events to help with debugging.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Breadcrumb {

    private static final java.util.Set<String> VALID_TYPES =
            java.util.Set.of("http", "log", "ui", "navigation", "query", "default");

    private static final java.util.Set<String> VALID_LEVELS =
            java.util.Set.of("info", "warn", "error", "debug");

    private final String timestamp;
    private final String type;
    private final String category;
    private final String message;
    private final String level;
    private final Map<String, Object> data;

    public Breadcrumb(String type, String message, String level, Map<String, Object> data) {
        this(type, null, message, level, data);
    }

    public Breadcrumb(String type, String category, String message, String level, Map<String, Object> data) {
        this.timestamp = Instant.now().toString();
        this.type = type != null && VALID_TYPES.contains(type) ? type : "default";
        // Free-form category — falls back to type so the dashboard always has something to group by
        this.category = category != null && !category.isBlank() ? category : this.type;
        this.message = message;
        this.level = level != null && VALID_LEVELS.contains(level) ? level : "info";
        this.data = data != null ? Collections.unmodifiableMap(data) : null;
    }

    public String getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public String getMessage() { return message; }
    public String getLevel() { return level; }
    public Map<String, Object> getData() { return data; }
}
