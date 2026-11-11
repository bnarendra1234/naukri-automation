package com.naukri.automation;

import com.naukri.automation.config.AppConfig;
import com.naukri.automation.config.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void start(boolean runImmediatelyFirst) {
        AppConfig config = ConfigLoader.load();
        String timeStr = config.getScheduleTime();
        String tzStr = config.getTimezone();

        LocalTime scheduledTime;
        try {
            scheduledTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            log.warn("Invalid SCHEDULE_TIME format '{}'. Defaulting to 08:30.", timeStr);
            scheduledTime = LocalTime.of(8, 30);
        }

        ZoneId zoneId = ZoneId.of(tzStr);

        log.info("=================================================================");
        log.info(" Naukri Daily Automation Scheduler Initialized");
        log.info(" Target Daily Time: {} ({})", scheduledTime, zoneId);
        log.info("=================================================================");

        if (runImmediatelyFirst) {
            log.info("Executing initial immediate run as requested...");
            try {
                Main.runAutomation();
            } catch (Exception e) {
                log.error("Error during initial run: {}", e.getMessage());
            }
        }

        scheduleNextDailyRun(scheduledTime, zoneId);

        // Keep process running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down scheduler service...");
            executor.shutdown();
        }));
    }

    private void scheduleNextDailyRun(LocalTime scheduledTime, ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime nextRun = now.with(scheduledTime).withSecond(0).withNano(0);

        if (now.compareTo(nextRun) >= 0) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelaySeconds = Duration.between(now, nextRun).getSeconds();
        log.info("Next automated run scheduled at: {} (in ~{} minutes)",
                nextRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")),
                initialDelaySeconds / 60);

        executor.schedule(() -> {
            try {
                Main.runAutomation();
            } catch (Exception e) {
                log.error("Scheduled execution encountered an error: {}", e.getMessage(), e);
            } finally {
                // Schedule next occurrence
                scheduleNextDailyRun(scheduledTime, zoneId);
            }
        }, initialDelaySeconds, TimeUnit.SECONDS);
    }
}
