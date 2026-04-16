package dev.allstak.database;

import dev.allstak.AllStakClient;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.logging.Logger;

/**
 * A DataSource wrapper that automatically captures DB query telemetry.
 * Usage: DataSource instrumented = AllStakDataSource.wrap(originalDataSource, client);
 */
public class AllStakDataSource implements DataSource {

    private final DataSource delegate;
    private final AllStakClient client;
    private final String databaseType;
    private final String databaseName;

    public AllStakDataSource(DataSource delegate, AllStakClient client, String databaseType, String databaseName) {
        this.delegate = delegate;
        this.client = client;
        this.databaseType = databaseType != null ? databaseType : "";
        this.databaseName = databaseName != null ? databaseName : "";
    }

    public static AllStakDataSource wrap(DataSource ds, AllStakClient client) {
        String dbName = extractDbName(ds);
        String dbType = extractDbType(ds);
        return new AllStakDataSource(ds, client, dbType, dbName);
    }

    private static String extractDbName(DataSource ds) {
        try {
            String url = extractJdbcUrl(ds);
            if (url != null) {
                // jdbc:postgresql://host:port/dbname?params
                int lastSlash = url.lastIndexOf('/');
                int queryStart = url.indexOf('?', lastSlash > 0 ? lastSlash : 0);
                if (lastSlash > 0) {
                    return url.substring(lastSlash + 1, queryStart > 0 ? queryStart : url.length());
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static String extractDbType(DataSource ds) {
        try {
            String url = extractJdbcUrl(ds);
            if (url != null) {
                // jdbc:postgresql://... or jdbc:mysql://...
                if (url.startsWith("jdbc:")) {
                    int colonAfterJdbc = url.indexOf(':', 5);
                    if (colonAfterJdbc > 5) {
                        return url.substring(5, colonAfterJdbc);
                    }
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static String extractJdbcUrl(DataSource ds) {
        try {
            // Try HikariCP via reflection (avoid compile-time dependency)
            if (ds.getClass().getName().contains("HikariDataSource")) {
                var method = ds.getClass().getMethod("getJdbcUrl");
                Object result = method.invoke(ds);
                if (result instanceof String s) return s;
            }
        } catch (Exception ignored) {}
        try {
            // Try generic approach via reflection
            var method = ds.getClass().getMethod("getUrl");
            Object result = method.invoke(ds);
            if (result instanceof String s) return s;
        } catch (Exception ignored) {}
        try {
            var method = ds.getClass().getMethod("getJdbcUrl");
            Object result = method.invoke(ds);
            if (result instanceof String s) return s;
        } catch (Exception ignored) {}
        return null;
    }

    public static AllStakDataSource wrap(DataSource ds, AllStakClient client, String dbType, String dbName) {
        return new AllStakDataSource(ds, client, dbType, dbName);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new InstrumentedConnection(delegate.getConnection(), client, databaseType, databaseName);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new InstrumentedConnection(delegate.getConnection(username, password), client, databaseType, databaseName);
    }

    // Delegate all other DataSource methods
    @Override public PrintWriter getLogWriter() throws SQLException { return delegate.getLogWriter(); }
    @Override public void setLogWriter(PrintWriter out) throws SQLException { delegate.setLogWriter(out); }
    @Override public void setLoginTimeout(int seconds) throws SQLException { delegate.setLoginTimeout(seconds); }
    @Override public int getLoginTimeout() throws SQLException { return delegate.getLoginTimeout(); }
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { return delegate.getParentLogger(); }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }
}
