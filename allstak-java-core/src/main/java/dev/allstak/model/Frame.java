package dev.allstak.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Phase 2 — structured stack frame matching the AllStak ingest v2
 * {@code ErrorIngestRequest.Frame} shape.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Frame {
    private final String filename;
    private final String absPath;
    private final String function;
    private final Integer lineno;
    private final Integer colno;
    private final Boolean inApp;
    private final String platform;
    private final String debugId;

    public Frame(String filename, String absPath, String function,
                 Integer lineno, Integer colno, Boolean inApp,
                 String platform, String debugId) {
        this.filename = filename; this.absPath = absPath; this.function = function;
        this.lineno = lineno; this.colno = colno; this.inApp = inApp;
        this.platform = platform; this.debugId = debugId;
    }

    public String getFilename() { return filename; }
    public String getAbsPath() { return absPath; }
    public String getFunction() { return function; }
    public Integer getLineno() { return lineno; }
    public Integer getColno() { return colno; }
    public Boolean getInApp() { return inApp; }
    public String getPlatform() { return platform; }
    public String getDebugId() { return debugId; }
}
