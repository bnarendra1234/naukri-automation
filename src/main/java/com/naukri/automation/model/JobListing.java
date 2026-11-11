package com.naukri.automation.model;

public class JobListing {
    private String jobId;
    private String title;
    private String company;
    private String location;
    private String url;
    private boolean easyApply;

    public JobListing() {
    }

    public JobListing(String jobId, String title, String company, String location, String url, boolean easyApply) {
        this.jobId = jobId;
        this.title = title;
        this.company = company;
        this.location = location;
        this.url = url;
        this.easyApply = easyApply;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEasyApply() {
        return easyApply;
    }

    public void setEasyApply(boolean easyApply) {
        this.easyApply = easyApply;
    }

    @Override
    public String toString() {
        return "JobListing{" +
                "jobId='" + jobId + '\'' +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", easyApply=" + easyApply +
                '}';
    }
}
