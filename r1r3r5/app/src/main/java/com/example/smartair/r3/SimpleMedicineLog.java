package com.example.smartair.r3;

/**
 * UI 用的简化版 log：
 * - 不再保存 Child
 * - 只保存 doseCount / breathRate / feel / type / time
 * 方便直接存到 /users/{childKey}/medicine_logs 下面
 */
public class SimpleMedicineLog {

    private int doseCount;
    private int breathRate;   // 1~5
    private String feel;      // "Better" / "Same" / "Worse"
    private String type;      // "RESCUE" / "CONTROLLER"
    private String time;      // "yyyy-MM-dd HH:mm"

    // Firebase 需要无参构造
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
