package io.choerodon.asgard.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("choerodon.asgard")
public class AsgardProperties {

    private Quartz quartz = new Quartz();

    private Saga saga = new Saga();

    public Quartz getQuartz() {
        return quartz;
    }

    public void setQuartz(Quartz quartz) {
        this.quartz = quartz;
    }

    public Saga getSaga() {
        return saga;
    }

    public void setSaga(Saga saga) {
        this.saga = saga;
    }

    public static class Quartz {
        private String schedulerName = "quartzScheduler";

        private boolean autoStartup = true;

        private boolean overwriteExistingJobs = false;

        private boolean waitForJobsToCompleteOnShutdown = false;

        private final Map<String, String> properties = new HashMap<>();

        public String getSchedulerName() {
            return schedulerName;
        }

        public void setSchedulerName(String schedulerName) {
            this.schedulerName = schedulerName;
        }

        public boolean isAutoStartup() {
            return autoStartup;
        }

        public void setAutoStartup(boolean autoStartup) {
            this.autoStartup = autoStartup;
        }

        public boolean isOverwriteExistingJobs() {
            return overwriteExistingJobs;
        }

        public void setOverwriteExistingJobs(boolean overwriteExistingJobs) {
            this.overwriteExistingJobs = overwriteExistingJobs;
        }

        public boolean isWaitForJobsToCompleteOnShutdown() {
            return waitForJobsToCompleteOnShutdown;
        }

        public void setWaitForJobsToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
            this.waitForJobsToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }


    public static class Saga {

        private long backCheckIntervalMs = 1_000L;

        private int unConfirmedTimeoutSeconds = 300;

        public long getBackCheckIntervalMs() {
            return backCheckIntervalMs;
        }

        public void setBackCheckIntervalMs(long backCheckIntervalMs) {
            this.backCheckIntervalMs = backCheckIntervalMs;
        }

        public int getUnConfirmedTimeoutSeconds() {
            return unConfirmedTimeoutSeconds;
        }

        public void setUnConfirmedTimeoutSeconds(int unConfirmedTimeoutSeconds) {
            this.unConfirmedTimeoutSeconds = unConfirmedTimeoutSeconds;
        }

    }


}
