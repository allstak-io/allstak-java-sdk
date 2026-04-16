package dev.allstak.transport;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Truncated exponential backoff with jitter per SDK guidelines.
 * Schedule: immediate, 1s+jitter, 2s+jitter, 4s+jitter, 8s+jitter (5 attempts total).
 */
public final class RetryPolicy {

    private static final int MAX_ATTEMPTS = 5;
    private static final long[] BASE_DELAYS_MS = {0, 1000, 2000, 4000, 8000};
    private static final long MAX_JITTER_MS = 500;

    private RetryPolicy() {}

    public static int maxAttempts() {
        return MAX_ATTEMPTS;
    }

    public static long delayForAttempt(int attempt) {
        if (attempt < 0 || attempt >= MAX_ATTEMPTS) return 0;
        long base = BASE_DELAYS_MS[attempt];
        if (base == 0) return 0;
        long jitter = ThreadLocalRandom.current().nextLong(0, MAX_JITTER_MS + 1);
        return base + jitter;
    }

    public static boolean isRetryable(int statusCode) {
        // Retry on 5xx and 429; do NOT retry on 400, 401, 403, 422
        if (statusCode == 429) return true;
        return statusCode >= 500;
    }

    public static boolean isClientError(int statusCode) {
        return statusCode == 400 || statusCode == 401 || statusCode == 403 || statusCode == 422;
    }

    public static boolean isAuthError(int statusCode) {
        return statusCode == 401;
    }
}
