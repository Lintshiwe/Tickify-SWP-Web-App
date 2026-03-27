package za.ac.tut.servlet;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import za.ac.tut.databaseManagement.EventDAO;
import za.ac.tut.entities.Event;

@MultipartConfig(maxFileSize = 12 * 1024 * 1024)
public class AdminEventAlbumServlet extends HttpServlet {

    private static final int TARGET_WIDTH = 960;
    private static final int TARGET_HEIGHT = 700;

    private final EventDAO eventDAO = new EventDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Event> events = eventDAO.getAllEvents();
            request.setAttribute("events", events);
            request.getRequestDispatcher("/Admin/AdminEventAlbum.jsp").forward(request, response);
        } catch (SQLException e) {
            log("Unable to load events for album management", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int eventID;
        try {
            eventID = Integer.parseInt(request.getParameter("eventID"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/AdminEventAlbum.do?err=InvalidEvent");
            return;
        }

        Part imagePart = request.getPart("eventAlbumImage");
        if (imagePart == null || imagePart.getSize() == 0) {
            response.sendRedirect(request.getContextPath() + "/AdminEventAlbum.do?err=MissingImage");
            return;
        }

        String contentType = imagePart.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            response.sendRedirect(request.getContextPath() + "/AdminEventAlbum.do?err=InvalidImage");
            return;
        }

        byte[] imageBytes;
        try {
            imageBytes = imagePart.getInputStream().readAllBytes();
        } catch (IOException e) {
            response.sendRedirect(request.getContextPath() + "/AdminEventAlbum.do?err=ImageRead");
            return;
        }

        try {
            imageBytes = optimizeForTicketAlbum(imageBytes, contentType);
        } catch (IOException e) {
            response.sendRedirect(request.getContextPath() + "/AdminEventAlbum.do?err=ImageRead");
            return;
        }

        try {
            boolean updated = eventDAO.updateEventAlbumImage(
                    eventID,
                    imagePart.getSubmittedFileName(),
                    contentType,
                    imageBytes
            );
            response.sendRedirect(request.getContextPath() + (updated
                    ? "/AdminEventAlbum.do?msg=Uploaded"
                    : "/AdminEventAlbum.do?err=UploadFailed"));
        } catch (SQLException e) {
            log("Unable to save event album image", e);
            response.sendRedirect(request.getContextPath() + "/AdminEventAlbum.do?err=UploadFailed");
        }
    }

    private byte[] optimizeForTicketAlbum(byte[] originalBytes, String mimeType) throws IOException {
        if (originalBytes == null || originalBytes.length == 0) {
            return originalBytes;
        }

        // Keep SVG uploads as-is; raster optimization is handled by ImageIO.
        if (mimeType != null && mimeType.toLowerCase().contains("svg")) {
            return originalBytes;
        }

        BufferedImage source = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (source == null) {
            return originalBytes;
        }

        double targetRatio = (double) TARGET_WIDTH / (double) TARGET_HEIGHT;
        int srcW = source.getWidth();
        int srcH = source.getHeight();
        double srcRatio = (double) srcW / (double) srcH;

        int cropW = srcW;
        int cropH = srcH;
        int x = 0;
        int y = 0;

        if (srcRatio > targetRatio) {
            cropW = (int) Math.round(srcH * targetRatio);
            x = Math.max(0, (srcW - cropW) / 2);
        } else if (srcRatio < targetRatio) {
            cropH = (int) Math.round(srcW / targetRatio);
            y = Math.max(0, (srcH - cropH) / 2);
        }

        BufferedImage cropped = source.getSubimage(x, y, cropW, cropH);
        BufferedImage resized = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(cropped, 0, 0, TARGET_WIDTH, TARGET_HEIGHT, null);
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String format = (mimeType != null && mimeType.toLowerCase().contains("png")) ? "png" : "jpg";
        ImageIO.write(resized, format, out);
        return out.toByteArray();
    }
}
