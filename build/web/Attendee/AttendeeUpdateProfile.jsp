<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tickify | Update Profile</title>
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
                background-color: var(--light-grey);
            }

            /* MAIN CONTENT - Now 100% width */
            .main-content {
                width: 100%;
                min-height: 100vh;
                display: flex;
                flex-direction: column;
            }

            header {
                background-color: var(--black);
                color: white;
                padding: 15px 40px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 4px solid var(--gold);
            }

            .logo {
                font-size: 1.5rem;
                font-weight: bold;
                color: var(--gold);
                text-decoration: none;
            }

            .dashboard-body {
                padding: 40px;
                display: flex;
                flex-direction: column;
                align-items: center; /* Centers the form horizontally */
            }

            .section-title {
                text-align: center;
                border-bottom: 2px solid var(--gold);
                padding-bottom: 10px;
                margin-bottom: 30px;
                width: 100%;
                max-width: 600px;
            }

            /* FORM STYLING */
            .profile-form {
                background: white;
                padding: 40px;
                border-radius: 8px;
                box-shadow: 0 4px 20px rgba(0,0,0,0.1);
                width: 100%;
                max-width: 500px;
                border-top: 5px solid var(--black);
            }

            .form-group {
                margin-bottom: 20px;
            }

            .form-group label {
                display: block;
                font-weight: bold;
                margin-bottom: 8px;
                color: var(--dark-grey);
            }

            .form-group input {
                width: 100%;
                padding: 12px;
                border: 1px solid #ddd;
                border-radius: 4px;
                font-size: 1rem;
                box-sizing: border-box;
                transition: 0.3s;
            }

            .form-group input:focus {
                border-color: var(--gold);
                outline: none;
                box-shadow: 0 0 8px rgba(255, 215, 0, 0.3);
            }

            .btn-save {
                background-color: var(--gold);
                color: var(--black);
                border: 2px solid var(--black);
                padding: 15px;
                font-weight: bold;
                cursor: pointer;
                width: 100%;
                font-size: 1rem;
                transition: 0.3s;
                margin-top: 10px;
            }

            .btn-save:hover {
                background-color: var(--black);
                color: var(--gold);
            }

            .back-link {
                display: inline-block;
                margin-top: 20px;
                color: #555;
                text-decoration: none;
                font-weight: 500;
            }

            .back-link:hover {
                color: var(--black);
                text-decoration: underline;
            }

            /* Feedback Messages */
            .alert {
                padding: 15px;
                margin-bottom: 20px;
                border-radius: 4px;
                width: 100%;
                max-width: 500px;
                text-align: center;
            }
            .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
            .alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        </style>
    </head>
    <body>

        <div class="main-content">
            <header>
                <a href="AttendeeDashboard.jsp" class="logo">TICKIFY</a>
                <div style="display: flex; gap: 20px; align-items: center;">
                    <span>Welcome, <strong>${userFullName}</strong></span>
                    <a href="LogoutServlet.do" style="color: white; text-decoration: none; font-size: 0.8rem; background: #900; padding: 5px 10px; border-radius: 3px;">LOGOUT</a>
                </div>
            </header>

            <div class="dashboard-body">
                <div class="section-title">
                    <h2>Update Your Profile</h2>
                </div>

                <c:if test="${not empty message}">
                    <div class="alert alert-success">${message}</div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert alert-error">${error}</div>
                </c:if>

                <div class="profile-form">
                    <form action="AttendeeViewProfileServlet.do" method="POST">
                        
                        <div class="form-group">
                            <label for="firstName">First Name</label>
                            <input type="text" id="firstName" name="firstName" value="${userProfile.firstname}" required>
                        </div>

                        <div class="form-group">
                            <label for="lastName">Last Name</label>
                            <input type="text" id="lastName" name="lastName" value="${userProfile.lastname}" required>
                        </div>

                        <div class="form-group">
                            <label for="email">Email Address</label>
                            <input type="email" id="email" name="email" value="${userProfile.email}" required>
                        </div>

                        <div class="form-group">
                            <label for="tertiary">Tertiary Institution</label>
                            <input type="text" id="tertiary" name="tertiary" value="${userProfile.tertiaryInstitution}" required>
                        </div>

                        <button type="submit" class="btn-save">SAVE CHANGES</button>
                    </form>
                </div>
                
                <a href="AttendeeDashboardServlet.do" class="back-link">← Return to Dashboard</a>
            </div>
        </div>

    </body>
</html>