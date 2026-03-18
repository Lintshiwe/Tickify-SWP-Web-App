<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tickify | Registration</title>
        <style>
            /* ... (Keep your existing CSS exactly as it is) ... */
            body, html { height: 100%; margin: 0; font-family: Arial, sans-serif; background-color: white; color: black; display: flex; flex-direction: column; }
            header { background-color: black; color: #FFD700; padding: 20px; text-align: center; border-bottom: 5px solid #FFD700; }
            .main-body { flex: 1; display: flex; flex-direction: column; align-items: center; padding: 30px; }
            .form-container { width: 100%; max-width: 500px; border: 2px solid black; padding: 20px; border-radius: 15px; }
            .role-selection { display: flex; justify-content: space-around; margin-bottom: 25px; padding-bottom: 15px; border-bottom: 2px solid black; }
            .input-group { margin-bottom: 15px; display: flex; flex-direction: column; }
            label { font-weight: bold; margin-bottom: 5px; }
            input[type="text"], input[type="email"], input[type="password"] { padding: 10px; border: 2px solid black; border-radius: 5px; }
            .btn-submit { width: 100%; background-color: #FFD700; color: black; padding: 15px; font-weight: bold; font-size: 18px; border: 3px solid black; border-radius: 50px; cursor: pointer; transition: all 0.4s ease; margin-top: 10px; }
            .btn-submit:hover { background-color: black; color: #FFD700; }
            footer { background-color: black; color: white; text-align: center; padding: 15px; border-top: 5px solid #FFD700; }
            .yellow { color: #FFD700; }
            .hidden { display: none; }
        </style>

        <script>
            function toggleFields() {
                const isAttendee = document.getElementById('roleAttendee').checked;
                const attendeeSection = document.getElementById('attendeeFields');
                const presenterSection = document.getElementById('presenterFields');
                
                // Get Label Elements by ID
                const lblFirst = document.getElementById('lblFirstname');
                const lblLast = document.getElementById('lblLastname');
                const lblEmail = document.getElementById('lblEmail');
                const lblPass = document.getElementById('lblPassword');

                if (isAttendee) {
                    // Update Sections
                    attendeeSection.classList.remove('hidden');
                    presenterSection.classList.add('hidden');
                    
                    // Update Labels for Attendee
                    lblFirst.innerText = "Attendee First Name";
                    lblLast.innerText = "Attendee Last Name";
                    lblEmail.innerText = "Attendee Contact Email";
                    lblPass.innerText = "Attendee Account Password";
                } else {
                    // Update Sections
                    attendeeSection.classList.add('hidden');
                    presenterSection.classList.remove('hidden');
                    
                    // Update Labels for Presenter
                    lblFirst.innerText = "Tertiary Presenter First Name";
                    lblLast.innerText = "Tertiary Presenter Last Name";
                    lblEmail.innerText = "Professional Presenter Email";
                    lblPass.innerText = "Presenter Security Password";
                }
            }
        </script>
    </head>
    <body onload="toggleFields()"> <header>
            <h1><span class="yellow">TICKIFY</span> | REGISTRATION</h1>
        </header>

        <div class="main-body">
            <div class="form-container">
                <form action="RegistrationServlet.do" method="POST"> <div class="role-selection">
                        <label>
                            <input type="radio" name="userRole" id="roleAttendee" value="ATTENDEE" checked onclick="toggleFields()"> Attendee
                        </label>
                        <label>
                            <input type="radio" name="userRole" id="rolePresenter" value="PRESENTER" onclick="toggleFields()"> Presenter
                        </label>
                    </div>

                    <div class="input-group">
                        <label id="lblFirstname">First Name</label>
                        <input type="text" name="firstname" required>
                    </div>
                    <div class="input-group">
                        <label id="lblLastname">Last Name</label>
                        <input type="text" name="lastname" required>
                    </div>
                    <div class="input-group">
                        <label id="lblEmail">Email</label>
                        <input type="email" name="email" required>
                    </div>
                    <div class="input-group">
                        <label id="lblPassword">Password</label>
                        <input type="password" name="password" required>
                    </div>

                    <div id="attendeeFields">
                        <div class="input-group">
                            <label>Which Tertiary institution are you from: (Optional)</label>
                            <input type="text" name="attendeeInstitution">
                        </div>
                    </div>

                    <div id="presenterFields" class="hidden">
                        <div class="input-group">
                            <label>Which Tertiary institution are you representing: (Required)</label>
                            <input type="text" name="presenterInstitution">
                        </div>
                    </div>

                    <button type="submit" class="btn-submit">CREATE ACCOUNT</button>
                </form>
            </div>
        </div>

        <footer>
            &copy; 2026 <span class="yellow">Tickify</span> | Secure University Portal
        </footer>

    </body>
</html>