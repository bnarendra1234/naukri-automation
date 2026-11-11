package com.naukri.automation.service;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.naukri.automation.browser.BrowserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProfileService {
    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    private final BrowserManager browserManager;
    private final Page page;

    public ProfileService(BrowserManager browserManager) {
        this.browserManager = browserManager;
        this.page = browserManager.getPage();
    }

    public boolean boostProfile() {
        log.info("Starting daily profile freshness booster...");
        try {
            page.navigate("https://www.naukri.com/mnjuser/profile", new Page.NavigateOptions().setTimeout(30000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            browserManager.humanDelay(2, 4);

            List<String> headlineEditSelectors = List.of(
                    ".resumeHeadline .edit",
                    "span:has-text('Resume Headline') ~ .edit",
                    ".widgetHead:has-text('Resume Headline') .edit",
                    "xpath=//span[contains(text(), 'Resume Headline')]/following-sibling::span[contains(@class, 'edit')]",
                    ".resume-headline-section .edit",
                    ".widget:has-text('Resume headline') .edit"
            );

            Locator editBtn = null;
            for (String sel : headlineEditSelectors) {
                Locator loc = page.locator(sel).first();
                if (loc.count() > 0 && loc.isVisible()) {
                    editBtn = loc;
                    break;
                }
            }

            if (editBtn != null && editBtn.isVisible()) {
                editBtn.click();
                browserManager.humanDelay(1, 2);

                Locator textarea = page.locator("textarea#resumeHeadlineTxt, .resumeHeadlineTxt textarea, textarea").first();
                if (textarea.isVisible()) {
                    String currentText = textarea.inputValue().trim();

                    // Toggle trailing period to register a change
                    String newText;
                    if (currentText.endsWith(".")) {
                        newText = currentText.substring(0, currentText.length() - 1);
                    } else {
                        newText = currentText + ".";
                    }

                    textarea.fill(newText);
                    browserManager.humanDelay(1, 2);

                    Locator saveBtn = page.locator("button:has-text('Save'), .saveBtn, button[type='submit']:has-text('Save')").first();
                    if (saveBtn.isVisible()) {
                        saveBtn.click();
                        browserManager.humanDelay(2, 3);
                        log.info("Profile freshness successfully updated! Profile tagged as 'Updated Today' for recruiters.");
                        return true;
                    }
                }
            }

            log.warn("Could not find resume headline edit button. Skipping profile boost.");
            return false;

        } catch (Exception e) {
            log.warn("Profile booster skipped due to notice: {}", e.getMessage());
            return false;
        }
    }
}
