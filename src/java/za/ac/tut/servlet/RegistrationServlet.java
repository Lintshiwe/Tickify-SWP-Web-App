package za.ac.tut.servlet;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.databaseManagement.AttendeeDAO;
import za.ac.tut.databaseManagement.TertiaryPresenterDAO;
import za.ac.tut.entities.Attendee;
import za.ac.tut.entities.TertiaryPresenter;
import za.ac.tut.entities.QRCode;

public class RegistrationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String role = request.getParameter("userRole");
        String fname = request.getParameter("firstname");
        String lname = request.getParameter("lastname");
        String email = request.getParameter("email");
        String pass = request.getParameter("password");

        try {
            boolean success = false;

            if ("ATTENDEE".equals(role)) {
                Attendee attendee = new Attendee();
                attendee.setFirstname(fname);
                attendee.setLastname(lname);
                attendee.setEmail(email);
                attendee.setPassword(pass);
                attendee.setTertiaryInstitution(request.getParameter("attendeeInstitution"));
                
                // Handling the mandatory QRCode for your entity
                QRCode qr = new QRCode(); 
                qr.setQrCodeID(1); 
                attendee.setQrCode(qr);

                success = new AttendeeDAO().insertAttendee(attendee);

            } else if ("PRESENTER".equals(role)) {
                TertiaryPresenter presenter = new TertiaryPresenter();
                presenter.setFirstname(fname);
                presenter.setLastname(lname);
                presenter.setEmail(email);
                presenter.setPassword(pass);
                presenter.setTertiaryInstitution(request.getParameter("presenterInstitution"));

                success = new TertiaryPresenterDAO().insertPresenter(presenter);
            }

            // REDIRECTION LOGIC - Fixes 404 by using correct filenames
            if (success) {
                // Happy Path: Send to Login.jsp
                response.sendRedirect(request.getContextPath() + "/Login.jsp?msg=RegSuccess");
            } else {
                // Business Failure: Stay on UserSignUp.jsp
                response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=DBFail");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Database Crash: Stay on UserSignUp.jsp
            response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=SQLCrash");
        }
    }
}