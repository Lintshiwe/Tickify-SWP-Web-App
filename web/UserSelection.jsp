<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tickify | User Selection</title>
        <style>
            /* Reset and Base Styles */
            body, html {
                height: 100%;
                margin: 0;
                font-family: 'Arial Black', Gadget, sans-serif;
                background-color: white; 
                color: black;
                display: flex;
                flex-direction: column;
            }

            /* 1. Header Section */
            header {
                background-color: black;
                color: #FFD700; /* Yellow */
                padding: 25px;
                text-align: center;
                border-bottom: 5px solid #FFD700;
            }

            /* 2. Main Body with Divider */
            .main-body {
                flex: 1; 
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .section {
                flex: 1;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                padding: 50px;
            }

            /* The Vertical Black Divider Line */
            .vertical-line {
                width: 4px;
                height: 60%;
                background-color: black;
            }

            /* 3. Interactive Button Styling */
            .btn {
                /* Default State: Yellow BG, Black Text */
                background-color: #FFD700; 
                color: black;
                
                padding: 20px 80px;
                text-decoration: none;
                font-weight: bold;
                font-size: 22px;
                
                /* Rounded Borders */
                border: 3px solid black;
                border-radius: 50px; 
                
                transition: all 0.4s ease; /* Smooth color swap */
                display: inline-block;
                text-align: center;
                cursor: pointer;
            }

            /* Hover State: Reversing the Colors */
            .btn:hover {
                background-color: black; 
                color: #FFD700; /* Yellow Text */
                transform: scale(1.1); /* Slight pop effect */
            }

            h2 {
                text-transform: uppercase;
                margin-bottom: 15px;
            }

            /* 4. Footer Section */
            footer {
                background-color: black;
                color: white;
                text-align: center;
                padding: 20px;
                border-top: 5px solid #FFD700;
            }

            .brand { color: #FFD700; }
        </style>
    </head>
    <body>

        <header>
            <h1><span class="brand">TICKIFY</span> | SELECT ACTION</h1>
        </header>

        <div class="main-body">
            <div class="section">
                <h2>Returning?</h2>
                <a href="Login.jsp" class="btn">LOGIN</a>
            </div>

            <div class="vertical-line"></div>

            <div class="section">
                <h2>New User?</h2>
                <a href="UserSignUp.jsp" class="btn">SIGN UP</a>
            </div>
        </div>

        <footer>
            &copy; 2026 <span class="brand">TICKIFY</span> - Secure University Management
        </footer>

    </body>
</html>