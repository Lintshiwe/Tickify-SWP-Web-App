package za.ac.tut.databaseManagement;

import java.sql.*;
import za.ac.tut.databaseConnection.DatabaseConnection;

public class UserDAO {

    /**
     * CENTRALIZED QUERY EXECUTOR
     * Handles connection boilerplate and mapping.
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

    @FunctionalInterface
    private interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * NEW: Targeted Authentication
     * This checks ONLY the table corresponding to the role selected on the Login UI.
     * Prevents "Attendee Overlap" where one email exists in multiple tables.
     */
    public String authenticateSpecific(String email, String password, String chosenRole) throws SQLException {
        String table;
        
        // Map the Role string from the radio button to the actual DB table name
        switch (chosenRole) {
            case "ADMIN": table = "admin"; break;
            case "EVENT_MANAGER": table = "event_manager"; break;
            case "VENUE_GUARD": table = "venue_guard"; break;
            case "TERTIARY_PRESENTER": table = "tertiary_presenter"; break;
            case "ATTENDEE": table = "attendee"; break;
            default: return null; 
        }

        // Only check that specific table
        if (check(email, password, table)) {
            return chosenRole; 
        }
        
        return null; 
    }

    /**
     * Legacy authenticate (Keep for compatibility if needed, but use Specific for Login)
     */
    public String authenticate(String email, String password) throws SQLException {
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
        String name = executeSingleQuery(sql, rs -> rs.getString("firstname") + " " + rs.getString("lastname"), email);
        return (name != null) ? name : "Unknown User";
    }

    private boolean check(String email, String password, String table) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE email=? AND password=?";
        Boolean exists = executeSingleQuery(sql, rs -> true, email, password);
        return (exists != null && exists);
    }
}