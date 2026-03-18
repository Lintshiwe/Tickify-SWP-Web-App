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
import za.ac.tut.databaseManagement.AttendeeDAO;


public class AttendeeDashboardServlet extends HttpServlet {


   @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    
    try {
        AttendeeDAO attendeeDAO = new AttendeeDAO();
        
        // 1. Fetch events using the method we added to AttendeeDAO
        List<Map<String, Object>> eventList = attendeeDAO.getAllEvents();
        
        // 2. Set the list as a request attribute
        // The name "eventList" must match the 'items' in the <c:forEach> in your JSP
        request.setAttribute("eventList", eventList);
        
        // 3. Forward to the JSP
        request.getRequestDispatcher("/Attendee/AttendeeDashboard.jsp").forward(request, response);
        
    } catch (SQLException e) {
        log("Error fetching events: " + e.getMessage());
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}

 
  
}
