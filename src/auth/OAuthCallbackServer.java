package auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lightweight local HTTP server that captures Google's OAuth redirect.
 *
 * Flow:
 *   1. Google redirects the browser to http://localhost:8765/callback?code=XXXX
 *   2. This server captures the code and sends a styled HTML success page
 *   3. waitForCode() returns the code (or null on timeout/error)
 */
public class OAuthCallbackServer {

    private HttpServer server;
    private final AtomicReference<String> authCode  = new AtomicReference<>();
    private final AtomicReference<String> authError = new AtomicReference<>();
    private final CountDownLatch latch = new CountDownLatch(1);

    /** Starts the local server on the configured port. */
    public void start() throws IOException {
        server = HttpServer.create(
            new InetSocketAddress("localhost", GoogleAuthConfig.CALLBACK_PORT), 0);
        server.createContext("/callback", new CallbackHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("[Auth] Callback server started on port " + GoogleAuthConfig.CALLBACK_PORT);
    }

    /**
     * Blocks until the auth code arrives or timeout expires.
     *
     * @return the authorization code, or null on timeout/error
     */
    public String waitForCode() {
        try {
            boolean arrived = latch.await(GoogleAuthConfig.AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!arrived) {
                System.err.println("[Auth] Timeout waiting for OAuth callback.");
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            stop();
        }
        String err = authError.get();
        if (err != null) {
            System.err.println("[Auth] OAuth error: " + err);
            return null;
        }
        return authCode.get();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    // ── Inner handler ─────────────────────────────────────────────────────────

    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI requestUri = exchange.getRequestURI();
            String query   = requestUri.getQuery(); // e.g. "code=XXXX&scope=..."

            String code  = extractParam(query, "code");
            String error = extractParam(query, "error");

            if (error != null) {
                authError.set(error);
                sendPage(exchange, buildErrorPage(error));
            } else if (code != null) {
                authCode.set(code);
                sendPage(exchange, buildSuccessPage());
            } else {
                authError.set("No code received");
                sendPage(exchange, buildErrorPage("No authorization code received."));
            }

            latch.countDown();
        }

        private void sendPage(HttpExchange exchange, String html) throws IOException {
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String extractParam(String query, String key) {
            if (query == null) return null;
            for (String pair : query.split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2 && parts[0].equals(key)) {
                    return parts[1];
                }
            }
            return null;
        }

        private String buildSuccessPage() {
            return "<!DOCTYPE html><html><head><meta charset='utf-8'>"
                + "<title>Factory Auto Chain — Authenticated</title>"
                + "<style>"
                + "body{margin:0;display:flex;align-items:center;justify-content:center;"
                + "height:100vh;background:#0d1118;font-family:'Segoe UI',sans-serif;color:#c8d2e6}"
                + ".card{background:#161e34;border:1px solid #2d3d60;border-radius:16px;"
                + "padding:48px 64px;text-align:center;max-width:420px}"
                + ".icon{font-size:64px;margin-bottom:16px}"
                + "h1{color:#f0aa28;font-size:24px;margin:0 0 12px}"
                + "p{color:#8090b0;font-size:15px;margin:0}"
                + ".pill{display:inline-block;margin-top:20px;padding:8px 20px;"
                + "background:#1a3a1a;border:1px solid #3cc878;border-radius:20px;"
                + "color:#3cc878;font-size:13px}"
                + "</style></head><body>"
                + "<div class='card'>"
                + "<div class='icon'>✅</div>"
                + "<h1>Authentication Successful!</h1>"
                + "<p>You can now return to the game.<br>This browser tab can be closed.</p>"
                + "<div class='pill'>⚙ Factory Auto Chain is ready</div>"
                + "</div></body></html>";
        }

        private String buildErrorPage(String err) {
            return "<!DOCTYPE html><html><head><meta charset='utf-8'>"
                + "<title>Auth Error</title>"
                + "<style>"
                + "body{margin:0;display:flex;align-items:center;justify-content:center;"
                + "height:100vh;background:#0d1118;font-family:'Segoe UI',sans-serif}"
                + ".card{background:#1e1010;border:1px solid #7a2020;border-radius:16px;"
                + "padding:48px 64px;text-align:center;max-width:420px;color:#e0b0b0}"
                + "h1{color:#e05050;font-size:22px}"
                + "p{color:#90706060;font-size:13px}"
                + "</style></head><body>"
                + "<div class='card'>"
                + "<div style='font-size:56px'>❌</div>"
                + "<h1>Authentication Failed</h1>"
                + "<p>Error: " + err + "</p>"
                + "<p>Close this tab and try again in the game.</p>"
                + "</div></body></html>";
        }
    }
}
