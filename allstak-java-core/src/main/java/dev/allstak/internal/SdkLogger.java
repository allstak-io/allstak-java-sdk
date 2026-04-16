package dev.allstak.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal SDK logger. When debug mode is off, only warnings are emitted.
 * The SDK never pollutes the host application's log output in non-debug mode.
 */
public final class SdkLogger {

    private static final Logger logger = LoggerFactory.getLogger("dev.allstak.sdk");
    private static volatile boolean debugEnabled = false;

    private SdkLogger() {}

    public static void setDebug(boolean debug) {
        debugEnabled = debug;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void debug(String msg, Object... args) {
        if (debugEnabled) {
            logger.info("[AllStak SDK DEBUG] " + msg, args);
        }
    }

    public static void warn(String msg, Object... args) {
        logger.warn("[AllStak SDK] " + msg, args);
    }

    public static void error(String msg, Throwable t) {
        logger.error("[AllStak SDK] " + msg, t);
    }
}
