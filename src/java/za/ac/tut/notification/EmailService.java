package za.ac.tut.notification;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

    public void sendPasswordResetEmail(String toEmail, String resetLink) throws Exception {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        if (resetLink == null || resetLink.trim().isEmpty()) {
            throw new IllegalArgumentException("Reset link is required");
        }

        String host = required("TICKIFY_SMTP_HOST", "tickify.smtp.host");
        String user = required("TICKIFY_SMTP_USER", "tickify.smtp.user");
        String pass = required("TICKIFY_SMTP_PASSWORD", "tickify.smtp.password");
        String from = required("TICKIFY_SMTP_FROM", "tickify.smtp.from");
        String port = optional("TICKIFY_SMTP_PORT", "tickify.smtp.port", "587");
        boolean tls = Boolean.parseBoolean(optional("TICKIFY_SMTP_STARTTLS", "tickify.smtp.starttls", "true"));
        boolean ssl = Boolean.parseBoolean(optional("TICKIFY_SMTP_SSL", "tickify.smtp.ssl", "false"));

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));
        props.put("mail.smtp.ssl.enable", String.valueOf(ssl));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from, "Tickify Security"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject("Tickify Password Reset Request", "UTF-8");

        String logoUrl = optional("TICKIFY_LOGO_URL", "tickify.logo.url", "https://tickify.example/assets/tickify-logo.svg");
        String html = buildProfessionalResetHtml(logoUrl, resetLink);
        message.setContent(html, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private String buildProfessionalResetHtml(String logoUrl, String resetLink) {
        return "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"
                + "<body style='margin:0;padding:0;background:#f4f8f3;font-family:Segoe UI,Arial,sans-serif;color:#243228;'>"
                + "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='background:#f4f8f3;padding:28px 12px;'>"
                + "<tr><td align='center'>"
                + "<table role='presentation' width='620' cellspacing='0' cellpadding='0' style='max-width:620px;width:100%;background:#ffffff;border:1px solid #dce7d8;border-radius:16px;overflow:hidden;'>"
                + "<tr><td style='background:linear-gradient(135deg,#e6f4dc,#ffffff);padding:24px 24px 10px 24px;text-align:center;'>"
                + "<img src='" + escapeHtml(logoUrl) + "' alt='Tickify' style='height:52px;max-width:220px;width:auto;display:inline-block;'/>"
                + "<h1 style='margin:14px 0 6px;font-size:22px;line-height:1.3;color:#24412b;'>Password Reset Requested</h1>"
                + "<p style='margin:0;color:#4d6355;font-size:15px;line-height:1.5;'>A secure reset link was generated for your Tickify account.</p>"
                + "</td></tr>"
                + "<tr><td style='padding:22px 24px 20px 24px;'>"
                + "<p style='margin:0 0 14px;color:#304537;font-size:15px;line-height:1.6;'>If you requested this change, click the button below. This link expires in <strong>20 minutes</strong>.</p>"
                + "<p style='text-align:center;margin:18px 0 22px;'>"
                + "<a href='" + escapeHtml(resetLink) + "' style='display:inline-block;background:#79c84a;color:#ffffff;text-decoration:none;font-weight:700;padding:12px 22px;border-radius:10px;'>Reset Password</a>"
                + "</p>"
                + "<p style='margin:0;color:#4d6355;font-size:13px;line-height:1.6;'>If the button does not work, copy and paste this link into your browser:</p>"
                + "<p style='margin:8px 0 0;word-break:break-all;color:#2a5b37;font-size:12px;line-height:1.5;'>" + escapeHtml(resetLink) + "</p>"
                + "</td></tr>"
                + "<tr><td style='background:#f6faf4;padding:14px 24px;color:#5d7263;font-size:12px;line-height:1.6;border-top:1px solid #e3ece0;'>"
                + "If you did not request this reset, you can ignore this email. Your password remains unchanged."
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String required(String envKey, String sysKey) {
        String value = optional(envKey, sysKey, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required mail setting: " + envKey + " or -D" + sysKey);
        }
        return value.trim();
    }

    private String optional(String envKey, String sysKey, String fallback) {
        String env = System.getenv(envKey);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        String prop = System.getProperty(sysKey);
        if (prop != null && !prop.trim().isEmpty()) {
            return prop.trim();
        }
        return fallback;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
