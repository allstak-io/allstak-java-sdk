package dev.allstak.e2e;

import dev.allstak.AllStak;
import dev.allstak.AllStakConfig;
import dev.allstak.model.HttpRequestItem;
import dev.allstak.model.JobHandle;
import dev.allstak.model.UserContext;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Standalone end-to-end runner that exercises every public capture API of the
 * AllStak Java SDK against a real backend.
 *
 * <p>Usage: pass the API key as the first argument or set ALLSTAK_API_KEY.
 * The ingest host is fixed by the SDK ({@link AllStakConfig#INGEST_HOST}).
 *
 * <pre>
 *   java -cp allstak-java-core/target/allstak-java-core-1.0.0.jar:... \
 *        dev.allstak.e2e.E2eRunner ask_live_xxxxxxxx
 * </pre>
 */
public final class E2eRunner {

    private E2eRunner() {}

    public static void main(String[] args) throws Exception {
        String apiKey = args.length > 0 ? args[0] : System.getenv("ALLSTAK_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("ERROR: pass API key as argv[0] or set ALLSTAK_API_KEY");
            System.exit(2);
        }

        System.out.println("=== AllStak Java SDK E2E Runner ===");
        System.out.println("Ingest host (static): " + AllStakConfig.INGEST_HOST);
        System.out.println("API key suffix: ..." + apiKey.substring(Math.max(0, apiKey.length() - 6)));

        AllStak.init(AllStakConfig.builder()
                .apiKey(apiKey)
                .environment("e2e")
                .release("v1.0.0-java-e2e")
                .serviceName("java-sdk-e2e-runner")
                .debug(true)
                .flushIntervalMs(1000)
                .bufferSize(100)
                .build());

        AllStak.setUser(UserContext.of("user-e2e-001", "e2e@allstak.test", "10.0.0.42"));

        try {
            section("1. Simple captured exception");
            try {
                throw new IllegalStateException("Simple captured exception from E2E runner");
            } catch (Exception e) {
                AllStak.captureException(e);
            }

            section("2. Nested exception with cause chain");
            try {
                try {
                    throw new java.io.IOException("disk full");
                } catch (java.io.IOException ioe) {
                    throw new RuntimeException("Failed to persist order", ioe);
                }
            } catch (Exception e) {
                AllStak.captureException(e, Map.of(
                        "orderId", "ORD-9921",
                        "amount", 199.99,
                        "currency", "USD"
                ));
            }

            section("3. Manual message capture (info / warn / error)");
            AllStak.captureLog("info", "E2E runner: starting checkout flow",
                    Map.of("flow", "checkout", "step", "begin"));
            AllStak.captureLog("warn", "E2E runner: payment retry triggered",
                    Map.of("attempt", 2, "gateway", "stripe"));
            AllStak.captureLog("error", "E2E runner: payment gateway timeout",
                    Map.of("gateway", "stripe", "timeoutMs", 30000));

            section("4. Custom tags / extra context / metadata");
            try {
                throw new ArithmeticException("Division by zero in tax calculator");
            } catch (Exception e) {
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("module", "tax-calculator");
                meta.put("severity", "high");
                meta.put("region", "EU");
                meta.put("featureFlag", "tax_v2_enabled");
                AllStak.captureException(e, meta);
            }

            section("5. High severity (fatal) event");
            try {
                throw new OutOfMemoryError("Simulated OOM in image processor");
            } catch (Throwable t) {
                AllStak.captureException(t, "fatal", Map.of("subsystem", "image-processor"));
            }

            section("6. Special characters & unicode in metadata");
            try {
                throw new RuntimeException("\u062E\u0637\u0623 \u062D\u0633\u0627\u0628\u064A — emoji \uD83D\uDD25 — line\nbreak");
            } catch (Exception e) {
                AllStak.captureException(e, Map.of(
                        "arabic", "\u0645\u0631\u062D\u0628\u0627",
                        "emoji", "\u2705 \u26A0\uFE0F \uD83D\uDC80",
                        "newlines", "line1\nline2\nline3"
                ));
            }

            section("7. Null / empty optional fields");
            try {
                throw new NullPointerException(null);
            } catch (Exception e) {
                AllStak.captureException(e, null);
            }
            AllStak.captureLog("info", "log with null metadata", null);

            section("8. Breadcrumbs leading up to an error");
            AllStak.addBreadcrumb("navigation", "User opened /checkout");
            AllStak.addBreadcrumb("ui", "User clicked PayNow", "info",
                    Map.of("method", "card", "saved", true));
            AllStak.addBreadcrumb("http", "POST /api/payments -> 502", "error",
                    Map.of("statusCode", 502, "durationMs", 1450));
            try {
                throw new IllegalStateException("Payment provider returned 502");
            } catch (Exception e) {
                AllStak.captureException(e, Map.of("provider", "stripe"));
            }

            section("9. HTTP request capture (outbound + inbound)");
            AllStak.captureHttpRequest(HttpRequestItem.builder()
                    .traceId(UUID.randomUUID().toString())
                    .direction("outbound")
                    .method("POST")
                    .host("api.stripe.com")
                    .path("/v1/charges")
                    .statusCode(200)
                    .durationMs(187)
                    .requestSize(512)
                    .responseSize(2048)
                    .timestamp(Instant.now().toString())
                    .environment("e2e")
                    .release("v1.0.0-java-e2e")
                    .build());
            AllStak.captureHttpRequest(HttpRequestItem.builder()
                    .traceId(UUID.randomUUID().toString())
                    .direction("inbound")
                    .method("GET")
                    .host("orders.example.com")
                    .path("/v1/orders/9921")
                    .statusCode(404)
                    .durationMs(12)
                    .requestSize(0)
                    .responseSize(56)
                    .timestamp(Instant.now().toString())
                    .environment("e2e")
                    .release("v1.0.0-java-e2e")
                    .build());

            section("10. Cron heartbeat success + failure");
            JobHandle ok = AllStak.startJob("java-e2e-success");
            Thread.sleep(40);
            AllStak.finishJob(ok, "success", "Processed 7 items");

            JobHandle fail = AllStak.startJob("java-e2e-failed");
            Thread.sleep(20);
            AllStak.finishJob(fail, "failed", "DB connection refused");

            section("11. Multiple events in sequence");
            for (int i = 0; i < 5; i++) {
                AllStak.captureLog("info", "burst log #" + i,
                        Map.of("iteration", i, "burst", true));
            }
            for (int i = 0; i < 3; i++) {
                try {
                    throw new RuntimeException("Burst error #" + i);
                } catch (Exception e) {
                    AllStak.captureException(e, Map.of("iteration", i, "burst", true));
                }
            }

            section("Flushing and waiting for delivery...");
            AllStak.flush();
            Thread.sleep(3000); // give buffered events time to drain

        } finally {
            AllStak.shutdown();
        }

        System.out.println("\n=== E2E Runner Done ===");
    }

    private static void section(String name) {
        System.out.println();
        System.out.println("--- " + name + " ---");
    }
}
