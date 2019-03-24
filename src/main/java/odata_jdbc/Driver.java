package odata_jdbc;

import odata_jdbc.jdbc.ODataConnection;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Driver implements java.sql.Driver {

    private static Driver registeredDriver;

    static {
        try {
            register();
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void register() throws SQLException {
        if (isRegistered()) {
            throw new IllegalStateException(
                    "Driver is already registered. It can only be registered once.");
        }
        Driver registeredDriver = new Driver();
        DriverManager.registerDriver(registeredDriver);
        Driver.registeredDriver = registeredDriver;
    }

    public static boolean isRegistered() {
        return registeredDriver != null;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return new ODataConnection();
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith("jdbc:odata-jdbc:https://") || url.startsWith("jdbc:odata-jdbc:http://");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
