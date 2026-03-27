(function () {
    var params = new URLSearchParams(window.location.search || "");
    if (params.get("suppressErrorPopup") === "1") {
        return;
    }

    var err = params.get("err");
    if (!err) {
        return;
    }

    var messages = {
        SessionExpired: "Your session expired. Please sign in again.",
        AccessDenied: "Access denied for your current role.",
        MissingFields: "Please complete all required fields.",
        OperationFailed: "Operation failed. Please try again.",
        UnknownAction: "Unknown action requested.",
        RootAuthFailed: "Root password is incorrect.",
        RootPasswordMismatch: "New root password and confirmation do not match.",
        InvalidEvent: "Selected event is invalid.",
        AgeRestricted: "Your account is under 18 and cannot purchase tickets for this event type.",
        CartUpdateFailed: "Unable to update cart. Please try again.",
        CartEmpty: "Your cart is empty.",
        CheckoutFailed: "Checkout failed. Please try again.",
        TermsRequired: "You must accept the no-refund terms before continuing.",
        PaymentFailed: "Payment failed. Please try again.",
        WishlistFailed: "Wishlist update failed. Please try again."
    };

    var text = messages[err] || ("Error: " + err.replace(/_/g, " "));
    window.alert(text);

    if (err === "SessionExpired" || err === "AccessDenied") {
        var loginUrl = (window.location.pathname.indexOf("/Tickify-SWP-Web-App/") !== -1)
            ? "/Tickify-SWP-Web-App/Login.jsp"
            : "Login.jsp";
        window.location.href = loginUrl;
    }
})();
