package za.ac.tut.databaseManagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import za.ac.tut.databaseConnection.DatabaseConnection;
import za.ac.tut.entities.Attendee;

public class AttendeeDAO {

    /**
     * MAPPING: Resultset to Attendee Object
     */
    private Attendee mapRow(ResultSet rs) throws SQLException {
        Attendee a = new Attendee();
        a.setAttendeeID(rs.getInt("attendeeID"));
        a.setTertiaryInstitution(rs.getString("tertiaryInstitution"));
        a.setFirstname(rs.getString("firstname"));
        a.setLastname(rs.getString("lastname"));
        a.setEmail(rs.getString("email"));
        a.setPassword(rs.getString("password"));
        return a;
    }

    // --- NEW METHODS FOR DASHBOARD FUNCTIONALITY ---
    /**
     * FETCH ALL EVENTS Used to populate the "Available Events" grid in the
     * dashboard. Returns a list of Maps to handle generic event data without
     * needing an Event entity yet.
     */
  public List<Map<String, Object>> getAllEvents() throws SQLException {
    List<Map<String, Object>> events = new ArrayList<>();
    
    // JOINing Event to Venue (for address) 
    // JOINing Event to Event_has_Ticket and then to Ticket (for price)
    String sql = "SELECT e.eventID, e.name AS eventName, e.type, e.date, " +
                 "v.name AS venueName, v.address, " +
                 "MIN(t.price) AS minPrice " + // Gets the lowest ticket price for the event
                 "FROM event e " +
                 "JOIN venue v ON e.venueID = v.venueID " +
                 "LEFT JOIN event_has_ticket eht ON e.eventID = eht.eventID " +
                 "LEFT JOIN ticket t ON eht.ticketID = t.ticketID " +
                 "GROUP BY e.eventID, e.name, e.type, e.date, v.name, v.address";

    try (Connection conn = DatabaseConnection.getConnection(); 
         PreparedStatement ps = conn.prepareStatement(sql); 
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", rs.getInt("eventID"));
            event.put("name", rs.getString("eventName"));
            event.put("type", rs.getString("type"));
            event.put("date", rs.getDate("date"));
            
            // Venue Data
            event.put("venueName", rs.getString("venueName"));
            event.put("address", rs.getString("address"));
            
            // Ticket Data (Price)
            event.put("price", rs.getDouble("minPrice"));
            
            events.add(event);
        }
    }
    return events;
}

    /**
     * FETCH PURCHASED TICKETS Joins the ticket and event tables to show what
     * the specific attendee owns.
     */
    public List<Map<String, Object>> getAttendeeTickets(int attendeeID) throws SQLException {
        List<Map<String, Object>> tickets = new ArrayList<>();

        // Using simple column names 'eventID' and 'attendeeID' as per your feedback
        String sql = "SELECT e.name, e.date, t.ticketID, t.status "
                + "FROM ticket t "
                + "JOIN event e ON t.eventID = e.eventID "
                + "WHERE t.attendeeID = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, attendeeID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> ticket = new HashMap<>();
                    ticket.put("eventName", rs.getString("name"));
                    ticket.put("eventDate", rs.getDate("date"));
                    ticket.put("ticketID", rs.getInt("ticketID"));
                    ticket.put("status", rs.getString("status"));
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    // --- EXISTING CRUD METHODS ---
    public Attendee getAttendeeByID(int id) throws SQLException {
        List<Attendee> results = executeQuery("SELECT * FROM attendee WHERE attendeeID = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean insertAttendee(Attendee a) throws SQLException {
        String sql = "INSERT INTO attendee(tertiaryInstitution, firstname, lastname, email, password, qrcode_QRcodeID) VALUES(?,?,?,?,?,?)";
        int qrId = (a.getQrCode() != null) ? a.getQrCode().getQrCodeID() : 0;
        return executeUpdate(sql, a.getTertiaryInstitution(), a.getFirstname(), a.getLastname(), a.getEmail(), a.getPassword(), qrId);
    }

    public boolean updateAttendee(Attendee a) throws SQLException {
        String sql = "UPDATE attendee SET tertiaryInstitution=?, firstname=?, lastname=?, email=?, password=? WHERE attendeeID=?";
        return executeUpdate(sql, a.getTertiaryInstitution(), a.getFirstname(), a.getLastname(), a.getEmail(), a.getPassword(), a.getAttendeeID());
    }

public boolean deleteAttendee(int id) throws SQLException {
    // 1. Define the SQL for the relationships first
    String deleteEventLinks = "DELETE FROM attendee_has_event WHERE attendeeID = ?";
    String deleteTicketLinks = "DELETE FROM attendee_has_ticket WHERE attendeeID = ?";
    
    // 2. Define the SQL for the Attendee
    String deleteAttendee = "DELETE FROM attendee WHERE attendeeID = ?";
    
    // 3. Optional: If you want to delete the QR code from the qrcode table too:
    // String deleteQR = "DELETE FROM qrcode WHERE QRcodeID = (SELECT qrcode_QRcodeID FROM attendee WHERE attendeeID = ?)";

    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false); // Start transaction

        // Step A: Clear Many-to-Many links (Child records)
        try (PreparedStatement ps1 = conn.prepareStatement(deleteEventLinks)) {
            ps1.setInt(1, id);
            ps1.executeUpdate();
        }
        
        try (PreparedStatement ps2 = conn.prepareStatement(deleteTicketLinks)) {
            ps2.setInt(1, id);
            ps2.executeUpdate();
        }

        // Step B: Clear the Attendee (Parent record)
        try (PreparedStatement ps3 = conn.prepareStatement(deleteAttendee)) {
            ps3.setInt(1, id);
            int rowsAffected = ps3.executeUpdate();

            conn.commit(); // Save all changes
            return rowsAffected > 0;
        }

    } catch (SQLException e) {
        if (conn != null) conn.rollback(); // Undo if any step fails
        throw e;
    } finally {
        if (conn != null) conn.close();
    }
}

    // --- UTILITY EXECUTORS ---
    private List<Attendee> executeQuery(String sql, Object... params) throws SQLException {
        List<Attendee> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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

    private boolean executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate() > 0;
        }
    }
}
