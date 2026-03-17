
package za.ac.tut.databaseManagement;

import java.sql.*;
import za.ac.tut.databaseConnection.DatabaseConnection;
 

public class UserDAO {
    /**
     * CENTRALIZED QUERY EXECUTOR
     * This handles the connection boilerplate and lets us pass a "Mapper" function 
     * to extract exactly what we need (Boolean, Integer, or String).
     */
    private <T> T executeSingleQuery(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper.map(rs);
                }
            }
        }
        return null;
    }

    // Functional interface for mapping the result
    @FunctionalInterface
    private interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Authenticates a user across all role tables.
     */
    public String authenticate(String email, String password) throws SQLException {
        // Table names from your SQL script (Tickify-Script (1).sql)
        String[] tables = {"admin", "event_manager", "venue_guard", "tertiary_presenter", "attendee"};
        String[] roles = {"ADMIN", "EVENT_MANAGER", "VENUE_GUARD", "TERTIARY_PRESENTER", "ATTENDEE"};

        for (int i = 0; i < tables.length; i++) {
            if (check(email, password, tables[i])) {
                return roles[i];
            }
        }
        return null;
    }

    public int getUserID(String email, String table, String idCol) throws SQLException {
        String sql = "SELECT " + idCol + " FROM " + table + " WHERE email=?";
        Integer id = executeSingleQuery(sql, rs -> rs.getInt(1), email);
        return (id != null) ? id : -1;
    }

    public String getFullName(String email, String table) throws SQLException {
        String sql = "SELECT firstname, lastname FROM " + table + " WHERE email=?";
        return executeSingleQuery(sql, rs -> rs.getString("firstname") + " " + rs.getString("lastname"), email);
    }

    private boolean check(String email, String password, String table) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE email=? AND password=?";
        Boolean exists = executeSingleQuery(sql, rs -> true, email, password);
        return (exists != null && exists);
    }
    
}
