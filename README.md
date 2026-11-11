# Naukri Automation - Daily Job Applier & Profile Booster (Java)

An enterprise-grade, automated job application bot for **Naukri.com** built in **Java 17+ and Playwright** with automated scheduling every day at **8:30 AM IST (Indian Standard Time)**.

---

## 🚀 Key Features

1. **Daily 8:30 AM IST Automated Run**:
   - Built-in Java Scheduler (`Scheduler.java` via `java -jar target/naukri-automation.jar --scheduler`)
   - One-click Windows Task Scheduler registration script (`setup_windows_task.ps1`)
   - Cloud GitHub Actions cron workflow (`03:00 UTC` = `08:30 AM IST`)
2. **Profile Freshness Booster**:
   - Automatically touches your Resume Headline daily so your profile gets tagged as **"Updated Today"**, boosting recruiter search rankings by 3–5x.
3. **Smart Job Search & Filtering**:
   - Multi-keyword search (e.g., *Full Stack Developer*, *React Developer*, *Software Engineer*).
   - Experience range filter (min/max years).
   - Location & Remote/WFH filters.
   - Job age freshness filter (jobs posted in the last 24 hours).
   - Direct 1-Click **Easy Apply** on Naukri (avoids external redirect dead-ends).
4. **Duplicate Application Prevention**:
   - Records applied jobs in `data/applied_jobs.json` so you never apply to the same job twice.
5. **Human-like Anti-Detection**:
   - Stealth browser automation with random mouse delays, realistic user agents, and session persistence to avoid repeated OTPs.
6. **Safety Caps**:
   - Configurable daily limit (default: 25 jobs/day) to keep your account healthy.

---

## 📁 Project Structure

```text
naukri-automation/
├── pom.xml                                  # Maven dependencies & shaded fat JAR build
├── .env.example                             # Template for credentials and settings
├── config.yaml                              # Search keywords, locations, exclusions
├── run_daily.bat                            # Windows batch runner
├── setup_windows_task.ps1                   # Windows Task Scheduler auto-installer
├── .github/
│   └── workflows/
│       └── daily_naukri.yml                # Free Cloud Runner via GitHub Actions
├── data/                                    # Stores session state and applied jobs tracker
├── logs/                                    # Rotating log files
└── src/
    └── main/
        ├── resources/
        │   └── logback.xml                  # Logback colorized logging configuration
        └── java/com/naukri/automation/
            ├── Main.java                    # Entry point & CLI runner
            ├── Scheduler.java               # Continuous daily scheduler (8:30 AM IST)
            ├── config/
            │   ├── AppConfig.java           # Configuration data model
            │   └── ConfigLoader.java        # YAML & .env config loader
            ├── browser/
            │   └── BrowserManager.java      # Playwright Chromium manager with stealth
            ├── service/
            │   ├── AuthService.java         # Naukri authentication & session manager
            │   ├── ProfileService.java      # Profile headline touch / freshness booster
            │   ├── JobSearchService.java    # Job query builder & listing extractor
            │   ├── ApplierService.java      # Application logic & easy-apply modal handler
            │   └── TrackerService.java      # Persistent applied jobs JSON tracker
            └── model/
                ├── JobListing.java          # Job listing model
                ├── AppliedJobInfo.java      # Job history model
                └── AutomationStats.java     # Execution summary metrics
```

---

## 🛠️ Quick Setup & Installation

### Step 1: Prerequisites
- **Java JDK 17 or higher** (e.g. Eclipse Adoptium Temurin 17/18/21).
- **Apache Maven 3.8+** (included or installed).

### Step 2: Build the Application

Open PowerShell in the project directory:

```powershell
mvn clean package -DskipTests
```

This compiles all classes and produces the single executable fat JAR at `target/naukri-automation.jar`.

### Step 3: Configure Credentials (`.env`)

Copy `.env.example` to `.env`:

```powershell
Copy-Item .env.example .env
```

Edit `.env` with your Naukri login details:

```env
NAUKRI_EMAIL=your_email@example.com
NAUKRI_PASSWORD=your_naukri_password
HEADLESS=false
UPDATE_PROFILE=true
MAX_APPLICATIONS_PER_RUN=25
SCHEDULE_TIME=08:30
TIMEZONE=Asia/Kolkata
```

> **Tip for First Run:** Keep `HEADLESS=false` on your first run. If Naukri prompts for an OTP or captcha, complete it once in the browser window. The session cookies will be saved in `data/session_state.json` for subsequent headless runs.

### Step 4: Customize Search Criteria (`config.yaml`)

Edit `config.yaml` to specify your target roles, locations, and experience:

```yaml
search_criteria:
  keywords:
    - "Full Stack Developer"
    - "React Developer"
    - "Java Developer"
  locations:
    - "Bengaluru"
    - "Hyderabad"
    - "Remote"
  experience:
    min: 2
    max: 5
  freshness_days: 1 # Jobs posted in last 24h
```

---

## 🏃 Ways to Run

### Option A: Run Immediately (Manual Test)

```powershell
java -jar target/naukri-automation.jar
```

### Option B: Keep Running via Built-in Java Scheduler

```powershell
java -jar target/naukri-automation.jar --run-now
```
*This starts the scheduler, executes one test run immediately, and then continues running daily at 8:30 AM IST.*

---

### Option C: Schedule in Windows Task Scheduler (Recommended for Local PC)

Run the included PowerShell installer (Run as Administrator):

```powershell
powershell -ExecutionPolicy Bypass -File setup_windows_task.ps1
```

This registers a Windows Task that executes `run_daily.bat` every morning at **08:30 AM IST** in the background.

---

### Option D: Run in the Cloud via GitHub Actions (100% Free, No PC Needed)

1. Push this repository to a private GitHub repository.
2. In GitHub, go to **Settings** > **Secrets and variables** > **Actions**.
3. Add two repository secrets:
   - `NAUKRI_EMAIL`: Your Naukri email
   - `NAUKRI_PASSWORD`: Your Naukri password
4. The workflow in [`.github/workflows/daily_naukri.yml`](.github/workflows/daily_naukri.yml) will trigger automatically every day at **8:30 AM IST (03:00 AM UTC)**.

---

## 📊 Logs & Tracking

- **Applied Jobs History**: Saved in [`data/applied_jobs.json`](data/applied_jobs.json) with company name, job title, and application timestamp.
- **Log Files**: Full execution logs are saved in [`logs/naukri_automation.log`](logs/naukri_automation.log).
- **Screenshots on Error**: If any step encounters an issue, a screenshot is saved in `data/debug_screenshots/`.
