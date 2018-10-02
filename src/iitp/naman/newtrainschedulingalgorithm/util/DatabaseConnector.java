package iitp.naman.newtrainschedulingalgorithm.util;

import java.sql.*;

/**
 * Helper class to connect to database.
 */
public class DatabaseConnector {
    protected Connection connection = null;

    public DatabaseConnector() {
        if (!connect()) {
            throw new RuntimeException("Unable to connect to database.");
        }
    }

    /**
     * Establishes a connection with databse if not already exists.
     *
     * @return true if successful.
     */
    protected boolean connect() {
        try {
            if (connection == null) {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/railwaydetails?autoReconnect=true&useSSL=false",
                        "testrailway", "testrailway");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            connection = null;
            return false;
        }
    }

    /**
     * @return the connection.
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Closes the connection
     *
     * @return true only if successfully closed.
     */
    public boolean closeConnection() {
        try {
            connection.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
