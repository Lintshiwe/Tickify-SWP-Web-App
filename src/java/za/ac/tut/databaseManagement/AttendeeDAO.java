
package za.ac.tut.databaseManagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import za.ac.tut.databaseConnection.DatabaseConnection;

import za.ac.tut.entities.Attendee;

/**
 *
 * @author ntoam
 */
public class AttendeeDAO {
    
   /**
     * CENTRALIZED MAPPING: 
     * Converts a single row of the ResultSet into an Attendee Object.
     * This eliminates redundancy and keeps the data structure consistent.
     */
    private Attendee mapRow(ResultSet rs) throws SQLException {
        Attendee a = new Attendee();
        a.setAttendeeID(rs.getInt("attendeeID"));
        a.setTertiaryInstitution(rs.getString("tertiaryInstitution"));
        a.setFirstname(rs.getString("firstname"));
        a.setLastname(rs.getString("lastname"));
        a.setEmail(rs.getString("email"));
        a.setPassword(rs.getString("password"));
        // Note: For pure JDBC, we usually just store the ID in a temporary 
        // field or fetch the QRCode object separately if needed.
        return a;
    }

    /**
     * REUSABLE QUERY EXECUTOR:
     * Handles the "Boilerplate" (Connection, Statement, Resultset, Try-Catch)
     * so your specific methods stay short.
     */
    private List<Attendee> executeQuery(String sql, Object... params) throws SQLException {
        List<Attendee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // --- Clean Public Methods ---

    public List<Attendee> getAllAttendees() throws SQLException {
        return executeQuery("SELECT * FROM attendee ORDER BY lastname ASC");
    }

    public Attendee getAttendeeByID(int id) throws SQLException {
        List<Attendee> results = executeQuery("SELECT * FROM attendee WHERE attendeeID = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public Attendee getAttendeeByEmail(String email) throws SQLException {
        List<Attendee> results = executeQuery("SELECT * FROM attendee WHERE email = ?", email);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Attendee> getAttendeesByInstitution(String institution) throws SQLException {
        return executeQuery("SELECT * FROM attendee WHERE tertiaryInstitution = ?", institution);
    }

    public boolean insertAttendee(Attendee a) throws SQLException {
        String sql = "INSERT INTO attendee(tertiaryInstitution, firstname, lastname, email, password, qrcode_QRcodeID) VALUES(?,?,?,?,?,?)";
        // Logic check: Pulling the ID from the QRCode object within the Attendee entity
        int qrId = (a.getQrCode() != null) ? a.getQrCode().getQrCodeID() : 0;
        
        return executeUpdate(sql, 
                a.getTertiaryInstitution(), 
                a.getFirstname(), 
                a.getLastname(), 
                a.getEmail(), 
                a.getPassword(), 
                qrId);
    }

    public boolean updateAttendee(Attendee a) throws SQLException {
        String sql = "UPDATE attendee SET tertiaryInstitution=?, firstname=?, lastname=?, email=?, password=? WHERE attendeeID=?";
        return executeUpdate(sql, 
                a.getTertiaryInstitution(), 
                a.getFirstname(), 
                a.getLastname(), 
                a.getEmail(), 
                a.getPassword(), 
                a.getAttendeeID());
    }

    public boolean deleteAttendee(int id) throws SQLException {
        return executeUpdate("DELETE FROM attendee WHERE attendeeID = ?", id);
    }

    // REUSABLE UPDATE EXECUTOR
    private boolean executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate() > 0;
        }
    }
}
