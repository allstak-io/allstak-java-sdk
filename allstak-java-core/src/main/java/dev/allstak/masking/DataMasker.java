package dev.allstak.masking;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Masks sensitive data before it leaves the SDK.
 * Applies to: log metadata fields, HTTP request paths/headers.
 */
public final class DataMasker {

    private static final String MASKED = "[MASKED]";
    private static final String FILTERED = "[FILTERED]";

    private static final Set<String> SENSITIVE_METADATA_KEYS = Set.of(
            "password", "secret", "token", "key", "authorization",
            "creditcard", "credit_card", "cardnumber", "card_number",
            "cvv", "ssn", "api_key", "apikey"
    );

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "cookie", "x-allstak-key", "x-api-key", "x-auth-token"
    );

    private static final Pattern SENSITIVE_QUERY_PARAM = Pattern.compile(
            "(token|key|secret|password|auth|api_key)=([^&]*)",
            Pattern.CASE_INSENSITIVE
    );

    private DataMasker() {}

    /**
     * Mask sensitive keys in metadata map. Returns a new map with values masked.
     */
    public static Map<String, Object> maskMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return metadata;
        Map<String, Object> result = new LinkedHashMap<>(metadata.size());
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String keyLower = entry.getKey().toLowerCase();
            if (SENSITIVE_METADATA_KEYS.contains(keyLower)) {
                result.put(entry.getKey(), MASKED);
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Strip sensitive query parameters from a URL path.
     */
    public static String stripSensitiveQueryParams(String path) {
        if (path == null) return null;
        int queryStart = path.indexOf('?');
        if (queryStart < 0) return path;
        // Strip everything after ? to be safe — query params should not be logged
        return path.substring(0, queryStart);
    }

    /**
     * Check if an HTTP header name is sensitive and should be filtered.
     */
    public static boolean isSensitiveHeader(String headerName) {
        if (headerName == null) return false;
        return SENSITIVE_HEADERS.contains(headerName.toLowerCase());
    }

    /**
     * Mask a SQL-like error message by stripping parameter values.
     */
    public static String sanitizeErrorMessage(String message) {
        if (message == null) return null;
        // Strip potential connection strings (jdbc:xxx://user:pass@host)
        return message.replaceAll("(://[^:]+:)[^@]+(@ )", "$1" + FILTERED + "$2");
    }
}
