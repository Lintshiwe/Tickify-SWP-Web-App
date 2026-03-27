package za.ac.tut.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.databaseManagement.AdminITDAO;

public class AdminRoleConsoleServlet extends HttpServlet {

    private static final Set<String> ALLOWED_ROLES = new HashSet<>(Arrays.asList(
            "ADMIN", "VENUE_GUARD", "EVENT_MANAGER", "TERTIARY_PRESENTER"
    ));

    private final AdminITDAO adminITDAO = new AdminITDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String role = normalizeRole(param(request, "role"));
            populateRoleModel(request, role);
            request.getRequestDispatcher("/Admin/AdminRoleConsole.jsp").forward(request, response);
        } catch (SQLException e) {
            log("Failed to load role console", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load role console");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String role = normalizeRole(param(request, "role"));
        String action = param(request, "action");
        Object adminIdObj = request.getSession().getAttribute("userID");
        int adminId = adminIdObj instanceof Integer ? (Integer) adminIdObj : 0;
        boolean isPrivilegedAdmin = false;

        try {
            isPrivilegedAdmin = adminITDAO.isPrivilegedAdmin(adminId);

            if (isMutation(action) && isPrivilegedAdmin) {
                String rootPassword = req(request, "rootPassword");
                if (!adminITDAO.verifyPrivilegedRootPassword(adminId, rootPassword)) {
                    response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=RootAuthFailed");
                    return;
                }
            }

            if ("create".equals(action)) {
                if (!adminITDAO.hasCampusAccessForRoleMutation(adminId, role, null,
                        parseOptionalInt(param(request, "eventID")),
                        parseOptionalInt(param(request, "venueID")),
                        parseOptionalInt(param(request, "venueGuardID")))) {
                    response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=CampusScopeDenied");
                    return;
                }
                createByRole(request, adminId, role);
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&msg=UserCreated");
                return;
            }

            if ("update".equals(action)) {
                Integer targetId = parseInt(req(request, "id"));
                if (!adminITDAO.hasCampusAccessForRoleMutation(adminId, role, targetId,
                        parseOptionalInt(param(request, "eventID")),
                        parseOptionalInt(param(request, "venueID")),
                        parseOptionalInt(param(request, "venueGuardID")))) {
                    response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=CampusScopeDenied");
                    return;
                }
                boolean ok = adminITDAO.updateUserByRole(
                        adminId,
                        role,
                        targetId,
                        req(request, "firstName"),
                        req(request, "lastName"),
                        req(request, "email").toLowerCase(),
                        parseOptionalInt(param(request, "eventID")),
                        parseOptionalInt(param(request, "venueID")),
                        parseOptionalInt(param(request, "venueGuardID")),
                        param(request, "tertiaryInstitution")
                );
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&msg=" + (ok ? "UserUpdated" : "NoChange"));
                return;
            }

            if ("delete".equals(action)) {
                int targetId = parseInt(req(request, "id"));
                if (isPrivilegedAdmin) {
                    boolean ok = adminITDAO.deleteByRole(adminId, role, targetId);
                    response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&msg=" + (ok ? "UserDeleted" : "NoChange"));
                    return;
                }
                boolean requested = adminITDAO.createDeletionRequest(adminId, role, targetId, param(request, "reason"));
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&msg=" + (requested ? "DeleteRequested" : "NoChange"));
                return;
            }

            if ("resolveDeleteRequest".equals(action)) {
                if (!isPrivilegedAdmin) {
                    response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=PrivilegedRequired");
                    return;
                }
                int requestId = parseInt(req(request, "deleteRequestID"));
                boolean approve = "approve".equalsIgnoreCase(req(request, "decision"));
                boolean ok = adminITDAO.resolveDeletionRequest(adminId, requestId, approve, param(request, "resolutionNote"));
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&msg=" + (ok ? (approve ? "DeleteApproved" : "DeleteRejected") : "NoChange"));
                return;
            }

            if ("lock".equals(action) || "unlock".equals(action)) {
                boolean lock = "lock".equals(action);
                int targetId = parseInt(req(request, "id"));
                if (!adminITDAO.hasCampusAccessForRoleMutation(adminId, role, targetId, null, null, null)) {
                    response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=CampusScopeDenied");
                    return;
                }
                boolean ok = adminITDAO.setAccountLock(adminId, role, targetId, lock);
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&msg=" + (ok ? (lock ? "UserLocked" : "UserUnlocked") : "NoChange"));
                return;
            }

            if ("resetPassword".equals(action)) {
                int targetId = parseInt(req(request, "id"));
                if (!adminITDAO.hasCampusAccessForRoleMutation(adminId, role, targetId, null, null, null)) {
                    response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=CampusScopeDenied");
                    return;
                }
                boolean ok = adminITDAO.resetPassword(adminId, role, targetId, req(request, "temporaryPassword"));
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&msg=" + (ok ? "PasswordReset" : "NoChange"));
                return;
            }

            if ("reassignRole".equals(action)) {
                String targetRole = normalizeRole(req(request, "targetRole"));
                int sourceId = parseInt(req(request, "sourceId"));
                if (!adminITDAO.hasCampusAccessForRoleMutation(adminId, role, sourceId,
                        parseOptionalInt(param(request, "eventID")),
                        parseOptionalInt(param(request, "venueID")),
                        parseOptionalInt(param(request, "venueGuardID")))) {
                    response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=CampusScopeDenied");
                    return;
                }
                boolean ok = adminITDAO.reassignRole(
                        adminId,
                        role,
                        sourceId,
                        targetRole,
                        parseOptionalInt(param(request, "eventID")),
                        parseOptionalInt(param(request, "venueID")),
                        parseOptionalInt(param(request, "venueGuardID")),
                        param(request, "tertiaryInstitution")
                );
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&msg=" + (ok ? "RoleReassigned" : "NoChange"));
                return;
            }

            response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=UnknownAction");
        } catch (IllegalArgumentException ex) {
            response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=MissingFields");
        } catch (SQLException ex) {
            log("Role console operation failed", ex);
            if (ex.getMessage() != null && ex.getMessage().contains("CampusScopeDenied")) {
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=CampusScopeDenied");
                return;
            }
            if (ex.getMessage() != null && ex.getMessage().contains("PrivilegedRequired")) {
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=PrivilegedRequired");
                return;
            }
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("invalid")) {
                response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=InvalidAssignment");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/AdminRoleConsole.do?role=" + role + "&err=OperationFailed");
        }
    }

    private void createByRole(HttpServletRequest request, int adminId, String role) throws SQLException {
        String firstName = req(request, "firstName");
        String lastName = req(request, "lastName");
        String email = req(request, "email").toLowerCase();
        String password = req(request, "password");

        switch (role) {
            case "ADMIN":
                adminITDAO.createAdmin(adminId, firstName, lastName, email, password, parseInt(req(request, "eventID")));
                break;
            case "VENUE_GUARD":
                adminITDAO.createGuard(adminId, firstName, lastName, email, password,
                        parseInt(req(request, "eventID")), parseInt(req(request, "venueID")));
                break;
            case "EVENT_MANAGER":
                adminITDAO.createManager(adminId, firstName, lastName, email, password,
                        parseInt(req(request, "venueGuardID")));
                break;
            case "TERTIARY_PRESENTER":
                adminITDAO.createPresenter(adminId, firstName, lastName, email, password,
                        req(request, "tertiaryInstitution"),
                        parseInt(req(request, "eventID")), parseInt(req(request, "venueID")));
                break;
            default:
                throw new IllegalArgumentException("Unsupported role");
        }
    }

    private void populateRoleModel(HttpServletRequest request, String role) throws SQLException {
        Object adminIdObj = request.getSession().getAttribute("userID");
        int adminId = adminIdObj instanceof Integer ? (Integer) adminIdObj : 0;
        request.setAttribute("role", role);
        request.setAttribute("isPrivilegedAdmin", adminITDAO.isPrivilegedAdmin(adminId));
        request.setAttribute("events", adminITDAO.getEventOptionsForScope(adminId));
        request.setAttribute("venues", adminITDAO.getVenueOptionsForScope(adminId));
        request.setAttribute("guardOptions", adminITDAO.getGuardOptionsForScope(adminId));
        request.setAttribute("deleteRequests", adminITDAO.getDeletionRequests(adminId));

        if ("ADMIN".equals(role)) {
            request.setAttribute("records", adminITDAO.getAdminsForScope(adminId));
        } else if ("VENUE_GUARD".equals(role)) {
            request.setAttribute("records", adminITDAO.getGuardsForScope(adminId));
        } else if ("EVENT_MANAGER".equals(role)) {
            request.setAttribute("records", adminITDAO.getManagersForScope(adminId));
        } else {
            request.setAttribute("records", adminITDAO.getPresentersForScope(adminId));
        }
    }

    private boolean isMutation(String action) {
        return "create".equals(action)
                || "update".equals(action)
                || "delete".equals(action)
                || "lock".equals(action)
                || "unlock".equals(action)
                || "resetPassword".equals(action)
                || "reassignRole".equals(action)
                || "resolveDeleteRequest".equals(action);
    }

    private String normalizeRole(String role) {
        String value = role == null ? "ADMIN" : role.trim().toUpperCase();
        if (!ALLOWED_ROLES.contains(value)) {
            return "ADMIN";
        }
        return value;
    }

    private String req(HttpServletRequest request, String key) {
        String value = param(request, key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing " + key);
        }
        return value;
    }

    private String param(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        return value == null ? null : value.trim();
    }

    private int parseInt(String value) {
        return Integer.parseInt(value.trim());
    }

    private Integer parseOptionalInt(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Integer.parseInt(value);
    }
}
