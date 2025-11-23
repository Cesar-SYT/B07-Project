package com.example.smartair.r3;

import com.example.smartair.model.Child;

import java.util.Calendar;
import java.util.Date;

public class InventoryItem {

    private final Child child;        // Belongs to which child
    private final String name;        // name of medicine
    private final MedicineType type;  // Rescue / Controller

    private Date purchaseDate;
    private Date expiryDate;

    private int totalDoses;     // Total dose at time of purchase
    private int remainingDoses; // Remaining dose

    public InventoryItem(Child child,
                         String name,
                         MedicineType type,
                         Date purchaseDate,
                         Date expiryDate,
                         int totalDoses,
                         int remainingDoses) {
        this.child = child;
        this.name = name;
        this.type = type;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.totalDoses = totalDoses;
        this.remainingDoses = remainingDoses;
    }

    public Child getChild() {
        return child;
    }

    public String getName() {
        return name;
    }

    public MedicineType getType() {
        return type;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getTotalDoses() {
        return totalDoses;
    }

    public void setTotalDoses(int totalDoses) {
        this.totalDoses = totalDoses;
    }

    public int getRemainingDoses() {
        return remainingDoses;
    }

    public void setRemainingDoses(int remainingDoses) {
        this.remainingDoses = remainingDoses;
    }

    /** Percentage remaining */
    public double getRemainingPercent() {
        if (totalDoses <= 0) return 0.0;
        return (remainingDoses * 100.0) / totalDoses;
    }

    /** Is inventory low */
    public boolean isLowCanister(double thresholdPercent) {
        return getRemainingPercent() <= thresholdPercent;
    }

    public boolean isExpired(Date today) {
        if (expiryDate == null) return false;

        // expiryDate is before today or exactly equal to today
        return !expiryDate.after(today);
    }
}
