package com.example.smartair;

/**
 * Data model for an alert sent to a parent.
 */
public class Alert {
    private String alertId;
    private String childKey;
    private String childName;
    private String type; // e.g., "TRIAGE_STARTED", "TRIAGE_ESCALATED"
    private long timestamp;
    private String status; // "new" or "viewed"

    public Alert() {
        // Default constructor for Firebase
    }

    public Alert(String alertId, String childKey, String childName, String type, long timestamp, String status) {
        this.alertId = alertId;
        this.childKey = childKey;
        this.childName = childName;
        this.type = type;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and Setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getChildKey() { return childKey; }
    public void setChildKey(String childKey) { this.childKey = childKey; }

    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
