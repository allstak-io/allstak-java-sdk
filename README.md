# AllStak Java SDK

**Error tracking, logs, HTTP monitoring, database insights, and distributed tracing for Java and Spring Boot applications.**

One dependency. One API key. Every exception, request, query, and log line from your service shipped to AllStak in production — with zero boilerplate.

---

## What is this?

AllStak is an observability platform. This SDK is the Java client that sends your application's telemetry to it.

**The problem it solves.** Wiring up error tracking, request monitoring, DB query insights, structured logging, and cron monitoring across a Spring Boot service usually means bolting on four or five tools — Sentry for errors, OpenTelemetry for traces, Datadog for APM, a log shipper, a cron watchdog. Each one needs its own SDK, its own config, its own dashboard.

**What this gives you.** One dependency that captures all of it, correlates it by trace ID, and sends it to a single dashboard:

- Unhandled exceptions with full stack traces, cause chains, request context, and the user who hit them.
- Every inbound HTTP request the service handles.
- Every outbound HTTP call via `RestTemplate`.
- Every JDBC query with normalized SQL and timing.
- Every `WARN` / `ERROR` log line from Logback.
- Per-request trace spans linking them together.
- Cron / scheduled-job heartbeats (success and failure).

**Who should use it.** Backend engineers running Java services in production who want Sentry-class error tracking **plus** APM **plus** structured logs without integrating three vendors.

---

## Features

All of the features below are **truly automatic** after adding
`allstak-spring-boot-starter` and setting `allstak.api-key`. No wiring, no
manual helper calls, no custom `@Bean` methods required.

- **Automatic exception capture** from `@ControllerAdvice` — full stack traces, cause chains, request context, user claims.
- **Inbound HTTP monitoring** via a servlet filter — method, path, host, status, duration, headers (with sensitive headers redacted).
- **Outbound HTTP (RestTemplate) — automatically attached** to every `RestTemplate` bean: both the idiomatic `RestTemplateBuilder.build()` path (via a `RestTemplateCustomizer`) and the direct `new RestTemplate()` path (via a `BeanPostProcessor`). Captured on first call — no code changes required.
- **Outbound HTTP (WebClient) — automatically attached** when `spring-webflux` is on the classpath. An `ExchangeFilterFunction` is registered as a `WebClientCustomizer`, so every `WebClient` built via the injected `WebClient.Builder` is instrumented.
- **`@Scheduled` / cron auto-instrumentation** — every Spring bean method annotated with `@Scheduled` is wrapped by an AOP proxy that emits an AllStak heartbeat (`status=success` on return, `status=failed` on exception, with duration). No `startJob`/`finishJob` boilerplate required. Monitors are auto-created in the dashboard on first ping. You can still call `AllStak.startJob(...)` manually when you want a custom slug.
- **JDBC query capture** — a `BeanPostProcessor` wraps every `DataSource` bean (HikariCP included), normalizes SQL, records duration, status, row counts. Works with JPA/Hibernate, JdbcTemplate, Flyway, Liquibase — anything that goes through the wrapped `DataSource`.
- **Structured logs** — a Logback appender attached automatically. All `WARN` and above are shipped; `INFO` optional.
- **Distributed tracing** — one root span per inbound request. Errors, logs, and queries are linked to their trace.
- **Breadcrumbs** — recent log lines and HTTP entries are attached to the next captured error automatically.
- **Cron monitoring** — `AllStak.startJob(slug)` / `AllStak.finishJob(...)` with auto-created monitors on first ping.
- **User context** — set once per thread; attached to everything captured after.
- **Sensitive-data masking** — `password`, `token`, `secret`, `authorization`, `api_key`, `cookie`, etc. are replaced with `[MASKED]` automatically.
- **Never blocks your hot path** — bounded ring buffers, background flush threads, 3 s connect / 5 s request timeouts, retry with backoff.
- **Never crashes your app** — every SDK internal failure is swallowed and logged at debug level.

---

## Installation

### Spring Boot

**Maven**

