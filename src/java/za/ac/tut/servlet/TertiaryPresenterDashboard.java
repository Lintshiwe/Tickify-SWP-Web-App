/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package za.ac.tut.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.databaseManagement.TertiaryPresenterDAO;

/**
 *
 * @author ntoam
 */
public class TertiaryPresenterDashboard extends HttpServlet {

    private final TertiaryPresenterDAO presenterDAO = new TertiaryPresenterDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object userIdObj = request.getSession().getAttribute("userID");
        int presenterId = userIdObj instanceof Integer ? (Integer) userIdObj : 0;
        if (presenterId <= 0) {
            response.sendRedirect(request.getContextPath() + "/Login.jsp?err=SessionExpired");
            return;
        }

        try {
            Map<String, Object> presenterProfile = presenterDAO.getDashboardProfile(presenterId);
            Map<String, Object> eventSnapshot = presenterDAO.getEventSnapshot(presenterId);
            List<Map<String, Object>> managerContacts = presenterDAO.getPresenterTeamContacts(presenterId);
            List<Map<String, Object>> guardContacts = presenterDAO.getVenueGuardContacts(presenterId);
            List<Map<String, Object>> peerPresenters = presenterDAO.getPeerPresentersAtVenue(presenterId);

            request.setAttribute("presenterProfile", presenterProfile);
            request.setAttribute("eventSnapshot", eventSnapshot);
            request.setAttribute("managerContacts", managerContacts);
            request.setAttribute("guardContacts", guardContacts);
            request.setAttribute("peerPresenters", peerPresenters);

            request.getRequestDispatcher("/Presenter/PresenterDashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            log("Failed to load presenter dashboard", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load presenter dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Routes presenter users to dashboard view";
    }

}
