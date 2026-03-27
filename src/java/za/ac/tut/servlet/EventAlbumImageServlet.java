package za.ac.tut.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.databaseManagement.EventDAO;

public class EventAlbumImageServlet extends HttpServlet {

    private final EventDAO eventDAO = new EventDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int eventID;
        try {
            eventID = Integer.parseInt(request.getParameter("eventID"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Map<String, Object> image = eventDAO.getEventAlbumImage(eventID);
            if (image == null || image.get("imageData") == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String mimeType = (String) image.get("mimeType");
            byte[] data = (byte[]) image.get("imageData");
            response.setContentType(mimeType != null ? mimeType : "image/jpeg");
            response.setHeader("Cache-Control", "public, max-age=300");
            response.setContentLength(data.length);
            response.getOutputStream().write(data);
        } catch (SQLException e) {
            log("Unable to load event album image", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
