package dev.allstak;

import java.util.Objects;

/**
 * Configuration for the AllStak SDK. Use the {@link Builder} to construct.
 *
 * <p>The ingest host is fixed to {@link #INGEST_HOST} and is not customer-configurable.
 * Customers only need to provide an API key (issued from the AllStak dashboard) and
 * optional environment / release / service metadata.
 */
public final class AllStakConfig {

    /**
     * The single, static AllStak ingest host. Not customer-configurable on purpose:
     * customers should never have to know or care about which URL their events go to.
     */
    public static final String INGEST_HOST = "http://localhost:8080";

    private final String apiKey;
    private final String environment;
    private final String release;
    private final long flushIntervalMs;
    private final int bufferSize;
    private final boolean debug;
    private final String serviceName;
    private final boolean autoBreadcrumbs;
    private final int maxBreadcrumbs;

    private AllStakConfig(Builder builder) {
        this.apiKey = builder.apiKey;
        this.environment = builder.environment;
        this.release = builder.release;
        this.flushIntervalMs = builder.flushIntervalMs;
        this.bufferSize = builder.bufferSize;
        this.debug = builder.debug;
        this.serviceName = builder.serviceName;
        this.autoBreadcrumbs = builder.autoBreadcrumbs;
        this.maxBreadcrumbs = builder.maxBreadcrumbs;
    }

    public String getApiKey() { return apiKey; }
    /** Returns the static ingest host. Always {@link #INGEST_HOST}. */
    public String getHost() { return INGEST_HOST; }
    public String getEnvironment() { return environment; }
    public String getRelease() { return release; }
    public long getFlushIntervalMs() { return flushIntervalMs; }
    public int getBufferSize() { return bufferSize; }
    public boolean isDebug() { return debug; }
    public String getServiceName() { return serviceName; }
    public boolean isAutoBreadcrumbs() { return autoBreadcrumbs; }
    public int getMaxBreadcrumbs() { return maxBreadcrumbs; }

    public void validate() {
        Objects.requireNonNull(apiKey, "apiKey must not be null");
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize must be positive");
        }
        if (flushIntervalMs <= 0) {
            throw new IllegalArgumentException("flushIntervalMs must be positive");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String apiKey;
        private String environment;
        private String release;
        private long flushIntervalMs = 5000;
        private int bufferSize = 500;
        private boolean debug = false;
        private String serviceName;
        private boolean autoBreadcrumbs = true;
        private int maxBreadcrumbs = 50;

        private Builder() {}

        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder environment(String environment) { this.environment = environment; return this; }
        public Builder release(String release) { this.release = release; return this; }
        public Builder flushIntervalMs(long flushIntervalMs) { this.flushIntervalMs = flushIntervalMs; return this; }
        public Builder bufferSize(int bufferSize) { this.bufferSize = bufferSize; return this; }
        public Builder debug(boolean debug) { this.debug = debug; return this; }
        public Builder serviceName(String serviceName) { this.serviceName = serviceName; return this; }
        public Builder autoBreadcrumbs(boolean autoBreadcrumbs) { this.autoBreadcrumbs = autoBreadcrumbs; return this; }
        public Builder maxBreadcrumbs(int maxBreadcrumbs) { this.maxBreadcrumbs = maxBreadcrumbs; return this; }

        public AllStakConfig build() {
            AllStakConfig config = new AllStakConfig(this);
            config.validate();
            return config;
        }
    }
}
