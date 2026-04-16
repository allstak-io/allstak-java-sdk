package dev.allstak.spring;

import dev.allstak.AllStakClient;
import dev.allstak.internal.SdkLogger;
import dev.allstak.model.HttpRequestItem;
import dev.allstak.model.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Servlet filter that automatically captures inbound HTTP requests for AllStak monitoring.
 * Measures timing, captures method/path/status/sizes, and sends to AllStak.
 */
public class AllStakServletFilter extends OncePerRequestFilter {

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "cookie", "set-cookie", "x-api-key", "x-auth-token", "x-allstak-key");

    private final AllStakClient client;

    public AllStakServletFilter(AllStakClient client) {
        this.client = client;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String timestamp = Instant.now().toString();
        long startTime = System.currentTimeMillis();
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String traceId = UUID.randomUUID().toString();
        RequestContext ctx = RequestContext.of(
                request.getMethod(),
                request.getRequestURI(),
                request.getServerName(),
                request.getHeader("User-Agent"),
                traceId);
        AllStakClient.setRequestContext(ctx);

        if (client.getConfig().isAutoBreadcrumbs()) {
            client.addBreadcrumb("http",
                    request.getMethod() + " " + request.getRequestURI() + " -> processing",
                    "info",
                    Map.of("method", request.getMethod(), "path", request.getRequestURI(), "host", request.getServerName()));
        }

        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            try {
                long durationMs = System.currentTimeMillis() - startTime;
                String spanId = UUID.randomUUID().toString().substring(0, 16);

                // Capture request headers (sanitized)
                Map<String, String> reqHeaders = new LinkedHashMap<>();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        String lowerName = name.toLowerCase();
                        if (SENSITIVE_HEADERS.contains(lowerName)) {
                            reqHeaders.put(name, "[REDACTED]");
                        } else {
                            reqHeaders.put(name, request.getHeader(name));
                        }
                    }
                }

                // Capture response headers (sanitized)
                Map<String, String> resHeaders = new LinkedHashMap<>();
                for (String name : responseWrapper.getHeaderNames()) {
                    String lowerName = name.toLowerCase();
                    if (SENSITIVE_HEADERS.contains(lowerName)) {
                        resHeaders.put(name, "[REDACTED]");
                    } else {
                        resHeaders.put(name, responseWrapper.getHeader(name));
                    }
                }

                // Serialize headers to JSON
                String reqHeadersJson = null;
                String resHeadersJson = null;
                try {
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    if (!reqHeaders.isEmpty()) reqHeadersJson = mapper.writeValueAsString(reqHeaders);
                    if (!resHeaders.isEmpty()) resHeadersJson = mapper.writeValueAsString(resHeaders);
                } catch (Exception ignored) {}

                HttpRequestItem item = HttpRequestItem.builder()
                        .traceId(traceId)
                        .spanId(spanId)
                        .direction("inbound")
                        .method(request.getMethod())
                        .host(request.getServerName())
                        .path(request.getRequestURI())  // query params are NOT included in getRequestURI
                        .statusCode(responseWrapper.getStatus())
                        .durationMs(durationMs)
                        .requestSize(request.getContentLengthLong() > 0 ? request.getContentLengthLong() : 0)
                        .responseSize(responseWrapper.getContentSize())
                        .userId(request.getRemoteUser())
                        .timestamp(timestamp)
                        .requestHeaders(reqHeadersJson)
                        .responseHeaders(resHeadersJson)
                        .environment(client.getConfig().getEnvironment())
                        .release(client.getConfig().getRelease())
                        .build();

                client.captureHttpRequest(item);

                // Send trace span
                client.captureSpan(
                    traceId,
                    spanId,
                    "",  // root span - no parent
                    request.getMethod() + " " + request.getRequestURI(),
                    "HTTP " + request.getMethod() + " " + request.getRequestURI(),
                    responseWrapper.getStatus() >= 500 ? "error" : "ok",
                    durationMs,
                    startTime,
                    startTime + durationMs,
                    null,  // uses config service name
                    null,  // uses config environment
                    Map.of(
                        "http.method", request.getMethod(),
                        "http.url", request.getRequestURI(),
                        "http.status_code", String.valueOf(responseWrapper.getStatus()),
                        "http.host", request.getServerName()
                    )
                );

                if (client.getConfig().isAutoBreadcrumbs()) {
                    client.addBreadcrumb("http",
                            request.getMethod() + " " + request.getRequestURI() + " -> " + responseWrapper.getStatus(),
                            responseWrapper.getStatus() >= 400 ? "error" : "info",
                            Map.of("method", request.getMethod(), "path", request.getRequestURI(),
                                   "statusCode", responseWrapper.getStatus(), "durationMs", durationMs));
                }
            } catch (Exception e) {
                SdkLogger.debug("Failed to capture HTTP request in filter: {}", e.getMessage());
            }

            AllStakClient.clearRequestContext();

            // IMPORTANT: copy content to actual response
            responseWrapper.copyBodyToResponse();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip actuator and health endpoints
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.equals("/health");
    }
}
