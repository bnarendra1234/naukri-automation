package com.naukri.automation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.naukri.automation.config.AppConfig;
import com.naukri.automation.model.AppliedJobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TrackerService {
    private static final Logger log = LoggerFactory.getLogger(TrackerService.class);
    private final Path storagePath;
    private final ObjectMapper objectMapper;
    private Map<String, Object> historyData;

    public TrackerService(AppConfig config) {
        this.storagePath = Paths.get(config.getStorage().getAppliedHistoryFile());
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.historyData = loadHistory();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadHistory() {
        if (Files.exists(storagePath)) {
            try {
                return objectMapper.readValue(storagePath.toFile(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("Could not read applied jobs history file ({}). Starting fresh.", e.getMessage());
            }
        }
        Map<String, Object> fresh = new HashMap<>();
        fresh.put("applied_jobs", new HashMap<String, AppliedJobInfo>());
        return fresh;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getAppliedJobsMap() {
        Object jobs = historyData.get("applied_jobs");
        if (jobs instanceof Map) {
            return (Map<String, Object>) jobs;
        }
        Map<String, Object> fresh = new HashMap<>();
        historyData.put("applied_jobs", fresh);
        return fresh;
    }

    private synchronized void save() {
        try {
            if (storagePath.getParent() != null) {
                Files.createDirectories(storagePath.getParent());
            }
            objectMapper.writeValue(storagePath.toFile(), historyData);
        } catch (Exception e) {
            log.error("Failed to save applied jobs tracker file: {}", e.getMessage());
        }
    }

    public synchronized boolean isApplied(String jobId) {
        if (jobId == null || jobId.trim().isEmpty()) {
            return false;
        }
        return getAppliedJobsMap().containsKey(jobId.trim());
    }

    public synchronized void recordApplied(String jobId, String title, String company, String location, String url) {
        if (jobId == null || jobId.trim().isEmpty()) {
            return;
        }
        String nowIso = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        AppliedJobInfo info = new AppliedJobInfo(title, company, location, url, nowIso, "APPLIED");
        getAppliedJobsMap().put(jobId.trim(), info);
        save();
        log.info("Recorded application in history: {} at {} (Job ID: {})", title, company, jobId);
    }

    @SuppressWarnings("unchecked")
    public int getTodayCount() {
        String todayStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int count = 0;
        Map<String, Object> appliedJobs = getAppliedJobsMap();
        for (Object value : appliedJobs.values()) {
            if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                String appliedAt = (String) map.get("applied_at");
                if (appliedAt != null && appliedAt.startsWith(todayStr)) {
                    count++;
                }
            } else if (value instanceof AppliedJobInfo) {
                AppliedJobInfo info = (AppliedJobInfo) value;
                if (info.getAppliedAt() != null && info.getAppliedAt().startsWith(todayStr)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getTotalAppliedCount() {
        return getAppliedJobsMap().size();
    }
}
