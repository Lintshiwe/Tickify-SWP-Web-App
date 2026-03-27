package za.ac.tut.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.databaseManagement.EventManagerDashboardDAO;

public class EventManagerDashboardServlet extends HttpServlet {

    private final EventManagerDashboardDAO dao = new EventManagerDashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object userIdObj = request.getSession().getAttribute("userID");
        int eventManagerId = userIdObj instanceof Integer ? (Integer) userIdObj : 0;

        if (eventManagerId <= 0) {
            response.sendRedirect(request.getContextPath() + "/Login.jsp?err=SessionExpired");
            return;
        }

        try {
            Map<String, Object> profile = dao.getManagerProfile(eventManagerId);
            List<Map<String, Object>> assignedEvents = dao.getAssignedEvents(eventManagerId);
            List<Map<String, Object>> guardCoverage = dao.getVenueGuardCoverage(eventManagerId);
            List<Map<String, Object>> presenterSessions = dao.getPresenterSessions(eventManagerId);

            int invalidScans24h = dao.countInvalidScansLast24h(eventManagerId);
            int validScans24h = dao.countValidScansLast24h(eventManagerId);
            int eventsWithoutTickets = dao.countEventsWithoutTickets(eventManagerId);
            int guardsWithoutScans = dao.countGuardsWithNoScans(eventManagerId);
            int presentersWithoutEvent = dao.countPresentersWithoutMappedEvent(eventManagerId);

            request.setAttribute("managerProfile", profile);
            request.setAttribute("assignedEvents", assignedEvents);
            request.setAttribute("guardCoverage", guardCoverage);
            request.setAttribute("presenterSessions", presenterSessions);

            request.setAttribute("eventCount", assignedEvents.size());
            request.setAttribute("guardCount", guardCoverage.size());
            request.setAttribute("presenterCount", presenterSessions.size());
            request.setAttribute("invalidScans24h", invalidScans24h);
            request.setAttribute("validScans24h", validScans24h);

            request.setAttribute("planningItems", buildPlanningItems(eventsWithoutTickets, guardsWithoutScans, presentersWithoutEvent));
            request.setAttribute("riskItems", buildRiskItems(invalidScans24h, validScans24h));

            request.getRequestDispatcher("/EventManager/EventManagerDashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            log("Failed to load Event Manager dashboard", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load Event Manager dashboard");
        }
    }

    private List<Map<String, Object>> buildPlanningItems(int eventsWithoutTickets, int guardsWithoutScans,
            int presentersWithoutEvent) {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(buildItem(
                eventsWithoutTickets > 0 ? "Ticket setup pending for " + eventsWithoutTickets + " event(s)."
                        : "All assigned events already have ticket templates.",
                eventsWithoutTickets > 0 ? "attention" : "ok"
        ));
        items.add(buildItem(
                guardsWithoutScans > 0 ? guardsWithoutScans + " guard profile(s) have no scan activity yet."
                        : "All guard profiles show scan activity.",
                guardsWithoutScans > 0 ? "attention" : "ok"
        ));
        items.add(buildItem(
                presentersWithoutEvent > 0 ? presentersWithoutEvent + " presenter profile(s) need event mapping."
                        : "All presenter profiles are mapped to events.",
                presentersWithoutEvent > 0 ? "attention" : "ok"
        ));
        return items;
    }

    private List<Map<String, Object>> buildRiskItems(int invalidScans24h, int validScans24h) {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(buildItem("Invalid scans in last 24h: " + invalidScans24h,
                invalidScans24h > 0 ? "attention" : "ok"));
        items.add(buildItem("Valid scans in last 24h: " + validScans24h,
                "ok"));
        items.add(buildItem(
                invalidScans24h > 15
                        ? "Scanner rejection rate is elevated. Review gate operations immediately."
                        : "Scanner rejection rate is within expected range.",
                invalidScans24h > 15 ? "attention" : "ok"
        ));
        return items;
    }

    private Map<String, Object> buildItem(String text, String state) {
        Map<String, Object> row = new HashMap<>();
        row.put("text", text);
        row.put("state", state);
        return row;
    }
}
