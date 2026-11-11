package com.naukri.automation.service;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.naukri.automation.browser.BrowserManager;
import com.naukri.automation.config.AppConfig;
import com.naukri.automation.model.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ApplierService {
    private static final Logger log = LoggerFactory.getLogger(ApplierService.class);
    private final BrowserManager browserManager;
    private final AppConfig config;
    private final TrackerService tracker;
    private final Page page;

    public ApplierService(BrowserManager browserManager, AppConfig config, TrackerService tracker) {
        this.browserManager = browserManager;
        this.config = config;
        this.tracker = tracker;
        this.page = browserManager.getPage();
    }

    public boolean shouldApply(JobListing job) {
        String jobId = job.getJobId();
        String title = job.getTitle() != null ? job.getTitle().toLowerCase() : "";
        String company = job.getCompany() != null ? job.getCompany().toLowerCase() : "";

        // 1. Check if already recorded in applied jobs history
        if (tracker.isApplied(jobId)) {
            log.debug("Skipping already applied job: {} ({})", job.getTitle(), jobId);
            return false;
        }

        // 2. Check excluded companies
        for (String excCompany : config.getFilters().getExcludeCompanies()) {
            if (company.contains(excCompany.toLowerCase())) {
                log.info("Skipping '{}' - Excluded company: {}", job.getTitle(), job.getCompany());
                return false;
            }
        }

        // 3. Check excluded title keywords
        for (String excKw : config.getFilters().getExcludeTitleKeywords()) {
            if (title.contains(excKw.toLowerCase())) {
                log.info("Skipping '{}' - Excluded keyword in title: {}", job.getTitle(), excKw);
                return false;
            }
        }

        return true;
    }

    public boolean applyToJob(JobListing job) {
        String jobUrl = job.getUrl();
        String jobId = job.getJobId();
        String title = job.getTitle();
        String company = job.getCompany();
        String location = job.getLocation();

        log.info("Processing job: '{}' at '{}'", title, company);

        try {
            page.navigate(jobUrl, new Page.NavigateOptions().setTimeout(30000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            browserManager.humanDelay(2, 4);

            // Check if page indicates already applied
            Locator appliedIndicator = page.locator("text='Already Applied', text='Applied', .already-applied, .applied");
            if (appliedIndicator.count() > 0 && appliedIndicator.first().isVisible()) {
                log.info("Already applied previously on portal for: {}", title);
                tracker.recordApplied(jobId, title, company, location, jobUrl);
                return false;
            }

            // Find Apply button
            List<String> applyBtnSelectors = List.of(
                    "button.apply-button",
                    "#apply-button",
                    "button:has-text('Apply')",
                    ".apply-btn",
                    "a:has-text('Apply on company website')",
                    "button:has-text('Apply on website')"
            );

            Locator applyButton = null;
            boolean isCompanySite = false;
            for (String sel : applyBtnSelectors) {
                Locator btn = page.locator(sel).first();
                if (btn.count() > 0 && btn.isVisible()) {
                    applyButton = btn;
                    String btnText = btn.innerText().toLowerCase();
                    if (btnText.contains("company") || btnText.contains("website")) {
                        isCompanySite = true;
                    }
                    break;
                }
            }

            if (applyButton == null) {
                log.warn("No Apply button found for: {}", title);
                return false;
            }

            if (isCompanySite && config.getFilters().isEasyApplyOnly()) {
                log.info("Skipping external company portal redirect for: {}", title);
                return false;
            }

            // Click Apply
            applyButton.click();
            browserManager.humanDelay(2, 3);

            // Handle standard Naukri questionnaire / chat modals
            handleApplicationModal();

            log.info("Successfully applied to: '{}' at '{}'", title, company);
            tracker.recordApplied(jobId, title, company, location, jobUrl);
            return true;

        } catch (Exception e) {
            log.error("Error applying to job '{}': {}", title, e.getMessage());
            browserManager.captureScreenshot("apply_error_" + jobId);
            return false;
        }
    }

    private void handleApplicationModal() {
        try {
            // 1. Submit application modal
            List<String> submitSelectors = List.of(
                    "button:has-text('Submit Application')",
                    "button:has-text('Apply')",
                    "button:has-text('Save and apply')",
                    ".bot-action-btn",
                    "button.submit-btn"
            );
            for (String sel : submitSelectors) {
                Locator subBtn = page.locator(sel).first();
                if (subBtn.count() > 0 && subBtn.isVisible()) {
                    subBtn.click();
                    Thread.sleep(1500);
                }
            }

            // 2. Dismiss close dialogs
            List<String> closeSelectors = List.of(
                    ".crossIcon",
                    "button.close",
                    ".drawer__close",
                    "span.close"
            );
            for (String cSel : closeSelectors) {
                Locator cBtn = page.locator(cSel).first();
                if (cBtn.count() > 0 && cBtn.isVisible()) {
                    cBtn.click();
                    break;
                }
            }
        } catch (Exception e) {
            log.debug("Modal handler notice: {}", e.getMessage());
        }
    }
}
