package com.example.smartair.r3;

import com.example.smartair.model.Child;

import java.util.Calendar;
import java.util.Date;

public class InventoryItem {

    private Child child;        // Belongs to which child
    private String name;        // name of medicine
    private MedicineType type;  // Rescue / Controller

    private Date purchaseDate;
    private Date expiryDate;

    private int totalDoses;     // Total dose at time of purchase
    private int remainingDoses; // Remaining dose
    private boolean low;
    private boolean expired;

    private String id;

    public boolean isLow() { return low; }
    public void setLow(boolean low) { this.low = low; }

    public boolean isExpiredFlag() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
    public InventoryItem() {
    }
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public InventoryItem(Child child, String name, Date purchaseDate, Date expiryDate) {
        this.child = child;
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;

        this.type = MedicineType.RESCUE;
        this.totalDoses = 0;
        this.remainingDoses = 0;
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


    public double getRemainingPercent() {
        if (totalDoses <= 0) {
            // 没有设置 totalDoses，就当作 100% 满罐
            return 100.0;
        }
        return (remainingDoses * 100.0) / totalDoses;
    }


    public boolean isLowCanister(double thresholdPercent) {
        if (totalDoses <= 0) {
            this.low = false;
            return false;
        }

        double percent = getRemainingPercent();
        boolean result = percent <= thresholdPercent;
        this.low = result;
        return result;
    }



    public boolean isExpired(Date today) {
        if (expiryDate == null) {
            this.expired = false;
            return false;
        }

        boolean result = !expiryDate.after(today);
        this.expired = result;
        return result;
    }

}
