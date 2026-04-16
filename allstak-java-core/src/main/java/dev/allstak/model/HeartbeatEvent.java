package dev.allstak.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class HeartbeatEvent {

    private final String slug;
    private final String status;
    private final long durationMs;
    private final String message;

    public HeartbeatEvent(String slug, String status, long durationMs, String message) {
        this.slug = slug;
        this.status = status;
        this.durationMs = durationMs;
        this.message = message;
    }

    public String getSlug() { return slug; }
    public String getStatus() { return status; }
    public long getDurationMs() { return durationMs; }
    public String getMessage() { return message; }
}
