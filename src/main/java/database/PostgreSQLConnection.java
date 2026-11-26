package main.java.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/uas_gui";
    private static final String USER = "Felicha";
    private static final String PASSWORD = "icha123";

    public static Connection getConnection() throws SQLException {
        try { Class.forName("org.postgresql.Driver"); }
        catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver jar missing from lib/ folder", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}