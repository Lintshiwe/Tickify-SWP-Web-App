package za.ac.tut.databaseConnection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public class DatabaseConnection {
    
    private static final String DB_HOST     = "localhost";
    private static final String DB_PORT     = "1527";
    private static final String DB_NAME     = "tickifyDB";
    private static final String DB_USER     = "tickify";
    private static final String DB_PASSWORD = "123";

    // Use the Network Client Driver for Derby
    private static final String DRIVER_CLASS = "org.apache.derby.jdbc.ClientDriver";
   // FIX: Added ;ssl=off to prevent the Distributed Protocol Error
    private static final String JDBC_URL = "jdbc:derby://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + ";create=true;ssl=off";

    static {
        try {
            // 1. Force load the class
            Class.forName(DRIVER_CLASS).newInstance();
            System.out.println("Tickify: Derby Driver loaded and instantiated.");
        } catch (Exception e) {
            System.err.println("Tickify Error: Driver check failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            // 2. Double check if the driver is registered, if not, do it manually
            boolean driverFound = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                if (drivers.nextElement().getClass().getName().equals(DRIVER_CLASS)) {
                    driverFound = true;
                    break;
                }
            }

            if (!driverFound) {
                DriverManager.registerDriver(new org.apache.derby.jdbc.ClientDriver());
            }

            return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Tickify SQL Error: " + e.getMessage() + " | State: " + e.getSQLState());
            throw e;
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}