package com.example.smartair;


public class SharingSettings {

    private boolean shareRescueLogs = false;
    private boolean shareAdherence = false;
    private boolean shareSymptoms = false;
    private boolean shareTriggers = false;
    private boolean sharePEF = false;
    private boolean shareTriage = false;
    private boolean shareCharts = false;

    public SharingSettings() {
    }

    public boolean isShareRescueLogs() { return shareRescueLogs; }
    public boolean isShareAdherence() { return shareAdherence; }
    public boolean isShareSymptoms() { return shareSymptoms; }
    public boolean isShareTriggers() { return shareTriggers; }
    public boolean isSharePEF() { return sharePEF; }
    public boolean isShareTriage() { return shareTriage; }
    public boolean isShareCharts() { return shareCharts; }

    public void setShareRescueLogs(boolean shareRescueLogs) { this.shareRescueLogs = shareRescueLogs; }
    public void setShareAdherence(boolean shareAdherence) { this.shareAdherence = shareAdherence; }
    public void setShareSymptoms(boolean shareSymptoms) { this.shareSymptoms = shareSymptoms; }
    public void setShareTriggers(boolean shareTriggers) { this.shareTriggers = shareTriggers; }
    public void setSharePEF(boolean sharePEF) { this.sharePEF = sharePEF; }
    public void setShareTriage(boolean shareTriage) { this.shareTriage = shareTriage; }
    public void setShareCharts(boolean shareCharts) { this.shareCharts = shareCharts; }
}
