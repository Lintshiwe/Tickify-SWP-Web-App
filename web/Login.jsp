<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tickify | Login</title>
        <style>
            body, html { height: 100%; margin: 0; font-family: 'Segoe UI', sans-serif; background-color: #f4f4f4; display: flex; flex-direction: column; }
            
            header { background-color: black; color: #FFD700; padding: 20px; text-align: center; border-bottom: 5px solid #FFD700; }

            .main-body { flex: 1; display: flex; justify-content: center; align-items: center; padding: 20px; }

            .login-card { background: white; width: 100%; max-width: 480px; padding: 30px; border: 2px solid black; border-radius: 15px; box-shadow: 8px 8px 0px black; }

            /* Radio Button Group Styling */
            .role-selector {
                display: flex;
                flex-wrap: wrap;
                justify-content: space-between;
                margin-bottom: 25px;
                padding: 15px;
                background-color: #f9f9f9;
                border: 1px solid #ddd;
                border-radius: 8px;
            }
            .role-option { font-size: 13px; font-weight: bold; display: flex; align-items: center; gap: 5px; cursor: pointer; margin: 5px; }

            .input-group { margin-bottom: 20px; }
            label { display: block; font-weight: bold; margin-bottom: 8px; }
            
            input[type="email"], input[type="password"] {
                width: 100%; padding: 12px; box-sizing: border-box; border: 2px solid black; border-radius: 8px; font-size: 16px;
            }

            .btn-login {
                width: 100%; background-color: #FFD700; color: black; padding: 15px; font-size: 18px; font-weight: bold;
                border: 3px solid black; border-radius: 50px; cursor: pointer; transition: 0.2s;
            }
            .btn-login:hover { background-color: black; color: #FFD700; }

            footer { background-color: black; color: white; text-align: center; padding: 15px; border-top: 5px solid #FFD700; margin-top: auto; }
            .gold { color: #FFD700; }
            .error { background-color: #ffe6e6; color: #cc0000; padding: 10px; border-radius: 5px; text-align: center; margin-bottom: 15px; font-weight: bold; border: 1px solid #cc0000; }
        </style>

        <script>
            function updatePlaceholders() {
                // Get the selected radio button value
                const selectedRole = document.querySelector('input[name="userRole"]:checked').value;
                const emailInput = document.getElementById('emailField');
                const passInput = document.getElementById('passwordField');
                const emailLabel = document.getElementById('emailLabel');
                const passLabel = document.getElementById('passLabel');

                // Create a readable version of the role for the UI
                const roleNames = {
                    'ATTENDEE': 'Attendee',
                    'TERTIARY_PRESENTER': 'Presenter',
                    'EVENT_MANAGER': 'Manager',
                    'VENUE_GUARD': 'Venue Guard',
                    'ADMIN': 'Administrator'
                };

                const friendlyName = roleNames[selectedRole];

                // Update Labels and Placeholders dynamically
                emailLabel.innerText = friendlyName + " Email Address";
                passLabel.innerText = friendlyName + " Password";
                
                emailInput.placeholder = "Enter your " + friendlyName.toLowerCase() + " email";
                passInput.placeholder = "Enter your " + friendlyName.toLowerCase() + " password";
            }
        </script>
    </head>
    <body onload="updatePlaceholders()">

        <header>
            <h1><span class="gold">TICKIFY</span> PORTAL LOGIN</h1>
        </header>

        <div class="main-body">
            <div class="login-card">
                
                <%-- Display Error Message --%>
                <% if(request.getAttribute("error") != null) { %>
                    <div class="error"><%= request.getAttribute("error") %></div>
                <% } %>

                <form action="LoginServlet.do" method="POST">
                    
                    <p style="text-align: center; font-weight: bold; margin-top: 0;">Select Your Role:</p>
                    <div class="role-selector">
                        <label class="role-option"><input type="radio" name="userRole" value="ATTENDEE" checked onclick="updatePlaceholders()"> Attendee</label>
                        <label class="role-option"><input type="radio" name="userRole" value="TERTIARY_PRESENTER" onclick="updatePlaceholders()"> Presenter</label>
                        <label class="role-option"><input type="radio" name="userRole" value="EVENT_MANAGER" onclick="updatePlaceholders()"> Manager</label>
                        <label class="role-option"><input type="radio" name="userRole" value="VENUE_GUARD" onclick="updatePlaceholders()"> Guard</label>
                        <label class="role-option"><input type="radio" name="userRole" value="ADMIN" onclick="updatePlaceholders()"> Admin</label>
                    </div>

                    <div class="input-group">
                        <label id="emailLabel">Email</label>
                        <input type="email" name="email" id="emailField" required>
                    </div>

                    <div class="input-group">
                        <label id="passLabel">Password</label>
                        <input type="password" name="password" id="passwordField" required>
                    </div>

                    <button type="submit" class="btn-login">ACCESS PORTAL</button>
                </form>
            </div>
        </div>

        <footer>
            &copy; 2026 <span class="gold">Tickify</span> | Secure Academic Portal
        </footer>

    </body>
</html>