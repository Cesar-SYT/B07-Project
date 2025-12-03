package com.example.smartair.r3;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.smartair.model.Child;
import com.example.smartair.model.Parent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public abstract class R3Service {

    // Medicines : record rescue / controller
    private final Map<String, MotivationState> motivationCache = new HashMap<>();

    private final DatabaseReference rootRef = FirebaseDatabase.getInstance(
            "https://smart-air-61888-default-rtdb.firebaseio.com/"
    ).getReference();;


    void getInventory(
            String childId,
            OnSuccessListener<List<InventoryItem>> onSuccess,
            OnFailureListener onFailure
    ) {

    }

    public void logRescueDose(Child child, int doseCount, int breathRate, String feel) {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());
        MedicineLogEntry entry = new MedicineLogEntry(
                doseCount, breathRate, feel, timeStr,  MedicineType.RESCUE);
        saveMedicineLog(child, entry);
        handleRescueAlertsIfNeeded(child, entry);
        updateLowRescueMonth(child, entry);
    }
    private void updateLowRescueMonth(Child child, MedicineLogEntry entry) {
        MotivationState state = loadMotivationState(child);

        if (entry.getType() == MedicineType.RESCUE) {
            state.resetStreak(StreakType.RESCUE_LOW_MONTH);
            saveMotivationState(child, state);
            return;
        }

        int streak = state.getStreak(StreakType.RESCUE_LOW_MONTH);
        streak += 1;
        state.setStreak(StreakType.RESCUE_LOW_MONTH, streak);

        if (streak >= 30 && !state.hasBadge(BadgeType.LOW_RESCUE_MONTH)) {
            state.awardBadge(BadgeType.LOW_RESCUE_MONTH);
            onBadgeAwarded(child, BadgeType.LOW_RESCUE_MONTH);
        }

        saveMotivationState(child, state);
    }

    public void logControllerDose(Child child, int doseCount, int breathRate, String feel) {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());
        MedicineLogEntry entry = new MedicineLogEntry(
                doseCount, breathRate, feel, timeStr, MedicineType.CONTROLLER);
        saveMedicineLog(child, entry);
        updateControllerStreak(child, true);
    }

    // Technique Helper

    public void completeTechniqueSession(TechniqueSession session) {
        saveTechniqueSession(session);

        if (session.isHighQuality()) {
            onHighQualityTechniqueSession(session.getChild());
        }
    }

    // Inventory

    public void updateInventory(Child child,
                                InventoryItem item,
                                int newRemainingDoses,
                                Date today) {

        item.setRemainingDoses(newRemainingDoses);
        saveInventoryItem(item);

        // low canister
        if (item.isLowCanister(getLowCanisterThresholdPercent())) {
            notifyLowCanister(child, item);
        }

        // expire
        if (item.isExpired(today)) {
            notifyExpiredMedication(child, item);
        }
    }

    // Motivation

    protected void onHighQualityTechniqueSession(Child child) {
        MotivationState state = loadMotivationState(child);

        // technique streak +1
        int currentStreak =
                state.getStreak(StreakType.TECHNIQUE_COMPLETED_DAYS);
        int newStreak = currentStreak + 1;
        state.setStreak(StreakType.TECHNIQUE_COMPLETED_DAYS, newStreak);

        // check “10 high-quality technique sessions” badge
        int totalHighQualityCount = getTotalHighQualityTechniqueCount(child);
        boolean hasTechniqueBadgeBefore =
                state.hasBadge(BadgeType.TEN_HIGH_QUALITY_TECHNIQUE_SESSIONS);

        if (totalHighQualityCount >= getHighQualityTechniqueBadgeThreshold()
                && !hasTechniqueBadgeBefore) {

            state.awardBadge(BadgeType.TEN_HIGH_QUALITY_TECHNIQUE_SESSIONS);
            onBadgeAwarded(child, BadgeType.TEN_HIGH_QUALITY_TECHNIQUE_SESSIONS);
        }
        saveTechniqueMotivationFields(child,
                newStreak,
                state.hasBadge(BadgeType.TEN_HIGH_QUALITY_TECHNIQUE_SESSIONS));
    }
    private void saveTechniqueMotivationFields(Child child,
                                               int techniqueStreak,
                                               boolean hasTechniqueBadge) {
        if (child == null || child.getId() == null) return;
        String childId = child.getId();

        MotivationState state = loadMotivationState(child);
        state.setStreak(StreakType.TECHNIQUE_COMPLETED_DAYS, techniqueStreak);
        if (hasTechniqueBadge) {
            state.awardBadge(BadgeType.TEN_HIGH_QUALITY_TECHNIQUE_SESSIONS);
        }
        motivationCache.put(childId, state);

        DatabaseReference baseRef = rootRef.child("users")
                .child(childId)
                .child("motivation");

        baseRef.child("streaks")
                .child(StreakType.TECHNIQUE_COMPLETED_DAYS.name())
                .setValue(techniqueStreak);

        baseRef.child("badges")
                .child(BadgeType.TEN_HIGH_QUALITY_TECHNIQUE_SESSIONS.name())
                .setValue(hasTechniqueBadge);
    }



    protected void updateControllerStreak(Child child, boolean takenToday) {
        if (!takenToday) {
            return;
        }

        MotivationState state = loadMotivationState(child);

        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        // last check streak date
        String lastDateStr = state.getLastStreakDate(StreakType.CONTROLLER_DAYS);

        if (todayStr.equals(lastDateStr)) {
            return;
        }

        int newStreak;

        if (lastDateStr == null) {
            newStreak = 1;
        } else {
            LocalDate lastDate = LocalDate.parse(lastDateStr);

            if (lastDate.plusDays(1).isEqual(today)) {
                int streak = state.getStreak(StreakType.CONTROLLER_DAYS);
                newStreak = streak + 1;
            } else {
                newStreak = 1;
            }
        }

        state.setStreak(StreakType.CONTROLLER_DAYS, newStreak);
        state.setLastStreakDate(StreakType.CONTROLLER_DAYS, todayStr);

        if (isFirstPerfectControllerWeek(child, state)
                && !state.hasBadge(BadgeType.FIRST_PERFECT_CONTROLLER_WEEK)) {

            state.awardBadge(BadgeType.FIRST_PERFECT_CONTROLLER_WEEK);
            onBadgeAwarded(child, BadgeType.FIRST_PERFECT_CONTROLLER_WEEK);
        }

        saveControllerMotivationFields(child, state);
    }
    private void saveControllerMotivationFields(Child child, MotivationState state) {
        if (child == null || child.getId() == null) return;
        String childId = child.getId();

        motivationCache.put(childId, state);

        DatabaseReference baseRef = rootRef.child("users")
                .child(childId)
                .child("motivation");

        int controllerDays = state.getStreak(StreakType.CONTROLLER_DAYS);
        baseRef.child("streaks")
                .child(StreakType.CONTROLLER_DAYS.name())
                .setValue(controllerDays);

        boolean hasControllerBadge =
                state.hasBadge(BadgeType.FIRST_PERFECT_CONTROLLER_WEEK);
        baseRef.child("badges")
                .child(BadgeType.FIRST_PERFECT_CONTROLLER_WEEK.name())
                .setValue(hasControllerBadge);
    }




    protected abstract void saveMedicineLog(Child child, MedicineLogEntry entry);


    protected abstract void saveTechniqueSession(TechniqueSession session);


    protected abstract void saveInventoryItem(InventoryItem item);


    protected abstract MotivationState loadMotivationState(Child child);


    protected abstract void saveMotivationState(Child child,
                                                MotivationState state);


    protected abstract int getTotalHighQualityTechniqueCount(Child child);


    protected abstract void handleRescueAlertsIfNeeded(Child child,
                                                       MedicineLogEntry entry);


    protected abstract void notifyLowCanister(Child child,
                                              InventoryItem item);


    protected abstract void notifyExpiredMedication(Child child,
                                                    InventoryItem item);


    protected abstract void onBadgeAwarded(Child child,
                                           BadgeType badgeType);


    protected abstract double getLowCanisterThresholdPercent();


    protected abstract int getHighQualityTechniqueBadgeThreshold();


    protected abstract boolean isFirstPerfectControllerWeek(Child child,
                                                            MotivationState state);

}
