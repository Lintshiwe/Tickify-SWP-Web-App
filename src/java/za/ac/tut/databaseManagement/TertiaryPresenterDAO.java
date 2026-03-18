package za.ac.tut.databaseManagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import za.ac.tut.databaseConnection.DatabaseConnection;
import za.ac.tut.entities.TertiaryPresenter;
import za.ac.tut.entities.Event;

public class TertiaryPresenterDAO {

    /**
     * Converts a DB row into a TertiaryPresenter object.
     */
    private TertiaryPresenter mapRow(ResultSet rs) throws SQLException {
        TertiaryPresenter tp = new TertiaryPresenter();
        tp.setTertiaryPresenterID(rs.getInt("tertiaryPresenterID"));
        tp.setFirstname(rs.getString("firstname"));
        tp.setLastname(rs.getString("lastname"));
        tp.setEmail(rs.getString("email"));
        tp.setPassword(rs.getString("password"));
        tp.setTertiaryInstitution(rs.getString("tertiaryInstitution"));
        // For JDBC, we leave event/venue as null or fetch separately if needed
        return tp;
    }

    public boolean insertPresenter(TertiaryPresenter tp) throws SQLException {
        // 1. Updated SQL to include all 7 columns (excluding the auto-increment PK)
        String sql = "INSERT INTO TERTIARY_PRESENTER (firstname, lastname, email, password, tertiaryInstitution, eventID, venueID) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            // 2. Set the basic Strings
            ps.setString(1, tp.getFirstname());
            ps.setString(2, tp.getLastname());
            ps.setString(3, tp.getEmail());
            ps.setString(4, tp.getPassword());
            ps.setString(5, tp.getTertiaryInstitution());

            ps.setInt(6, 2);

            ps.setInt(7, 3);

            return ps.executeUpdate() > 0;
        }
    }

    public TertiaryPresenter getPresenterByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM tertiary_presenter WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * DELETE ACCOUNT: Removes the presenter from the database by their ID.
     */
    public boolean deletePresenterAccount(int presenterID) throws SQLException {
        String sql = "DELETE FROM tertiary_presenter WHERE tertiaryPresenterID = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, presenterID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * BOOK EVENT: Updates the presenter's record to link them to a specific
     * eventID. In your ERD, the presenter "owns" the relationship via the
     * eventID column.
     */
    public boolean bookEvent(int presenterID, int eventID) throws SQLException {
        String sql = "UPDATE tertiary_presenter SET eventID = ? WHERE tertiaryPresenterID = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventID);
            ps.setInt(2, presenterID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * GET BOOKED EVENTS: Fetches details of the event currently linked to the
     * presenter. Since a presenter is linked to one event at a time
     * (Many-to-One), this returns the specific Event object.
     */
    public Event getBookedEvent(int presenterID) throws SQLException {
        String sql = "SELECT e.* FROM event e "
                + "JOIN tertiary_presenter tp ON e.eventID = tp.eventID "
                + "WHERE tp.tertiaryPresenterID = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, presenterID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Event event = new Event();
                    event.setEventID(rs.getInt("eventID"));
                    event.setName(rs.getString("name"));
                    // Map other event fields as per your Event entity
                    return event;
                }
            }
        }
        return null; // Returns null if no event is booked
    }

}
