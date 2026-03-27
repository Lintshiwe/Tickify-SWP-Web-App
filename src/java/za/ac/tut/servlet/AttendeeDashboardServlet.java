/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package za.ac.tut.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.databaseManagement.AdvertDAO;
import za.ac.tut.databaseManagement.AttendeeDAO;


public class AttendeeDashboardServlet extends HttpServlet {


   @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    
    try {
        Integer attendeeId = (Integer) request.getSession().getAttribute("userID");
        if (attendeeId == null) {
            response.sendRedirect(request.getContextPath() + "/Login.jsp");
            return;
        }

        AttendeeDAO attendeeDAO = new AttendeeDAO();
        AdvertDAO advertDAO = new AdvertDAO();
        
        List<Map<String, Object>> eventList = attendeeDAO.getAllEventsForAttendee(attendeeId);
        List<Map<String, Object>> wishlistEvents = attendeeDAO.getWishlistEvents(attendeeId);
        List<Map<String, Object>> adverts = advertDAO.getActiveSelectedAdverts();
        Set<Integer> wishlistEventIds = attendeeDAO.getWishlistEventIds(attendeeId);
        HttpSession session = request.getSession();
        Map<Integer, Map<String, Object>> cart = getOrCreateCart(session);
        double checkoutTotal = calculateCartTotal(cart);
        int cartCount = calculateCartCount(cart);

        for (Map<String, Object> event : eventList) {
            Integer eventId = (Integer) event.get("id");
            event.put("wishlisted", eventId != null && wishlistEventIds.contains(eventId));
        }

        for (Map<String, Object> event : wishlistEvents) {
            event.put("wishlisted", true);
        }

        List<Map<String, Object>> nearSoldOutWishlistEvents = new ArrayList<>();
        for (Map<String, Object> event : wishlistEvents) {
            boolean purchased = Boolean.TRUE.equals(event.get("purchased"));
            boolean nearlySoldOut = Boolean.TRUE.equals(event.get("nearlySoldOut"));
            if (!purchased && nearlySoldOut) {
                nearSoldOutWishlistEvents.add(event);
            }
        }
        
        request.setAttribute("eventList", eventList);
        request.setAttribute("wishlistEvents", wishlistEvents);
        request.setAttribute("adverts", adverts);
        request.setAttribute("wishlistEventIds", wishlistEventIds);
        request.setAttribute("nearSoldOutWishlistEvents", nearSoldOutWishlistEvents);
        request.setAttribute("nearSoldOutWishlistCount", nearSoldOutWishlistEvents.size());
        request.setAttribute("checkoutTotal", checkoutTotal);
        request.setAttribute("cartCount", cartCount);
        request.setAttribute("cartItems", cart.values());
        
        request.getRequestDispatcher("/Attendee/AttendeeDashboard.jsp").forward(request, response);
        
    } catch (SQLException e) {
        log("Error fetching events: " + e.getMessage());
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}

    @SuppressWarnings("unchecked")
    private Map<Integer, Map<String, Object>> getOrCreateCart(HttpSession session) {
        Object existing = session.getAttribute("attendeeCart");
        if (existing instanceof Map) {
            return (Map<Integer, Map<String, Object>>) existing;
        }
        Map<Integer, Map<String, Object>> cart = new HashMap<>();
        session.setAttribute("attendeeCart", cart);
        return cart;
    }

    private int calculateCartCount(Map<Integer, Map<String, Object>> cart) {
        int count = 0;
        for (Map<String, Object> item : cart.values()) {
            count += (Integer) item.get("quantity");
        }
        return count;
    }

    private double calculateCartTotal(Map<Integer, Map<String, Object>> cart) {
        double total = 0.0;
        for (Map<String, Object> item : cart.values()) {
            double price = ((Number) item.get("price")).doubleValue();
            int quantity = (Integer) item.get("quantity");
            total += price * quantity;
        }
        return total;
    }

}
