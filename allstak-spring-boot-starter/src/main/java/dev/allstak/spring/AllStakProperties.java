package dev.allstak.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for AllStak SDK.
 * Configured via application.properties/yml under the "allstak" prefix.
 *
 * <p>Note: the ingest host is hardcoded inside the SDK and is intentionally
 * not configurable. Customers only need to provide their API key.
 *
 * <pre>
 * allstak.api-key=ask_live_xxx
 * allstak.environment=production
 * allstak.release=v1.0.0
 * allstak.debug=false
 * allstak.enabled=true
 * allstak.service-name=my-service
 * allstak.flush-interval-ms=5000
 * allstak.buffer-size=500
 * allstak.capture-http-requests=true
 * allstak.capture-exceptions=true
 * </pre>
 */
@ConfigurationProperties(prefix = "allstak")
public class AllStakProperties {

    private String apiKey;
    private String environment;
    private String release;
    private boolean debug = false;
    private boolean enabled = true;
    private String serviceName;
    private long flushIntervalMs = 5000;
    private int bufferSize = 500;
    private boolean captureHttpRequests = true;
    private boolean captureExceptions = true;
    private boolean captureDbQueries = true;
    private boolean captureLogs = true;
    private boolean captureScheduled = true;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getRelease() { return release; }
    public void setRelease(String release) { this.release = release; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public long getFlushIntervalMs() { return flushIntervalMs; }
    public void setFlushIntervalMs(long flushIntervalMs) { this.flushIntervalMs = flushIntervalMs; }
    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }
    public boolean isCaptureHttpRequests() { return captureHttpRequests; }
    public void setCaptureHttpRequests(boolean captureHttpRequests) { this.captureHttpRequests = captureHttpRequests; }
    public boolean isCaptureExceptions() { return captureExceptions; }
    public void setCaptureExceptions(boolean captureExceptions) { this.captureExceptions = captureExceptions; }
    public boolean isCaptureDbQueries() { return captureDbQueries; }
    public void setCaptureDbQueries(boolean captureDbQueries) { this.captureDbQueries = captureDbQueries; }
    public boolean isCaptureLogs() { return captureLogs; }
    public void setCaptureLogs(boolean captureLogs) { this.captureLogs = captureLogs; }
    public boolean isCaptureScheduled() { return captureScheduled; }
    public void setCaptureScheduled(boolean captureScheduled) { this.captureScheduled = captureScheduled; }
}
