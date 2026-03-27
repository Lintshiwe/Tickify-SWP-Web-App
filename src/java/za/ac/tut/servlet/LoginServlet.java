package za.ac.tut.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.databaseManagement.UserDAO;

public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000L;
    private static final long LOCK_MS = 15 * 60 * 1000L;
    private static final String PRIVILEGED_ADMIN_EMAIL = "admin@tickify.ac.za";
    private static final ConcurrentMap<String, AttemptRecord> ACCOUNT_ATTEMPTS = new ConcurrentHashMap<>();
    
    // Role configuration map for dynamic redirection and data fetching
    private static final Map<String, RoleConfig> ROLE_MAP = new HashMap<>();

    static {
        ROLE_MAP.put("ADMIN", new RoleConfig("admin", "adminID", "/AdminDashboard.do"));
        ROLE_MAP.put("EVENT_MANAGER", new RoleConfig("event_manager", "eventManagerID", "/EventManagerDashboard.do"));
        ROLE_MAP.put("VENUE_GUARD", new RoleConfig("venue_guard", "venueGuardID", "/VenueGuard/VenueGuardDashboard.jsp"));
        ROLE_MAP.put("TERTIARY_PRESENTER", new RoleConfig("tertiary_presenter", "tertiaryPresenterID", "/TertiaryPresenterDashboard.do"));
        ROLE_MAP.put("ATTENDEE", new RoleConfig("attendee", "attendeeID", "/AttendeeDashboardServlet.do"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Capture parameters from the form
        String loginId = req.getParameter("loginId");
        if (loginId == null || loginId.trim().isEmpty()) {
            loginId = req.getParameter("email");
        }
        String password = req.getParameter("password");
        String chosenRole = req.getParameter("userRole"); // Captured from the Radio Buttons
        chosenRole = normalizeRole(chosenRole);
        String normalizedLoginId = loginId != null ? loginId.trim().toLowerCase() : null;

        // 2. Input Validation
        if (loginId == null || loginId.trim().isEmpty() || 
            password == null || password.trim().isEmpty() ||
            chosenRole == null || !ROLE_MAP.containsKey(chosenRole)) {
            handleError(req, resp, "All fields and a role selection are required.");
            return;
        }

        String accountKey = chosenRole + "|" + normalizedLoginId;
        if (isLocked(ACCOUNT_ATTEMPTS, accountKey)) {
            handleError(req, resp, "Too many failed login attempts. Please wait 15 minutes and try again.");
            return;
        }

        try {
            /* * 3. Targeted Authentication
             * Instead of looping, we check the specific table chosen by the user.
             * Make sure you have added 'authenticateSpecific' to your UserDAO.
             */
            String role = userDAO.authenticateSpecific(loginId, password, chosenRole);

            if (role == null) {
                boolean accountThresholdTriggered = registerFailure(ACCOUNT_ATTEMPTS, accountKey);
                String sourceIp = extractClientIp(req);

                String normalizedRole = chosenRole.trim().toUpperCase();
                if (accountThresholdTriggered && !"ADMIN".equals(normalizedRole)) {
                    int knownUserId = userDAO.resolveUserIdForRoleIdentifier(normalizedRole, loginId);
                    if (knownUserId > 0) {
                        userDAO.lockAccountForFailedAuth(normalizedRole, knownUserId, normalizedLoginId, sourceIp);
                        String subjectName = "";
                        RoleConfig failedRoleConfig = ROLE_MAP.get(normalizedRole);
                        if (failedRoleConfig != null) {
                            subjectName = userDAO.getDisplayNameByIdentifier(loginId, failedRoleConfig.table, supportsUsername(normalizedRole));
                        }
                        if (subjectName != null && !subjectName.trim().isEmpty()) {
                            handleError(req, resp, "Profile for " + subjectName.trim() + " has been locked after repeated failed authorization attempts. Admin has been alerted and must unblock the account.");
                        } else {
                            handleError(req, resp, "This profile has been locked after repeated failed authorization attempts. Admin has been alerted and must unblock the account.");
                        }
                        return;
                    }
                }
                handleError(req, resp, "Invalid username/email, password, or role selection.");
                return;
            }

            clearFailures(ACCOUNT_ATTEMPTS, accountKey);

            // 4. Retrieve metadata for the authenticated role
            RoleConfig config = ROLE_MAP.get(role);
            boolean allowUsername = supportsUsername(role);

            // 5. Fetch specific user details from the correct table
            int uid = userDAO.getUserIDByIdentifier(loginId, config.table, config.idCol, allowUsername);
            if (uid <= 0) {
                handleError(req, resp, "Unable to resolve user account.");
                return;
            }

            String displayName = userDAO.getDisplayNameByIdentifier(loginId, config.table, allowUsername);

            if (userDAO.isAccountLocked(role, uid)) {
                if (displayName != null && !displayName.trim().isEmpty()) {
                    handleError(req, resp, "Profile for " + displayName.trim() + " is locked. Please contact an admin to unblock your account.");
                } else {
                    handleError(req, resp, "This profile is locked. Please contact an admin to unblock your account.");
                }
                return;
            }

            String resolvedEmail = userDAO.getEmailByIdentifier(loginId, config.table, allowUsername);
            String fullName = userDAO.getFullNameByIdentifier(loginId, config.table, allowUsername);
            String campusName = userDAO.getCampusNameForRole(role, uid);
            if ("ADMIN".equals(role) && resolvedEmail != null
                    && PRIVILEGED_ADMIN_EMAIL.equalsIgnoreCase(resolvedEmail.trim())) {
                campusName = "Tickify Admin";
            }
            String roleNumberLabel = buildRoleNumberLabel(role, uid);

            // 6. Secure Session Management
            HttpSession session = req.getSession(true);
            session.setAttribute("userEmail", resolvedEmail);
            session.setAttribute("userRole", role);
            session.setAttribute("userID", uid);
            session.setAttribute("userFullName", displayName);
            session.setAttribute("userLegalName", fullName);
            session.setAttribute("userLoginId", loginId.trim());
            session.setAttribute("userCampusName", campusName != null && !campusName.trim().isEmpty() ? campusName.trim() : "Campus Unassigned");
            session.setAttribute("userRoleNumberLabel", roleNumberLabel);
            
            // Security best practice: rotate session ID on login
            req.changeSessionId(); 

            // 7. Redirect to the appropriate dashboard
            resp.sendRedirect(req.getContextPath() + config.redirectPath);

        } catch (SQLException e) {
            log("Database error: " + e.getMessage());
            handleError(req, resp, "System connection issue. Please try again later.");
        } catch (Exception e) {
            log("General error: " + e.getMessage());
            handleError(req, resp, "An unexpected error occurred.");
        }
    }

    private void handleError(HttpServletRequest req, HttpServletResponse resp, String message) 
            throws ServletException, IOException {
        req.setAttribute("error", message);
        req.getRequestDispatcher("Login.jsp").forward(req, resp);
    }

    private String extractClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private boolean isLocked(ConcurrentMap<String, AttemptRecord> tracker, String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        AttemptRecord rec = tracker.get(key);
        if (rec == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (rec.lockUntil > now) {
            return true;
        }

        if (rec.lockUntil > 0 && rec.lockUntil <= now) {
            tracker.remove(key, rec);
        }
        return false;
    }

    private boolean registerFailure(ConcurrentMap<String, AttemptRecord> tracker, String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        long now = System.currentTimeMillis();
        final boolean[] thresholdTriggered = new boolean[]{false};
        tracker.compute(key, (k, rec) -> {
            AttemptRecord current = rec;
            if (current == null || now - current.windowStart > WINDOW_MS) {
                current = new AttemptRecord();
                current.windowStart = now;
                current.failedCount = 1;
            } else {
                current.failedCount++;
            }

            if (current.failedCount >= MAX_FAILED_ATTEMPTS) {
                current.lockUntil = now + LOCK_MS;
                current.failedCount = 0;
                current.windowStart = now;
                thresholdTriggered[0] = true;
            }

            return current;
        });
        return thresholdTriggered[0];
    }

    private void clearFailures(ConcurrentMap<String, AttemptRecord> tracker, String key) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }
        tracker.remove(key);
    }

    private boolean supportsUsername(String role) {
        return "ATTENDEE".equals(role) || "TERTIARY_PRESENTER".equals(role);
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        if ("PRESENTER".equals(normalized)) {
            return "TERTIARY_PRESENTER";
        }
        if ("MANAGER".equals(normalized)) {
            return "EVENT_MANAGER";
        }
        if ("GUARD".equals(normalized)) {
            return "VENUE_GUARD";
        }
        return normalized;
    }

    private String buildRoleNumberLabel(String role, int uid) {
        if (uid <= 0) {
            return "Profile #0";
        }
        if ("ADMIN".equals(role)) {
            return "Admin #" + uid;
        }
        if ("EVENT_MANAGER".equals(role)) {
            return "Manager #" + uid;
        }
        if ("TERTIARY_PRESENTER".equals(role)) {
            return "Presenter #" + uid;
        }
        if ("VENUE_GUARD".equals(role)) {
            return "Guard #" + uid;
        }
        if ("ATTENDEE".equals(role)) {
            return "Attendee #" + uid;
        }
        return "Profile #" + uid;
    }

    private static class AttemptRecord {
        long windowStart;
        int failedCount;
        long lockUntil;
    }

    private static class RoleConfig {
        String table, idCol, redirectPath;
        RoleConfig(String table, String idCol, String redirectPath) {
            this.table = table;
            this.idCol = idCol;
            this.redirectPath = redirectPath;
        }
    }
}