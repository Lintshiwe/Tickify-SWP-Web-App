package za.ac.tut.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.databaseManagement.AdminITDAO;
import za.ac.tut.notification.EmailService;

public class AdminDashboardServlet extends HttpServlet {

    private final AdminITDAO adminITDAO = new AdminITDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String export = param(request, "export");
            if ("identities".equals(export)) {
                writeIdentityCsv(response, adminITDAO.getIdentityDirectory());
                return;
            }
            if ("faults".equals(export)) {
                writeFaultCsv(response, adminITDAO.getFaultSignals());
                return;
            }
            if ("finance".equals(export)) {
                writeFinanceCsv(request, response);
                return;
            }
            if ("reconciliation".equals(export)) {
                writeReconciliationCsv(request, response);
                return;
            }

            populateDashboardModel(request);
            request.getRequestDispatcher("/Admin/AdminDashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            log("Failed to load admin dashboard", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load admin dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = param(request, "action");
        Object adminIdObj = request.getSession().getAttribute("userID");
        int adminId = adminIdObj instanceof Integer ? (Integer) adminIdObj : 0;
        boolean isPrivilegedAdmin = false;
        try {
            isPrivilegedAdmin = adminITDAO.isPrivilegedAdmin(adminId);
            if (isPrivilegedOnlyAction(action) && !isPrivilegedAdmin) {
                redirectWithErr(request, response, "PrivilegedRequired");
                return;
            }

            if ("createAdmin".equals(action)) {
                adminITDAO.createAdmin(
                        adminId,
                        req(request, "firstName"),
                        req(request, "lastName"),
                        req(request, "email").toLowerCase(),
                        req(request, "password"),
                        parseInt(req(request, "eventID"))
                );
                redirectWithMsg(request, response, "UserCreated");
                return;
            }

            if ("updateMyProfile".equals(action)) {
                String updatedEmail = req(request, "email").toLowerCase();
                boolean updated = adminITDAO.updateOwnAdminProfile(
                        adminId,
                        req(request, "firstName"),
                        req(request, "lastName"),
                        updatedEmail,
                        param(request, "newPassword")
                );
                if (updated) {
                    HttpSession session = request.getSession();
                    String legalName = req(request, "firstName") + " " + req(request, "lastName");
                    session.setAttribute("userEmail", updatedEmail);
                    session.setAttribute("userLoginId", updatedEmail);
                    session.setAttribute("userFullName", legalName);
                    session.setAttribute("userLegalName", legalName);
                    if (adminITDAO.isPrivilegedAdmin(adminId)) {
                        session.setAttribute("userCampusName", "Tickify Admin");
                    }
                }
                redirectWithMsg(request, response, updated ? "ProfileUpdated" : "NoChange");
                return;
            }

            if ("createGuard".equals(action)) {
                adminITDAO.createGuard(
                        adminId,
                        req(request, "firstName"),
                        req(request, "lastName"),
                        req(request, "email").toLowerCase(),
                        req(request, "password"),
                        parseInt(req(request, "eventID")),
                        parseInt(req(request, "venueID"))
                );
                redirectWithMsg(request, response, "UserCreated");
                return;
            }

            if ("provisionGuard".equals(action)) {
                adminITDAO.createGuard(
                        adminId,
                        req(request, "firstName"),
                        req(request, "lastName"),
                        req(request, "email").toLowerCase(),
                        req(request, "password"),
                        parseInt(req(request, "eventID")),
                        parseInt(req(request, "venueID"))
                );
                redirectWithMsg(request, response, "GuardProvisioned");
                return;
            }

            if ("createManager".equals(action)) {
                adminITDAO.createManager(
                        adminId,
                        req(request, "firstName"),
                        req(request, "lastName"),
                        req(request, "email").toLowerCase(),
                        req(request, "password"),
                        parseInt(req(request, "venueGuardID"))
                );
                redirectWithMsg(request, response, "UserCreated");
                return;
            }

            if ("provisionManager".equals(action)) {
                adminITDAO.createManager(
                        adminId,
                        req(request, "firstName"),
                        req(request, "lastName"),
                        req(request, "email").toLowerCase(),
                        req(request, "password"),
                        parseInt(req(request, "venueGuardID"))
                );
                redirectWithMsg(request, response, "ManagerProvisioned");
                return;
            }

            if ("createEvent".equals(action)) {
                Timestamp eventTs = parseDateTimeLocal(req(request, "eventDate"));
                boolean ok = adminITDAO.createEvent(
                        adminId,
                        req(request, "eventName"),
                        req(request, "eventType"),
                        eventTs,
                        parseInt(req(request, "venueID"))
                );
                redirectWithMsg(request, response, ok ? "EventCreated" : "NoChange");
                return;
            }

            if ("updateEvent".equals(action)) {
                Timestamp eventTs = parseDateTimeLocal(req(request, "eventDate"));
                boolean ok = adminITDAO.updateEvent(
                        adminId,
                        parseInt(req(request, "eventID")),
                        req(request, "eventName"),
                        req(request, "eventType"),
                        eventTs,
                        parseInt(req(request, "venueID"))
                );
                redirectWithMsg(request, response, ok ? "EventUpdated" : "NoChange");
                return;
            }

            if ("deleteEvent".equals(action)) {
                boolean ok = adminITDAO.deleteEvent(adminId, parseInt(req(request, "eventID")));
                redirectWithMsg(request, response, ok ? "EventDeleted" : "NoChange");
                return;
            }

            if ("createPresenter".equals(action)) {
                adminITDAO.createPresenter(
                        adminId,
                        req(request, "firstName"),
                        req(request, "lastName"),
                        req(request, "email").toLowerCase(),
                        req(request, "password"),
                        req(request, "tertiaryInstitution"),
                        parseInt(req(request, "eventID")),
                        parseInt(req(request, "venueID"))
                );
                redirectWithMsg(request, response, "UserCreated");
                return;
            }

            if ("createEventProposal".equals(action)) {
                boolean ok = adminITDAO.createEventProposal(
                        adminId,
                        req(request, "eventName"),
                        req(request, "eventType"),
                        parseDateTimeLocal(req(request, "eventDate")),
                        parseInt(req(request, "venueID")),
                        param(request, "notes")
                );
                redirectWithMsg(request, response, ok ? "ProposalCreated" : "NoChange");
                return;
            }

            if ("reviewEventProposal".equals(action)) {
                boolean approve = "approve".equalsIgnoreCase(req(request, "decision"));
                boolean ok = adminITDAO.reviewEventProposal(
                        adminId,
                        parseInt(req(request, "proposalID")),
                        approve,
                        param(request, "reviewNote")
                );
                redirectWithMsg(request, response, ok ? (approve ? "ProposalApproved" : "ProposalRejected") : "NoChange");
                return;
            }

            if ("createRefundCase".equals(action)) {
                boolean ok = adminITDAO.createRefundCase(
                        adminId,
                        parseInt(req(request, "attendeeID")),
                        parseOptionalInt(param(request, "orderID")),
                        parseOptionalInt(param(request, "eventID")),
                        param(request, "reason")
                );
                redirectWithMsg(request, response, ok ? "RefundCaseCreated" : "NoChange");
                return;
            }

            if ("resolveRefundCase".equals(action)) {
                boolean approve = "approve".equalsIgnoreCase(req(request, "decision"));
                boolean ok = adminITDAO.resolveRefundCase(
                        adminId,
                        parseInt(req(request, "refundRequestID")),
                        approve,
                        param(request, "resolutionNote")
                );
                redirectWithMsg(request, response, ok ? (approve ? "RefundApproved" : "RefundRejected") : "NoChange");
                return;
            }

            if ("deleteUser".equals(action)) {
                String targetRole = req(request, "role");
                int targetId = parseInt(req(request, "id"));
                if (isPrivilegedAdmin) {
                    boolean deleted = adminITDAO.deleteByRole(adminId, targetRole, targetId);
                    redirectWithMsg(request, response, deleted ? "UserDeleted" : "NoChange");
                    return;
                }
                boolean requested = adminITDAO.createDeletionRequest(adminId, targetRole, targetId, param(request, "reason"));
                redirectWithMsg(request, response, requested ? "DeleteRequested" : "NoChange");
                return;
            }

            if ("resolveDeleteRequest".equals(action)) {
                int requestId = parseInt(req(request, "deleteRequestID"));
                boolean approve = "approve".equalsIgnoreCase(req(request, "decision"));
                boolean resolved = adminITDAO.resolveDeletionRequest(adminId, requestId, approve, param(request, "resolutionNote"));
                redirectWithMsg(request, response, resolved ? (approve ? "DeleteApproved" : "DeleteRejected") : "NoChange");
                return;
            }

            if ("updateUser".equals(action)) {
                boolean updated = adminITDAO.updateUserByRole(
                        adminId,
                        req(request, "role"),
                        parseInt(req(request, "id")),
                        req(request, "firstName"),
                        req(request, "lastName"),
                        req(request, "email").toLowerCase(),
                        parseOptionalInt(param(request, "eventID")),
                        parseOptionalInt(param(request, "venueID")),
                        parseOptionalInt(param(request, "venueGuardID")),
                        param(request, "tertiaryInstitution")
                );
                redirectWithMsg(request, response, updated ? "UserUpdated" : "NoChange");
                return;
            }

            if ("lockUser".equals(action) || "unlockUser".equals(action)) {
                boolean lock = "lockUser".equals(action);
                boolean ok = adminITDAO.setAccountLock(adminId, req(request, "role"), parseInt(req(request, "id")), lock);
                redirectWithMsg(request, response, ok ? (lock ? "UserLocked" : "UserUnlocked") : "NoChange");
                return;
            }

            if ("resetPassword".equals(action)) {
                boolean ok = adminITDAO.resetPassword(
                        adminId,
                        req(request, "role"),
                        parseInt(req(request, "id")),
                        req(request, "temporaryPassword")
                );
                redirectWithMsg(request, response, ok ? "PasswordReset" : "NoChange");
                return;
            }

            if ("reassignRole".equals(action)) {
                boolean ok = adminITDAO.reassignRole(
                        adminId,
                        req(request, "sourceRole"),
                        parseInt(req(request, "sourceId")),
                        req(request, "targetRole"),
                        parseOptionalInt(param(request, "eventID")),
                        parseOptionalInt(param(request, "venueID")),
                        parseOptionalInt(param(request, "venueGuardID")),
                        param(request, "tertiaryInstitution")
                );
                redirectWithMsg(request, response, ok ? "RoleReassigned" : "NoChange");
                return;
            }

            if ("safeDeleteRow".equals(action)) {
                boolean ok = adminITDAO.safeDeleteRow(
                        adminId,
                        req(request, "tableName"),
                        parseInt(req(request, "rowId"))
                );
                redirectWithMsg(request, response, ok ? "RowDeleted" : "NoChange");
                return;
            }

            if ("rotateRootPassword".equals(action)) {
                String currentRootPassword = req(request, "currentRootPassword");
                String newRootPassword = req(request, "newRootPassword");
                String confirmRootPassword = req(request, "confirmRootPassword");
                if (!newRootPassword.equals(confirmRootPassword)) {
                    redirectWithErr(request, response, "RootPasswordMismatch");
                    return;
                }
                boolean ok = adminITDAO.rotateRootPassword(
                        adminId,
                        currentRootPassword,
                        newRootPassword
                );
                if (!ok) {
                    redirectWithErr(request, response, "RootAuthFailed");
                } else {
                    redirectWithMsg(request, response, "RootPasswordRotated");
                }
                return;
            }

            if ("purgeScanLogs".equals(action)) {
                int days = parseInt(param(request, "days"));
                int removed = adminITDAO.purgeOldScanLogs(adminId, days <= 0 ? 30 : days);
                response.sendRedirect(request.getContextPath() + "/AdminDashboard.do?msg=ScanLogsPurged&rows=" + removed);
                return;
            }

            if ("runEmailHealthCheck".equals(action)) {
                String targetEmail = req(request, "healthCheckEmail").toLowerCase();
                String context = request.getContextPath();
                if (context == null || context.isEmpty()) {
                    context = "/";
                }
                if (!context.endsWith("/")) {
                    context = context + "/";
                }

                String resetLink = request.getScheme() + "://" + request.getServerName()
                        + ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort())
                        + context + "ClientPasswordReset.jsp?healthCheck=1";

                try {
                    emailService.sendPasswordResetEmail(targetEmail, resetLink);
                    redirectWithMsg(request, response, "EmailHealthCheckPassed");
                } catch (Exception mailEx) {
                    log("Email health check failed for " + targetEmail, mailEx);
                    redirectWithErr(request, response, "EmailHealthCheckFailed");
                }
                return;
            }

            redirectWithErr(request, response, "UnknownAction");
        } catch (IllegalArgumentException ex) {
            redirectWithErr(request, response, "MissingFields");
        } catch (SQLException ex) {
            log("Admin operation failed", ex);
            if (ex.getMessage() != null && ex.getMessage().contains("MissingFields")) {
                redirectWithErr(request, response, "MissingFields");
                return;
            }
            if (ex.getMessage() != null && ex.getMessage().contains("EmailInUse")) {
                redirectWithErr(request, response, "EmailInUse");
                return;
            }
            if (ex.getMessage() != null && ex.getMessage().contains("CampusScopeDenied")) {
                redirectWithErr(request, response, "CampusScopeDenied");
                return;
            }
            if (ex.getMessage() != null && ex.getMessage().contains("PrivilegedRequired")) {
                redirectWithErr(request, response, "PrivilegedRequired");
                return;
            }
            if (ex.getMessage() != null && ex.getMessage().contains("EventHasSales")) {
                redirectWithErr(request, response, "EventHasSales");
                return;
            }
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("invalid")) {
                redirectWithErr(request, response, "InvalidAssignment");
                return;
            }
            redirectWithErr(request, response, "OperationFailed");
        }
    }

    private void populateDashboardModel(HttpServletRequest request) throws SQLException {
        Object adminIdObj = request.getSession().getAttribute("userID");
        int adminId = adminIdObj instanceof Integer ? (Integer) adminIdObj : 0;
        String previewTable = param(request, "table");
        Integer auditAdminId = parseOptionalInt(param(request, "auditAdminID"));
        String auditAction = param(request, "auditAction");
        String auditFrom = param(request, "auditFrom");
        String auditTo = param(request, "auditTo");
        int eventPageSize = parsePageSize(param(request, "eventPageSize"), 10);
        int eventPage = parsePositiveInt(param(request, "eventPage"), 1);
        int reconPageSize = parsePageSize(param(request, "reconPageSize"), 10);
        int reconPage = parsePositiveInt(param(request, "reconPage"), 1);
        Map<String, Object> preview = adminITDAO.getTablePreview(previewTable);

        int eventTotalRows = adminITDAO.countEventControlRowsForScope(adminId);
        int eventTotalPages = Math.max(1, (int) Math.ceil(eventTotalRows / (double) eventPageSize));
        if (eventPage > eventTotalPages) {
            eventPage = eventTotalPages;
        }

        int reconTotalRows = adminITDAO.countFinancialReconciliationRowsForScope(adminId);
        int reconTotalPages = Math.max(1, (int) Math.ceil(reconTotalRows / (double) reconPageSize));
        if (reconPage > reconTotalPages) {
            reconPage = reconTotalPages;
        }

        boolean isPrivilegedAdmin = adminITDAO.isPrivilegedAdmin(adminId);
        String adminCampusDisplayName = (String) request.getSession().getAttribute("userCampusName");
        if (isPrivilegedAdmin) {
            adminCampusDisplayName = "Tickify Admin";
        }
        if (adminCampusDisplayName == null || adminCampusDisplayName.trim().isEmpty()) {
            adminCampusDisplayName = "Campus Unassigned";
        }

        request.setAttribute("metrics", adminITDAO.getDashboardMetrics());
        request.setAttribute("myAdminProfile", adminITDAO.getAdminProfile(adminId));
        request.setAttribute("admins", adminITDAO.getAdminsForScope(adminId));
        request.setAttribute("guards", adminITDAO.getGuardsForScope(adminId));
        request.setAttribute("managers", adminITDAO.getManagersForScope(adminId));
        request.setAttribute("presenters", adminITDAO.getPresentersForScope(adminId));
        request.setAttribute("events", adminITDAO.getEventOptionsForScope(adminId));
        request.setAttribute("venues", adminITDAO.getVenueOptionsForScope(adminId));
        request.setAttribute("guardOptions", adminITDAO.getGuardOptionsForScope(adminId));
        request.setAttribute("deleteRequests", adminITDAO.getDeletionRequests(adminId));
        request.setAttribute("eventProposals", adminITDAO.getEventProposalsForScope(adminId));
        request.setAttribute("refundRequests", adminITDAO.getRefundRequestsForScope(adminId));
        request.setAttribute("tableSummary", adminITDAO.getUserTableSummary());
        request.setAttribute("faultSignals", adminITDAO.getFaultSignals());
        request.setAttribute("dbTables", adminITDAO.getAllUserTables());
        request.setAttribute("auditActors", adminITDAO.getAdminActors());
        request.setAttribute("auditLogs", adminITDAO.getAuditLogs(auditAdminId, auditAction, auditFrom, auditTo));
        request.setAttribute("auditAdminID", auditAdminId);
        request.setAttribute("auditAction", auditAction);
        request.setAttribute("auditFrom", auditFrom);
        request.setAttribute("auditTo", auditTo);
        request.setAttribute("rootPasswordStatus", adminITDAO.getRootPasswordStatusForAdmin(adminId));
        request.setAttribute("isPrivilegedAdmin", isPrivilegedAdmin);
        request.setAttribute("adminCampusDisplayName", adminCampusDisplayName);
        request.setAttribute("ticketIntelligence", adminITDAO.getTicketIntelligenceForScope(adminId));
        request.setAttribute("campusRevenue", adminITDAO.getCampusRevenueReportForScope(adminId));
        request.setAttribute("campusOwnership", adminITDAO.getCampusOwnershipReportForScope(adminId));
        request.setAttribute("reconciliation", adminITDAO.getFinancialReconciliationForScope(adminId, reconPage, reconPageSize));
        request.setAttribute("eventRows", adminITDAO.getEventControlRowsForScope(adminId, eventPage, eventPageSize));
        request.setAttribute("eventPage", eventPage);
        request.setAttribute("eventPageSize", eventPageSize);
        request.setAttribute("eventTotalRows", eventTotalRows);
        request.setAttribute("eventTotalPages", eventTotalPages);
        request.setAttribute("reconPage", reconPage);
        request.setAttribute("reconPageSize", reconPageSize);
        request.setAttribute("reconTotalRows", reconTotalRows);
        request.setAttribute("reconTotalPages", reconTotalPages);

        request.setAttribute("previewTable", preview.get("table"));
        request.setAttribute("previewColumns", preview.get("columns"));
        request.setAttribute("previewRows", preview.get("rows"));
        request.setAttribute("previewAllowedTables", preview.get("allowedTables"));
        request.setAttribute("safeDeleteTables", preview.get("safeDeleteTables"));
    }

    private Integer parseOptionalInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }

    private void writeIdentityCsv(HttpServletResponse response, List<Map<String, Object>> rows) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=identity-directory.csv");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("role,id,firstname,lastname,email");
            for (Map<String, Object> row : rows) {
                writer.println(csv(row.get("role")) + "," + csv(row.get("uid")) + ","
                        + csv(row.get("firstname")) + "," + csv(row.get("lastname")) + "," + csv(row.get("email")));
            }
        }
    }

    private void writeFaultCsv(HttpServletResponse response, List<Map<String, Object>> rows) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=fault-signals.csv");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("scanLogID,result,reason,scannedAt");
            for (Map<String, Object> row : rows) {
                writer.println(csv(row.get("scanLogID")) + "," + csv(row.get("result")) + ","
                        + csv(row.get("reason")) + "," + csv(row.get("scannedAt")));
            }
        }
    }

    private String csv(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String text = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + text + "\"";
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
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(value.trim());
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value == null ? "" : value.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private int parsePageSize(String value, int fallback) {
        int parsed = parsePositiveInt(value, fallback);
        if (parsed > 100) {
            return 100;
        }
        return parsed;
    }

    private boolean isMutationAction(String action) {
        return "createAdmin".equals(action)
                || "updateMyProfile".equals(action)
                || "createGuard".equals(action)
                || "provisionGuard".equals(action)
                || "createManager".equals(action)
                || "provisionManager".equals(action)
                || "createPresenter".equals(action)
                || "createEventProposal".equals(action)
                || "reviewEventProposal".equals(action)
                || "createRefundCase".equals(action)
                || "resolveRefundCase".equals(action)
                || "createEvent".equals(action)
                || "updateEvent".equals(action)
                || "deleteEvent".equals(action)
                || "deleteUser".equals(action)
                || "updateUser".equals(action)
                || "lockUser".equals(action)
                || "unlockUser".equals(action)
                || "resetPassword".equals(action)
                || "reassignRole".equals(action)
                || "safeDeleteRow".equals(action)
                || "rotateRootPassword".equals(action)
                || "purgeScanLogs".equals(action)
                || "runEmailHealthCheck".equals(action)
                || "resolveDeleteRequest".equals(action);
    }

    private boolean isPrivilegedOnlyAction(String action) {
        return "safeDeleteRow".equals(action)
                || "rotateRootPassword".equals(action)
                || "purgeScanLogs".equals(action)
                || "runEmailHealthCheck".equals(action)
                || "resolveDeleteRequest".equals(action);
    }

    private void redirectWithMsg(HttpServletRequest request, HttpServletResponse response, String msg) throws IOException {
        response.sendRedirect(request.getContextPath() + "/AdminDashboard.do?msg=" + msg);
    }

    private void redirectWithErr(HttpServletRequest request, HttpServletResponse response, String err) throws IOException {
        response.sendRedirect(request.getContextPath() + "/AdminDashboard.do?err=" + err);
    }

    private Timestamp parseDateTimeLocal(String value) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            return Timestamp.valueOf(dateTime);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid event date/time");
        }
    }

    private void writeFinanceCsv(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Object adminIdObj = request.getSession().getAttribute("userID");
        int adminId = adminIdObj instanceof Integer ? (Integer) adminIdObj : 0;
        List<Map<String, Object>> rows = adminITDAO.getCampusRevenueReportForScope(adminId);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=campus-revenue-report.csv");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("\uFEFF");
            writer.println("campusName,campusAddress,ticketsSold,revenue");
            for (Map<String, Object> row : rows) {
                writer.println(csv(row.get("campusName")) + ","
                        + csv(row.get("campusAddress")) + ","
                        + csv(row.get("ticketsSold")) + ","
                        + csv(row.get("revenue")));
            }
        }
    }

    private void writeReconciliationCsv(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Object adminIdObj = request.getSession().getAttribute("userID");
        int adminId = adminIdObj instanceof Integer ? (Integer) adminIdObj : 0;
        List<Map<String, Object>> rows = adminITDAO.getFinancialReconciliationForScope(adminId);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=financial-reconciliation.csv");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("\uFEFF");
            writer.println("campusName,soldTickets,validatedTickets,ticketDelta,recordedRevenue,validatedRevenue,revenueDelta,status");
            for (Map<String, Object> row : rows) {
                writer.println(csv(row.get("campusName")) + ","
                        + csv(row.get("soldTickets")) + ","
                        + csv(row.get("validatedTickets")) + ","
                        + csv(row.get("ticketDelta")) + ","
                        + csv(row.get("recordedRevenue")) + ","
                        + csv(row.get("validatedRevenue")) + ","
                        + csv(row.get("revenueDelta")) + ","
                        + csv(row.get("status")));
            }
        }
    }
}
