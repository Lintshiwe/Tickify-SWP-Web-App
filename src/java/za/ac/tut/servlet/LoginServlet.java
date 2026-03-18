package za.ac.tut.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.databaseManagement.UserDAO;

public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    
    // Role configuration map for dynamic redirection and data fetching
    private static final Map<String, RoleConfig> ROLE_MAP = new HashMap<>();

    static {
        ROLE_MAP.put("ADMIN", new RoleConfig("admin", "adminID", "/Admin/AdminDashboard.jsp"));
        ROLE_MAP.put("EVENT_MANAGER", new RoleConfig("event_manager", "eventManagerID", "/EventManager/EventManagerDashboard.jsp"));
        ROLE_MAP.put("VENUE_GUARD", new RoleConfig("venue_guard", "venueGuardID", "/VenueGuard/VenueGuardDashboard.jsp"));
        ROLE_MAP.put("TERTIARY_PRESENTER", new RoleConfig("tertiary_presenter", "tertiaryPresenterID", "/TertiaryPresenterDashboard.do"));
        ROLE_MAP.put("ATTENDEE", new RoleConfig("attendee", "attendeeID", "/AttendeeDashboardServlet.do"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Capture parameters from the form
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String chosenRole = req.getParameter("userRole"); // Captured from the Radio Buttons

        // 2. Input Validation
        if (email == null || email.trim().isEmpty() || 
            password == null || password.trim().isEmpty() || 
            chosenRole == null) {
            handleError(req, resp, "All fields and a role selection are required.");
            return;
        }

        try {
            /* * 3. Targeted Authentication
             * Instead of looping, we check the specific table chosen by the user.
             * Make sure you have added 'authenticateSpecific' to your UserDAO.
             */
            String role = userDAO.authenticateSpecific(email, password, chosenRole);

            if (role == null) {
                handleError(req, resp, "Invalid credentials for the selected role: " + chosenRole);
                return;
            }

            // 4. Retrieve metadata for the authenticated role
            RoleConfig config = ROLE_MAP.get(role);

            // 5. Fetch specific user details from the correct table
            int uid = userDAO.getUserID(email, config.table, config.idCol);
            String fname = userDAO.getFullName(email, config.table);

            // 6. Secure Session Management
            HttpSession session = req.getSession(true);
            session.setAttribute("userEmail", email);
            session.setAttribute("userRole", role);
            session.setAttribute("userID", uid);
            session.setAttribute("userFullName", fname);
            
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

    private static class RoleConfig {
        String table, idCol, redirectPath;
        RoleConfig(String table, String idCol, String redirectPath) {
            this.table = table;
            this.idCol = idCol;
            this.redirectPath = redirectPath;
        }
    }
}