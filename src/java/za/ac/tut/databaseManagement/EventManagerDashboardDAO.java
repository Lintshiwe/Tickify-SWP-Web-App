package za.ac.tut.databaseManagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import za.ac.tut.databaseConnection.DatabaseConnection;

public class EventManagerDashboardDAO {

    public Map<String, Object> getManagerProfile(int eventManagerId) throws SQLException {
        String sql = "SELECT m.eventManagerID, m.firstname, m.lastname, m.email, m.venueGuardID, "
                + "vg.venueID, v.name AS campusName "
                + "FROM event_manager m "
                + "LEFT JOIN venue_guard vg ON vg.venueGuardID = m.venueGuardID "
                + "LEFT JOIN venue v ON v.venueID = vg.venueID "
                + "WHERE m.eventManagerID = ?";
        List<Map<String, Object>> rows = runListQuery(sql, eventManagerId);
        return rows.isEmpty() ? new HashMap<String, Object>() : rows.get(0);
    }

    public List<Map<String, Object>> getAssignedEvents(int eventManagerId) throws SQLException {
        String sql = "SELECT e.eventID, e.name AS eventName, e.type AS eventType, e.date AS eventDate, "
                + "v.name AS campusName, "
                + "COUNT(DISTINCT eht.ticketID) AS ticketTemplates, "
                + "COUNT(aht.attendeeID) AS soldTickets, "
                + "COALESCE(SUM(t.price), 0) AS revenue "
                + "FROM event e "
                + "JOIN event_has_manager ehm ON ehm.eventID = e.eventID "
                + "LEFT JOIN venue v ON v.venueID = e.venueID "
                + "LEFT JOIN event_has_ticket eht ON eht.eventID = e.eventID "
                + "LEFT JOIN attendee_has_ticket aht ON aht.ticketID = eht.ticketID "
                + "LEFT JOIN ticket t ON t.ticketID = aht.ticketID "
                + "WHERE ehm.eventManagerID = ? "
                + "GROUP BY e.eventID, e.name, e.type, e.date, v.name "
                + "ORDER BY e.date ASC";
        return runListQuery(sql, eventManagerId);
    }

    public List<Map<String, Object>> getVenueGuardCoverage(int eventManagerId) throws SQLException {
        String sql = "SELECT g.venueGuardID, g.firstname, g.lastname, g.email, g.eventID, e.name AS eventName, "
                + "SUM(CASE WHEN sl.result = 'VALID' THEN 1 ELSE 0 END) AS validScans, "
                + "SUM(CASE WHEN sl.result = 'INVALID' THEN 1 ELSE 0 END) AS invalidScans "
                + "FROM event_manager m "
                + "JOIN venue_guard ownerGuard ON ownerGuard.venueGuardID = m.venueGuardID "
                + "JOIN venue_guard g ON g.venueID = ownerGuard.venueID "
                + "LEFT JOIN event e ON e.eventID = g.eventID "
                + "LEFT JOIN scan_log sl ON sl.venueGuardID = g.venueGuardID "
                + "WHERE m.eventManagerID = ? "
                + "GROUP BY g.venueGuardID, g.firstname, g.lastname, g.email, g.eventID, e.name "
                + "ORDER BY g.venueGuardID DESC";
        return runListQuery(sql, eventManagerId);
    }

    public List<Map<String, Object>> getPresenterSessions(int eventManagerId) throws SQLException {
        String sql = "SELECT p.tertiaryPresenterID, p.firstname, p.lastname, p.email, p.tertiaryInstitution, "
                + "p.eventID, e.name AS eventName, e.date AS eventDate "
                + "FROM event_manager m "
                + "JOIN venue_guard vg ON vg.venueGuardID = m.venueGuardID "
                + "JOIN tertiary_presenter p ON p.venueID = vg.venueID "
                + "LEFT JOIN event e ON e.eventID = p.eventID "
                + "WHERE m.eventManagerID = ? "
                + "ORDER BY e.date ASC, p.tertiaryPresenterID DESC";
        return runListQuery(sql, eventManagerId);
    }

