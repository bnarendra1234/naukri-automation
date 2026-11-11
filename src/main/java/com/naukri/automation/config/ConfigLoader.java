package com.naukri.automation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoader {
    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static AppConfig instance;

    public static synchronized AppConfig load() {
        if (instance != null) {
            return instance;
        }

        AppConfig config = new AppConfig();

        // 1. Load config.yaml if present
        File yamlFile = new File("config.yaml");
        if (yamlFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                config = mapper.readValue(yamlFile, AppConfig.class);
                log.info("Loaded configuration from config.yaml");
            } catch (Exception e) {
                log.warn("Failed to parse config.yaml: {}. Using default configuration.", e.getMessage());
            }
        } else {
            log.warn("config.yaml not found. Using default search rules.");
        }

        // 2. Load .env file into map
        Map<String, String> dotEnvMap = loadDotEnv();

        config.setEmail(getEnv(dotEnvMap, "NAUKRI_EMAIL", ""));
        config.setPassword(getEnv(dotEnvMap, "NAUKRI_PASSWORD", ""));

        String headlessStr = getEnv(dotEnvMap, "HEADLESS", "false");
        config.setHeadless(Boolean.parseBoolean(headlessStr));

        String updateProfileStr = getEnv(dotEnvMap, "UPDATE_PROFILE", "true");
        config.setUpdateProfile(Boolean.parseBoolean(updateProfileStr));

        String maxAppsStr = getEnv(dotEnvMap, "MAX_APPLICATIONS_PER_RUN", "25");
        try {
            config.setMaxApplicationsPerRun(Integer.parseInt(maxAppsStr.trim()));
        } catch (NumberFormatException ignored) {
            config.setMaxApplicationsPerRun(25);
        }

        config.setScheduleTime(getEnv(dotEnvMap, "SCHEDULE_TIME", "08:30"));
        config.setTimezone(getEnv(dotEnvMap, "TIMEZONE", "Asia/Kolkata"));

        instance = config;
        return instance;
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> map = new HashMap<>();
        Path path = Paths.get(".env");
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String val = line.substring(eqIdx + 1).trim();
                        if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                            val = val.substring(1, val.length() - 1);
                        } else if (val.startsWith("'") && val.endsWith("'") && val.length() >= 2) {
                            val = val.substring(1, val.length() - 1);
                        }
                        map.put(key, val);
                    }
                }
            } catch (Exception e) {
                log.debug("Notice reading .env: {}", e.getMessage());
            }
        }
        return map;
    }

    private static String getEnv(Map<String, String> dotEnvMap, String key, String defaultValue) {
        String val = System.getenv(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        if (dotEnvMap != null && dotEnvMap.containsKey(key)) {
            val = dotEnvMap.get(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return defaultValue;
    }

    public static void validate(AppConfig config) {
        if (config.getEmail() == null || config.getEmail().trim().isEmpty() ||
            config.getPassword() == null || config.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("NAUKRI_EMAIL and NAUKRI_PASSWORD must be configured in .env or system environment variables.");
        }
    }
}
