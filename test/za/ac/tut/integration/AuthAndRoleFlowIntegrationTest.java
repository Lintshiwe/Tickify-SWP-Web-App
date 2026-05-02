package za.ac.tut.integration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import za.ac.tut.databaseConnection.DatabaseInitializer;
import za.ac.tut.databaseManagement.AdminITDAO;
import za.ac.tut.databaseManagement.TertiaryPresenterDAO;
import za.ac.tut.databaseManagement.UserDAO;

public class AuthAndRoleFlowIntegrationTest {

    private static final String[] DB_CANDIDATES = resolveDatabaseCandidates();

    private static String[] resolveDatabaseCandidates() {
        String envPath = System.getenv("TICKIFY_TEST_DB_PATH");
        if (envPath != null && !envPath.trim().isEmpty()) {
            return new String[]{ envPath.trim() };
        }
        String propPath = System.getProperty("tickify.test.db.path");
        if (propPath != null && !propPath.trim().isEmpty()) {
            return new String[]{ propPath.trim() };
        }
        String userHome = System.getProperty("user.home");
        String glassfishHome = System.getProperty("tickify.glassfish.home",
                System.getenv().getOrDefault("GLASSFISH_HOME", userHome + "/GlassFish_Server"));
        return new String[]{
            userHome + "/.netbeans-derby/tickifyDB",
            glassfishHome + "/glassfish/databases/tickifyDB",
            glassfishHome + "/glassfish/domains/domain1/config/tickifyDB"
        };
    }

    private static String dbPath;

    public static void configureDatabase() {
        dbPath = findUsableDatabasePath();
        require(dbPath != null && !dbPath.trim().isEmpty(), "No usable Tickify Derby database found");

        System.setProperty("tickify.db.user", "app");
        System.setProperty("tickify.db.password", "app");
        System.setProperty("tickify.db.mode", "embedded");
        System.setProperty("tickify.db.name", dbPath);

        // Ensure initialization migrations/repairs run before auth checks.
        DatabaseInitializer.initialize();
    }

    public static void seededRoleCredentialsAuthenticate() throws Exception {
        UserDAO dao = new UserDAO();

        assertRoleLogin(dao, "ADMIN", "ntoampilp@gmail.com", "sudoAdmin1", false);
        assertRoleLogin(dao, "EVENT_MANAGER", "ntoampilp@gmail.com", "manager1", false);
        assertRoleLogin(dao, "VENUE_GUARD", "ntoampilp@gmail.com", "guard1", false);
        assertRoleLogin(dao, "TERTIARY_PRESENTER", "ntoampilp@gmail.com", "presenter1", true);
        assertRoleLogin(dao, "ATTENDEE", "ntoampilp@gmail.com", "attendee1", true);

        System.out.println("Seeded role credential checks passed for ADMIN, EVENT_MANAGER, VENUE_GUARD, TERTIARY_PRESENTER, ATTENDEE.");
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
        seededRoleCredentialsAuthenticate();
        newAdminWorkflowQueriesReturnNonNullCollections();
        newPresenterWorkflowQueriesReturnNonNullCollections();
        System.out.println("AuthAndRoleFlowIntegrationTest: PASS");
    }

    private static void assertRoleLogin(UserDAO dao, String role, String identifier, String password, boolean allowUsername) throws Exception {
        String authenticatedRole = dao.authenticateSpecific(identifier, password, role);
        require(role.equals(authenticatedRole), "Expected " + role + " role for login of " + identifier);

        String table = tableForRole(role);
        String idColumn = idColumnForRole(role);
        int uid = dao.getUserIDByIdentifier(identifier, table, idColumn, allowUsername);
        require(uid > 0, "User ID should resolve for " + role + " login: " + identifier);
        require(!dao.isAccountLocked(role, uid), role + " account should not be locked for " + identifier);
    }

    private static String tableForRole(String role) {
        switch (role) {
            case "ADMIN":
                return "admin";
            case "EVENT_MANAGER":
                return "event_manager";
            case "VENUE_GUARD":
                return "venue_guard";
            case "TERTIARY_PRESENTER":
                return "tertiary_presenter";
            case "ATTENDEE":
                return "attendee";
            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }

    private static String idColumnForRole(String role) {
        switch (role) {
            case "ADMIN":
                return "adminID";
            case "EVENT_MANAGER":
                return "eventManagerID";
            case "VENUE_GUARD":
                return "venueGuardID";
            case "TERTIARY_PRESENTER":
                return "tertiaryPresenterID";
            case "ATTENDEE":
                return "attendeeID";
            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }
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
