package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place that opens a JDBC connection to MySQL.
 *
 * IMPORTANT (read this before running the project):
 *  1. Create the database first by running database/vehicle_rental.sql
 *     in MySQL Workbench / the mysql command line.
 *  2. Update DB_USER and DB_PASSWORD below to match your local MySQL
 *     installation.
 *  3. Make sure the MySQL Connector/J .jar file is on the classpath
 *     (see the README for the exact command).
 */
public class DBConnection {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/vehicle_rental_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root"; // <-- change this to your MySQL password

    /** Opens and returns a brand-new connection. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on classpath. " +
                    "Download mysql-connector-j and add it with -cp as described in the README.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
