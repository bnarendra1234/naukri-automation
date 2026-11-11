package com.naukri.automation.service;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.naukri.automation.browser.BrowserManager;
import com.naukri.automation.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final BrowserManager browserManager;
    private final AppConfig config;
    private final Page page;

    public AuthService(BrowserManager browserManager, AppConfig config) {
        this.browserManager = browserManager;
        this.config = config;
        this.page = browserManager.getPage();
    }

    public boolean isLoggedIn() {
        try {
            log.info("Checking existing session state...");
            page.navigate("https://www.naukri.com/mnjuser/profile", new Page.NavigateOptions().setTimeout(25000));
            browserManager.humanDelay(2, 3);

            String currentUrl = page.url();
            if (!currentUrl.contains("nlogin") && (currentUrl.contains("mnjuser") || currentUrl.contains("profile") || currentUrl.contains("mynaukri"))) {
                Locator userIndicator = page.locator(".user-name, .nI-gNb-drawer__user-name, .profile-name, .nI-gNb-header__logo");
                if (userIndicator.count() > 0) {
                    log.info("Already logged in via saved session cookies.");
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Session check error: {}", e.getMessage());
        }
        return false;
    }

    public boolean login() {
        if (isLoggedIn()) {
            return true;
        }

        log.info("Initiating login at https://www.naukri.com/nlogin/login ...");
        try {
            page.navigate("https://www.naukri.com/nlogin/login", new Page.NavigateOptions().setTimeout(30000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            browserManager.humanDelay(2, 3);

            // Find email input
            List<String> emailSelectors = List.of("#usernameField", "input[placeholder*='Username']", "input[placeholder*='Email']", "input[type='text']");
            Locator emailElem = null;
            for (String sel : emailSelectors) {
                Locator loc = page.locator(sel).first();
                if (loc.isVisible()) {
                    emailElem = loc;
                    break;
                }
            }

            if (emailElem == null) {
                log.error("Could not find email/username input field on login page.");
                browserManager.captureScreenshot("login_failed_no_email_field");
                return false;
            }

            emailElem.click();
            emailElem.fill("");
            emailElem.pressSequentially(config.getEmail(), new Locator.PressSequentiallyOptions().setDelay(60));
            browserManager.humanDelay(1, 2);

            // Find password input
            List<String> pwdSelectors = List.of("#passwordField", "input[type='password']", "input[placeholder*='Password']");
            Locator pwdElem = null;
            for (String sel : pwdSelectors) {
                Locator loc = page.locator(sel).first();
                if (loc.isVisible()) {
                    pwdElem = loc;
                    break;
                }
            }

            if (pwdElem == null) {
                log.error("Could not find password input field on login page.");
                browserManager.captureScreenshot("login_failed_no_password_field");
                return false;
            }

            pwdElem.click();
            pwdElem.fill("");
            pwdElem.pressSequentially(config.getPassword(), new Locator.PressSequentiallyOptions().setDelay(60));
            browserManager.humanDelay(1, 2);

            // Click submit button
            Locator submitBtn = page.locator("button[type='submit'], .btn-primary:has-text('Login'), button:has-text('Login')").first();
            submitBtn.click();
            log.info("Submitted login credentials. Waiting for authentication...");

            // Wait for redirect or OTP prompt
            for (int i = 0; i < 15; i++) {
                Thread.sleep(2000);
                String currentUrl = page.url();
                if (currentUrl.contains("mnjuser") || currentUrl.contains("homepage") || !currentUrl.contains("nlogin")) {
                    log.info("Login successful! Saving session state...");
                    browserManager.saveSession();
                    return true;
                }

                // Check for OTP prompt
                Locator otpInput = page.locator("input[placeholder*='OTP'], input[name*='otp'], .otp-input");
                if (otpInput.count() > 0 && otpInput.first().isVisible()) {
                    log.warn("OTP verification prompt detected on Naukri portal!");
                    if (!config.isHeadless()) {
                        log.info("Please enter the OTP manually in the browser window within 60 seconds...");
                        page.waitForTimeout(60000);
                        if (!page.url().contains("nlogin")) {
                            log.info("OTP verified successfully!");
                            browserManager.saveSession();
                            return true;
                        }
                    } else {
                        log.error("OTP challenge received in headless mode. Please run once with HEADLESS=false to authenticate.");
                        return false;
                    }
                }
            }

            if (!page.url().contains("nlogin")) {
                log.info("Login completed successfully.");
                browserManager.saveSession();
                return true;
            } else {
                log.error("Login did not redirect. Possible invalid credentials, captcha, or IP rate limit.");
                browserManager.captureScreenshot("login_redirect_failed");
                return false;
            }

        } catch (Exception e) {
            log.error("Login encountered an exception: {}", e.getMessage(), e);
            browserManager.captureScreenshot("login_exception");
            return false;
        }
    }
}
