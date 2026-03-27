<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/favicon.ico">
    <title>Tickify | Payment Complete</title>
    <style>
        body { margin:0; font-family:"Trebuchet MS","Segoe UI",sans-serif; background:#f7faf6; color:#223028; }
        .wrap { min-height:100vh; width:100%; display:grid; place-items:center; padding:20px clamp(12px,2.7vw,36px); }
        .card {
            background:#fff; border:1px solid #d7e2d1; border-radius:14px; padding:18px;
            box-shadow:0 8px 22px rgba(33,47,32,.08); max-width:none; width:100%;
        }
        .btn { display:inline-block; margin-top:10px; padding:10px 12px; border-radius:10px; font-weight:800; text-decoration:none; border:1px solid #d7e2d1; color:#2f3a32; background:#fff; }
    </style>
</head>
<body>
    <div class="wrap">
        <div class="card">
            <h2 style="margin:0 0 8px;">Payment Complete</h2>
            <p style="margin:0 0 10px;color:#5d6a60;">Your tickets are being opened in a new window.</p>
            <p style="margin:0 0 10px;color:#5d6a60;">Reference: <strong>${transactionRef}</strong></p>
            <p style="margin:0;color:#5d6a60;">If the popup is blocked, use the button below.</p>
            <a class="btn" href="${ticketWindowUrl}" target="_blank" rel="noopener">Open Tickets</a>
            <a class="btn" href="${nextUrl}">Continue to Dashboard</a>
        </div>
    </div>

    <script>
        (function () {
            var ticketUrl = "${ticketWindowUrl}";
            var nextUrl = "${nextUrl}";
            var popupName = "${popupWindowName}";
            if (!popupName) {
                popupName = "tickifyTickets";
            }
            var popup = window.open(ticketUrl, popupName, "width=1040,height=820,resizable=yes,scrollbars=yes");
            setTimeout(function () {
                window.location.href = nextUrl;
            }, 900);
        })();
    </script>
    <script src="${pageContext.request.contextPath}/assets/error-popup.js"></script>
</body>
</html>