```xml
<dependency>
    <groupId>dev.allstak</groupId>
    <artifactId>allstak-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'dev.allstak:allstak-spring-boot-starter:1.0.0'
```

### Plain Java (no Spring)

**Maven**

```xml
<dependency>
    <groupId>dev.allstak</groupId>
    <artifactId>allstak-java-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'dev.allstak:allstak-java-core:1.0.0'
```

**Requirements**: Java 17+. Spring Boot 3.x for the starter.

---

## Quick Start

### Spring Boot

Add the starter dependency, then add one property file:

```properties
# src/main/resources/application.properties
allstak.api-key=ask_live_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
allstak.environment=production
allstak.release=v1.0.0
allstak.service-name=checkout-api
```

Restart your app. **That's it.** No init code, no `@Configuration`, no `@Bean`. The starter auto-configures the exception handler, servlet filter, Logback appender, JDBC wrapper, and `RestTemplate` interceptor.

**First error in 60 seconds.** Boot your app and hit any endpoint that throws:

```bash
curl https://api.allstak.sa/api/any-route-that-errors
```

Then open your AllStak dashboard → **Errors**. You'll see the exception with the full stack trace, request context, and trace ID.

### Plain Java

```java
import dev.allstak.AllStak;
import dev.allstak.AllStakConfig;

public class Main {
    public static void main(String[] args) {
        AllStak.init(AllStakConfig.builder()
                .apiKey("ask_live_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                .environment("production")
                .release("v1.0.0")
                .serviceName("batch-worker")
                .build());

        try {
            doWork();
        } catch (Exception e) {
            AllStak.captureException(e);
        } finally {
            AllStak.shutdown(); // flush before JVM exit
        }
    }
}
```

Get your API key from: **AllStak dashboard → Project → Install SDK**.

---

## Basic Usage

### Error tracking

```java
import dev.allstak.AllStak;
import java.util.Map;

try {
    orderService.process(orderId);
} catch (Exception e) {
    // Minimal — one line, full stack + request context.
    AllStak.captureException(e);

    // With custom metadata (shown as tags on the error detail page).
    AllStak.captureException(e, Map.of(
        "orderId", orderId,
        "amount",  199.99,
        "stage",   "checkout"
    ));

    // With severity level — one of: debug, info, warn, error, fatal.
    AllStak.captureException(e, "fatal", Map.of("subsystem", "billing"));
}
```

> In Spring Boot, unhandled exceptions are captured automatically by the global handler. Manual `captureException` is for cases where you want to attach extra metadata before the exception bubbles up, or for exceptions you catch and handle.

### Logging

If you use SLF4J, you don't need to do anything — every `WARN` / `ERROR` log line your service emits is shipped automatically via the Logback appender.

To send a structured log line from SDK code directly:

```java
AllStak.captureLog("info", "User signed in");
AllStak.captureLog("warn", "Retrying payment", Map.of("attempt", 2));
AllStak.captureLog("error", "Payment failed",  Map.of(
    "orderId", orderId,
    "gateway", "stripe"
));
```

Valid levels: `debug`, `info`, `warn`, `error`, `fatal`. Use `warn`, **not** `warning`.

### User context

Attach the authenticated user to every subsequent event. Typical place: your auth filter or interceptor.

```java
import dev.allstak.model.UserContext;

AllStak.setUser(UserContext.of("user-42", "alice@example.com", "10.0.0.1"));

// On logout:
AllStak.clearUser();
```

Once set, errors, logs, and traces all carry `userId` + `email`, and the dashboard shows "1 user affected" / "Affected users" on every error.

### Breadcrumbs

Breadcrumbs are short entries attached to the *next* captured exception, so you can see what happened just before the crash.

```java
AllStak.addBreadcrumb("ui",   "User clicked Pay");
AllStak.addBreadcrumb("http", "POST /api/payments -> 502", "error",
        Map.of("statusCode", 502));
```

Recent log lines and HTTP requests are added automatically; you rarely need to add breadcrumbs manually.

### HTTP tracking

