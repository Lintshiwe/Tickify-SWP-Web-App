package za.ac.tut.notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
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

        String logoUrl = optional("TICKIFY_LOGO_URL", "tickify.logo.url", "https://tickify.example/assets/tickify-logo.svg");
        String inlineLogoSvg = defaultInlineLogoSvg();
        String html = buildProfessionalResetHtml(logoUrl, resetLink, inlineLogoSvg != null && !inlineLogoSvg.trim().isEmpty());

        try {
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
            message.setFrom(new InternetAddress(from, "Tickify - no-reply"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setReplyTo(javax.mail.internet.InternetAddress.parse("noreply@tickify"));
            message.setSubject("Tickify Password Reset Request", "UTF-8");
            message.setContent(html, "text/html; charset=UTF-8");

            Transport.send(message);
        } catch (Throwable primaryFailure) {
            // Work around JVM TLS classpath conflicts by relaying via python3 smtplib.
            try {
                sendViaPythonSmtp(toEmail, "Tickify Password Reset Request", buildPlainTextResetBody(resetLink), html,
                        inlineLogoSvg, null, host, port, user, pass, from, ssl, tls);
            } catch (Exception fallbackFailure) {
                fallbackFailure.addSuppressed(primaryFailure);
                throw fallbackFailure;
            }
        }
    }

    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) throws Exception {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        String host = required("TICKIFY_SMTP_HOST", "tickify.smtp.host");
        String user = required("TICKIFY_SMTP_USER", "tickify.smtp.user");
        String pass = required("TICKIFY_SMTP_PASSWORD", "tickify.smtp.password");
        String from = required("TICKIFY_SMTP_FROM", "tickify.smtp.from");
        String port = optional("TICKIFY_SMTP_PORT", "tickify.smtp.port", "587");
        boolean tls = Boolean.parseBoolean(optional("TICKIFY_SMTP_STARTTLS", "tickify.smtp.starttls", "true"));
        boolean ssl = Boolean.parseBoolean(optional("TICKIFY_SMTP_SSL", "tickify.smtp.ssl", "false"));

        try {
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
            message.setFrom(new InternetAddress(from, "Tickify - no-reply"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setReplyTo(javax.mail.internet.InternetAddress.parse("noreply@tickify"));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlBody, "text/html; charset=UTF-8");
            Transport.send(message);
        } catch (Throwable primaryFailure) {
            try {
                sendViaPythonSmtp(toEmail, subject, htmlBody.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim(), htmlBody,
                        defaultInlineLogoSvg(), null, host, port, user, pass, from, ssl, tls);
            } catch (Exception fallbackFailure) {
                fallbackFailure.addSuppressed(primaryFailure);
                throw fallbackFailure;
            }
        }
    }

    public void sendTicketPurchaseEmail(String toEmail, String attendeeName, String transactionRef,
            List<Map<String, Object>> tickets, String myTicketsLink) throws Exception {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        if (tickets == null || tickets.isEmpty()) {
            throw new IllegalArgumentException("At least one ticket is required");
        }

        String host = required("TICKIFY_SMTP_HOST", "tickify.smtp.host");
        String user = required("TICKIFY_SMTP_USER", "tickify.smtp.user");
        String pass = required("TICKIFY_SMTP_PASSWORD", "tickify.smtp.password");
        String from = required("TICKIFY_SMTP_FROM", "tickify.smtp.from");
        String port = optional("TICKIFY_SMTP_PORT", "tickify.smtp.port", "587");
        boolean tls = Boolean.parseBoolean(optional("TICKIFY_SMTP_STARTTLS", "tickify.smtp.starttls", "true"));
        boolean ssl = Boolean.parseBoolean(optional("TICKIFY_SMTP_SSL", "tickify.smtp.ssl", "false"));

        String logoUrl = optional("TICKIFY_LOGO_URL", "tickify.logo.url", "https://tickify.example/assets/tickify-logo.svg");
        String inlineLogoSvg = defaultInlineLogoSvg();
        String html = buildTicketPurchaseHtml(logoUrl, inlineLogoSvg != null && !inlineLogoSvg.trim().isEmpty(), attendeeName,
                transactionRef, tickets, myTicketsLink);
        String text = buildTicketPurchaseText(attendeeName, transactionRef, tickets, myTicketsLink);
        List<MailAttachment> attachments = buildTicketPdfAttachments(tickets);

        try {
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
            message.setFrom(new InternetAddress(from, "Tickify - no-reply"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setReplyTo(javax.mail.internet.InternetAddress.parse("noreply@tickify"));
            message.setSubject("Your Tickify Ticket Purchase", "UTF-8");
            message.setContent(html, "text/html; charset=UTF-8");

            Transport.send(message);
        } catch (Throwable primaryFailure) {
            try {
                sendViaPythonSmtp(toEmail, "Your Tickify Ticket Purchase", text, html, inlineLogoSvg,
                        attachments, host, port, user, pass, from, ssl, tls);
            } catch (Exception fallbackFailure) {
                fallbackFailure.addSuppressed(primaryFailure);
                throw fallbackFailure;
            }
        }
    }

    private String buildProfessionalResetHtml(String logoUrl, String resetLink, boolean useInlineCidLogo) {
        String logoBlock = renderLogoBlock(logoUrl, useInlineCidLogo);
        return "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"
                + "<body style='margin:0;padding:0;background:#f4f8f3;font-family:Segoe UI,Arial,sans-serif;color:#243228;'>"
                + "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='background:#f4f8f3;padding:28px 12px;'>"
                + "<tr><td align='center'>"
                + "<table role='presentation' width='620' cellspacing='0' cellpadding='0' style='max-width:620px;width:100%;background:#ffffff;border:1px solid #dce7d8;border-radius:16px;overflow:hidden;'>"
                + "<tr><td style='background:linear-gradient(135deg,#e6f4dc,#ffffff);padding:24px 24px 10px 24px;text-align:center;'>"
            + logoBlock
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

    private String renderLogoBlock(String logoUrl, boolean useInlineCidLogo) {
        if (logoUrl != null) {
            String normalized = logoUrl.trim().toLowerCase();
            if (!normalized.isEmpty() && !normalized.contains("tickify.example")) {
                return "<img src='" + escapeHtml(logoUrl) + "' alt='Tickify' style='height:52px;max-width:220px;width:auto;display:inline-block;'/>";
            }
        }

        if (useInlineCidLogo) {
            return "<div style='text-align:center;'>"
                    + "<img src='cid:tickifylogo' alt='Tickify' style='height:52px;max-width:220px;width:auto;display:inline-block;'/>"
                    + "<div style='font-size:11px;color:#5d7263;margin-top:6px;'>Tickify</div>"
                    + "</div>";
        }

        return "<div style='display:inline-block;border:2px solid #79c84a;border-radius:12px;padding:8px 14px;background:#ffffff;'>"
                + "<span style='font-size:26px;line-height:1;font-weight:800;color:#2d5a36;letter-spacing:1px;'>TICKIFY</span>"
                + "</div>";
    }

    private String buildPlainTextResetBody(String resetLink) {
        return "Tickify Password Reset Request\n\n"
                + "A secure reset link was generated for your Tickify account.\n"
                + "This link expires in 20 minutes.\n\n"
                + "Reset Password: " + resetLink + "\n\n"
                + "If you did not request this reset, you can ignore this email. "
                + "Your password remains unchanged.";
    }

    private String buildTicketPurchaseText(String attendeeName, String transactionRef,
            List<Map<String, Object>> tickets, String myTicketsLink) {
        StringBuilder body = new StringBuilder();
        body.append("Tickify Ticket Purchase Confirmation\n\n");
        body.append("Hi ").append(attendeeName == null || attendeeName.trim().isEmpty() ? "there" : attendeeName.trim()).append(",\n");
        body.append("Your payment was successful and your tickets are ready.\n");
        if (transactionRef != null && !transactionRef.trim().isEmpty()) {
            body.append("Transaction Ref: ").append(transactionRef.trim()).append("\n");
        }
        body.append("\nTickets:\n");

        for (Map<String, Object> ticket : tickets) {
            body.append("- ").append(safe(ticket.get("ticketNumber")))
                    .append(" | Event: ").append(safe(ticket.get("eventName")))
                    .append(" | Date: ").append(formatDate(ticket.get("eventDate")))
                    .append(" | Venue: ").append(safe(ticket.get("venueName")))
                    .append(" | QR: ").append(safe(ticket.get("qrCode")))
                    .append("\n");
        }

        if (myTicketsLink != null && !myTicketsLink.trim().isEmpty()) {
            body.append("\nView all tickets: ").append(myTicketsLink.trim()).append("\n");
        }
        body.append("\nThank you for booking with Tickify.");
        return body.toString();
    }

    private String buildTicketPurchaseHtml(String logoUrl, boolean useInlineCidLogo,
            String attendeeName, String transactionRef, List<Map<String, Object>> tickets, String myTicketsLink) {
        String appBase = optional("TICKIFY_APP_BASE_URL", "tickify.app.baseUrl", "http://localhost:8080/Tickify-SWP-Web-App");
        if (appBase.endsWith("/")) {
            appBase = appBase.substring(0, appBase.length() - 1);
        }
        
        StringBuilder rows = new StringBuilder();
        StringBuilder imageSection = new StringBuilder();
        for (Map<String, Object> ticket : tickets) {
            rows.append("<tr>")
                    .append("<td style='padding:8px;border-bottom:1px solid #e7efe3;'>").append(escapeHtml(safe(ticket.get("ticketNumber")))).append("</td>")
                    .append("<td style='padding:8px;border-bottom:1px solid #e7efe3;'>").append(escapeHtml(safe(ticket.get("eventName")))).append("</td>")
                    .append("<td style='padding:8px;border-bottom:1px solid #e7efe3;'>").append(escapeHtml(formatDate(ticket.get("eventDate")))).append("</td>")
                    .append("<td style='padding:8px;border-bottom:1px solid #e7efe3;'>").append(escapeHtml(safe(ticket.get("venueName")))).append("</td>")
                    .append("<td style='padding:8px;border-bottom:1px solid #e7efe3;font-family:monospace;'>").append(escapeHtml(safe(ticket.get("qrCode")))).append("</td>")
                    .append("</tr>");
            
            Object imgUrl = ticket.get("eventImageUrl");
            if (imgUrl != null && !imgUrl.toString().trim().isEmpty()) {
                String fullUrl = appBase + "/" + imgUrl.toString().trim();
                imageSection.append("<tr><td style='padding:8px 0;'>")
                    .append("<img src='").append(escapeHtml(fullUrl))
                    .append("' alt='").append(escapeHtml(safe(ticket.get("eventName"))))
                    .append("' style='width:100%;max-width:640px;height:auto;border-radius:12px;border:1px solid #e7efe3;'/>")
                    .append("</td></tr>");
            }
        }

        String logoBlock = renderLogoBlock(logoUrl, useInlineCidLogo);
        String greeting = attendeeName == null || attendeeName.trim().isEmpty() ? "Hi there," : "Hi " + escapeHtml(attendeeName.trim()) + ",";
        String tx = transactionRef == null || transactionRef.trim().isEmpty()
                ? ""
                : "<p style='margin:8px 0 0;color:#4d6355;font-size:14px;'>Transaction Ref: <strong>" + escapeHtml(transactionRef.trim()) + "</strong></p>";
        String cta = (myTicketsLink == null || myTicketsLink.trim().isEmpty())
                ? ""
                : "<p style='margin:16px 0 4px;'><a href='" + escapeHtml(myTicketsLink.trim())
                + "' style='display:inline-block;background:#79c84a;color:#fff;text-decoration:none;padding:10px 18px;border-radius:10px;font-weight:700;'>Open My Tickets</a></p>";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>"
                + "<body style='margin:0;padding:0;background:#f4f8f3;font-family:Segoe UI,Arial,sans-serif;color:#243228;'>"
                + "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='background:#f4f8f3;padding:28px 12px;'><tr><td align='center'>"
                + "<table role='presentation' width='680' cellspacing='0' cellpadding='0' style='max-width:680px;width:100%;background:#fff;border:1px solid #dce7d8;border-radius:16px;overflow:hidden;'>"
                + "<tr><td style='background:linear-gradient(135deg,#e6f4dc,#ffffff);padding:24px 24px 10px 24px;text-align:center;'>"
                + logoBlock
                + "<h1 style='margin:14px 0 6px;font-size:22px;line-height:1.3;color:#24412b;'>Your Tickets Are Ready</h1>"
                + "<p style='margin:0;color:#4d6355;font-size:15px;line-height:1.5;'>" + greeting + " Your purchase was successful.</p>"
                + tx
                + "</td></tr>"
                + "<tr><td style='padding:18px 24px 16px 24px;'>"
                + "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='border-collapse:collapse;font-size:13px;'>"
                + "<thead><tr style='background:#f0f7ec;color:#2e4f37;'>"
                + "<th align='left' style='padding:8px;border-bottom:1px solid #cfe2c9;'>Ticket</th>"
                + "<th align='left' style='padding:8px;border-bottom:1px solid #cfe2c9;'>Event</th>"
                + "<th align='left' style='padding:8px;border-bottom:1px solid #cfe2c9;'>Date</th>"
                + "<th align='left' style='padding:8px;border-bottom:1px solid #cfe2c9;'>Venue</th>"
                + "<th align='left' style='padding:8px;border-bottom:1px solid #cfe2c9;'>QR Code</th>"
                + "</tr></thead><tbody>" + rows + "</tbody></table>"
                + (imageSection.length() > 0 ? 
                    "<table role='presentation' width='100%' cellspacing='0' cellpadding='0' style='margin-top:14px;'>" 
                    + imageSection + "</table>" : "")
                + cta
                + "<p style='margin:12px 0 0;color:#5d7263;font-size:12px;'>Please keep this email for check-in. Each ticket has a unique QR code.</p>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private void sendViaPythonSmtp(String to, String subject, String textBody, String htmlBody, String logoSvg,
            List<MailAttachment> attachments,
            String host, String port, String user, String pass, String from,
            boolean ssl, boolean starttls) throws Exception {
        String script = "import os, smtplib, ssl, sys\n"
                + "from email import encoders\n"
                + "from email.mime.application import MIMEApplication\n"
                + "from email.mime.base import MIMEBase\n"
                + "from email.mime.multipart import MIMEMultipart\n"
                + "from email.mime.text import MIMEText\n"
                + "to = sys.argv[1]\n"
                + "subject = sys.argv[2]\n"
                + "msg = MIMEMultipart('related')\n"
                + "alt = MIMEMultipart('alternative')\n"
                + "msg.attach(alt)\n"
                + "msg['Subject'] = subject\n"
                + "msg['From'] = os.environ['TICKIFY_PY_FROM']\n"
                + "msg['To'] = to\n"
                + "alt.attach(MIMEText(os.environ['TICKIFY_PY_TEXT'], 'plain', 'utf-8'))\n"
                + "alt.attach(MIMEText(os.environ['TICKIFY_PY_HTML'], 'html', 'utf-8'))\n"
                + "logo_svg = os.environ.get('TICKIFY_PY_LOGO_SVG', '').strip()\n"
                + "if logo_svg:\n"
                + "    logo = MIMEBase('image', 'svg+xml')\n"
                + "    logo.set_payload(logo_svg.encode('utf-8'))\n"
                + "    encoders.encode_base64(logo)\n"
                + "    logo.add_header('Content-ID', '<tickifylogo>')\n"
                + "    logo.add_header('Content-Disposition', 'inline', filename='tickify-logo.svg')\n"
                + "    logo.add_header('Content-Type', 'image/svg+xml; name=tickify-logo.svg')\n"
                + "    msg.attach(logo)\n"
                + "attach_dir = os.environ.get('TICKIFY_PY_ATTACH_DIR', '').strip()\n"
                + "if attach_dir and os.path.isdir(attach_dir):\n"
                + "    for name in sorted(os.listdir(attach_dir)):\n"
                + "        fp = os.path.join(attach_dir, name)\n"
                + "        if not os.path.isfile(fp):\n"
                + "            continue\n"
                + "        with open(fp, 'rb') as f:\n"
                + "            part = MIMEApplication(f.read(), _subtype='pdf')\n"
                + "        part.add_header('Content-Disposition', 'attachment', filename=name)\n"
                + "        msg.attach(part)\n"
                + "host = os.environ['TICKIFY_PY_HOST']\n"
                + "port = int(os.environ['TICKIFY_PY_PORT'])\n"
                + "user = os.environ['TICKIFY_PY_USER']\n"
                + "password = os.environ['TICKIFY_PY_PASS']\n"
                + "use_ssl = os.environ['TICKIFY_PY_SSL'].lower() == 'true'\n"
                + "use_starttls = os.environ['TICKIFY_PY_STARTTLS'].lower() == 'true'\n"
                + "if use_ssl:\n"
                + "    server = smtplib.SMTP_SSL(host, port, timeout=20)\n"
                + "else:\n"
                + "    server = smtplib.SMTP(host, port, timeout=20)\n"
                + "try:\n"
                + "    server.ehlo()\n"
                + "    if use_starttls:\n"
                + "        server.starttls(context=ssl.create_default_context())\n"
                + "        server.ehlo()\n"
                + "    server.login(user, password)\n"
                + "    server.sendmail(msg['From'], [to], msg.as_string())\n"
                + "finally:\n"
                + "    server.quit()\n";

        Path attachDir = null;
        if (attachments != null && !attachments.isEmpty()) {
            attachDir = Files.createTempDirectory("tickify-mail-attach-");
            int index = 1;
            for (MailAttachment attachment : attachments) {
                if (attachment == null || attachment.filename == null || attachment.content == null) {
                    continue;
                }
                String safeName = sanitizeFilename(attachment.filename);
                if (!safeName.toLowerCase(Locale.ENGLISH).endsWith(".pdf")) {
                    safeName = safeName + ".pdf";
                }
                String prefixed = String.format(Locale.ENGLISH, "%02d-%s", index++, safeName);
                Files.write(attachDir.resolve(prefixed), attachment.content,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            }
        }

        ProcessBuilder pb = new ProcessBuilder(Arrays.asList("python3", "-c", script, to, subject));
        pb.redirectErrorStream(true);
        Map<String, String> env = pb.environment();
        env.put("TICKIFY_PY_HOST", host);
        env.put("TICKIFY_PY_PORT", port);
        env.put("TICKIFY_PY_USER", user);
        env.put("TICKIFY_PY_PASS", pass);
        env.put("TICKIFY_PY_FROM", from);
        env.put("TICKIFY_PY_SSL", String.valueOf(ssl));
        env.put("TICKIFY_PY_STARTTLS", String.valueOf(starttls));
        env.put("TICKIFY_PY_TEXT", textBody);
        env.put("TICKIFY_PY_HTML", htmlBody);
        env.put("TICKIFY_PY_LOGO_SVG", logoSvg == null ? "" : logoSvg);
        if (attachDir != null) {
            env.put("TICKIFY_PY_ATTACH_DIR", attachDir.toAbsolutePath().toString());
        }

        try {
            Process p = pb.start();
            String output = readAll(p);
            boolean finished = p.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                throw new IOException("Python SMTP relay timed out.");
            }
            if (p.exitValue() != 0) {
                throw new IOException("Python SMTP relay failed: " + output);
            }
        } finally {
            deleteDirectoryQuietly(attachDir);
        }
    }

    private String readAll(Process p) throws IOException {
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (out.length() > 0) {
                    out.append('\n');
                }
                out.append(line);
            }
        }
        return out.toString();
    }

    private String formatDate(Object dateValue) {
        if (dateValue instanceof Date) {
            return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format((Date) dateValue);
        }
        return safe(dateValue);
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private List<MailAttachment> buildTicketPdfAttachments(List<Map<String, Object>> tickets) {
        List<MailAttachment> attachments = new ArrayList<>();
        for (Map<String, Object> ticket : tickets) {
            byte[] pdf = buildStyledTicketPdf(ticket);
            String ticketNumber = safe(ticket.get("ticketNumber"));
            if (ticketNumber.trim().isEmpty()) {
                ticketNumber = "ticket";
            }
            String filename = "tickify-" + ticketNumber.replace(' ', '-') + ".pdf";
            attachments.add(new MailAttachment(filename, pdf));
        }
        return attachments;
    }

    private byte[] buildStyledTicketPdf(Map<String, Object> ticket) {
        String ticketNo = safe(ticket.get("ticketNumber"));
        String eventName = safe(ticket.get("eventName"));
        String eventDate = formatDate(ticket.get("eventDate"));
        String venue = safe(ticket.get("venueName"));
        String qrCode = safe(ticket.get("qrCode"));
        String eventType = safe(ticket.get("eventType"));
        String price = String.format(Locale.ENGLISH, "R %.2f",
                ticket.get("price") instanceof Number ? ((Number) ticket.get("price")).doubleValue() : 0.0);

        List<String> objects = new ArrayList<>();
        int objNum = 1;

        int x0 = 30, y0 = 780, w = 535, h = 250;
        int headerH = 50;
        String green = "0.475 0.784 0.29";
        String greenDark = "0.36 0.66 0.2";
        String gray = "0.42 0.50 0.44";
        String lightBg = "0.96 0.97 0.95";

        // Object 1: Catalog
        objects.add(objNum + " 0 obj << /Type /Catalog /Pages 2 0 R >> endobj");
        objNum++;

        // Object 2: Pages
        objects.add(objNum + " 0 obj << /Type /Pages /Kids [4 0 R] /Count 1 >> endobj");
        objNum++;

        // Object 3: Font
        objects.add(objNum + " 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj");
        objNum++;
        int fontObj = objNum - 1;

        // Object 4: Font Bold
        objects.add(objNum + " 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >> endobj");
        objNum++;
        int fontBoldObj = objNum - 1;

        // Object for page
        int pageObj = objNum;
        objects.add(null); // placeholder

        // Build content stream
        StringBuilder stream = new StringBuilder();
        // Background
        stream.append("q ").append(lightBg).append(" rg ").append(x0).append(" ").append(y0 - h).append(" ").append(w).append(" ").append(h).append(" re f Q\n");
        // Border
        stream.append("q 0.85 0.90 0.84 rg ").append(x0).append(" ").append(y0 - h).append(" ").append(w).append(" ").append(h).append(" re S Q\n");
        // Header bar
        stream.append("q ").append(green).append(" rg ").append(x0).append(" ").append(y0 - headerH).append(" ").append(w).append(" ").append(headerH).append(" re f Q\n");
        // Title
        stream.append("BT /F").append(fontBoldObj).append(" 22 Tf 1 1 1 rg ").append(x0 + 15).append(" ").append(y0 - 32).append(" Td (TICKIFY) Tj ET\n");
        // Ticket label
        stream.append("BT /F").append(fontBoldObj).append(" 10 Tf 1 1 1 rg ").append(x0 + 150).append(" ").append(y0 - 32).append(" Td (OFFICIAL EVENT TICKET) Tj ET\n");

        // Content area
        int cx = x0 + 15, cy = y0 - headerH - 20;
        stream.append("BT /F").append(fontBoldObj).append(" 9 Tf ").append(green).append(" rg ").append(cx).append(" ").append(cy).append(" Td (EVENT) Tj ET\n");
        cy -= 14;
        stream.append("BT /F").append(fontBoldObj).append(" 13 Tf 0.15 0.17 0.15 rg ").append(cx).append(" ").append(cy).append(" Td (").append(escapePdf(eventName.length() > 45 ? eventName.substring(0, 42) + "..." : eventName)).append(") Tj ET\n");

        cy -= 22;
        stream.append("BT /F").append(fontBoldObj).append(" 9 Tf ").append(green).append(" rg ").append(cx).append(" ").append(cy).append(" Td (DATE & VENUE) Tj ET\n");
        cy -= 14;
        stream.append("BT /F").append(fontObj).append(" 11 Tf 0.15 0.17 0.15 rg ").append(cx).append(" ").append(cy).append(" Td (").append(escapePdf(eventDate)).append("  |  ").append(escapePdf(venue.length() > 35 ? venue.substring(0, 32) + "..." : venue)).append(") Tj ET\n");

        // Separator
        cy -= 18;
        stream.append("q 0.85 0.90 0.84 rg ").append(cx).append(" ").append(cy).append(" ").append(w - 30).append(" 1 re f Q\n");
        cy -= 12;

        // Ticket details
        stream.append("BT /F").append(fontBoldObj).append(" 8 Tf ").append(gray).append(" rg ").append(cx).append(" ").append(cy).append(" Td (TICKET #) Tj ET\n");
        cy -= 13;
        stream.append("BT /F").append(fontObj).append(" 10 Tf 0.15 0.17 0.15 rg ").append(cx).append(" ").append(cy).append(" Td (").append(escapePdf(ticketNo)).append(") Tj ET\n");

        int rx = cx + 200;
        stream.append("BT /F").append(fontBoldObj).append(" 8 Tf ").append(gray).append(" rg ").append(rx).append(" ").append(cy + 13).append(" Td (TYPE) Tj ET\n");
        cy -= 13;
        stream.append("BT /F").append(fontObj).append(" 10 Tf 0.15 0.17 0.15 rg ").append(rx).append(" ").append(cy).append(" Td (").append(escapePdf(eventType)).append(") Tj ET\n");

        cy -= 18;
        stream.append("BT /F").append(fontBoldObj).append(" 8 Tf ").append(gray).append(" rg ").append(cx).append(" ").append(cy).append(" Td (PRICE) Tj ET\n");
        cy -= 13;
        stream.append("BT /F").append(fontBoldObj).append(" 14 Tf ").append(green).append(" rg ").append(cx).append(" ").append(cy).append(" Td (").append(escapePdf(price)).append(") Tj ET\n");

        // QR code box
        int qrX = x0 + w - 140, qrY = y0 - headerH - 30;
        stream.append("q 0.95 0.97 0.93 rg ").append(qrX).append(" ").append(qrY - 110).append(" 120 120 re f Q\n");
        stream.append("q 0.85 0.90 0.84 rg ").append(qrX).append(" ").append(qrY - 110).append(" 120 120 re S Q\n");
        stream.append("BT /F").append(fontObj).append(" 6 Tf 0.6 0.65 0.6 rg ").append(qrX + 5).append(" ").append(qrY - 30).append(" Td (QR: ").append(escapePdf(qrCode.length() > 18 ? qrCode.substring(0, 15) + "..." : qrCode)).append(") Tj ET\n");
        stream.append("BT /F").append(fontObj).append(" 7 Tf 0.6 0.65 0.6 rg ").append(qrX + 8).append(" ").append(qrY - 50).append(" Td (SCAN AT ENTRY) Tj ET\n");
        stream.append("BT /F").append(fontObj).append(" 7 Tf 0.6 0.65 0.6 rg ").append(qrX + 8).append(" ").append(qrY - 64).append(" Td (FOR VALIDATION) Tj ET\n");

        // Footer
        stream.append("q 0.9 0.93 0.88 rg ").append(x0).append(" ").append(y0 - h - 18).append(" ").append(w).append(" 18 re f Q\n");
        stream.append("BT /F").append(fontObj).append(" 7 Tf ").append(gray).append(" rg ").append(cx).append(" ").append(y0 - h - 10).append(" Td (Status: CONFIRMED  |  Generated by Tickify  |  tickify.sladedeploy.co.za) Tj ET\n");

        int contentObj = objNum;
        String streamStr = stream.toString();
        int length = streamStr.getBytes(StandardCharsets.ISO_8859_1).length;
        objects.add(contentObj + " 0 obj << /Length " + length + " >>\nstream\n" + streamStr + "endstream");
        objNum++;

        // Page object
        objects.set(pageObj - 1, pageObj + " 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 " + fontObj + " 0 R /F2 " + fontBoldObj + " 0 R >> >> /Contents " + contentObj + " 0 R >>");

        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.length());
            pdf.append(objects.get(i)).append("\n");
            pdf.append("endobj\n");
        }

        int xrefOffset = pdf.length();
        pdf.append("xref\n");
        pdf.append("0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int i = 1; i <= objects.size(); i++) {
            pdf.append(String.format(Locale.ENGLISH, "%010d 00000 n \n", offsets.get(i)));
        }
        pdf.append("trailer\n");
        pdf.append("<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n");
        pdf.append(xrefOffset).append("\n");
        pdf.append("%%EOF");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private String escapePdf(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "ticket.pdf";
        }
        return filename.replaceAll("[^A-Za-z0-9._-]", "-");
    }

    private void deleteDirectoryQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try {
            if (Files.exists(dir)) {
                Files.list(dir).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
                Files.deleteIfExists(dir);
            }
        } catch (IOException ignored) {
        }
    }

    private static final class MailAttachment {
        private final String filename;
        private final byte[] content;

        private MailAttachment(String filename, byte[] content) {
            this.filename = filename;
            this.content = content;
        }
    }

    private String defaultInlineLogoSvg() {
        return "<svg viewBox='0 0 400 100' xmlns='http://www.w3.org/2000/svg' width='400' height='100'>"
                + "<defs><filter id='tagShadow' x='-10%' y='-10%' width='130%' height='130%'>"
                + "<feDropShadow dx='0' dy='2' stdDeviation='4' flood-color='#C8CDD6' flood-opacity='0.4'/></filter></defs>"
                + "<path d='M 14,22 Q 14,14 22,14 L 70,14 L 92,50 L 70,86 L 22,86 Q 14,86 14,78 Z' fill='#ECEEF2' stroke='#D8DCE4' stroke-width='1.5' filter='url(#tagShadow)'/>"
                + "<circle cx='30' cy='50' r='5.5' fill='#FFFFFF' stroke='#D8DCE4' stroke-width='1.5'/>"
                + "<line x1='50' y1='14' x2='50' y2='86' stroke='#D8DCE4' stroke-width='1' stroke-dasharray='3,3'/>"
                + "<line x1='63' y1='35' x2='82' y2='35' stroke='#A9B3BF' stroke-width='3' stroke-linecap='round'/>"
                + "<line x1='72' y1='35' x2='72' y2='63' stroke='#A9B3BF' stroke-width='3' stroke-linecap='round'/>"
                + "<text x='108' y='63' font-family='Segoe UI, Trebuchet MS, Arial, sans-serif' font-size='40' font-weight='400' letter-spacing='-2' fill='#4A5568'>Tickify</text>"
                + "<circle cx='116' cy='76' r='3' fill='#B0BAC8'/></svg>";
    }

    private String required(String envKey, String sysKey) {
        String value = optional(envKey, sysKey, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required mail setting: " + envKey + " or -D" + sysKey
                    + ". Email service not configured. Set SMTP environment variables to enable email delivery.");
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
