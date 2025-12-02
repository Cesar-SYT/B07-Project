package com.example.smartair.r3;


public class SimpleMedicineLog {

    private int doseCount;
    private int breathRate;   // 1~5
    private String feel;      // "Better" / "Same" / "Worse"
    private String type;      // "RESCUE" / "CONTROLLER"
    private String time;      // "yyyy-MM-dd HH:mm"

    public SimpleMedicineLog() {}

    public SimpleMedicineLog(int doseCount,
                             int breathRate,
                             String feel,
                             String type,
                             String time) {
        this.doseCount = doseCount;
        this.breathRate = breathRate;
        this.feel = feel;
        this.type = type;
        this.time = time;
    }

    public int getDoseCount() { return doseCount; }
    public void setDoseCount(int doseCount) { this.doseCount = doseCount; }

    public int getBreathRate() { return breathRate; }
    public void setBreathRate(int breathRate) { this.breathRate = breathRate; }

    public String getFeel() { return feel; }
    public void setFeel(String feel) { this.feel = feel; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
