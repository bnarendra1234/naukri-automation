package com.naukri.automation.browser;

import com.microsoft.playwright.*;
import com.naukri.automation.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class BrowserManager implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(BrowserManager.class);
    private final AppConfig config;
    private final Random random = new Random();

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    public BrowserManager(AppConfig config) {
        this.config = config;
    }

    public Page start() {
        log.info("Starting Playwright Chromium browser (Headless={})...", config.isHeadless());
        this.playwright = Playwright.create();

        List<String> launchArgs = List.of(
                "--disable-blink-features=AutomationControlled",
                "--start-maximized",
                "--no-sandbox",
                "--disable-setuid-sandbox",
                "--disable-infobars",
                "--window-position=0,0",
                "--ignore-certificate-errors",
                "--disable-extensions"
        );

        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(config.isHeadless())
                .setArgs(launchArgs)
                .setSlowMo(50));

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1366, 768)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .setLocale("en-IN")
                .setTimezoneId(config.getTimezone());

        Path sessionPath = Paths.get(config.getStorage().getSessionFile());
        if (Files.exists(sessionPath)) {
            log.info("Restoring browser session state from {}", sessionPath);
            contextOptions.setStorageStatePath(sessionPath);
        }

        this.context = browser.newContext(contextOptions);

        // Apply anti-detection stealth scripts
        this.context.addInitScript(
                "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });" +
                "Object.defineProperty(navigator, 'languages', { get: () => ['en-IN', 'en-US', 'en'] });" +
                "Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });"
        );

        this.page = context.newPage();
        this.page.setDefaultTimeout(30000);
        return this.page;
    }

    public Page getPage() {
        return this.page;
    }

    public void saveSession() {
        if (this.context != null) {
            try {
                Path sessionPath = Paths.get(config.getStorage().getSessionFile());
                if (sessionPath.getParent() != null) {
                    Files.createDirectories(sessionPath.getParent());
                }
                this.context.storageState(new BrowserContext.StorageStateOptions().setPath(sessionPath));
                log.info("Session state saved successfully to {}", sessionPath);
            } catch (Exception e) {
                log.warn("Failed to save session state: {}", e.getMessage());
            }
        }
    }

    public void humanDelay(double minSeconds, double maxSeconds) {
        try {
            double delaySeconds = minSeconds + (maxSeconds - minSeconds) * random.nextDouble();
            long millis = (long) (delaySeconds * 1000);
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public void humanDelay() {
        humanDelay(config.getApplication().getMinDelaySeconds(), config.getApplication().getMaxDelaySeconds());
    }

    public void captureScreenshot(String name) {
        if (this.page != null) {
            try {
                Path screenshotDir = Paths.get("data/debug_screenshots");
                Files.createDirectories(screenshotDir);
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Path path = screenshotDir.resolve(name + "_" + timestamp + ".png");
                this.page.screenshot(new Page.ScreenshotOptions().setPath(path));
                log.debug("Saved debug screenshot to {}", path);
            } catch (Exception e) {
                log.debug("Could not capture screenshot: {}", e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        try {
            if (this.context != null) {
                saveSession();
                this.context.close();
            }
            if (this.browser != null) {
                this.browser.close();
            }
            if (this.playwright != null) {
                this.playwright.close();
            }
            log.info("Browser closed cleanly.");
        } catch (Exception e) {
            log.warn("Error closing browser: {}", e.getMessage());
        }
    }
}
