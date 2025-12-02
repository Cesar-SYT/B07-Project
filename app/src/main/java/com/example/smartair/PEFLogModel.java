package com.example.smartair;


public class PEFLogModel {
    private String logId;
    private String dateTime; // Format: yyyy-MM-dd HH:mm
    private long timestamp;  // For sorting
    private int preValue;    // Pre-med value (or single value)
    private int postValue;   // Post-med value (0 if not applicable)
    private boolean isComparison; // true if Pre/Post mode
    private String zone;     // Green, Yellow, Red (Calculated based on best value)
    private String userId;

    public PEFLogModel() {
        // Default constructor required for Firebase
    }

    public PEFLogModel(String logId, String dateTime, long timestamp, int preValue, int postValue, boolean isComparison, String zone, String userId) {
        this.logId = logId;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
        this.preValue = preValue;
        this.postValue = postValue;
        this.isComparison = isComparison;
        this.zone = zone;
        this.userId = userId;
    }

    // Getters and Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getPreValue() { return preValue; }
    public void setPreValue(int preValue) { this.preValue = preValue; }

    public int getPostValue() { return postValue; }
    public void setPostValue(int postValue) { this.postValue = postValue; }

    public boolean isComparison() { return isComparison; }
    public void setComparison(boolean comparison) { isComparison = comparison; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}