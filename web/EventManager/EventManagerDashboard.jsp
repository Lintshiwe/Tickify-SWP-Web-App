<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tickify | Event Manager Dashboard</title>
    <style>
        :root { --green:#79c84a; --green-dark:#5ca833; --bg:#f7faf6; --ink:#3a4a3e; --muted:#76857a; --line:#d8e5d5; }
        * { box-sizing:border-box; }
        body { margin:0; font-family:"Trebuchet MS","Segoe UI",sans-serif; background:var(--bg); color:var(--ink); }
        .wrap { width:100%; max-width:none; margin:0; padding:20px clamp(12px,2.7vw,36px); }
        .top { background:#fff; border:1px solid var(--line); border-radius:14px; padding:14px 16px; display:flex; justify-content:space-between; align-items:center; gap:10px; }
        .brand { color:var(--green-dark); font-weight:900; letter-spacing:.1em; }
        .profile-meta { color:#607167; font-weight:700; font-size:.92rem; }
        .logout { text-decoration:none; background:#eef8e9; color:var(--green-dark); border:1px solid #cfe2c9; border-radius:10px; padding:10px 12px; font-weight:800; }
        .grid { margin-top:12px; display:grid; grid-template-columns:1fr 1fr; gap:10px; }
        .card { background:#fff; border:1px solid var(--line); border-radius:12px; padding:14px; }
        .flash { margin-top:10px; border-radius:10px; padding:10px 12px; font-weight:800; }
        .ok { background:#eaf7e7; color:#1f7c39; border:1px solid #cce6c7; }
        .err { background:#ffecec; color:#9b1c1c; border:1px solid #f0c2c2; }
        .full { grid-column:1/-1; }
        .kpis { margin-top:12px; display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:10px; }
        .kpi { background:#fff; border:1px solid var(--line); border-radius:12px; padding:12px; }
        .kpi strong { display:block; font-size:1.3rem; color:#31502f; }
        .kpi span { color:var(--muted); font-size:.9rem; }
        .btn-primary { border:1px solid #6eb83f; background:var(--green); color:#fff; border-radius:10px; padding:9px 12px; font-weight:800; cursor:pointer; }
        .state-attention { color:#8b2d1f; font-weight:700; }
        .state-ok { color:#2e6b2a; font-weight:700; }
        .table-wrap { margin-top:8px; overflow:auto; border:1px solid #e2ece0; border-radius:10px; }
        .table-toolbar { display:flex; justify-content:space-between; align-items:center; gap:8px; flex-wrap:wrap; margin-top:6px; }
        .table-search { border:1px solid #d5e3d1; border-radius:10px; padding:8px 10px; min-width:240px; font:inherit; }
        .table-pager { display:flex; align-items:center; gap:8px; }
        .table-pager button { border:1px solid #cfe0c9; border-radius:8px; background:#fff; color:#365037; font-weight:700; padding:7px 10px; cursor:pointer; }
        .table-pager button:disabled { opacity:.5; cursor:not-allowed; }
        .table-pager select { border:1px solid #d5e3d1; border-radius:8px; padding:6px 8px; font:inherit; }
        .page-label { color:#6b7a6f; font-size:.88rem; min-width:84px; text-align:center; }
        table { width:100%; border-collapse:collapse; min-width:760px; }
        th, td { border-bottom:1px solid #edf3ea; padding:8px 10px; text-align:left; font-size:.9rem; }
        th { background:#f7fbf5; color:#5d6f61; }
        h1,h2{margin:0 0 8px;} p,li{color:var(--muted);} ul{margin:0;padding-left:18px;}
        @media(max-width:980px){ .kpis{grid-template-columns:repeat(2,minmax(0,1fr));} }
        @media(max-width:780px){ .top{flex-direction:column; align-items:flex-start; gap:10px;} .grid{grid-template-columns:1fr;} .kpis{grid-template-columns:1fr;} }
    </style>
</head>
<body>
    <div class="wrap">
        <div class="top">
            <div>
                <div class="brand">TICKIFY EVENT MANAGER</div>
                <div class="profile-meta">${userFullName} | ${sessionScope.userRoleNumberLabel} | ${sessionScope.userCampusName}</div>
            </div>
            <a class="logout" href="${pageContext.request.contextPath}/LogoutServlet.do">Logout</a>
        </div>

        <c:if test="${param.msg != null}">
            <div class="flash ok">
                <c:choose>
                    <c:when test="${param.msg == 'EventUpdated'}">Assigned event details updated successfully.</c:when>
                    <c:when test="${param.msg == 'TierCreated'}">Ticket tier templates were created successfully.</c:when>
                    <c:when test="${param.msg == 'NoChange'}">No changes were applied.</c:when>
                    <c:otherwise>Operation completed successfully.</c:otherwise>
                </c:choose>
            </div>
        </c:if>
        <c:if test="${param.err != null}">
            <div class="flash err">
                <c:choose>
                    <c:when test="${param.err == 'UnknownAction'}">Unknown manager action requested.</c:when>
                    <c:when test="${param.err == 'InvalidEventId'}">Please provide a valid event ID.</c:when>
                    <c:when test="${param.err == 'InvalidEventName'}">Event name must be between 3 and 120 characters.</c:when>
                    <c:when test="${param.err == 'InvalidEventType'}">Event type must be between 2 and 80 characters.</c:when>
                    <c:when test="${param.err == 'InvalidEventDate'}">Please provide a valid event date and time.</c:when>
                    <c:when test="${param.err == 'InvalidTierName'}">Tier name must be between 2 and 80 characters.</c:when>
                    <c:when test="${param.err == 'InvalidTierPrice'}">Tier price must be greater than 0.</c:when>
                    <c:when test="${param.err == 'InvalidTierQuantity'}">Tier quantity must be greater than 0.</c:when>
                    <c:when test="${param.err == 'OperationFailed'}">Operation failed. Check your input and try again.</c:when>
                    <c:otherwise>Unable to process request.</c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <section class="kpis">
            <article class="kpi"><strong>${eventCount}</strong><span>Assigned Events</span></article>
            <article class="kpi"><strong>${guardCount}</strong><span>Guard Profiles</span></article>
            <article class="kpi"><strong>${presenterCount}</strong><span>Presenter Profiles</span></article>
            <article class="kpi"><strong>${invalidScans24h}</strong><span>Invalid Scans (24h)</span></article>
        </section>

        <div class="grid">
            <section class="card full">
                <h1>Event Operations Hub</h1>
                <p>Live operational view generated from assigned events, guard scans, presenters, and ticket sales.</p>

                <h2 style="margin-top:10px;">Update Assigned Event Details</h2>
                <form action="${pageContext.request.contextPath}/EventManagerDashboard.do" method="POST" style="display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:8px;align-items:end;">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="updateAssignedEvent">
                    <input type="number" name="eventID" min="1" placeholder="Event ID" required>
                    <input type="text" name="eventName" placeholder="Event Name" required>
                    <input type="text" name="eventType" placeholder="Event Type" required>
                    <input type="datetime-local" name="eventDate" required>
                    <button class="btn-primary" type="submit">Update Event</button>
                </form>

                <h2 style="margin-top:10px;">Create Ticket Tier</h2>
                <form action="${pageContext.request.contextPath}/EventManagerDashboard.do" method="POST" style="display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:8px;align-items:end;">
                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="createTicketTier">
                    <input type="number" name="eventID" min="1" placeholder="Event ID" required>
                    <input type="text" name="tierName" placeholder="Tier Name" required>
                    <input type="number" step="0.01" min="0" name="tierPrice" placeholder="Price" required>
                    <input type="number" min="1" name="tierQuantity" placeholder="Quantity" required>
                    <button class="btn-primary" type="submit">Create Tier</button>
                </form>
            </section>

            <section class="card">
                <h2>Planning Queue</h2>
                <ul>
                    <c:forEach var="item" items="${planningItems}">
                        <li class="state-${item.state}">${item.text}</li>
                    </c:forEach>
                    <c:if test="${empty planningItems}"><li>No planning items available.</li></c:if>
                </ul>
            </section>

            <section class="card">
                <h2>Risk Controls</h2>
                <ul>
                    <c:forEach var="item" items="${riskItems}">
                        <li class="state-${item.state}">${item.text}</li>
                    </c:forEach>
                    <c:if test="${empty riskItems}"><li>No risk controls available.</li></c:if>
                </ul>
            </section>

            <section class="card full">
                <h2>Assigned Events</h2>
                <div class="table-toolbar">
                    <input id="eventsSearch" class="table-search" type="text" placeholder="Search event, type, campus...">
                    <div class="table-pager">
                        <label for="eventsPageSize" class="page-label">Rows</label>
                        <select id="eventsPageSize">
                            <option value="5">5</option>
                            <option value="10" selected>10</option>
                            <option value="20">20</option>
                        </select>
                        <button id="eventsPrev" type="button">Prev</button>
                        <span id="eventsPage" class="page-label">Page 1/1</span>
                        <button id="eventsNext" type="button">Next</button>
                    </div>
                </div>
                <div class="table-wrap">
                    <table id="eventsTable">
                        <thead>
                            <tr>
                                <th>Event</th>
                                <th>Type</th>
                                <th>Date</th>
                                <th>Campus</th>
                                <th>Ticket Templates</th>
                                <th>Sold Tickets</th>
                                <th>Revenue</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="ev" items="${assignedEvents}">
                                <tr class="data-row">
                                    <td>${ev.eventName}</td>
                                    <td>${ev.eventType}</td>
                                    <td>${ev.eventDate}</td>
                                    <td>${ev.campusName}</td>
                                    <td>${ev.ticketTemplates}</td>
                                    <td>${ev.soldTickets}</td>
                                    <td>R <fmt:formatNumber value="${ev.revenue}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty assignedEvents}"><tr class="no-data-row"><td colspan="7">No assigned events found.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </section>

            <section class="card full">
                <h2>Venue Guard Coverage</h2>
                <div class="table-toolbar">
                    <input id="guardsSearch" class="table-search" type="text" placeholder="Search guard name, email, event...">
                    <div class="table-pager">
                        <label for="guardsPageSize" class="page-label">Rows</label>
                        <select id="guardsPageSize">
                            <option value="5">5</option>
                            <option value="10" selected>10</option>
                            <option value="20">20</option>
                        </select>
                        <button id="guardsPrev" type="button">Prev</button>
                        <span id="guardsPage" class="page-label">Page 1/1</span>
                        <button id="guardsNext" type="button">Next</button>
                    </div>
                </div>
                <div class="table-wrap">
                    <table id="guardsTable">
                        <thead>
                            <tr>
                                <th>Guard</th>
                                <th>Email</th>
                                <th>Event</th>
                                <th>Valid Scans</th>
                                <th>Invalid Scans</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="g" items="${guardCoverage}">
                                <tr class="data-row">
                                    <td>${g.firstname} ${g.lastname}</td>
                                    <td>${g.email}</td>
                                    <td>${g.eventName}</td>
                                    <td>${g.validScans}</td>
                                    <td>${g.invalidScans}</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty guardCoverage}"><tr class="no-data-row"><td colspan="5">No guard coverage records found.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </section>

            <section class="card full">
                <h2>Presenter Session Mapping</h2>
                <div class="table-toolbar">
                    <input id="presentersSearch" class="table-search" type="text" placeholder="Search presenter, institution, event...">
                    <div class="table-pager">
                        <label for="presentersPageSize" class="page-label">Rows</label>
                        <select id="presentersPageSize">
                            <option value="5">5</option>
                            <option value="10" selected>10</option>
                            <option value="20">20</option>
                        </select>
                        <button id="presentersPrev" type="button">Prev</button>
                        <span id="presentersPage" class="page-label">Page 1/1</span>
                        <button id="presentersNext" type="button">Next</button>
                    </div>
                </div>
                <div class="table-wrap">
                    <table id="presentersTable">
                        <thead>
                            <tr>
                                <th>Presenter</th>
                                <th>Email</th>
                                <th>Institution</th>
                                <th>Event</th>
                                <th>Event Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${presenterSessions}">
                                <tr class="data-row">
                                    <td>${p.firstname} ${p.lastname}</td>
                                    <td>${p.email}</td>
                                    <td>${p.tertiaryInstitution}</td>
                                    <td>${p.eventName}</td>
                                    <td>${p.eventDate}</td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty presenterSessions}"><tr class="no-data-row"><td colspan="5">No presenter sessions found.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    </div>
    <script>
        function readStateParam(key, fallback) {
            var params = new URLSearchParams(window.location.search || "");
            var value = params.get(key);
            return (value === null || value === "") ? fallback : value;
        }

        function writeStateParam(key, value) {
            var params = new URLSearchParams(window.location.search || "");
            if (value === null || value === undefined || value === "") {
                params.delete(key);
            } else {
                params.set(key, String(value));
            }
            var next = window.location.pathname + (params.toString() ? ("?" + params.toString()) : "");
            window.history.replaceState({}, "", next);
        }

        function setupPaginatedTable(config) {
            var table = document.getElementById(config.tableId);
            if (!table) {
                return;
            }
            var tbody = table.querySelector("tbody");
            if (!tbody) {
                return;
            }

            var dataRows = Array.prototype.slice.call(tbody.querySelectorAll("tr.data-row"));
            var emptyRow = tbody.querySelector("tr.no-data-row");
            var searchInput = document.getElementById(config.searchId);
            var prevBtn = document.getElementById(config.prevId);
            var nextBtn = document.getElementById(config.nextId);
            var pageLabel = document.getElementById(config.pageLabelId);
            var pageSizeSelect = document.getElementById(config.pageSizeId);

            var page = parseInt(readStateParam(config.statePageKey, "1"), 10);
            if (isNaN(page) || page <= 0) {
                page = 1;
            }

            if (searchInput) {
                searchInput.value = readStateParam(config.stateSearchKey, "");
            }

            if (pageSizeSelect) {
                var persistedSize = readStateParam(config.stateSizeKey, pageSizeSelect.value || "10");
                pageSizeSelect.value = persistedSize;
            }

            function normalize(text) {
                return (text || "").toLowerCase();
            }

            function render() {
                var query = normalize(searchInput ? searchInput.value : "");
                var pageSize = parseInt(pageSizeSelect ? pageSizeSelect.value : "10", 10);
                if (isNaN(pageSize) || pageSize <= 0) {
                    pageSize = 10;
                }

                var filtered = dataRows.filter(function (row) {
                    return normalize(row.textContent).indexOf(query) !== -1;
                });

                var totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
                if (page > totalPages) {
                    page = totalPages;
                }

                var start = (page - 1) * pageSize;
                var end = start + pageSize;

                dataRows.forEach(function (row) {
                    row.style.display = "none";
                });

                filtered.slice(start, end).forEach(function (row) {
                    row.style.display = "";
                });

                if (emptyRow) {
                    emptyRow.style.display = filtered.length === 0 ? "" : "none";
                }

                if (pageLabel) {
                    pageLabel.textContent = "Page " + page + "/" + totalPages;
                }
                if (prevBtn) {
                    prevBtn.disabled = page <= 1 || filtered.length === 0;
                }
                if (nextBtn) {
                    nextBtn.disabled = page >= totalPages || filtered.length === 0;
                }

                writeStateParam(config.stateSearchKey, searchInput ? searchInput.value : "");
                writeStateParam(config.statePageKey, page);
                writeStateParam(config.stateSizeKey, pageSize);
            }

            if (searchInput) {
                searchInput.addEventListener("input", function () {
                    page = 1;
                    render();
                });
            }
            if (pageSizeSelect) {
                pageSizeSelect.addEventListener("change", function () {
                    page = 1;
                    render();
                });
            }
            if (prevBtn) {
                prevBtn.addEventListener("click", function () {
                    if (page > 1) {
                        page--;
                        render();
                    }
                });
            }
            if (nextBtn) {
                nextBtn.addEventListener("click", function () {
                    page++;
                    render();
                });
            }

            render();
        }

        setupPaginatedTable({
            tableId: "eventsTable",
            searchId: "eventsSearch",
            prevId: "eventsPrev",
            nextId: "eventsNext",
            pageLabelId: "eventsPage",
            pageSizeId: "eventsPageSize",
            stateSearchKey: "evQ",
            statePageKey: "evP",
            stateSizeKey: "evS"
        });

        setupPaginatedTable({
            tableId: "guardsTable",
            searchId: "guardsSearch",
            prevId: "guardsPrev",
            nextId: "guardsNext",
            pageLabelId: "guardsPage",
            pageSizeId: "guardsPageSize",
            stateSearchKey: "gdQ",
            statePageKey: "gdP",
            stateSizeKey: "gdS"
        });

        setupPaginatedTable({
            tableId: "presentersTable",
            searchId: "presentersSearch",
            prevId: "presentersPrev",
            nextId: "presentersNext",
            pageLabelId: "presentersPage",
            pageSizeId: "presentersPageSize",
            stateSearchKey: "prQ",
            statePageKey: "prP",
            stateSizeKey: "prS"
        });
    </script>
    <script src="${pageContext.request.contextPath}/assets/error-popup.js"></script>
</body>
</html>
