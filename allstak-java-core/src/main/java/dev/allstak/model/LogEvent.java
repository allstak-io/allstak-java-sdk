package dev.allstak.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LogEvent {

    private final String level;
    private final String message;
    private final String service;
    private final String traceId;
    private final String environment;
    private final String spanId;
    private final String requestId;
    private final String userId;
    private final String errorId;
    private final Map<String, Object> metadata;
    private final String release;

    public LogEvent(String level, String message, String service,
                    String traceId, Map<String, Object> metadata) {
        this(level, message, service, traceId, null, null, null, null, null, metadata, null);
    }

    public LogEvent(String level, String message, String service,
                    String traceId, String environment, String spanId,
                    String requestId, String userId, String errorId,
                    Map<String, Object> metadata) {
        this(level, message, service, traceId, environment, spanId,
                requestId, userId, errorId, metadata, null);
    }

    public LogEvent(String level, String message, String service,
                    String traceId, String environment, String spanId,
                    String requestId, String userId, String errorId,
                    Map<String, Object> metadata, String release) {
        this.level = level;
        this.message = message;
        this.service = service;
        this.traceId = traceId;
        this.environment = environment;
        this.spanId = spanId;
        this.requestId = requestId;
        this.userId = userId;
        this.errorId = errorId;
        this.metadata = metadata;
        this.release = release;
    }

    public String getLevel() { return level; }
    public String getMessage() { return message; }
    public String getService() { return service; }
    public String getTraceId() { return traceId; }
    public String getEnvironment() { return environment; }
    public String getSpanId() { return spanId; }
    public String getRequestId() { return requestId; }
    public String getUserId() { return userId; }
    public String getErrorId() { return errorId; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getRelease() { return release; }
}