    public int countInvalidScansLast24h(int eventManagerId) throws SQLException {
        String sql = "SELECT COUNT(*) "
                + "FROM event_manager m "
                + "JOIN venue_guard ownerGuard ON ownerGuard.venueGuardID = m.venueGuardID "
                + "JOIN venue_guard g ON g.venueID = ownerGuard.venueID "
                + "JOIN scan_log sl ON sl.venueGuardID = g.venueGuardID "
                + "WHERE m.eventManagerID = ? "
                + "AND sl.result = 'INVALID' "
                + "AND sl.scannedAt >= CURRENT_TIMESTAMP - 1 DAY";
        return runCount(sql, eventManagerId);
    }

    public int countValidScansLast24h(int eventManagerId) throws SQLException {
        String sql = "SELECT COUNT(*) "
                + "FROM event_manager m "
                + "JOIN venue_guard ownerGuard ON ownerGuard.venueGuardID = m.venueGuardID "
                + "JOIN venue_guard g ON g.venueID = ownerGuard.venueID "
                + "JOIN scan_log sl ON sl.venueGuardID = g.venueGuardID "
                + "WHERE m.eventManagerID = ? "
                + "AND sl.result = 'VALID' "
                + "AND sl.scannedAt >= CURRENT_TIMESTAMP - 1 DAY";
        return runCount(sql, eventManagerId);
    }

    public int countEventsWithoutTickets(int eventManagerId) throws SQLException {
        String sql = "SELECT COUNT(*) "
                + "FROM event e "
                + "JOIN event_has_manager ehm ON ehm.eventID = e.eventID "
                + "LEFT JOIN event_has_ticket eht ON eht.eventID = e.eventID "
                + "WHERE ehm.eventManagerID = ? "
                + "GROUP BY e.eventID "
                + "HAVING COUNT(eht.ticketID) = 0";
        List<Map<String, Object>> grouped = runListQuery(sql, eventManagerId);
        return grouped.size();
    }

    public int countGuardsWithNoScans(int eventManagerId) throws SQLException {
        String sql = "SELECT COUNT(*) "
                + "FROM event_manager m "
                + "JOIN venue_guard ownerGuard ON ownerGuard.venueGuardID = m.venueGuardID "
                + "JOIN venue_guard g ON g.venueID = ownerGuard.venueID "
                + "LEFT JOIN scan_log sl ON sl.venueGuardID = g.venueGuardID "
                + "WHERE m.eventManagerID = ? "
                + "GROUP BY g.venueGuardID "
                + "HAVING COUNT(sl.scanLogID) = 0";
        List<Map<String, Object>> grouped = runListQuery(sql, eventManagerId);
        return grouped.size();
    }

    public int countPresentersWithoutMappedEvent(int eventManagerId) throws SQLException {
        String sql = "SELECT COUNT(*) "
                + "FROM event_manager m "
                + "JOIN venue_guard vg ON vg.venueGuardID = m.venueGuardID "
                + "JOIN tertiary_presenter p ON p.venueID = vg.venueID "
                + "LEFT JOIN event e ON e.eventID = p.eventID "
                + "WHERE m.eventManagerID = ? "
                + "AND e.eventID IS NULL";
        return runCount(sql, eventManagerId);
    }

    private int runCount(String sql, int eventManagerId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventManagerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return 0;
                }
                return rs.getInt(1);
            }
        }
    }

    private List<Map<String, Object>> runListQuery(String sql, int eventManagerId) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventManagerId);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    for (int i = 1; i <= cols; i++) {
                        String label = meta.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        if (label == null || label.trim().isEmpty()) {
                            label = meta.getColumnName(i);
                        }
                        if (label == null || label.trim().isEmpty()) {
                            continue;
                        }
                        row.put(label, value);
                        row.putIfAbsent(label.toLowerCase(), value);
                        row.putIfAbsent(label.toUpperCase(), value);
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
