package com.example.smartair.r3;

import com.example.smartair.model.Child;
import java.util.Date;

/**
 * 单次用药记录。
 * 用统一的 java.util.Date，避免 LocalDateTime 在低 API 的兼容问题。
 */
public class MedicineLogEntry {

    private Child child;
    private MedicineType type;
    private int doseCount;
    private Date time;                // <-- 新的时间字段
    private BreathStatus preStatus;
    private BreathStatus postStatus;

    public MedicineLogEntry() {
        // Firebase 反序列化需要空构造器
    }

    public MedicineLogEntry(Child child,
                            MedicineType type,
                            int doseCount,
                            Date time,
                            BreathStatus preStatus,
                            BreathStatus postStatus) {
        this.child = child;
        this.type = type;
        this.doseCount = doseCount;
        this.time = time;
        this.preStatus = preStatus;
        this.postStatus = postStatus;
    }

    public Child getChild() {
        return child;
    }

    public MedicineType getType() {
        return type;
    }

    public int getDoseCount() {
        return doseCount;
    }

    public Date getTime() {
        return time;
    }

    public BreathStatus getPreStatus() {
        return preStatus;
    }

    public BreathStatus getPostStatus() {
        return postStatus;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public void setType(MedicineType type) {
        this.type = type;
    }

    public void setDoseCount(int doseCount) {
        this.doseCount = doseCount;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setPreStatus(BreathStatus preStatus) {
        this.preStatus = preStatus;
    }

    public void setPostStatus(BreathStatus postStatus) {
        this.postStatus = postStatus;
    }
}
