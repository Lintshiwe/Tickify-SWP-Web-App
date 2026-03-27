<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tickify | Presenter Dashboard</title>
    <style>
        :root { --green:#79c84a; --green-dark:#5ca833; --bg:#f7faf6; --ink:#3a4a3e; --muted:#76857a; --line:#d8e5d5; }
        * { box-sizing:border-box; }
        body { margin:0; font-family:"Trebuchet MS","Segoe UI",sans-serif; background:var(--bg); color:var(--ink); }
        .wrap { width:100%; max-width:none; margin:0; padding:20px clamp(12px,2.7vw,36px); }
        .top { background:#fff; border:1px solid var(--line); border-radius:14px; padding:14px 16px; display:flex; justify-content:space-between; align-items:center; gap:10px; }
        .brand { color:var(--green-dark); font-weight:900; letter-spacing:.1em; }
        .profile-meta { color:#607167; font-weight:700; font-size:.92rem; }
        .logout { text-decoration:none; background:#eef8e9; color:var(--green-dark); border:1px solid #cfe2c9; border-radius:10px; padding:10px 12px; font-weight:800; }
        .card { margin-top:12px; background:#fff; border:1px solid var(--line); border-radius:12px; padding:14px; }
        h1,h2{margin:0 0 8px;} p,li{color:var(--muted);} ul{margin:0;padding-left:18px;}
        @media(max-width:780px){ .top{flex-direction:column; align-items:flex-start; gap:10px;} }
    </style>
</head>
<body>
    <div class="wrap">
        <div class="top">
            <div>
                <div class="brand">TICKIFY PRESENTER</div>
                <div class="profile-meta">${userFullName} | ${sessionScope.userRoleNumberLabel} | ${sessionScope.userCampusName}</div>
            </div>
            <a class="logout" href="../LogoutServlet.do">Logout</a>
        </div>
        <section class="card"><h1>Presenter Workspace</h1><p>Track assigned events, coordinate venue details, and validate speaking-session readiness.</p></section>
        <section class="card"><h2>Current Focus</h2><ul><li>Review event lineup and session timing windows.</li><li>Confirm venue logistics and equipment availability.</li><li>Coordinate with event managers on attendee thresholds.</li></ul></section>
    </div>
    <script src="${pageContext.request.contextPath}/assets/error-popup.js"></script>
</body>
</html>
