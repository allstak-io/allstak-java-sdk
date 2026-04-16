package dev.allstak.sample.scheduled;

import dev.allstak.AllStak;
import dev.allstak.model.JobHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Scheduled task that demonstrates cron job monitoring with AllStak SDK.
 */
@Component
public class ReportTask {

    private static final Logger log = LoggerFactory.getLogger(ReportTask.class);

    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void generateDailyReport() {
        JobHandle handle = AllStak.startJob("daily-report-generator");
        try {
            log.info("Starting daily report generation...");
            // Simulate work
            Thread.sleep(500);
            AllStak.captureLog("info", "Daily report generated", Map.of("records", 1842));
            AllStak.finishJob(handle, "success", "Processed 1842 records");
            log.info("Daily report generation completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            AllStak.finishJob(handle, "failed", "Interrupted");
        } catch (Exception e) {
            AllStak.finishJob(handle, "failed", e.getMessage());
            log.error("Daily report generation failed", e);
        }
    }
}
