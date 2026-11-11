package com.naukri.automation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppliedJobInfo {
    private String title;
    private String company;
    private String location;
    private String url;

    @JsonProperty("applied_at")
    private String appliedAt;

    private String status;

    public AppliedJobInfo() {
    }

    public AppliedJobInfo(String title, String company, String location, String url, String appliedAt, String status) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.url = url;
        this.appliedAt = appliedAt;
        this.status = status;
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

    public String getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(String appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
