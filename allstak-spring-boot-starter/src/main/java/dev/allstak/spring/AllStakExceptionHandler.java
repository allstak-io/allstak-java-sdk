package dev.allstak.spring;

import dev.allstak.AllStakClient;
import dev.allstak.internal.SdkLogger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler that automatically captures unhandled exceptions to AllStak.
 * Does NOT suppress the exception — it re-throws after capture so Spring's normal
 * error handling still applies.
 */
@ControllerAdvice
public class AllStakExceptionHandler {

    private final AllStakClient client;

    public AllStakExceptionHandler(AllStakClient client) {
        this.client = client;
    }

    @ExceptionHandler(Exception.class)
    public void handleException(Exception ex) throws Exception {
        try {
            client.captureException(ex);
        } catch (Exception captureError) {
            SdkLogger.debug("Failed to capture exception in global handler: {}", captureError.getMessage());
        }
        // Re-throw so Spring's default error handling still works
        throw ex;
    }
}