Inbound HTTP is captured automatically by the servlet filter. **Outbound HTTP is also fully automatic** for both `RestTemplate` and `WebClient` — no code changes required in your app.

**RestTemplate — works both ways out of the box:**

```java
// Path A: the idiomatic Spring Boot way (RestTemplateBuilder).
// A RestTemplateCustomizer bean attaches the AllStak interceptor automatically.
@Bean
RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
}

// Path B: direct construction. A BeanPostProcessor catches this too.
@Bean
RestTemplate rawRestTemplate() {
    return new RestTemplate();
}

// Every call through either template is captured — method, host, path, status, duration.
var response = restTemplate.getForObject("https://api.stripe.com/v1/customers", String.class);
```

**WebClient — automatic via `WebClient.Builder`:**

```java
@Bean
WebClient webClient(WebClient.Builder builder) {
    return builder.baseUrl("https://api.example.com").build();
}

// Captured automatically. Failures (timeouts, connection refused, etc.)
// are still reported with an errorFingerprint and the exception re-raised.
webClient.get().uri("/health").retrieve().bodyToMono(String.class).block();
```

> WebClient auto-instrumentation activates only when `spring-webflux` is on the application classpath. If you only use RestTemplate, the WebClient bits stay inactive and add zero overhead.

If you need to record an HTTP call from a non-`RestTemplate` / non-`WebClient` client (e.g. OkHttp or Apache HttpClient), call:

```java
import dev.allstak.model.HttpRequestItem;

AllStak.captureHttpRequest(HttpRequestItem.builder()
        .direction("outbound")
        .method("POST")
        .host("api.stripe.com")
        .path("/v1/charges")
        .statusCode(200)
        .durationMs(142)
        .build());
```

### Cron / scheduled jobs — fully automatic

Every Spring bean method annotated with `@Scheduled` is wrapped automatically. You don't need to write a single line of AllStak code:

```java
@Component
public class ReportTask {

    @Scheduled(cron = "0 0 2 * * *")
    public void generateDailyReport() {
        // ... your job. No AllStak calls needed.
        // The SDK automatically emits a heartbeat on return (success)
        // or on exception (failed, with the exception message).
    }
}
```

The slug is derived from the bean class + method name in lowercase-hyphenated form, e.g. `report-task-generate-daily-report`. The monitor is auto-created in the dashboard on the first ping.

Disable the auto-wrapper globally with `allstak.capture-scheduled=false`.

**Manual heartbeats** (for non-`@Scheduled` jobs, or when you want a custom slug):

```java
import dev.allstak.model.JobHandle;

JobHandle h = AllStak.startJob("daily-report");
try {
    runReport();
    AllStak.finishJob(h, "success", "42 rows processed");
} catch (Exception e) {
    AllStak.finishJob(h, "failed", e.getMessage());
    throw e;
}
```

Slug-based, one monitor per unique slug.

---

## Configuration

### Spring Boot (application.properties)

| Property | Default | Description |
|---|---|---|
| `allstak.api-key` | _required_ | Your `ask_live_...` key. |
| `allstak.environment` | `null` | `production`, `staging`, `dev`, etc. Shown on every event. |
| `allstak.release` | `null` | e.g. `v1.4.2` — shown on every error for release tracking. |
| `allstak.service-name` | `null` | Logical service name, shown on spans and logs. |
| `allstak.enabled` | `true` | Set `false` to fully disable the SDK (useful in tests). |
| `allstak.debug` | `false` | Verbose SDK logs to the application log. |
| `allstak.capture-exceptions` | `true` | Auto-capture unhandled exceptions. |
| `allstak.capture-http-requests` | `true` | Auto-capture inbound HTTP via the servlet filter. |
| `allstak.capture-db-queries` | `true` | Auto-capture JDBC queries via the DataSource wrapper. |
| `allstak.capture-logs` | `true` | Ship Logback WARN+ via the appender. |
| `allstak.capture-scheduled` | `true` | Auto-wrap every `@Scheduled` method with a cron heartbeat. |
| `allstak.flush-interval-ms` | `5000` | Background flush interval for buffered feeds. |
| `allstak.buffer-size` | `500` | Max items per channel buffer before oldest is dropped. |

