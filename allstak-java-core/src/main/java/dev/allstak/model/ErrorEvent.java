package dev.allstak.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorEvent {

    private final String exceptionClass;
    private final String message;
    private final List<String> stackTrace;
    private final String level;
    private final String environment;
    private final String release;
    private final String sessionId;
    private final UserContext user;
    private final Map<String, Object> metadata;
    private final String traceId;
    private final RequestContext requestContext;
    private final List<Breadcrumb> breadcrumbs;

    public ErrorEvent(String exceptionClass, String message, List<String> stackTrace,
                      String level, String environment, String release,
                      String sessionId, UserContext user, Map<String, Object> metadata) {
        this(exceptionClass, message, stackTrace, level, environment, release,
                sessionId, user, metadata, null, null, null);
    }

    public ErrorEvent(String exceptionClass, String message, List<String> stackTrace,
                      String level, String environment, String release,
                      String sessionId, UserContext user, Map<String, Object> metadata,
                      String traceId, RequestContext requestContext) {
        this(exceptionClass, message, stackTrace, level, environment, release,
                sessionId, user, metadata, traceId, requestContext, null);
    }

    public ErrorEvent(String exceptionClass, String message, List<String> stackTrace,
                      String level, String environment, String release,
                      String sessionId, UserContext user, Map<String, Object> metadata,
                      String traceId, RequestContext requestContext,
                      List<Breadcrumb> breadcrumbs) {
        this.exceptionClass = exceptionClass;
        this.message = message;
        this.stackTrace = stackTrace;
        this.level = level;
        this.environment = environment;
        this.release = release;
        this.sessionId = sessionId;
        this.user = user;
        this.metadata = metadata;
        this.traceId = traceId;
        this.requestContext = requestContext;
        this.breadcrumbs = breadcrumbs;
    }

    public String getExceptionClass() { return exceptionClass; }
    public String getMessage() { return message; }
    public List<String> getStackTrace() { return stackTrace; }
    public String getLevel() { return level; }
    public String getEnvironment() { return environment; }
    public String getRelease() { return release; }
    public String getSessionId() { return sessionId; }
    public UserContext getUser() { return user; }
    public Map<String, Object> getMetadata() { return metadata; }
    public String getTraceId() { return traceId; }
    public RequestContext getRequestContext() { return requestContext; }
    public List<Breadcrumb> getBreadcrumbs() { return breadcrumbs; }
}
