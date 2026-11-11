package com.naukri.automation.model;

public class AutomationStats {
    private int scanned = 0;
    private int eligible = 0;
    private int applied = 0;
    private int skipped = 0;
    private boolean profileBoosted = false;

    public void incrementScanned() {
        this.scanned++;
    }

    public void incrementScanned(int count) {
        this.scanned += count;
    }

    public void incrementEligible() {
        this.eligible++;
    }

    public void incrementApplied() {
        this.applied++;
    }

    public void incrementSkipped() {
        this.skipped++;
    }

    public int getScanned() {
        return scanned;
    }

    public int getEligible() {
        return eligible;
    }

    public int getApplied() {
        return applied;
    }

    public int getSkipped() {
        return skipped;
    }

    public boolean isProfileBoosted() {
        return profileBoosted;
    }

    public void setProfileBoosted(boolean profileBoosted) {
        this.profileBoosted = profileBoosted;
    }
}