Every property can also be set via environment variable: `ALLSTAK_API_KEY`, `ALLSTAK_ENVIRONMENT`, etc.

### Plain Java (AllStakConfig.Builder)

```java
AllStakConfig config = AllStakConfig.builder()
        .apiKey("ask_live_xxx")
        .environment("production")
        .release("v1.0.0")
        .serviceName("batch-worker")
        .flushIntervalMs(5_000)
        .bufferSize(500)
        .autoBreadcrumbs(true)
        .maxBreadcrumbs(50)
        .debug(false)
        .build();

AllStak.init(config);
```

> There is **no host / DSN** to configure. The ingest URL is baked into the SDK. You never have to think about it.

---

## Advanced Usage

### Attaching metadata and tags to errors

Anything in the metadata map becomes a searchable tag on the error detail page.

```java
AllStak.captureException(e, Map.of(
    "orderId",     "ORD-9921",
    "tenantId",    "acme-corp",
    "featureFlag", "new-checkout",
    "amount",      199.99
));
```

Prefer short, high-cardinality **identifiers** (order ID, tenant ID) and low-cardinality **tags** (environment, flag name). Both end up as `key=value` facets in the dashboard.

### Manual spans / tracing

Most apps don't need manual spans — the servlet filter creates a root span per request, and errors/logs/queries are linked to it automatically. If you want explicit spans around a hot block:

```java
// Errors and logs emitted inside this block will carry the same trace ID,
// which lets the dashboard correlate them on the trace detail page.
try (var span = AllStak.getClient().tracing().startSpan("checkout.process")) {
    span.setTag("order.id", orderId);
    orderService.process(orderId);
}
```

### Releases

Set `allstak.release` to your deploy version (git tag, semver, build number). The dashboard groups errors by release and shows "first seen in" / "last seen in" per release — so you know exactly which deploy introduced a regression.

```properties
allstak.release=v1.4.2
# or from an env var, set by your CI:
allstak.release=${GIT_SHA}
```

### Environments

```properties
# dev / staging / production / load-test / canary — your choice, consistent naming.
allstak.environment=${SPRING_PROFILES_ACTIVE}
```

The dashboard lets you filter every feature page by environment.

### Sampling

Not currently exposed as a config knob. All errors and cron heartbeats are sent immediately. HTTP, DB, and log feeds are bounded ring-buffered (default 500 items) with a tail-drop policy — under extreme load, the oldest items are discarded rather than blocking your request.

### Graceful shutdown

```java
// Flush buffers and stop the worker threads.
AllStak.shutdown();
```

The starter registers this automatically on Spring context shutdown. For plain Java apps, call it in a shutdown hook or `finally` block before JVM exit.

---

## Framework Support

### Spring Boot (first-class)

Drop in `allstak-spring-boot-starter` — everything auto-configures:

| What | Auto-wired by |
|---|---|
| Global exception handler | `AllStakExceptionHandler` (`@ControllerAdvice`) |
| Inbound HTTP capture | `AllStakServletFilter` (registered at `HIGHEST_PRECEDENCE+10`) |
| Outbound HTTP (`RestTemplate`) | `AllStakRestTemplateCustomizer` (picked up by `RestTemplateBuilder`) + `AllStakRestTemplatePostProcessor` (catches `new RestTemplate()` beans) |
| Outbound HTTP (`WebClient`) | `AllStakWebClientFilter` + `AllStakWebClientCustomizer` (picked up by `WebClient.Builder`) — activated only when `spring-webflux` is on the classpath |
| `@Scheduled` cron heartbeats | `AllStakScheduledPostProcessor` (CGLIB proxy around scheduled beans) |
| JDBC query capture | `AllStakDataSourcePostProcessor` — wraps any `DataSource` bean, works with HikariCP |
| Log capture | `AllStakLogbackAppender` — attached to the root logger on startup |
| Config binding | `@ConfigurationProperties("allstak")` → `AllStakProperties` |
| Graceful shutdown | `@PreDestroy` hook |

