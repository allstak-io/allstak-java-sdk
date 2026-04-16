package dev.allstak.database;

import java.security.MessageDigest;
import java.util.regex.Pattern;

public final class SqlNormalizer {

    private static final Pattern STRING_LITERAL = Pattern.compile("'[^']*'");
    private static final Pattern NUMERIC_LITERAL = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private SqlNormalizer() {}

    public static String normalize(String sql) {
        if (sql == null || sql.isEmpty()) return sql;
        String result = STRING_LITERAL.matcher(sql).replaceAll("?");
        result = NUMERIC_LITERAL.matcher(result).replaceAll("?");
        result = WHITESPACE.matcher(result.trim()).replaceAll(" ");
        return result;
    }

    public static String hash(String normalized) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(normalized.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(normalized.hashCode());
        }
    }

    public static String detectQueryType(String sql) {
        if (sql == null || sql.isEmpty()) return "OTHER";
        String first = sql.trim().split("\\s+")[0].toUpperCase();
        return switch (first) {
            case "SELECT", "INSERT", "UPDATE", "DELETE" -> first;
            default -> "OTHER";
        };
    }
}
