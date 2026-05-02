package za.ac.tut.payment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PayFastPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        String merchantId = resolve("TICKIFY_PAYFAST_MERCHANT_ID", "tickify.payfast.merchantId", null);
        String merchantKey = resolve("TICKIFY_PAYFAST_MERCHANT_KEY", "tickify.payfast.merchantKey", null);
        String passphrase = resolve("TICKIFY_PAYFAST_PASSPHRASE", "tickify.payfast.passphrase", "");

        if (merchantId == null || merchantKey == null) {
            return PaymentResult.failure("PayFast not configured. Set TICKIFY_PAYFAST_MERCHANT_ID and TICKIFY_PAYFAST_MERCHANT_KEY.");
        }

        String paymentId = "PAYFAST-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String notifyUrl = resolveAppBaseUrl() + "/PaymentGateway.do?action=payfastNotify";

        try {
            URL url = new URL("https://www.payfast.co.za/eng/process");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String payload = "merchant_id=" + urlEncode(merchantId)
                    + "&merchant_key=" + urlEncode(merchantKey)
                    + "&return_url=" + urlEncode(notifyUrl)
                    + "&cancel_url=" + urlEncode(notifyUrl)
                    + "&notify_url=" + urlEncode(notifyUrl)
                    + "&m_payment_id=" + urlEncode(paymentId)
                    + "&amount=" + urlEncode(String.format("%.2f", request.getAmount()))
                    + "&item_name=" + urlEncode("Tickify Ticket");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                return PaymentResult.success(paymentId, "PayFast payment processed");
            }

            StringBuilder errorBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorBody.append(line);
                }
            }
            return PaymentResult.failure("PayFast returned HTTP " + responseCode + ": " + errorBody);
        } catch (Exception e) {
            return PaymentResult.failure("PayFast payment failed: " + e.getMessage());
        }
    }

    @Override
    public String modeName() {
        return "payfast";
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value == null ? "" : value, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private String resolve(String envKey, String sysKey, String fallback) {
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

    private String resolveAppBaseUrl() {
        String env = System.getenv("TICKIFY_APP_BASE_URL");
        if (env != null && !env.trim().isEmpty()) {
            return stripTrailingSlash(env.trim());
        }
        String prop = System.getProperty("tickify.app.baseUrl");
        if (prop != null && !prop.trim().isEmpty()) {
            return stripTrailingSlash(prop.trim());
        }
        return "http://localhost:8080/Tickify-SWP-Web-App";
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
