package com.example.smartair;

import java.util.List;

/**
 * Model representing detailed health data for a child.
 * Includes fields for various metrics and their sharing status.
 */
public class ChildHealthData {
    private String childId;

    // Data Categories
    private List<LogEntry> rescueLogs;
    private AdherenceSummary controllerAdherence;
    private List<String> symptoms;
    private List<String> triggers;
    private List<PEFLogModel> pefLogs;
    private List<LogEntry> triageIncidents;
    // Summary charts data would typically be complex objects, using placeholder for now
    private String chartDataSummary;

    // Sharing Status (True if shared with provider)
    private boolean shareRescueLogs;
    private boolean shareAdherence;
    private boolean shareSymptoms;
    private boolean shareTriggers;
    private boolean sharePEF;
    private boolean shareTriage;
    private boolean shareCharts;

    public ChildHealthData(String childId) {
        this.childId = childId;
    }

    // Getters and Setters

    public boolean isShareRescueLogs() { return shareRescueLogs; }
    public void setShareRescueLogs(boolean shareRescueLogs) { this.shareRescueLogs = shareRescueLogs; }

    public boolean isShareAdherence() { return shareAdherence; }
    public void setShareAdherence(boolean shareAdherence) { this.shareAdherence = shareAdherence; }

    public boolean isShareSymptoms() { return shareSymptoms; }
    public void setShareSymptoms(boolean shareSymptoms) { this.shareSymptoms = shareSymptoms; }

    public boolean isShareTriggers() { return shareTriggers; }
    public void setShareTriggers(boolean shareTriggers) { this.shareTriggers = shareTriggers; }

    public boolean isSharePEF() { return sharePEF; }
    public void setSharePEF(boolean sharePEF) { this.sharePEF = sharePEF; }

    public boolean isShareTriage() { return shareTriage; }
    public void setShareTriage(boolean shareTriage) { this.shareTriage = shareTriage; }

    public boolean isShareCharts() { return shareCharts; }
    public void setShareCharts(boolean shareCharts) { this.shareCharts = shareCharts; }

    // Inner classes for specific data types
    public static class LogEntry {
        public String date;
        public String description;
        public LogEntry(String date, String description) {
            this.date = date;
            this.description = description;
        }
    }

    public static class AdherenceSummary {
        public int percentage;
        public String period; // e.g., "Last 7 days"
        public AdherenceSummary(int percentage, String period) {
            this.percentage = percentage;
            this.period = period;
        }
    }
}