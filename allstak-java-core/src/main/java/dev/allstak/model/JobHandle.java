package dev.allstak.model;

/**
 * Opaque handle returned by {@code AllStak.startJob(slug)} to track job timing.
 */
public final class JobHandle {

    private final String slug;
    private final long startTimeMs;

    public JobHandle(String slug, long startTimeMs) {
        this.slug = slug;
        this.startTimeMs = startTimeMs;
    }

    public String getSlug() { return slug; }
    public long getStartTimeMs() { return startTimeMs; }
}
