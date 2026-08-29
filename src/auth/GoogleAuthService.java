package auth;

import java.awt.Desktop;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Core Google OAuth 2.0 service for the Installed App flow.
 *
 * Steps:
 *   1. Build the Google authorization URL
 *   2. Open the user's browser to the consent screen
 *   3. Start the local callback server (port 8765)
 *   4. Wait for the redirect with the auth code
 *   5. Exchange the code for an access token (HTTPS POST)
 *   6. Fetch user info (name, email, picture) with the token
 *   7. Return a UserSession
 *
 * All network calls use java.net.HttpURLConnection (no extra libraries).
 */
public class GoogleAuthService {

    /**
     * Runs the full OAuth flow on the calling thread.
     * Call this from a background thread — it blocks until auth completes or times out.
     *
     * @param statusCallback optional; receives status messages for the UI (e.g. spinner text)
     * @return a UserSession on success, or null on failure
     */
    public UserSession authenticate(Consumer<String> statusCallback) {
        try {
            // ── Step 1: Start callback server ──────────────────────────────
            notify(statusCallback, "Starting local auth server...");
            OAuthCallbackServer callbackServer = new OAuthCallbackServer();
            callbackServer.start();

            // ── Step 2: Open browser ───────────────────────────────────────
            String authUrl = buildAuthUrl();
            notify(statusCallback, "Opening browser for Google sign-in...");
            System.out.println("[Auth] Opening: " + authUrl);
            openBrowser(authUrl);

            // ── Step 3: Wait for code ──────────────────────────────────────
            notify(statusCallback, "Waiting for Google sign-in in browser...");
            String code = callbackServer.waitForCode();
            if (code == null) {
                notify(statusCallback, "Authentication timed out or was cancelled.");
                return null;
            }

            // ── Step 4: Exchange code for token ───────────────────────────
            notify(statusCallback, "Exchanging authorization code for token...");
            String tokenResponse = exchangeCodeForToken(code);
            if (tokenResponse == null) {
                notify(statusCallback, "Failed to obtain access token.");
                return null;
            }

            String accessToken = extractJsonField(tokenResponse, "access_token");
            if (accessToken == null) {
                notify(statusCallback, "Invalid token response from Google.");
                return null;
            }

            // ── Step 5: Fetch user info ────────────────────────────────────
            notify(statusCallback, "Fetching your Google profile...");
            String userInfoJson = fetchUserInfo(accessToken);
            if (userInfoJson == null) {
                notify(statusCallback, "Could not retrieve profile information.");
                return null;
            }

            String email      = extractJsonField(userInfoJson, "email");
            String name       = extractJsonField(userInfoJson, "name");
            String givenName  = extractJsonField(userInfoJson, "given_name");
            String pictureUrl = extractJsonField(userInfoJson, "picture");

            System.out.println("[Auth] Logged in as: " + name + " <" + email + ">");
            notify(statusCallback, "Welcome, " + (givenName != null ? givenName : name) + "!");

            return new UserSession(email, name, givenName, pictureUrl, accessToken);

        } catch (Exception e) {
            System.err.println("[Auth] Authentication error: " + e.getMessage());
            e.printStackTrace();
            notify(statusCallback, "Authentication error: " + e.getMessage());
            return null;
        }
    }

    // ── URL builder ───────────────────────────────────────────────────────────

    private String buildAuthUrl() {
        return GoogleAuthConfig.AUTH_URI
            + "?client_id="     + GoogleAuthConfig.CLIENT_ID
            + "&redirect_uri="  + GoogleAuthConfig.REDIRECT_URI
            + "&response_type=code"
            + "&scope="         + GoogleAuthConfig.SCOPES
            + "&access_type=offline"
            + "&prompt=consent";
    }

    // ── Browser ───────────────────────────────────────────────────────────────

    private void openBrowser(String url) throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        } else {
            // Fallback for systems where Desktop.browse is not supported
            new ProcessBuilder("cmd", "/c", "start", url).start();
        }
    }

    // ── Token exchange (POST to Google) ──────────────────────────────────────

    private String exchangeCodeForToken(String code) throws IOException {
        URL url = new URL(GoogleAuthConfig.TOKEN_URI);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(15_000);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "code="          + urlEncode(code)
            + "&client_id="           + urlEncode(GoogleAuthConfig.CLIENT_ID)
            + "&client_secret="       + urlEncode(GoogleAuthConfig.CLIENT_SECRET)
            + "&redirect_uri="        + urlEncode(GoogleAuthConfig.REDIRECT_URI)
            + "&grant_type=authorization_code";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();
        String response = readStream(is);
        conn.disconnect();

        if (status != 200) {
            System.err.println("[Auth] Token exchange failed (" + status + "): " + response);
            return null;
        }
        return response;
    }

    // ── User info fetch (GET from Google) ─────────────────────────────────────

    private String fetchUserInfo(String accessToken) throws IOException {
        URL url = new URL(GoogleAuthConfig.USERINFO_URI);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        int status = conn.getResponseCode();
        InputStream is = (status >= 400) ? conn.getErrorStream() : conn.getInputStream();
        String response = readStream(is);
        conn.disconnect();

        if (status != 200) {
            System.err.println("[Auth] User info fetch failed (" + status + "): " + response);
            return null;
        }
        return response;
    }

    // ── Minimal JSON field extractor ─────────────────────────────────────────

    /**
     * Extracts a simple string field from flat JSON without a library.
     * Works for the predictable Google API responses.
     * Example: extractJsonField({"name":"Alice","email":"a@b.com"}, "name") → "Alice"
     */
    static String extractJsonField(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx += search.length();
        // skip whitespace and colon
        while (idx < json.length() && (json.charAt(idx) == ' ' || json.charAt(idx) == ':')) idx++;
        if (idx >= json.length()) return null;

        char first = json.charAt(idx);
        if (first == '"') {
            // String value
            idx++;
            StringBuilder sb = new StringBuilder();
            while (idx < json.length()) {
                char c = json.charAt(idx++);
                if (c == '"') break;
                if (c == '\\' && idx < json.length()) {
                    char esc = json.charAt(idx++);
                    if (esc == 'n') sb.append('\n');
                    else if (esc == 't') sb.append('\t');
                    else sb.append(esc);
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            // Non-string value (number, bool)
            int end = idx;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
            return json.substring(idx, end).trim();
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    private void notify(Consumer<String> cb, String msg) {
        if (cb != null) cb.accept(msg);
    }
}
