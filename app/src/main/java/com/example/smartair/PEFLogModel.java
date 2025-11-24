package com.example.smartair;

/**
 * Data model representing a single PEF (Peak Expiratory Flow) log entry.
 */
public class PEFLogModel {
    private String logId;
    private String dateTime; // Format: yyyy-MM-dd HH:mm
    private float pefValue;
    private String zone; // Green, Yellow, Red
    private String userId;

    public PEFLogModel() {
        // Default constructor required for calls to DataSnapshot.getValue(PEFLogModel.class)
    }

    public PEFLogModel(String logId, String dateTime, float pefValue, String zone, String userId) {
        this.logId = logId;
        this.dateTime = dateTime;
        this.pefValue = pefValue;
        this.zone = zone;
        this.userId = userId;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public float getPefValue() {
        return pefValue;
    }

    public void setPefValue(float pefValue) {
        this.pefValue = pefValue;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}