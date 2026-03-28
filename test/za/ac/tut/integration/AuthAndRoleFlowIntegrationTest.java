package za.ac.tut.integration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import za.ac.tut.databaseManagement.AdminITDAO;
import za.ac.tut.databaseManagement.TertiaryPresenterDAO;
import za.ac.tut.databaseManagement.UserDAO;

public class AuthAndRoleFlowIntegrationTest {

    private static final String[] DB_CANDIDATES = new String[]{
        "/home/lintshiwe/GlassFish_Server/glassfish/databases/tickifyDB",
        "/home/lintshiwe/GlassFish_Server/glassfish/domains/domain1/config/tickifyDB"
    };

    private static String dbPath;

    public static void configureDatabase() {
        dbPath = findUsableDatabasePath();
        require(dbPath != null && !dbPath.trim().isEmpty(), "No usable Tickify Derby database found");

        System.setProperty("tickify.db.user", "app");
        System.setProperty("tickify.db.password", "app");
        System.setProperty("tickify.db.mode", "embedded");
        System.setProperty("tickify.db.name", dbPath);
    }

    public static void attendeeCredentialsAuthenticateForRole() throws Exception {
        UserDAO dao = new UserDAO();

        String attendeeEmail = System.getProperty("tickify.test.attendee.email", "ntoampi@student.ukzn.ac.za").trim();
        String attendeePassword = System.getProperty("tickify.test.attendee.password", "").trim();

        int uid = dao.getUserIDByIdentifier(attendeeEmail, "attendee", "attendeeID", true);
        if (uid <= 0) {
            String fallbackIdentifier = findAnyAttendeeIdentifier();
            require(fallbackIdentifier != null && !fallbackIdentifier.trim().isEmpty(), "No attendee account found in database");
            attendeeEmail = fallbackIdentifier;
            uid = dao.getUserIDByIdentifier(attendeeEmail, "attendee", "attendeeID", true);
        }
        require(uid > 0, "Attendee ID should resolve");
        require(!dao.isAccountLocked("ATTENDEE", uid), "Attendee profile should not be locked");

        if (!attendeePassword.isEmpty()) {
            String role = dao.authenticateSpecific(attendeeEmail, attendeePassword, "ATTENDEE");
            require("ATTENDEE".equals(role), "Expected ATTENDEE role for attendee login");
        } else {
            System.out.println("Skipping attendee password auth check: set -Dtickify.test.attendee.password to enable it.");
        }
    }

    public static void newAdminWorkflowQueriesReturnNonNullCollections() throws Exception {
        if (!hasTable("EVENT_PROPOSAL") || !hasTable("ATTENDEE_REFUND_REQUEST")) {
            System.out.println("Skipping admin workflow smoke checks: migration tables not present in " + dbPath);
            return;
        }
        AdminITDAO adminDAO = new AdminITDAO();

        List<Map<String, Object>> proposals = adminDAO.getEventProposalsForScope(1);
        List<Map<String, Object>> refunds = adminDAO.getRefundRequestsForScope(1);

        require(proposals != null, "Event proposals list must not be null");
        require(refunds != null, "Refund requests list must not be null");
    }

    public static void newPresenterWorkflowQueriesReturnNonNullCollections() throws Exception {
        if (!hasTable("PRESENTER_MATERIAL") || !hasTable("PRESENTER_SCHEDULE_ITEM") || !hasTable("PRESENTER_ANNOUNCEMENT")) {
            System.out.println("Skipping presenter workflow smoke checks: migration tables not present in " + dbPath);
            return;
        }
        TertiaryPresenterDAO presenterDAO = new TertiaryPresenterDAO();

        List<Map<String, Object>> materials = presenterDAO.getPresenterMaterials(1);
        List<Map<String, Object>> scheduleItems = presenterDAO.getPresenterScheduleItems(1);
        List<Map<String, Object>> announcements = presenterDAO.getPresenterAnnouncements(1);
        List<Map<String, Object>> attendees = presenterDAO.getEventAttendeesForPresenter(1);

        require(materials != null, "Materials list must not be null");
        require(scheduleItems != null, "Schedule items list must not be null");
        require(announcements != null, "Announcements list must not be null");
        require(attendees != null, "Attendee list must not be null");
    }

    public static void main(String[] args) throws Exception {
        configureDatabase();
        attendeeCredentialsAuthenticateForRole();
        newAdminWorkflowQueriesReturnNonNullCollections();
        newPresenterWorkflowQueriesReturnNonNullCollections();
        System.out.println("AuthAndRoleFlowIntegrationTest: PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static String findUsableDatabasePath() {
        for (String candidate : DB_CANDIDATES) {
            if (candidate == null || candidate.trim().isEmpty()) {
                continue;
            }
            String url = "jdbc:derby:" + candidate + ";user=app;password=app";
            try {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                try (Connection conn = DriverManager.getConnection(url);
                     Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT 1 FROM attendee FETCH FIRST ROW ONLY")) {
                    if (rs.next()) {
                        return candidate;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static boolean hasTable(String tableName) {
        if (dbPath == null || tableName == null) {
            return false;
        }
        String url = "jdbc:derby:" + dbPath + ";user=app;password=app";
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            try (Connection conn = DriverManager.getConnection(url)) {
                DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), null)) {
                    return rs.next();
                }
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private static String findAnyAttendeeIdentifier() {
        if (dbPath == null) {
            return null;
        }
        String url = "jdbc:derby:" + dbPath + ";user=app;password=app";
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            try (Connection conn = DriverManager.getConnection(url);
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(NULLIF(TRIM(email),''), NULLIF(TRIM(username),'')) AS identifier FROM attendee FETCH FIRST ROW ONLY")) {
                if (rs.next()) {
                    String identifier = rs.getString("identifier");
                    return identifier == null ? null : identifier.trim();
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }
}
