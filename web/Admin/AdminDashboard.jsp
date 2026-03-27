<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <title>Tickify | Admin Console</title>
    <style>
        :root { --green:#7fc342; --line:#e8ece6; --muted:#5f6f63; --ink:#243228; --ok:#1f7c39; --ok-bg:#eaf7e7; --err:#9b1c1c; --err-bg:#ffecec; }
        * { box-sizing:border-box; }
        body { margin:0; font-family:"Trebuchet MS","Segoe UI",sans-serif; background:#f7faf5; color:var(--ink); }
        .site-header { position:sticky; top:0; z-index:30; background:#f7f8f6; border-bottom:1px solid #dfe5dc; }
        .header-inner { padding:14px clamp(12px,2.7vw,36px); }
        .header-top { display:flex; justify-content:space-between; align-items:center; gap:14px; flex-wrap:wrap; }
        .brand { display:flex; align-items:center; gap:10px; text-decoration:none; }
        .brand-logo { height:50px; width:auto; display:block; }
        .brand-text { font-weight:900; color:#47596b; letter-spacing:.08em; }
        .profile-wrap { position:relative; }
        .profile-btn { display:flex; align-items:center; gap:10px; border:1px solid #d7ded3; background:#fff; border-radius:999px; padding:8px 10px; color:#2a312b; font-weight:700; cursor:pointer; }
        .profile-meta { max-width:0; opacity:0; overflow:hidden; white-space:nowrap; transition:max-width .28s ease, opacity .22s ease; }
        .profile-wrap:hover .profile-meta, .profile-wrap:focus-within .profile-meta { max-width:260px; opacity:1; }
        .profile-icon { width:28px; height:28px; border-radius:50%; background:#e6eedf; display:flex; align-items:center; justify-content:center; color:#4c5b4b; font-weight:800; }
        .profile-menu { position:absolute; right:0; top:calc(100% + 10px); min-width:220px; background:#fff; border:1px solid #dee5da; border-radius:12px; box-shadow:0 14px 26px rgba(24,32,20,.12); padding:8px; display:none; }
        .profile-menu.open { display:block; }
        .profile-menu a { display:block; text-decoration:none; color:#2c342d; border-radius:8px; padding:10px; font-weight:700; }
        .profile-menu a:hover { background:#f3f7f1; }
        .profile-menu .danger { color:#9b1c1c; background:#fff5f5; }
        .header-nav { margin-top:10px; padding-top:10px; border-top:1px solid #e4e9e1; display:flex; gap:14px; flex-wrap:wrap; justify-content:center; }
        .header-nav a { text-decoration:none; color:#2d352e; font-weight:800; font-size:.95rem; border:1px solid #d8e0d2; background:#fff; border-radius:999px; padding:8px 14px; }
        .layout { width:100%; max-width:none; margin:0; padding:18px clamp(12px,2.7vw,36px) 60px; }
        .hero { background:#fff; border:1px solid var(--line); border-radius:12px; padding:14px; }
        .hero h1 { margin:0 0 6px; }
        .hero p { margin:0; color:var(--muted); }
        .flash { margin:10px 0; padding:10px 12px; border-radius:10px; font-weight:700; }
        .ok { background:var(--ok-bg); color:var(--ok); border:1px solid #cce6c7; }
        .err { background:var(--err-bg); color:var(--err); border:1px solid #f0c2c2; }
        .metrics { margin-top:10px; display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:8px; }
        .metric { background:#fff; border:1px solid var(--line); border-radius:12px; padding:12px; }
        .metric strong { font-size:1.3rem; display:block; color:#3a5c3c; }
        .metric span { color:var(--muted); font-size:.9rem; }
        .grid { margin-top:10px; display:grid; grid-template-columns:1fr 1fr; gap:10px; }
        .card { background:#fff; border:1px solid var(--line); border-radius:12px; padding:14px; }
        .card h3 { margin:0 0 8px; }
        .actions { display:flex; gap:8px; flex-wrap:wrap; }
        .btn { border:none; border-radius:10px; padding:9px 12px; font-weight:800; background:var(--green); color:#fff; text-decoration:none; cursor:pointer; }
        .btn-alt { border:1px solid #d8e0d2; background:#fff; color:#304132; }
        .summary-grid { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:8px; }
        .summary-item { background:#f9fcf7; border:1px solid #e4ece0; border-radius:10px; padding:10px; }
        .summary-item strong { color:#2f4a31; font-size:1.08rem; display:block; }
        .table-wrap { overflow:auto; border:1px solid #e5ece2; border-radius:10px; margin-top:8px; }
        table { width:100%; border-collapse:collapse; min-width:760px; }
        th, td { border-bottom:1px solid #edf3ea; padding:8px 10px; text-align:left; font-size:.9rem; }
        th { background:#f8fbf6; color:#5d6f61; }
        .field { display:flex; flex-direction:column; gap:5px; }
        .field input, .field select { border:1px solid #d8e0d2; border-radius:10px; padding:8px 10px; }
        .root-form-grid { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:8px; align-items:end; }
        .audit-form-grid { display:grid; grid-template-columns:repeat(5,minmax(0,1fr)); gap:8px; align-items:end; }
        @media(max-width:980px){ .metrics{grid-template-columns:repeat(2,minmax(0,1fr));} .grid{grid-template-columns:1fr;} .summary-grid{grid-template-columns:repeat(2,minmax(0,1fr));} .root-form-grid{grid-template-columns:1fr 1fr;} .audit-form-grid{grid-template-columns:1fr 1fr;} }
        @media(max-width:768px){ .field input, .field select, .btn{font-size:16px;} }
        @media(max-width:620px){ .root-form-grid, .audit-form-grid{grid-template-columns:1fr;} }
    </style>
</head>
<body>
    <header class="site-header">
        <div class="header-inner">
            <div class="header-top">
                <a class="brand" href="${pageContext.request.contextPath}/AdminDashboard.do">
                    <img class="brand-logo" src="${pageContext.request.contextPath}/assets/tickify-admin-logo.svg" alt="Tickify Admin" onerror="this.style.display='none';">
                    <span class="brand-text">TICKIFY ADMIN</span>
                </a>
                <div class="profile-wrap">
                    <button class="profile-btn" id="profileBtn" type="button" onclick="toggleProfileMenu()">
                        <span class="profile-icon">A</span>
                        <span class="profile-meta">${userFullName} | ${sessionScope.userRoleNumberLabel} | ${sessionScope.userCampusName}</span>
                    </button>
                    <div class="profile-menu" id="profileMenu">
                        <a href="${pageContext.request.contextPath}/AdminDashboard.do">Dashboard</a>
                        <a href="${pageContext.request.contextPath}/AdminAdverts.do">Adverts</a>
                        <a href="${pageContext.request.contextPath}/AdminEventAlbum.do">Event Albums</a>
                        <a href="${pageContext.request.contextPath}/LogoutServlet.do" class="danger">Logout</a>
                    </div>
                </div>
            </div>
            <nav class="header-nav" aria-label="Admin navigation">
                <a href="${pageContext.request.contextPath}/AdminDashboard.do">Dashboard</a>
                <a href="${pageContext.request.contextPath}/AdminAlerts.do">Alerts</a>
                <a href="${pageContext.request.contextPath}/Admin/AdminManageAdmins.jsp">Admins</a>
                <a href="${pageContext.request.contextPath}/Admin/AdminManageGuards.jsp">Guards</a>
                <a href="${pageContext.request.contextPath}/Admin/AdminManageManagers.jsp">Managers</a>
                <a href="${pageContext.request.contextPath}/Admin/AdminManagePresenters.jsp">Presenters</a>
                <a href="${pageContext.request.contextPath}/AdminDatabase.do">Database</a>
            </nav>
        </div>
    </header>

    <main class="layout">
        <section class="hero">
            <h1>Admin Console</h1>
            <p>Operations dashboard with dedicated audit visibility and role/database management pages.</p>
        </section>

        <c:if test="${param.msg != null}">
            <div class="flash ok">
                <c:choose>
                    <c:when test="${param.msg == 'UserCreated'}">User account created successfully.</c:when>
                    <c:when test="${param.msg == 'UserUpdated'}">User account updated successfully.</c:when>
                    <c:when test="${param.msg == 'UserDeleted'}">User account deleted successfully.</c:when>
                    <c:when test="${param.msg == 'UserLocked'}">User account locked successfully.</c:when>
                    <c:when test="${param.msg == 'UserUnlocked'}">User account unlocked successfully.</c:when>
                    <c:when test="${param.msg == 'PasswordReset'}">User password reset successfully.</c:when>
                    <c:when test="${param.msg == 'RoleReassigned'}">User role reassigned successfully.</c:when>
                    <c:when test="${param.msg == 'DeleteRequested'}">Delete request submitted to admin for approval.</c:when>
                    <c:when test="${param.msg == 'DeleteApproved'}">Delete request approved and action completed.</c:when>
                    <c:when test="${param.msg == 'DeleteRejected'}">Delete request rejected by admin.</c:when>
                    <c:when test="${param.msg == 'RowDeleted'}">Database row deleted successfully.</c:when>
                    <c:when test="${param.msg == 'RootPasswordRotated'}">Root password rotated successfully.</c:when>
                    <c:when test="${param.msg == 'ScanLogsPurged'}">Scan logs purged successfully.</c:when>
                    <c:when test="${param.msg == 'NoChange'}">No changes were applied.</c:when>
                    <c:otherwise>Operation completed successfully.</c:otherwise>
                </c:choose>
            </div>
        </c:if>
        <c:if test="${param.err != null}">
            <div class="flash err">
                <c:choose>
                    <c:when test="${param.err == 'RootAuthFailed'}">Current root password is incorrect.</c:when>
                    <c:when test="${param.err == 'RootPasswordMismatch'}">New root password and confirmation do not match.</c:when>
                    <c:when test="${param.err == 'PrivilegedRequired'}">Only admin@tickify.ac.za can perform this operation.</c:when>
                    <c:when test="${param.err == 'CampusScopeDenied'}">This action is restricted to your assigned campus scope.</c:when>
                    <c:when test="${param.err == 'InvalidAssignment'}">Assignment values must reference existing events, venues, guards, and institutions.</c:when>
                    <c:when test="${param.err == 'MissingFields'}">Please complete all required fields.</c:when>
                    <c:when test="${param.err == 'UnknownAction'}">Unknown action requested.</c:when>
                    <c:when test="${param.err == 'OperationFailed'}">Operation failed. Please try again.</c:when>
                    <c:otherwise>An error occurred. Please verify your input and try again.</c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <section class="metrics">
            <article class="metric"><strong>${metrics.activeEvents}</strong><span>Active events</span></article>
            <article class="metric"><strong>${metrics.ticketsSold}</strong><span>Tickets sold</span></article>
            <article class="metric"><strong>R <fmt:formatNumber value="${metrics.revenue}" minFractionDigits="2" maxFractionDigits="2"/></strong><span>Revenue</span></article>
            <article class="metric"><strong><fmt:formatNumber value="${metrics.scannerUptime}" minFractionDigits="2" maxFractionDigits="2"/>%</strong><span>Scanner uptime</span></article>
        </section>

        <section class="grid">
            <article class="card">
                <h3>Role Management Pages</h3>
                <p style="color:#5f6f63;margin:0 0 8px;">Admin can do all actions. Other admins can create/edit/assign only in their campus and submit delete requests.</p>
                <div class="actions">
                    <a class="btn" href="${pageContext.request.contextPath}/Admin/AdminManageAdmins.jsp">Admins</a>
                    <a class="btn" href="${pageContext.request.contextPath}/Admin/AdminManageGuards.jsp">Guards</a>
                    <a class="btn" href="${pageContext.request.contextPath}/Admin/AdminManageManagers.jsp">Managers</a>
                    <a class="btn" href="${pageContext.request.contextPath}/Admin/AdminManagePresenters.jsp">Presenters</a>
                </div>
                <div class="actions" style="margin-top:8px;">
                    <a class="btn btn-alt" href="${pageContext.request.contextPath}/AdminDatabase.do">Open Database Page</a>
                    <a class="btn btn-alt" href="${pageContext.request.contextPath}/AdminAlerts.do">Open Alerts Page</a>
                </div>
                <c:if test="${!isPrivilegedAdmin}">
                    <div class="flash err" style="margin:8px 0 0;">Scoped admin mode: operations outside your campus and direct delete actions are blocked.</div>
                </c:if>
            </article>
            <article class="card">
                <h3>Database Summary</h3>
                <div class="summary-grid">
                    <c:forEach var="row" items="${tableSummary}">
                        <div class="summary-item"><strong>${row.count}</strong><span style="color:#5f6f63;">${row.label}</span></div>
                    </c:forEach>
                </div>
            </article>
        </section>

        <section class="card" style="margin-top:10px;">
            <h3>Delete Requests</h3>
            <p style="margin:0 0 8px;color:#5f6f63;">Other admins submit delete requests. Admin reviews and approves/rejects.</p>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>ID</th><th>Requested By</th><th>Target</th><th>Reason</th><th>Status</th><th>Time</th><th>Action</th></tr></thead>
                    <tbody>
                        <c:forEach var="dr" items="${deleteRequests}">
                            <tr>
                                <td>${dr.deleteRequestID}</td>
                                <td>${dr.requestedByFirst} ${dr.requestedByLast} (#${dr.requestedByAdminID})</td>
                                <td>${dr.targetRole}:${dr.targetUserID}</td>
                                <td>${dr.reason}</td>
                                <td>${dr.status}</td>
                                <td>${dr.requestedAt}</td>
                                <td>
                                    <c:if test="${isPrivilegedAdmin && dr.status == 'PENDING'}">
                                        <form action="${pageContext.request.contextPath}/AdminDashboard.do" method="POST" class="actions">
                                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="action" value="resolveDeleteRequest">
                                            <input type="hidden" name="deleteRequestID" value="${dr.deleteRequestID}">
                                            <input type="hidden" name="decision" value="approve">
                                            <button class="btn" type="submit">Approve</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/AdminDashboard.do" method="POST" class="actions" style="margin-top:4px;">
                                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="action" value="resolveDeleteRequest">
                                            <input type="hidden" name="deleteRequestID" value="${dr.deleteRequestID}">
                                            <input type="hidden" name="decision" value="reject">
                                            <button class="btn btn-alt" type="submit">Reject</button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty deleteRequests}"><tr><td colspan="7">No delete requests found.</td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="card" style="margin-top:10px;">
            <h3>Root Password Settings</h3>
            <p style="margin:0 0 8px;color:#5f6f63;">For privileged admin, root password is the same as login password.</p>
            <p style="margin:0 0 10px;color:#5f6f63;">
                Current source: <strong>${rootPasswordStatus.source}</strong>
                <c:if test="${rootPasswordStatus.updatedAt != null}">| Updated: ${rootPasswordStatus.updatedAt}</c:if>
            </p>
            <c:choose>
                <c:when test="${isPrivilegedAdmin}">
                    <form action="${pageContext.request.contextPath}/AdminDashboard.do" method="POST" class="root-form-grid">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="rotateRootPassword">
                        <div class="field"><label>Current Login Password</label><input type="password" name="currentRootPassword" required></div>
                        <div class="field"><label>New Login/Root Password</label><input type="password" name="newRootPassword" minlength="6" required></div>
                        <div class="field"><label>Confirm New Password</label><input type="password" name="confirmRootPassword" minlength="6" required oninput="validateRootConfirm(this)"></div>
                        <div class="field"><button class="btn" type="submit">Rotate Password</button></div>
                    </form>
                </c:when>
            </c:choose>
        </section>

        <section class="card" style="margin-top:10px;">
            <h3>Ticket Intelligence</h3>
            <p style="margin:0 0 8px;color:#5f6f63;">Tracks purchaser, ticket, scan result, and guard accountability from database records.</p>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Ticket</th><th>Purchaser</th><th>Purchaser Email</th><th>Event</th><th>Campus</th><th>Price</th><th>Scan Result</th><th>Guard</th><th>Scanned At</th></tr></thead>
                    <tbody>
                        <c:forEach var="row" items="${ticketIntelligence}">
                            <tr>
                                <td>${row.ticketNumber} (#${row.ticketID})</td>
                                <td>${row.attendeeFirst} ${row.attendeeLast}</td>
                                <td>${row.attendeeEmail}</td>
                                <td>${row.eventName}</td>
                                <td>${row.venueName}</td>
                                <td>R <fmt:formatNumber value="${row.price}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                <td>${row.scanResult}</td>
                                <td>${row.guardName}</td>
                                <td>${row.scannedAt}</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty ticketIntelligence}"><tr><td colspan="9">No ticket intelligence rows found.</td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="grid">
            <article class="card">
                <h3>Campus Revenue</h3>
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Campus</th><th>Address</th><th>Tickets Sold</th><th>Revenue</th></tr></thead>
                        <tbody>
                            <c:forEach var="row" items="${campusRevenue}">
                                <tr>
                                    <td>${row.campusName}</td>
                                    <td>${row.campusAddress}</td>
                                    <td>${row.ticketsSold}</td>
                                    <td>R <fmt:formatNumber value="${row.revenue}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty campusRevenue}"><tr><td colspan="4">No campus revenue rows found.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </article>
            <article class="card">
                <h3>Campus Ownership and Responsibility</h3>
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Campus</th><th>Database</th><th>Admins</th><th>Managers</th><th>Students</th></tr></thead>
                        <tbody>
                            <c:forEach var="row" items="${campusOwnership}">
                                <tr>
                                    <td>${row.campusName}</td>
                                    <td>${row.databaseOwner}</td>
                                    <td>${row.admins}</td>
                                    <td>${row.managers}</td>
                                    <td>${row.studentCount}</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty campusOwnership}"><tr><td colspan="5">No campus ownership rows found.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </article>
        </section>

        <section class="card" style="margin-top:10px;">
            <h3>Audit Viewer</h3>
            <form action="${pageContext.request.contextPath}/AdminDashboard.do" method="GET" class="audit-form-grid">
                <div class="field">
                    <label>Admin User</label>
                    <select name="auditAdminID">
                        <option value="">All admins</option>
                        <c:forEach var="actor" items="${auditActors}">
                            <option value="${actor.adminID}" <c:if test="${auditAdminID != null && auditAdminID == actor.adminID}">selected</c:if>>${actor.firstname} ${actor.lastname} (#${actor.adminID})</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="field">
                    <label>Action Type</label>
                    <input type="text" name="auditAction" value="${auditAction}" placeholder="CREATE_ADMIN, SQL_MUTATION...">
                </div>
                <div class="field">
                    <label>From Date</label>
                    <input type="date" name="auditFrom" value="${auditFrom}">
                </div>
                <div class="field">
                    <label>To Date</label>
                    <input type="date" name="auditTo" value="${auditTo}">
                </div>
                <div class="field">
                    <button class="btn" type="submit">Filter Audit Logs</button>
                </div>
            </form>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Log ID</th><th>Admin</th><th>Action</th><th>Target</th><th>Details</th><th>Time</th></tr></thead>
                    <tbody>
                        <c:forEach var="log" items="${auditLogs}">
                            <tr>
                                <td>${log.adminAuditLogID}</td>
                                <td>${log.firstname} ${log.lastname} (#${log.adminID})</td>
                                <td>${log.actionType}</td>
                                <td>${log.targetTable} / ${log.targetID}</td>
                                <td>${log.details}</td>
                                <td>${log.createdAt}</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty auditLogs}"><tr><td colspan="6">No audit logs found for selected filters.</td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </main>

    <script>
        function validateRootConfirm(el) {
            var form = el.form;
            if (!form) { return; }
            var next = form.querySelector('input[name="newRootPassword"]');
            if (!next) { return; }
            el.setCustomValidity(el.value === next.value ? '' : 'Passwords do not match');
        }

        function toggleProfileMenu() {
            document.getElementById("profileMenu").classList.toggle("open");
        }
        window.addEventListener("click", function (event) {
            var menu = document.getElementById("profileMenu");
            var btn = document.getElementById("profileBtn");
            if (!menu || !btn) { return; }
            if (!menu.contains(event.target) && !btn.contains(event.target)) {
                menu.classList.remove("open");
            }
        });
    </script>
    <script src="${pageContext.request.contextPath}/assets/error-popup.js"></script>
</body>
</html>
