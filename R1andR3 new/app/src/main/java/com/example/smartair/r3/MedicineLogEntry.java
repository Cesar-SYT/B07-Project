package com.example.smartair.r3;

public class MedicineLogEntry {

    private int doseCount;
    private int breathRate;
    private String feel;
    private String time;
    private MedicineType type;      // ★ RESCUE / CONTROLLER

    public MedicineLogEntry() {}

    public MedicineLogEntry(int doseCount, int breathRate, String feel, String time, MedicineType type) {
        this.doseCount = doseCount;
        this.breathRate = breathRate;
        this.feel = feel;
        this.time = time;
        this.type = type;
    }

    public int getDoseCount() {
        return doseCount;
    }

    public int getBreathRate() {
        return breathRate;
    }

    public String getFeel() {
        return feel;
    }

    public String getTime() {
        return time;
    }

    public MedicineType getType() {
        return type;
    }

    public void setDoseCount(int doseCount) {
        this.doseCount = doseCount;
    }

    public void setBreathRate(int breathRate) {
        this.breathRate = breathRate;
    }

    public void setFeel(String feel) {
        this.feel = feel;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(MedicineType type) {
        this.type = type;
    }
}
