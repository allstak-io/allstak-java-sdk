package dev.allstak.database;

import dev.allstak.AllStakClient;
import dev.allstak.model.DatabaseQueryItem;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

/**
 * PreparedStatement wrapper that captures query telemetry.
 * The SQL is known at prepare time, so timing is captured around execute calls.
 */
class InstrumentedPreparedStatement extends InstrumentedStatement implements PreparedStatement {

    private final PreparedStatement psDelegate;
    private final String sql;

    InstrumentedPreparedStatement(PreparedStatement delegate, String sql, AllStakClient client, String databaseType, String databaseName) {
        super(delegate, client, databaseType, databaseName);
        this.psDelegate = delegate;
        this.sql = sql;
    }

    // ---- Instrumented execute methods ----

    @Override
    public ResultSet executeQuery() throws SQLException {
        long start = System.currentTimeMillis();
        try {
            ResultSet rs = psDelegate.executeQuery();
            captureQuery(sql, System.currentTimeMillis() - start, "success", null, -1);
            return rs;
        } catch (SQLException e) {
            captureQuery(sql, System.currentTimeMillis() - start, "error", e.getMessage(), 0);
            throw e;
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        long start = System.currentTimeMillis();
        try {
            int rows = psDelegate.executeUpdate();
            captureQuery(sql, System.currentTimeMillis() - start, "success", null, rows);
            return rows;
        } catch (SQLException e) {
            captureQuery(sql, System.currentTimeMillis() - start, "error", e.getMessage(), 0);
            throw e;
        }
    }

    @Override
    public boolean execute() throws SQLException {
        long start = System.currentTimeMillis();
        try {
            boolean result = psDelegate.execute();
            captureQuery(sql, System.currentTimeMillis() - start, "success", null, -1);
            return result;
        } catch (SQLException e) {
            captureQuery(sql, System.currentTimeMillis() - start, "error", e.getMessage(), 0);
            throw e;
        }
    }

    // ---- Pure delegation for parameter setters ----

    @Override public void setNull(int parameterIndex, int sqlType) throws SQLException { psDelegate.setNull(parameterIndex, sqlType); }
    @Override public void setBoolean(int parameterIndex, boolean x) throws SQLException { psDelegate.setBoolean(parameterIndex, x); }
    @Override public void setByte(int parameterIndex, byte x) throws SQLException { psDelegate.setByte(parameterIndex, x); }
    @Override public void setShort(int parameterIndex, short x) throws SQLException { psDelegate.setShort(parameterIndex, x); }
    @Override public void setInt(int parameterIndex, int x) throws SQLException { psDelegate.setInt(parameterIndex, x); }
    @Override public void setLong(int parameterIndex, long x) throws SQLException { psDelegate.setLong(parameterIndex, x); }
    @Override public void setFloat(int parameterIndex, float x) throws SQLException { psDelegate.setFloat(parameterIndex, x); }
    @Override public void setDouble(int parameterIndex, double x) throws SQLException { psDelegate.setDouble(parameterIndex, x); }
    @Override public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException { psDelegate.setBigDecimal(parameterIndex, x); }
    @Override public void setString(int parameterIndex, String x) throws SQLException { psDelegate.setString(parameterIndex, x); }
    @Override public void setBytes(int parameterIndex, byte[] x) throws SQLException { psDelegate.setBytes(parameterIndex, x); }
    @Override public void setDate(int parameterIndex, Date x) throws SQLException { psDelegate.setDate(parameterIndex, x); }
    @Override public void setTime(int parameterIndex, Time x) throws SQLException { psDelegate.setTime(parameterIndex, x); }
    @Override public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException { psDelegate.setTimestamp(parameterIndex, x); }
    @Override public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException { psDelegate.setAsciiStream(parameterIndex, x, length); }
    @SuppressWarnings("deprecation")
    @Override public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException { psDelegate.setUnicodeStream(parameterIndex, x, length); }
    @Override public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException { psDelegate.setBinaryStream(parameterIndex, x, length); }
    @Override public void clearParameters() throws SQLException { psDelegate.clearParameters(); }
    @Override public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException { psDelegate.setObject(parameterIndex, x, targetSqlType); }
    @Override public void setObject(int parameterIndex, Object x) throws SQLException { psDelegate.setObject(parameterIndex, x); }
    @Override public void addBatch() throws SQLException { psDelegate.addBatch(); }
    @Override public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException { psDelegate.setCharacterStream(parameterIndex, reader, length); }
    @Override public void setRef(int parameterIndex, Ref x) throws SQLException { psDelegate.setRef(parameterIndex, x); }
    @Override public void setBlob(int parameterIndex, Blob x) throws SQLException { psDelegate.setBlob(parameterIndex, x); }
    @Override public void setClob(int parameterIndex, Clob x) throws SQLException { psDelegate.setClob(parameterIndex, x); }
    @Override public void setArray(int parameterIndex, Array x) throws SQLException { psDelegate.setArray(parameterIndex, x); }
    @Override public ResultSetMetaData getMetaData() throws SQLException { return psDelegate.getMetaData(); }
    @Override public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException { psDelegate.setDate(parameterIndex, x, cal); }
    @Override public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException { psDelegate.setTime(parameterIndex, x, cal); }
    @Override public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException { psDelegate.setTimestamp(parameterIndex, x, cal); }
    @Override public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException { psDelegate.setNull(parameterIndex, sqlType, typeName); }
    @Override public void setURL(int parameterIndex, URL x) throws SQLException { psDelegate.setURL(parameterIndex, x); }
    @Override public ParameterMetaData getParameterMetaData() throws SQLException { return psDelegate.getParameterMetaData(); }
    @Override public void setRowId(int parameterIndex, RowId x) throws SQLException { psDelegate.setRowId(parameterIndex, x); }
    @Override public void setNString(int parameterIndex, String value) throws SQLException { psDelegate.setNString(parameterIndex, value); }
    @Override public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException { psDelegate.setNCharacterStream(parameterIndex, value, length); }
    @Override public void setNClob(int parameterIndex, NClob value) throws SQLException { psDelegate.setNClob(parameterIndex, value); }
    @Override public void setClob(int parameterIndex, Reader reader, long length) throws SQLException { psDelegate.setClob(parameterIndex, reader, length); }
    @Override public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException { psDelegate.setBlob(parameterIndex, inputStream, length); }
    @Override public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException { psDelegate.setNClob(parameterIndex, reader, length); }
    @Override public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException { psDelegate.setSQLXML(parameterIndex, xmlObject); }
    @Override public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException { psDelegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength); }
    @Override public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException { psDelegate.setAsciiStream(parameterIndex, x, length); }
    @Override public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException { psDelegate.setBinaryStream(parameterIndex, x, length); }
    @Override public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException { psDelegate.setCharacterStream(parameterIndex, reader, length); }
    @Override public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException { psDelegate.setAsciiStream(parameterIndex, x); }
    @Override public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException { psDelegate.setBinaryStream(parameterIndex, x); }
    @Override public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException { psDelegate.setCharacterStream(parameterIndex, reader); }
    @Override public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException { psDelegate.setNCharacterStream(parameterIndex, value); }
    @Override public void setClob(int parameterIndex, Reader reader) throws SQLException { psDelegate.setClob(parameterIndex, reader); }
    @Override public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException { psDelegate.setBlob(parameterIndex, inputStream); }
    @Override public void setNClob(int parameterIndex, Reader reader) throws SQLException { psDelegate.setNClob(parameterIndex, reader); }
}
