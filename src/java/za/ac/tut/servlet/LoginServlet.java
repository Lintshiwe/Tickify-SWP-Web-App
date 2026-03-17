package za.ac.tut.servlet;

import java.io.IOException;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.databaseManagement.UserDAO;

 // Using .do extension for professional standard
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Forward to the login page
        req.getRequestDispatcher("Login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            // 1. Authenticate and get Role
            String role = userDAO.authenticate(email, password);

            if (role == null) {
                req.setAttribute("error", "Invalid email or password.");
                req.getRequestDispatcher("Login.jsp").forward(req, resp);
                return;
            }

            // 2. Map Role to Table and ID Column names
            String table;
            String idCol;
            String redirectPath;

            switch (role) {
                case "ADMIN":
                    table = "admin";
                    idCol = "adminID";
                    redirectPath = "/Admin/Dashboard.jsp";
                    break;
                case "EVENT_MANAGER":
                    table = "event_manager";
                    idCol = "eventManagerID";
                    redirectPath = "/EventManager/Dashboard.jsp";
                    break;
                case "VENUE_GUARD":
                    table = "venue_guard";
                    idCol = "venueGuardID";
                    redirectPath = "/VenueGuard/Dashboard.jsp";
                    break;
                case "TERTIARY_PRESENTER":
                    table = "tertiary_presenter";
                    idCol = "tertiaryPresenterID";
                    redirectPath = "/Presenter/Dashboard.jsp";
                    break;
                default: // ATTENDEE
                    table = "attendee";
                    idCol = "attendeeID";
                    redirectPath = "/Attendee/Dashboard.jsp";
                    break;
            }

            // 3. Fetch specific User Data
            int uid = userDAO.getUserID(email, table, idCol);
            // FIXED: Removed idCol argument to match UserDAO.getFullName(String, String)
            String fname = userDAO.getFullName(email, table);

            // 4. Set Session Attributes
            HttpSession session = req.getSession(true);
            session.setAttribute("userEmail", email);
            session.setAttribute("userRole", role);
            session.setAttribute("userID", uid);
            session.setAttribute("userFullName", fname);

            // 5. Final Redirect
            resp.sendRedirect(req.getContextPath() + redirectPath);

        } catch (Exception e) {
            req.setAttribute("error", "System error: " + e.getMessage());
            req.getRequestDispatcher("Login.jsp").forward(req, resp);
        }
    }
}