package com.naukri.automation.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {

    // Loaded from .env or System Environment
    private String email;
    private String password;
    private boolean headless = false;
    private boolean updateProfile = true;
    private int maxApplicationsPerRun = 25;
    private String scheduleTime = "08:30";
    private String timezone = "Asia/Kolkata";

    // Loaded from config.yaml
    @JsonProperty("search_criteria")
    private SearchCriteria searchCriteria = new SearchCriteria();

    @JsonProperty("filters")
    private Filters filters = new Filters();

    @JsonProperty("profile_update")
    private ProfileUpdate profileUpdate = new ProfileUpdate();

    @JsonProperty("application")
    private ApplicationSettings application = new ApplicationSettings();

    @JsonProperty("storage")
    private StorageSettings storage = new StorageSettings();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchCriteria {
        private List<String> keywords = new ArrayList<>();
        private List<String> locations = new ArrayList<>();
        private Experience experience = new Experience();

        @JsonProperty("freshness_days")
        private Integer freshnessDays = 1;

        @JsonProperty("min_salary_lakhs")
        private Integer minSalaryLakhs;

        public List<String> getKeywords() {
            return keywords;
        }

        public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
        }

        public List<String> getLocations() {
            return locations;
        }

        public void setLocations(List<String> locations) {
            this.locations = locations;
        }

        public Experience getExperience() {
            return experience;
        }

        public void setExperience(Experience experience) {
            this.experience = experience;
        }

        public Integer getFreshnessDays() {
            return freshnessDays;
        }

        public void setFreshnessDays(Integer freshnessDays) {
            this.freshnessDays = freshnessDays;
        }

        public Integer getMinSalaryLakhs() {
            return minSalaryLakhs;
        }

        public void setMinSalaryLakhs(Integer minSalaryLakhs) {
            this.minSalaryLakhs = minSalaryLakhs;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Experience {
        private Integer min = 2;
        private Integer max = 5;

        public Integer getMin() {
            return min;
        }

        public void setMin(Integer min) {
            this.min = min;
        }

        public Integer getMax() {
            return max;
        }

        public void setMax(Integer max) {
            this.max = max;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Filters {
        @JsonProperty("exclude_companies")
        private List<String> excludeCompanies = new ArrayList<>();

        @JsonProperty("exclude_title_keywords")
        private List<String> excludeTitleKeywords = new ArrayList<>();

        @JsonProperty("easy_apply_only")
        private boolean easyApplyOnly = true;

        public List<String> getExcludeCompanies() {
            return excludeCompanies;
        }

        public void setExcludeCompanies(List<String> excludeCompanies) {
            this.excludeCompanies = excludeCompanies;
        }

        public List<String> getExcludeTitleKeywords() {
            return excludeTitleKeywords;
        }

        public void setExcludeTitleKeywords(List<String> excludeTitleKeywords) {
            this.excludeTitleKeywords = excludeTitleKeywords;
        }

        public boolean isEasyApplyOnly() {
            return easyApplyOnly;
        }

        public void setEasyApplyOnly(boolean easyApplyOnly) {
            this.easyApplyOnly = easyApplyOnly;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProfileUpdate {
        private boolean enabled = true;

        @JsonProperty("headline_suffix_toggle")
        private boolean headlineSuffixToggle = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isHeadlineSuffixToggle() {
            return headlineSuffixToggle;
        }

        public void setHeadlineSuffixToggle(boolean headlineSuffixToggle) {
            this.headlineSuffixToggle = headlineSuffixToggle;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApplicationSettings {
        @JsonProperty("min_delay_seconds")
        private double minDelaySeconds = 4.0;

        @JsonProperty("max_delay_seconds")
        private double maxDelaySeconds = 8.0;

        @JsonProperty("max_pages_per_keyword")
        private int maxPagesPerKeyword = 3;

        @JsonProperty("daily_max_applications")
        private int dailyMaxApplications = 30;

        public double getMinDelaySeconds() {
            return minDelaySeconds;
        }

        public void setMinDelaySeconds(double minDelaySeconds) {
            this.minDelaySeconds = minDelaySeconds;
        }

        public double getMaxDelaySeconds() {
            return maxDelaySeconds;
        }

        public void setMaxDelaySeconds(double maxDelaySeconds) {
            this.maxDelaySeconds = maxDelaySeconds;
        }

        public int getMaxPagesPerKeyword() {
            return maxPagesPerKeyword;
        }

        public void setMaxPagesPerKeyword(int maxPagesPerKeyword) {
            this.maxPagesPerKeyword = maxPagesPerKeyword;
        }

        public int getDailyMaxApplications() {
            return dailyMaxApplications;
        }

        public void setDailyMaxApplications(int dailyMaxApplications) {
            this.dailyMaxApplications = dailyMaxApplications;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StorageSettings {
        @JsonProperty("applied_history_file")
        private String appliedHistoryFile = "data/applied_jobs.json";

        @JsonProperty("session_file")
        private String sessionFile = "data/session_state.json";

        public String getAppliedHistoryFile() {
            return appliedHistoryFile;
        }

        public void setAppliedHistoryFile(String appliedHistoryFile) {
            this.appliedHistoryFile = appliedHistoryFile;
        }

        public String getSessionFile() {
            return sessionFile;
        }

        public void setSessionFile(String sessionFile) {
            this.sessionFile = sessionFile;
        }
    }

    // Getters and Setters for Top-Level
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public boolean isUpdateProfile() {
        return updateProfile;
    }

    public void setUpdateProfile(boolean updateProfile) {
        this.updateProfile = updateProfile;
    }

    public int getMaxApplicationsPerRun() {
        return maxApplicationsPerRun;
    }

    public void setMaxApplicationsPerRun(int maxApplicationsPerRun) {
        this.maxApplicationsPerRun = maxApplicationsPerRun;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public SearchCriteria getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public Filters getFilters() {
        return filters;
    }

    public void setFilters(Filters filters) {
        this.filters = filters;
    }

    public ProfileUpdate getProfileUpdate() {
        return profileUpdate;
    }

    public void setProfileUpdate(ProfileUpdate profileUpdate) {
        this.profileUpdate = profileUpdate;
    }

    public ApplicationSettings getApplication() {
        return application;
    }

    public void setApplication(ApplicationSettings application) {
        this.application = application;
    }

    public StorageSettings getStorage() {
        return storage;
    }

    public void setStorage(StorageSettings storage) {
        this.storage = storage;
    }
}
