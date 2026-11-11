package com.naukri.automation.service;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.naukri.automation.browser.BrowserManager;
import com.naukri.automation.config.AppConfig;
import com.naukri.automation.model.JobListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JobSearchService {
    private static final Logger log = LoggerFactory.getLogger(JobSearchService.class);
    private final BrowserManager browserManager;
    private final AppConfig config;
    private final Page page;

    public JobSearchService(BrowserManager browserManager, AppConfig config) {
        this.browserManager = browserManager;
        this.config = config;
        this.page = browserManager.getPage();
    }

    public String buildSearchUrl(String keyword, String location, int pageNum) {
        String kwSlug = keyword.toLowerCase().trim()
                .replace(" ", "-")
                .replace(".", "")
                .replace("/", "-");

        String baseUrl = "https://www.naukri.com/" + kwSlug + "-jobs";
        if (location != null && !location.trim().isEmpty() && !location.equalsIgnoreCase("remote")) {
            String locSlug = location.toLowerCase().trim().replace(" ", "-");
            baseUrl = "https://www.naukri.com/" + kwSlug + "-jobs-in-" + locSlug;
        }

        if (pageNum > 1) {
            baseUrl = baseUrl + "-" + pageNum;
        }

        List<String> queryParams = new ArrayList<>();
        if (config.getSearchCriteria().getExperience() != null && config.getSearchCriteria().getExperience().getMin() != null) {
            queryParams.add("experience=" + config.getSearchCriteria().getExperience().getMin());
        }
        if (config.getSearchCriteria().getFreshnessDays() != null) {
            queryParams.add("jobAge=" + config.getSearchCriteria().getFreshnessDays());
        }
        if (location != null && location.equalsIgnoreCase("remote")) {
            queryParams.add("wfhType=0");
        }

        if (!queryParams.isEmpty()) {
            return baseUrl + "?" + String.join("&", queryParams);
        }
        return baseUrl;
    }

    public List<JobListing> searchJobs(String keyword, String location, int pageNum) {
        String url = buildSearchUrl(keyword, location, pageNum);
        log.info("Searching Naukri: {}", url);

        List<JobListing> jobs = new ArrayList<>();
        try {
            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            browserManager.humanDelay(2, 4);

            // Smooth scroll down to load lazy cards
            for (int i = 0; i < 3; i++) {
                page.mouse().wheel(0, 800);
                browserManager.humanDelay(0.5, 1.0);
            }

            Locator jobCards = page.locator(".srp-jobtuple-wrapper, .cust-job-tuple, .jobTuple, article.jobTuple");
            int count = jobCards.count();
            log.info("Found {} job listings on page {} for '{}'", count, pageNum, keyword);

            for (int i = 0; i < count; i++) {
                Locator card = jobCards.nth(i);
                try {
                    // Extract Title & Link
                    Locator titleElem = card.locator("a.title, .title a, a[class*='title']").first();
                    String title = titleElem.count() > 0 ? titleElem.innerText().trim() : "Unknown Title";
                    String href = titleElem.count() > 0 ? titleElem.getAttribute("href") : "";

                    // Extract Job ID
                    String jobId = card.getAttribute("data-job-id");
                    if ((jobId == null || jobId.trim().isEmpty()) && href != null) {
                        String cleanUrl = href.split("\\?")[0];
                        String[] parts = cleanUrl.split("-");
                        if (parts.length > 0 && parts[parts.length - 1].matches("\\d+")) {
                            jobId = parts[parts.length - 1];
                        } else {
                            jobId = cleanUrl.length() > 20 ? cleanUrl.substring(cleanUrl.length() - 20) : cleanUrl;
                        }
                    }

                    // Extract Company
                    Locator companyElem = card.locator("a.comp-name, .comp-name, a[class*='comp-name'], .companyName").first();
                    String company = companyElem.count() > 0 ? companyElem.innerText().trim() : "Unknown Company";

                    // Extract Location
                    Locator locElem = card.locator(".locWdth, .loc-wrap, span[class*='location'], .loc").first();
                    String loc = locElem.count() > 0 ? locElem.innerText().trim() : (location != null ? location : "");

                    // Check Easy Apply
                    boolean isEasyApply = card.locator("text='Easy Apply', text='APPLY', .apply-button").count() > 0;

                    String fullUrl = href != null && href.startsWith("http") ? href : "https://www.naukri.com" + href;

                    if (jobId != null && !jobId.trim().isEmpty() && !title.isEmpty()) {
                        jobs.add(new JobListing(jobId.trim(), title, company, loc, fullUrl, isEasyApply));
                    }
                } catch (Exception cardErr) {
                    log.debug("Failed to parse card #{}: {}", i, cardErr.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error searching jobs: {}", e.getMessage());
            browserManager.captureScreenshot("search_error");
        }

        return jobs;
    }
}
