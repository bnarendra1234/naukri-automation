package com.naukri.automation;

import com.naukri.automation.browser.BrowserManager;
import com.naukri.automation.config.AppConfig;
import com.naukri.automation.config.ConfigLoader;
import com.naukri.automation.model.AutomationStats;
import com.naukri.automation.model.JobListing;
import com.naukri.automation.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        boolean runScheduler = false;
        boolean runImmediately = true;

        for (String arg : args) {
            if ("--scheduler".equalsIgnoreCase(arg)) {
                runScheduler = true;
                runImmediately = false;
            } else if ("--run-now".equalsIgnoreCase(arg)) {
                runImmediately = true;
                runScheduler = true;
            } else if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                printHelp();
                return;
            }
        }

        if (runScheduler) {
            Scheduler scheduler = new Scheduler();
            scheduler.start(runImmediately);
        } else {
            runAutomation();
        }
    }

    public static void runAutomation() {
        System.out.println("=================================================================");
        System.out.println(" Starting Naukri Automation (Java) - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("=================================================================");

        AppConfig config = ConfigLoader.load();
        try {
            ConfigLoader.validate(config);
        } catch (IllegalArgumentException e) {
            log.error("Configuration validation error: {}", e.getMessage());
            System.err.println("\n[ERROR] " + e.getMessage());
            System.err.println("Please configure NAUKRI_EMAIL and NAUKRI_PASSWORD in .env or environment variables.\n");
            return;
        }

        TrackerService tracker = new TrackerService(config);
        int todayApplied = tracker.getTodayCount();
        if (todayApplied >= config.getMaxApplicationsPerRun()) {
            log.warn("Daily limit already reached today ({}/{} applications). Skipping run.", todayApplied, config.getMaxApplicationsPerRun());
            return;
        }

        AutomationStats stats = new AutomationStats();
        int applicationsLeft = config.getMaxApplicationsPerRun() - todayApplied;

        try (BrowserManager browserManager = new BrowserManager(config)) {
            browserManager.start();

            AuthService auth = new AuthService(browserManager, config);
            if (!auth.login()) {
                log.error("Authentication failed. Aborting daily run.");
                return;
            }

            // 1. Profile Freshness Booster
            if (config.isUpdateProfile() && config.getProfileUpdate().isEnabled()) {
                ProfileService profileService = new ProfileService(browserManager);
                stats.setProfileBoosted(profileService.boostProfile());
            }

            // 2. Job Search and Apply
            JobSearchService searcher = new JobSearchService(browserManager, config);
            ApplierService applier = new ApplierService(browserManager, config, tracker);

            List<String> keywords = config.getSearchCriteria().getKeywords();
            List<String> locations = config.getSearchCriteria().getLocations();
            if (locations.isEmpty()) {
                locations = List.of("");
            }

            for (String keyword : keywords) {
                if (applicationsLeft <= 0) {
                    log.info("Reached target daily application limit.");
                    break;
                }

                for (String location : locations) {
                    if (applicationsLeft <= 0) {
                        break;
                    }

                    int maxPages = config.getApplication().getMaxPagesPerKeyword();
                    for (int page = 1; page <= maxPages; page++) {
                        if (applicationsLeft <= 0) {
                            break;
                        }

                        List<JobListing> jobs = searcher.searchJobs(keyword, location, page);
                        if (jobs.isEmpty()) {
                            break;
                        }

                        stats.incrementScanned(jobs.size());

                        for (JobListing job : jobs) {
                            if (applicationsLeft <= 0) {
                                break;
                            }

                            if (applier.shouldApply(job)) {
                                stats.incrementEligible();
                                boolean applied = applier.applyToJob(job);
                                if (applied) {
                                    stats.incrementApplied();
                                    applicationsLeft--;
                                    browserManager.humanDelay();
                                } else {
                                    stats.incrementSkipped();
                                }
                            } else {
                                stats.incrementSkipped();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Fatal error during automation run: {}", e.getMessage(), e);
        }

        printSummary(stats, tracker);
    }

    private static void printSummary(AutomationStats stats, TrackerService tracker) {
        System.out.println("\n+-------------------------------------------------------------+");
        System.out.println("|                   DAILY RUN SUMMARY REPORT                  |");
        System.out.println("+------------------------------------+------------------------+");
        System.out.printf("| %-34s | %-22d |\n", "Jobs Scanned", stats.getScanned());
        System.out.printf("| %-34s | %-22d |\n", "Jobs Eligible", stats.getEligible());
        System.out.printf("| %-34s | %-22d |\n", "Jobs Applied Today", stats.getApplied());
        System.out.printf("| %-34s | %-22d |\n", "Jobs Skipped / Already Applied", stats.getSkipped());
        System.out.printf("| %-34s | %-22s |\n", "Profile Freshness Boosted", stats.isProfileBoosted() ? "Yes" : "No");
        System.out.printf("| %-34s | %-22d |\n", "Total History Applications", tracker.getTotalAppliedCount());
        System.out.println("+------------------------------------+------------------------+\n");
    }

    private static void printHelp() {
        System.out.println("Naukri Automation Bot (Java)");
        System.out.println("Usage:");
        System.out.println("  java -jar naukri-automation.jar              # Run once immediately");
        System.out.println("  java -jar naukri-automation.jar --scheduler  # Start daily 8:30 AM IST scheduler");
        System.out.println("  java -jar naukri-automation.jar --run-now    # Run immediately once, then schedule daily");
    }
}
