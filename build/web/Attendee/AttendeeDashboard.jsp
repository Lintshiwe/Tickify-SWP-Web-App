<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tickify | Attendee Dashboard</title>
        <style>
            :root {
                --gold: #FFD700;
                --black: #000000;
                --dark-grey: #1a1a1a;
                --light-grey: #f4f4f4;
            }

            body, html {
                height: 100%;
                margin: 0;
                font-family: 'Segoe UI', Tahoma, sans-serif;
                display: flex;
                background-color: var(--light-grey);
                overflow: hidden;
            }

            /* SECTION 1: SIDEBAR (15%) */
            .sidebar {
                width: 25%;
                background-color: var(--black);
                color: white;
                display: flex;
                flex-direction: column;
                border-right: 4px solid var(--gold);
                height: 100vh;
            }
            .sidebar-header {
                padding: 30px 10px;
                text-align: center;
                font-size: 1.5vw;
                font-weight: bold;
                color: var(--gold);
                border-bottom: 1px solid #333;
            }
            .nav-links {
                flex: 1;
                padding: 20px 0;
            }
            .nav-item {
                padding: 15px 20px;
                display: block;
                color: white;
                text-decoration: none;
                transition: 0.3s;
                font-size: 0.9vw;
            }
            .nav-item:hover {
                background-color: var(--gold);
                color: var(--black);
            }
            .nav-item.active {
                border-left: 5px solid var(--gold);
                background-color: var(--dark-grey);
            }
            .logout-btn {
                padding: 15px;
                background-color: #900;
                color: white;
                text-decoration: none;
                text-align: center;
                font-weight: bold;
                font-size: 0.9vw;
            }

            /* SECTION 2: MAIN CONTENT (85%) */
            .main-content {
                width: 100%;
                display: flex;
                flex-direction: column;
                height: 100vh;
                overflow-y: auto;

            }

            header {
                background-color: white;
                padding: 20px 40px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            }
            .user-welcome h2 {
                margin: 0;
                font-size: 1.5rem;
            }

            /* BODY AREA - Fills Full Width */
            .dashboard-body {
                padding: 40px;
                width: 100%;
                box-sizing: border-box;
            }
            .section-title {
                border-bottom: 2px solid var(--gold);
                padding-bottom: 10px;
                margin-bottom: 25px;
                width: 100%;
            }

            /* DYNAMIC EVENT GRID - Optimised for Full Width */
            .event-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                gap: 20px;
                width: 100%;
            }

            .event-card {
                background: white;
                border: 1px solid #ddd;
                border-radius: 8px;
                overflow: hidden;
                transition: 0.3s;
                display: flex;
                flex-direction: column;
                height: 100%;
            }
            .event-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 10px 20px rgba(0,0,0,0.1);
                border-color: var(--gold);
            }

            .event-img {
                height: 140px;
                background: var(--black);
                display: flex;
                align-items: center;
                justify-content: center;
                color: var(--gold);
                font-weight: bold;
            }
            .event-info {
                padding: 20px;
                flex-grow: 1;
            }
            .event-info h3 {
                margin: 0 0 10px 0;
                color: var(--black);
                font-size: 1.2rem;
            }
            .event-info p {
                margin: 5px 0;
                color: #555;
                font-size: 0.9rem;
            }

            .price-tag {
                font-size: 1.1rem;
                font-weight: bold;
                color: var(--black);
                margin-top: 10px;
                display: block;
            }
            .btn-book {
                display: block;
                text-align: center;
                margin-top: 15px;
                padding: 10px;
                background: var(--black);
                color: var(--gold);
                border-radius: 4px;
                text-decoration: none;
                font-weight: bold;
            }
            .btn-book:hover {
                background: var(--gold);
                color: var(--black);
            }

            .empty-state {
                text-align: center;
                padding: 100px 0;
                color: #888;
                grid-column: 1 / -1;
                width: 100%;
            }
            /* 1. LOGOUT BUTTON INTERACTION */
            .logout-btn {
                padding: 15px;
                background-color: #900;
                color: white;
                text-decoration: none;
                text-align: center;
                font-weight: bold;
                font-size: 0.9vw;
                transition: background-color 0.3s ease, transform 0.2s ease; /* Smooth transition */
            }

            .logout-btn:hover, .logout-btn:focus {
                background-color: #cc0000; /* Lighter red on hover */
                color: white;
                outline: none;
            }

            .logout-btn:active {
                transform: scale(0.98); /* Slight click effect */
            }

            /* 2. UPDATE PROFILE BUTTON INTERACTION */
            /* Adding a class or selecting the specific inline style */
            .quick-actions-update {
                text-decoration:none;
                padding: 15px 30px;
                background: var(--gold);
                color: black;
                font-weight: bold;
                border: 2px solid black;
                transition: all 0.3s ease;
            }

            .quick-actions-update:hover, .quick-actions-update:focus {
                background-color: var(--black); /* Flips colors on hover */
                color: var(--gold);
                outline: none;
            }

            /* 3. DELETE ACCOUNT BUTTON INTERACTION */
            .btn-delete-account {
                padding: 15px 30px;
                border: 2px solid #900;
                background: white;
                color: #900;
                font-weight: bold;
                cursor: pointer;
                transition: all 0.3s ease;
            }

            .btn-delete-account:hover, .btn-delete-account:focus {
                background-color: #900; /* Shaded Red background */
                color: white;           /* White text when shaded */
                outline: none;
            }

            .btn-delete-account:active {
                background-color: #600; /* Darker red when actually clicked */
            }
        </style>
    </head>
    <body>

       <div class="sidebar">
            <div class="sidebar-header">TICKIFY</div>
            <div class="nav-links">
                <a href="ViewMyTickets.do" class="nav-item">🎟 View Tickets</a>
                <a href="AttendeeViewProfileServlet.do" class="nav-item">👤 Update Profile</a>
                <a href="javascript:void(0);" onclick="confirmDelete()" class="nav-item">🗑 Delete Account</a>
            </div>
            <a href="LogoutServlet.do" class="logout-btn">LOGOUT</a>
        </div>

        <div class="main-content">
            <header>
                <div class="user-welcome">
                    <h2>Welcome, ${userFullName}!</h2>
                    <span style="color:#666">ID: #${userID}</span>
                </div>
                <div>📅 <strong>2026</strong></div>
            </header>

            <div class="dashboard-body">
                <div class="section-title">
                    <h2>Available Events</h2>
                </div>

                <div class="event-grid">
                    <c:choose>
                        <%-- Logic: If the servlet sent 'eventList' and it's not empty --%>
                        <c:when test="${not empty eventList}">
                            <c:forEach var="event" items="${eventList}">
                                <div class="event-card">
                                    <div class="event-img">
                                        ${event.type}
                                    </div>

                                    <div class="event-info">
                                        <h3>${event.name}</h3>

                                        <p>📍 ${event.venueName} - ${event.address}</p>

                                        <p>📅 ${event.date}</p>

                                        <span class="price-tag">
                                            <c:choose>
                                                <c:when test="${event.price > 0}">
                                                    R ${event.price}
                                                </c:when>
                                                <c:otherwise>
                                                    FREE
                                                </c:otherwise>
                                            </c:choose>
                                        </span>

                                        <a href="BookTicket.do?eventID=${event.id}" class="btn-book">BOOK TICKET</a>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state">
                                <h3>No events were found in the system.</h3>
                                <p>If you see this, ensure your Servlet is passing 'eventList' correctly.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="section-title" style="margin-top: 50px;">
                    <h2>Quick Actions</h2>
                </div>
                <div style="display: flex; gap: 20px;">
                    <a href="AttendeeViewProfileServlet.do" class="quick-actions-update">UPDATE PROFILE</a>

                    <button onclick="confirmDelete()" class="btn-delete-account">DELETE ACCOUNT</button>
                </div>
            </div>
        </div>

        <script>
            function confirmDelete() {
                if (confirm("Permanently delete your account?")) {
                    window.location.href = "AttendeeDeleteProfileServlet.do";
                }
            }
        </script>
    </body>
</html>