Spring Boot versions: **3.x**. Tested on 3.2.

### Plain Java / non-Spring JVM apps

Use `allstak-java-core` and call `AllStak.init(...)` once at startup. No auto-wiring — you capture exceptions and logs manually with the static API.

### Kotlin

The Java SDK works unchanged from Kotlin. There is no separate Kotlin artifact.

```kotlin
try {
    riskyWork()
} catch (e: Exception) {
    AllStak.captureException(e, mapOf("orderId" to orderId))
}
```

### Non-`RestTemplate` / non-`WebClient` HTTP clients (OkHttp, Apache HttpClient)

Outbound capture is automatic for **`RestTemplate`** and **`WebClient`** (when `spring-webflux` is on the classpath). For other clients (OkHttp, Apache HttpClient), call `AllStak.captureHttpRequest(HttpRequestItem.builder()...build())` from your own interceptor. Official integrations for those two clients are on the roadmap.

---

## Example Project

A complete working Spring Boot example lives in this repo under `allstak-sample-app/`.

```
allstak-java-sdk/
├── allstak-java-core/           # The core SDK — plain Java
├── allstak-spring-boot-starter/ # Spring Boot auto-config
└── allstak-sample-app/          # Full working Spring Boot app using the starter
```

Minimal controller using the starter:

```java
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final OrderService orderService;

    public CheckoutController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{orderId}")
    public Map<String, Object> checkout(@PathVariable String orderId) {
        AllStak.addBreadcrumb("ui", "Customer clicked checkout for " + orderId);
        try {
            var receipt = orderService.process(orderId);
            return Map.of("status", "ok", "receipt", receipt);
        } catch (Exception e) {
            // Already captured by the global handler. Manual call adds richer metadata.
            AllStak.captureException(e, Map.of(
                    "orderId", orderId,
                    "stage",   "checkout"
            ));
            throw e;
        }
    }
}
```

Nothing else is required. The starter handles exception capture, request timing, log shipping, SQL capture, and trace linking.

---

## Best Practices

**Never log sensitive data.** Don't pass raw passwords, tokens, or card numbers into `captureLog` / `captureException` metadata. The SDK auto-masks keys named `password` / `token` / `secret` / `authorization` / `api_key` / `cookie` / `x-api-key` / `x-auth-token` — but don't rely on it as your only defense. Treat metadata like logs: sanitize at the source.

**Use `environment` consistently across services.** Set it from `SPRING_PROFILES_ACTIVE` or an env var so that every service in a deployment writes to the same environment tag. It makes the dashboard's environment filter actually useful.

**Set `release` from your CI.** Use `git rev-parse --short HEAD` or a semver tag so the dashboard can show regressions per deploy. Without a release, "first seen in version" doesn't work.

**Attach a stable identifier in user context, not an email.** Emails rotate. Use your internal user ID as `id` and keep `email` as a secondary display field.

**Don't over-log INFO.** The default Logback level is `INFO`, which can flood the dashboard's Logs page. Set `logging.level.root=WARN` (or filter the appender) in production if you only care about warnings and errors.

**Use `captureException` sparingly in hot paths.** The exception send is synchronous and fast (~few ms) but not free. For extreme-throughput endpoints, prefer letting the global handler catch it once rather than catching-and-rethrowing everywhere.

**Shut down cleanly.** Spring Boot does this for you. For plain Java batch jobs, call `AllStak.shutdown()` before `System.exit(...)` so buffered events get flushed.

**Test against a stub.** In integration tests, override the `HttpTransport` bean with a WireMock-backed one so tests don't hit the real ingest host — see the troubleshooting section for an example.

---

## Troubleshooting

**Events aren't appearing in the dashboard.**

1. Is the SDK initialized? With `allstak.debug=true`, look for `AllStak SDK initialized — env=..., release=...` at startup.
2. Is the API key correct? A `401` from ingest causes the SDK to log `Invalid API key — disabling SDK` and stop sending.
3. Is the project picker set to the right project in the dashboard? Events show under the project that owns the API key.
4. Is the environment filter set correctly? The dashboard defaults to "All Envs" but filters stick across page loads.

