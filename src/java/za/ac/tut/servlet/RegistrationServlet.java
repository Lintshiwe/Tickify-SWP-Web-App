package za.ac.tut.servlet;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.databaseManagement.AttendeeDAO;
import za.ac.tut.databaseManagement.TertiaryPresenterDAO;
import za.ac.tut.databaseManagement.UserDAO;
import za.ac.tut.security.PasswordUtil;
import za.ac.tut.entities.Attendee;
import za.ac.tut.entities.TertiaryPresenter;
import za.ac.tut.entities.QRCode;

public class RegistrationServlet extends HttpServlet {

    private static final int MAX_SIGNUP_ATTEMPTS = -1;
    private static final long WINDOW_MS = 15 * 60 * 1000L;
    private static final long LOCK_MS = 15 * 60 * 1000L;
    private static final ConcurrentMap<String, AttemptRecord> SIGNUP_ATTEMPTS = new ConcurrentHashMap<>();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String ipKey = extractClientIp(request);
        if (isLocked(ipKey)) {
            response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=RateLimit");
            return;
        }

        String website = request.getParameter("website");
        if (website != null && !website.trim().isEmpty()) {
            registerAttempt(ipKey);
            response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=DBFail");
            return;
        }
        
        String role = request.getParameter("userRole");
        String fname = request.getParameter("firstname");
        String lname = request.getParameter("lastname");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String pass = request.getParameter("password");
        String phoneNumber = request.getParameter("phoneNumber");
        String biography = request.getParameter("biography");

        String normalizedRole = normalizeRole(role);
        String normalizedUsername = trimToNull(username);
        String normalizedEmail = trimToNull(email);

        if (isBlank(normalizedRole) || isBlank(fname) || isBlank(lname) || isBlank(normalizedUsername)
                || isBlank(pass) || pass.length() < 8 || !isValidUsername(normalizedUsername)
                || !isEmailOptionalValid(normalizedEmail)) {
            registerAttempt(ipKey);
            response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=Validation");
            return;
        }

        if ("ATTENDEE".equals(normalizedRole)) {
            String clientType = trimToNull(request.getParameter("clientType"));
            String idPassport = trimToNull(request.getParameter("idPassportNumber"));
            Date dob = parseDate(request.getParameter("dateOfBirth"));
            if (clientType == null || idPassport == null || dob == null) {
                registerAttempt(ipKey);
                response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=Validation");
                return;
            }
        }

        try {
            if (userDAO.identifierExistsInRole(normalizedUsername, normalizedRole)
                    || (normalizedEmail != null && userDAO.identifierExistsInRole(normalizedEmail, normalizedRole))) {
                registerAttempt(ipKey);
                response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=Duplicate");
                return;
            }
        } catch (SQLException e) {
            registerAttempt(ipKey);
            response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=SQLCrash");
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(pass);

        try {
            boolean success = false;

            if ("ATTENDEE".equals(normalizedRole)) {
                Attendee attendee = new Attendee();
                attendee.setFirstname(fname);
                attendee.setLastname(lname);
                attendee.setUsername(normalizedUsername);
                attendee.setEmail(normalizedEmail);
                attendee.setPassword(hashedPassword);
                attendee.setClientType(trimToNull(request.getParameter("clientType")));
                attendee.setTertiaryInstitution(trimToNull(request.getParameter("attendeeInstitution")));
                attendee.setPhoneNumber(phoneNumber);
                attendee.setStudentNumber(trimToNull(request.getParameter("studentNumber")));
                attendee.setIdPassportNumber(trimToNull(request.getParameter("idPassportNumber")));
                attendee.setDateOfBirth(parseDate(request.getParameter("dateOfBirth")));
                attendee.setBiography(biography);
                
                // Handling the mandatory QRCode for your entity
                QRCode qr = new QRCode(); 
                qr.setQrCodeID(1); 
                attendee.setQrCode(qr);

                success = new AttendeeDAO().insertAttendee(attendee);

            } else if ("TERTIARY_PRESENTER".equals(normalizedRole)) {
                TertiaryPresenter presenter = new TertiaryPresenter();
                presenter.setFirstname(fname);
                presenter.setLastname(lname);
                presenter.setUsername(normalizedUsername);
                presenter.setEmail(normalizedEmail);
                presenter.setPassword(hashedPassword);
                presenter.setTertiaryInstitution(trimToNull(request.getParameter("presenterInstitution")));
                presenter.setPhoneNumber(phoneNumber);
                presenter.setBiography(biography);

                success = new TertiaryPresenterDAO().insertPresenter(presenter);
            }

            // REDIRECTION LOGIC - Fixes 404 by using correct filenames
            if (success) {
                clearAttempts(ipKey);
                // Happy Path: Send to Login.jsp
                response.sendRedirect(request.getContextPath() + "/Login.jsp?msg=RegSuccess");
            } else {
                registerAttempt(ipKey);
                // Business Failure: Stay on UserSignUp.jsp
                response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=DBFail");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            registerAttempt(ipKey);
            if (userDAO.isUniqueConstraintViolation(e)) {
                response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=Duplicate");
                return;
            }
            // Database Crash: Stay on UserSignUp.jsp
            response.sendRedirect(request.getContextPath() + "/UserSignUp.jsp?err=SQLCrash");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase();
        if ("PRESENTER".equals(normalized)) {
            return "TERTIARY_PRESENTER";
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isValidUsername(String username) {
        return username != null && username.matches("^[A-Za-z0-9_.-]{4,30}$");
    }

    private boolean isEmailOptionalValid(String email) {
        if (email == null) {
            return true;
        }
        return email.contains("@") && email.length() <= 60;
    }

    private Date parseDate(String rawDate) {
        String value = trimToNull(rawDate);
        if (value == null) {
            return null;
        }
        try {
            return Date.valueOf(LocalDate.parse(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private boolean isLocked(String ipKey) {
        if (MAX_SIGNUP_ATTEMPTS <= 0) {
            return false;
        }

        AttemptRecord rec = SIGNUP_ATTEMPTS.get(ipKey);
        if (rec == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (rec.lockUntil > now) {
            return true;
        }

        if (rec.lockUntil > 0 && rec.lockUntil <= now) {
            SIGNUP_ATTEMPTS.remove(ipKey, rec);
        }
        return false;
    }

    private void registerAttempt(String ipKey) {
        if (MAX_SIGNUP_ATTEMPTS <= 0) {
            return;
        }

        long now = System.currentTimeMillis();
        SIGNUP_ATTEMPTS.compute(ipKey, (k, rec) -> {
            AttemptRecord current = rec;
            if (current == null || now - current.windowStart > WINDOW_MS) {
                current = new AttemptRecord();
                current.windowStart = now;
                current.count = 1;
            } else {
                current.count++;
            }

            if (current.count >= MAX_SIGNUP_ATTEMPTS) {
                current.lockUntil = now + LOCK_MS;
                current.count = 0;
                current.windowStart = now;
            }

            return current;
        });
    }

    private void clearAttempts(String ipKey) {
        SIGNUP_ATTEMPTS.remove(ipKey);
    }

    private static class AttemptRecord {
        long windowStart;
        int count;
        long lockUntil;
    }
}