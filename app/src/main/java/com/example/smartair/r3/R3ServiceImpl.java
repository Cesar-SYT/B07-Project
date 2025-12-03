package com.example.smartair.r3;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.smartair.model.Child;
import com.example.smartair.model.Parent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class R3ServiceImpl extends R3Service {

    private static final String TAG = "R3ServiceImpl";

    private final DatabaseReference rootRef =
            FirebaseDatabase.getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/").getReference();


    // childId -> MotivationState
    private final Map<String, MotivationState> motivationCache = new HashMap<>();

    // childId -> total high-quality technique count
    private final Map<String, Integer> highQualityCountCache = new HashMap<>();


    @Override
    protected void saveMedicineLog(Child child, MedicineLogEntry entry) {

        if (child == null || child.getId() == null) {
            Log.w(TAG, "saveMedicineLog: child or child id is null");
            return;
        }

        String childId = child.getId();

        rootRef.child("users")
                .child(childId)
                .child("medicine_logs")
                .push()
                .setValue(entry);

        Log.d(TAG, "saveMedicineLog: saved for childId=" + childId);
    }


    @Override
    protected void saveTechniqueSession(TechniqueSession session) {
        Child child = session.getChild();
        if (child == null) {
            Log.w(TAG, "saveTechniqueSession: child is null");
            return;
        }
        String childId = child.getId();

        rootRef.child("technique_sessions")
                .child(childId)
                .push()
                .setValue(session);

        if (session.isHighQuality()) {
            int current = highQualityCountCache.getOrDefault(childId, 0);
            highQualityCountCache.put(childId, current + 1);
        }

        Log.d(TAG, "saveTechniqueSession: saved for childId=" + childId);
    }

    @Override
    protected void saveInventoryItem(InventoryItem item) {
        Child child = item.getChild();
        if (child == null) {
            Log.w(TAG, "saveInventoryItem: child is null");
            return;
        }
        String childId = child.getId();

        rootRef.child("inventory")
                .child(childId)
                .setValue(item);

        Log.d(TAG, "saveInventoryItem: saved for childId=" + childId);
    }

    @Override
    protected MotivationState loadMotivationState(Child child) {
        if (child == null || child.getId() == null) {
            Log.w(TAG, "loadMotivationState: child is null or has null id, return new state");
            return new MotivationState();
        }
        String childId = child.getId();

        MotivationState cached = motivationCache.get(childId);
        if (cached != null) {
            return cached;
        }

        MotivationState state = new MotivationState();
        motivationCache.put(childId, state);

        DatabaseReference motivationRef = rootRef
                .child("users")
                .child(childId)
                .child("motivation");

        motivationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MotivationStateRecord record = snapshot.getValue(MotivationStateRecord.class);
                if (record == null) {
                    Log.d(TAG, "loadMotivationState: no remote record, keep default for childId=" + childId);
                    motivationRef.setValue(toRecord(state));
                    return;
                }

                MotivationState loaded = fromRecord(record);
                motivationCache.put(childId, loaded);
                Log.d(TAG, "loadMotivationState: loaded remote state for childId=" + childId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadMotivationState: firebase cancelled for childId=" + childId
                        + ", error=" + error.getMessage());
            }
        });

        return state;
    }
    private MotivationState fromRecord(MotivationStateRecord record) {
        MotivationState state = new MotivationState();

        // 1) streaks: String -> StreakType
        if (record.streaks != null) {
            for (Map.Entry<String, Integer> entry : record.streaks.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                if (value == null) continue;

                try {
                    StreakType type = StreakType.valueOf(key);
                    state.setStreak(type, value);
                } catch (IllegalArgumentException e) {
                }
            }
        }

        // 2) lastStreakDates: String -> StreakType
        if (record.lastStreakDates != null) {
            for (Map.Entry<String, String> entry : record.lastStreakDates.entrySet()) {
                String key = entry.getKey();
                String date = entry.getValue();
                if (date == null) continue;

                try {
                    StreakType type = StreakType.valueOf(key);
                    state.setLastStreakDate(type, date);
                } catch (IllegalArgumentException e) {
                }
            }
        }

        // 3) badges: String -> BadgeType
        if (record.badges != null) {
            for (Map.Entry<String, Boolean> entry : record.badges.entrySet()) {
                String key = entry.getKey();
                Boolean unlocked = entry.getValue();
                if (!Boolean.TRUE.equals(unlocked)) continue;

                try {
                    BadgeType type = BadgeType.valueOf(key);
                    state.awardBadge(type);
                } catch (IllegalArgumentException e) {
                }
            }
        }

        return state;
    }



    @Override
    protected void saveMotivationState(Child child, MotivationState state) {
        String childId = child.getId();
        DatabaseReference ref = rootRef.child("users")
                .child(childId)
                .child("motivation");

        MotivationStateRecord record = toRecord(state);
        ref.setValue(record);
    }
    private MotivationStateRecord toRecord(MotivationState state) {
        MotivationStateRecord record = new MotivationStateRecord();

        // streaks:  StreakType -> int
        for (StreakType type : StreakType.values()) {
            int value = state.getStreak(type);
            record.streaks.put(type.name(), value);

            String date = state.getLastStreakDate(type);
            if (date != null) {
                record.lastStreakDates.put(type.name(), date);
            }
        }

        // badges: BadgeType -> boolean
        for (BadgeType type : BadgeType.values()) {
            boolean unlocked = state.hasBadge(type);
            record.badges.put(type.name(), unlocked);
        }

        return record;
    }



    @Override
    protected int getTotalHighQualityTechniqueCount(Child child) {
        if (child == null) return 0;
        String childId = child.getId();
        Integer value = highQualityCountCache.get(childId);
        return value != null ? value : 0;
    }


    @Override
    protected void handleRescueAlertsIfNeeded(Child child,
                                              MedicineLogEntry entry) {

        if (child != null) {
            Log.d(TAG, "handleRescueAlertsIfNeeded: childId=" + child.getId());
        }
    }

    @Override
    protected void notifyLowCanister(Child child, InventoryItem item) {

        String parentName = child != null ? child.getDisplayName() : "unknown";
        String medName = item != null ? item.getName() : "unknown";
        Log.d(TAG, "notifyLowCanister: parent=" + parentName + ", med=" + medName);
    }

    @Override
    protected void notifyExpiredMedication(Child child, InventoryItem item) {
        String parentName = child != null ? child.getDisplayName() : "unknown";
        String medName = item != null ? item.getName() : "unknown";
        Log.d(TAG, "notifyExpiredMedication: parent=" + parentName + ", med=" + medName);
    }

    @Override
    protected void onBadgeAwarded(Child child, BadgeType badgeType) {
        String childName = (child != null) ? child.getDisplayName() : "unknown";
        Log.d(TAG, "onBadgeAwarded: child=" + childName + ", badge=" + badgeType);

    }

    @Override
    protected double getLowCanisterThresholdPercent() {
        return 20.0;
    }

    @Override
    protected int getHighQualityTechniqueBadgeThreshold() {
        return 10;
    }

    @Override
    protected boolean isFirstPerfectControllerWeek(Child child,
                                                   MotivationState state) {
        int streak = state.getStreak(StreakType.CONTROLLER_DAYS);
        return streak >= 7;
    }
    public void addMedicine(String childId, InventoryItem item,
                            Runnable onSuccess,
                            OnFailureListener onFailure) {

        String key = rootRef.child("inventory")
                .child(childId)
                .push()
                .getKey();

        if (key == null) return;

        rootRef.child("inventory")
                .child(childId)
                .child(key)
                .setValue(item)
                .addOnSuccessListener(a -> onSuccess.run())
                .addOnFailureListener(onFailure);
    }
    @Override
    public void getInventory(
            String childId,
            OnSuccessListener<List<InventoryItem>> onSuccess,
            OnFailureListener onFailure
    ) {
        rootRef.child("inventory")
                .child(childId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<InventoryItem> list = new ArrayList<>();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        InventoryItem item = child.getValue(InventoryItem.class);
                        if (item != null) list.add(item);
                    }

                    onSuccess.onSuccess(list);
                })
                .addOnFailureListener(onFailure);
    }


}