**`401 INVALID_API_KEY`.** The key in `allstak.api-key` doesn't match a project. Copy it from the dashboard's "Install SDK" step (**Project → Settings → API Keys**).

**Wrong project receiving events.** API keys are project-scoped. If you copy-pasted from another project, events show up there. Confirm in **Settings → API Keys**.

**Dashboard is empty even though my logs say `202`.** The events were accepted — you're looking at the wrong project or the wrong environment filter. Switch the project picker, then click "All Envs" / "Last 24h".

**`Tomcat started on port X` shows as a log entry.** That's expected — the Logback appender catches every WARN/ERROR and, by default, INFO lines that go through SLF4J. Set `logging.level.root=WARN` to suppress them.

**I see recursive log shipping in the debug output.** Should never happen — the appender filters its own `dev.allstak.sdk` logger and any message starting with `[AllStak SDK`. If you see it, please file an issue with `allstak.debug=true` output.

**Network issues / connection refused.** The SDK retries 5 times with exponential backoff on 5xx and network errors. If the backend is unreachable the entire time, events are dropped silently — your app is unaffected. `allstak.debug=true` will log each retry.

**My integration test fails with connection refused to the ingest host.** Override the `HttpTransport` bean in a `@TestConfiguration` to point at your test backend (e.g. WireMock). The starter exposes `HttpTransport` as `@ConditionalOnMissingBean`, so your test bean takes precedence:

```java
@TestConfiguration
static class TestTransport {
    @Bean
    HttpTransport allStakHttpTransport() {
        return new HttpTransport("http://localhost:" + wireMockPort, "test-key");
    }
}
```

**Where's the `allstak.host` property?** There isn't one. The ingest URL is baked into `AllStakConfig.INGEST_HOST`. Change that constant only if you're running against a private / self-hosted AllStak installation.

**I set `allstak.enabled=false` but events are still being sent.** Restart the app — the flag is read at init, not per-request.

**Spring context shutdown takes too long.** The SDK has a 5 s drain deadline on shutdown. If you need it faster, set `allstak.flush-interval-ms` lower during development and call `AllStak.shutdown()` explicitly in a `@PreDestroy` on one of your own beans.

---

## Production behavior

- **Buffering.** Logs, HTTP requests, and DB queries batch in a per-channel ring buffer (default 500) and flush every 5 s or at 80 % capacity. Errors and cron heartbeats send immediately.
- **Retries.** 5 attempts with exponential backoff on 5xx and network errors. 4xx is dropped (it's a payload bug, not a transient failure). A `401` permanently disables the SDK in-process so a bad key never floods the backend.
- **Timeouts.** 3 s connect, 5 s request. The SDK never blocks your hot path for long.
- **Masking.** Keys named `password`, `token`, `secret`, `authorization`, `api_key`, `apikey`, `cookie`, `set-cookie`, `x-api-key`, `x-auth-token` are replaced with `[MASKED]` in metadata. Query-string secrets (`?token=`, `?apikey=`) are stripped from captured paths. Sensitive headers become `[REDACTED]` in HTTP capture.
- **Self-protection.** The Logback appender skips its own `dev.allstak.sdk` logger and any message starting with `[AllStak SDK` to prevent recursive log shipping.
- **Thread safety.** One global static client. `AllStak.init(...)` is idempotent. Worker threads are daemon threads so they never hold up JVM shutdown.

---

## License

Apache 2.0 — see [LICENSE](LICENSE).

---

## Links

- **Docs**: [https://allstak.dev/docs/sdks/java](https://allstak.dev/docs/sdks/java)
- **Dashboard**: [https://allstak.dev](https://allstak.dev)
- **Issues**: [https://github.com/allstak/allstak-java-sdk/issues](https://github.com/allstak/allstak-java-sdk/issues)
- **Changelog**: [CHANGELOG.md](CHANGELOG.md)
