package auth;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Google OAuth 2.0 configuration.
 *
 * Loads credentials dynamically from the local google_credentials.json file
 * or environment variables (GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET) at runtime,
 * keeping secrets out of version control.
 */
public final class GoogleAuthConfig {

    private GoogleAuthConfig() {}

    // ── OAuth Credentials loaded dynamically at runtime ───────────────────────
    public static final String CLIENT_ID;
    public static final String CLIENT_SECRET;

    static {
        String id = System.getenv("GOOGLE_CLIENT_ID");
        String secret = System.getenv("GOOGLE_CLIENT_SECRET");

        File credsFile = new File("google_credentials.json");
        if (credsFile.exists()) {
            try {
                String content = Files.readString(credsFile.toPath());
                String parsedId = extractJsonValue(content, "client_id");
                String parsedSecret = extractJsonValue(content, "client_secret");
                if (parsedId != null && !parsedId.isEmpty()) id = parsedId;
                if (parsedSecret != null && !parsedSecret.isEmpty()) secret = parsedSecret;
            } catch (Exception ignored) {}
        }

        CLIENT_ID = (id != null) ? id : "YOUR_GOOGLE_CLIENT_ID";
        CLIENT_SECRET = (secret != null) ? secret : "YOUR_GOOGLE_CLIENT_SECRET";
    }

    private static String extractJsonValue(String json, String key) {
        String regex = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        Matcher m = Pattern.compile(regex).matcher(json);
        return m.find() ? m.group(1) : null;
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────
    public static final String AUTH_URI    = "https://accounts.google.com/o/oauth2/auth";
    public static final String TOKEN_URI   = "https://oauth2.googleapis.com/token";
    public static final String USERINFO_URI= "https://www.googleapis.com/oauth2/v2/userinfo";

    // ── Local callback server ─────────────────────────────────────────────────
    public static final int    CALLBACK_PORT = 8765;
    public static final String REDIRECT_URI  = "http://localhost:" + CALLBACK_PORT + "/callback";

    // ── Scopes ────────────────────────────────────────────────────────────────
    public static final String SCOPES = "openid%20email%20profile";

    // ── Timeout ───────────────────────────────────────────────────────────────
    public static final int AUTH_TIMEOUT_SECONDS = 120;
}
