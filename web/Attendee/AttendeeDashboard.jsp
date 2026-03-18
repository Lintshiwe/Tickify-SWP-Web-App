<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Tickify | Attendee Dashboard</title>

    <style>
            /* ===================== ROOT ===================== */
     :root {
         --gold: #FFD700;
         --black: #000;
         --light: #f5f5f5;
         --card-radius: 14px;
     }

     /* ===================== BASE ===================== */
     body {
         margin: 0;
         font-family: 'Segoe UI', system-ui, sans-serif;
         background: var(--light);
         -webkit-font-smoothing: antialiased;
     }

     /* ===================== TOPBAR ===================== */
     .topbar {
         position: sticky;
         top: 0;
         z-index: 100;
         display: flex;
         align-items: center;
         justify-content: space-between;
         padding: 18px 30px;

         background: rgba(255,255,255,0.85);
         backdrop-filter: blur(10px);
         border-bottom: 1px solid rgba(0,0,0,0.05);
     }

     .logo {
         font-weight: 700;
         letter-spacing: 1px;
         color: var(--gold);
         font-size: 1.1rem;
     }

     /* ===================== SEARCH ===================== */
     .search-bar input {
         padding: 10px 16px;
         width: 280px;
         border-radius: 999px;
         border: 1px solid #e5e5e5;
         outline: none;
         transition: all 0.25s ease;
         background: #fafafa;
     }

     .search-bar input:focus {
         border-color: var(--gold);
         background: white;
         box-shadow: 0 0 0 3px rgba(255,215,0,0.15);
     }

     /* ===================== PROFILE ===================== */
     .profile-btn {
         border: none;
         background: none;
         cursor: pointer;
         padding: 6px;
         border-radius: 50%;
         transition: 0.2s;
     }

     .profile-btn:hover {
         background: rgba(0,0,0,0.05);
     }

     /* ===================== DROPDOWN ===================== */
     .dropdown {
         position: absolute;
         right: 0;
         top: 45px;
         width: 240px;

         background: white;
         border-radius: 16px;
         padding: 8px 0;

         box-shadow:
             0 10px 25px rgba(0,0,0,0.08),
             0 2px 8px rgba(0,0,0,0.05);

         display: none;
         animation: fadeIn 0.2s ease;
     }

     .dropdown.show {
         display: block;
     }

     @keyframes fadeIn {
         from {opacity: 0; transform: translateY(-8px);}
         to {opacity: 1; transform: translateY(0);}
     }

     .dropdown-header {
         padding: 14px 16px;
         border-bottom: 1px solid #f0f0f0;
         font-size: 0.85rem;
     }

     .dropdown-header strong {
         font-size: 0.95rem;
     }

     .dropdown a {
         display: flex;
         align-items: center;
         gap: 12px;
         padding: 12px 16px;
         font-size: 0.9rem;
         color: #222;
         text-decoration: none;
         transition: all 0.2s ease;
     }

     .dropdown a:hover {
         background: rgba(255,215,0,0.15);
     }

     .dropdown-divider {
         height: 1px;
         background: #f0f0f0;
         margin: 6px 0;
     }

     /* ===================== CONTENT ===================== */
     .content {
         padding: 30px 40px;
     }

     h2 {
         margin-bottom: 25px;
         font-weight: 600;
     }

     /* ===================== GRID ===================== */
     .event-grid {
         display: grid;
         grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
         gap: 24px;
     }

     /* ===================== CARD ===================== */
     .event-card {
         background: white;
         border-radius: var(--card-radius);
         overflow: hidden;
         transition: all 0.25s ease;

         box-shadow:
             0 4px 12px rgba(0,0,0,0.04),
             0 1px 3px rgba(0,0,0,0.06);
     }

     .event-card:hover {
         transform: translateY(-6px);

         box-shadow:
             0 12px 30px rgba(0,0,0,0.08),
             0 4px 12px rgba(0,0,0,0.06);
     }

     /* ===================== IMAGE ===================== */
     .event-img {
         height: 150px;
         background: linear-gradient(135deg, #000, #222);
         color: var(--gold);
         display: flex;
         align-items: center;
         justify-content: center;
         font-weight: 600;
         letter-spacing: 0.5px;
     }

     /* ===================== INFO ===================== */
     .event-info {
         padding: 18px;
     }

     .event-info h3 {
         margin: 0 0 10px;
         font-size: 1.05rem;
         font-weight: 600;
     }

     .event-meta {
         display: flex;
         align-items: center;
         gap: 8px;
         font-size: 0.85rem;
         color: #666;
         margin: 6px 0;
     }

     /* ===================== PRICE ===================== */
     .price-tag {
         display: block;
         margin-top: 10px;
         font-weight: 600;
     }

     /* ===================== BUTTON ===================== */
     .btn-book {
         display: block;
         margin-top: 14px;
         padding: 10px;
         text-align: center;

         background: var(--black);
         color: var(--gold);
         border-radius: 8px;

         font-size: 0.85rem;
         letter-spacing: 0.3px;
         text-decoration: none;

         transition: all 0.25s ease;
     }

     .btn-book:hover {
         background: var(--gold);
         color: var(--black);
         transform: translateY(-1px);
     }

     /* ===================== EMPTY ===================== */
     .empty-state {
         text-align: center;
         padding: 80px;
         color: #999;
     }

     /* ===================== MOBILE ===================== */
     @media (max-width: 768px) {

         .content {
             padding: 20px;
         }

         .search-bar input {
             width: 150px;
         }

         .event-grid {
             grid-template-columns: 1fr;
         }
     }
    </style>
</head>

<body>

<div class="topbar">
    <div class="logo">TICKIFY</div>

    <div class="search-bar">
        <input type="text" placeholder="Search events..." />
    </div>

    <div class="profile-menu">
        <button class="profile-btn" onclick="toggleMenu()">
            <svg width="24" height="24" viewBox="0 0 24 24">
                <path fill="currentColor" d="M12 12c2.7 0 5-2.3 5-5s-2.3-5-5-5-5 2.3-5 5 2.3 5 5 5zm0 2c-3.3 0-10 1.7-10 5v3h20v-3c0-3.3-6.7-5-10-5z"/>
            </svg>
        </button>

        <div id="dropdown" class="dropdown">

            <div class="dropdown-header">
                <strong>${userFullName}</strong>
                <span>ID: #${userID}</span>
            </div>

            <a href="AttendeeViewProfileServlet.do">
                <svg width="18" height="18"><path fill="currentColor" d="M12 12c2.7 0 5-2.3 5-5s-2.3-5-5-5-5 2.3-5 5 2.3 5 5 5z"/></svg>
                Profile
            </a>

            <a href="ViewMyTickets.do">
                <svg width="18" height="18"><path fill="currentColor" d="M4 6h16v12H4z"/></svg>
                My Tickets
            </a>

            <a href="#">
                <svg width="18" height="18"><path fill="currentColor" d="M19.14 12.94a7.49 7.49 0 0 0 .05-.94 7.49 7.49 0 0 0-.05-.94l2.11-1.65-2-3.46-2.49 1a7.28 7.28 0 0 0-1.63-.94l-.38-2.65H9.25l-.38 2.65a7.28 7.28 0 0 0-1.63.94l-2.49-1-2 3.46 2.11 1.65a7.49 7.49 0 0 0-.05.94 7.49 7.49 0 0 0 .05.94L2.75 14.6l2 3.46 2.49-1c.5.38 1.04.7 1.63.94l.38 2.65h5.5l.38-2.65c.59-.24 1.13-.56 1.63-.94l2.49 1 2-3.46-2.11-1.66z"/></svg>
                Settings
            </a>

            <a href="#">
                <svg width="18" height="18"><path fill="currentColor" d="M12 2a10 10 0 100 20 10 10 0 000-20z"/></svg>
                Help
            </a>

            <div class="dropdown-divider"></div>

            <a href="javascript:void(0);" onclick="confirmDelete()">
                <svg width="18" height="18"><path fill="currentColor" d="M6 7h12l-1 14H7z"/></svg>
                Delete Account
            </a>

            <a href="LogoutServlet.do">
                <svg width="18" height="18"><path fill="currentColor" d="M10 17l-5-5 5-5v3h9v4h-9z"/></svg>
                Logout
            </a>

        </div>
    </div>
</div>

<div class="content">

    <h2>Available Events</h2>

    <div class="event-grid">
        <c:choose>
            <c:when test="${not empty eventList}">
                <c:forEach var="event" items="${eventList}">
                    <div class="event-card">
                        <div class="event-img">${event.type}</div>

                        <div class="event-info">
                            <h3>${event.name}</h3>

                            <p class="event-meta">
                                <svg width="16" height="16"><path fill="currentColor" d="M12 2C8 2 5 5 5 9c0 5 7 13 7 13s7-8 7-13c0-4-3-7-7-7z"/></svg>
                                ${event.venueName}
                            </p>

                            <p class="event-meta">
                                <svg width="16" height="16"><path fill="currentColor" d="M7 2h10v20H7z"/></svg>
                                ${event.date}
                            </p>

                            <span>
                                <c:choose>
                                    <c:when test="${event.price > 0}">
                                        R ${event.price}
                                    </c:when>
                                    <c:otherwise>
                                        FREE
                                    </c:otherwise>
                                </c:choose>
                            </span>

                            <a href="BookTicket.do?eventID=${event.id}" class="btn-book">Book</a>
                        </div>
                    </div>
                </c:forEach>
            </c:when>

            <c:otherwise>
                <div class="empty-state">
                    No events available
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</div>

<script>
function toggleMenu() {
    document.getElementById("dropdown").classList.toggle("show");
}

window.onclick = function(e) {
    if (!e.target.closest('.profile-menu')) {
        document.getElementById("dropdown").classList.remove("show");
    }
}

function confirmDelete() {
    if (confirm("Permanently delete your account?")) {
        window.location.href = "AttendeeDeleteProfileServlet.do";
    }
}
</script>

</body>
</html>