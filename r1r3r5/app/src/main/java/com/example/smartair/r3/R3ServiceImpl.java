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

/**
 * R3Service 的具体实现：
 * - 写入：使用 Firebase Realtime Database
 * - 读取：使用内存里的简单缓存 Map（不做真正的异步读取）
 * 这样既能把数据同步到云端，又不会让代码被异步逻辑搞得很复杂，
 * 非常适合作为 B07 项目的 R3 实现。
 */
public class R3ServiceImpl extends R3Service {

    private static final String TAG = "R3ServiceImpl";

    // Firebase 根引用
    private final DatabaseReference rootRef =
            FirebaseDatabase.getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/").getReference();

    // -------- 内存缓存（简单可用；如果 app 被杀会丢，但作业足够） --------

    // childId -> MotivationState
    private final Map<String, MotivationState> motivationCache = new HashMap<>();

    // childId -> total high-quality technique count
    private final Map<String, Integer> highQualityCountCache = new HashMap<>();

    // ------------------------------------------------------------------
    // 持久化相关
    // ------------------------------------------------------------------

    @Override
    protected void saveMedicineLog(Child child, MedicineLogEntry entry) {
        // 这里 child 是调用 logRescueDose / logControllerDose 时传进来的那个孩子
        if (child == null || child.getId() == null) {
            Log.w(TAG, "saveMedicineLog: child or child id is null");
            return;
        }

        String childId = child.getId();    // 例如 "testa2@child,com"

        // 写到 /users/{childId}/medicine_logs/{autoKey}
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

        // /technique_sessions/{childId}/{autoKey}
        rootRef.child("technique_sessions")
                .child(childId)
                .push()
                .setValue(session);

        // 更新内存中的高质量技巧次数
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

        // /inventory/{childId}
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

        // 1. 先看内存缓存
        MotivationState cached = motivationCache.get(childId);
        if (cached != null) {
            return cached;
        }

        // 2. 缓存里没有：先放一个“默认空 state”，保证立即有东西可用
        MotivationState state = new MotivationState();
        motivationCache.put(childId, state);

        // 3. 异步从 Firebase 读 /users/{childId}/motivation
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
                    // 第一次使用：可以选择顺便写一份默认值
                    motivationRef.setValue(toRecord(state));
                    return;
                }

                MotivationState loaded = fromRecord(record);
                // 覆盖缓存里刚才放的默认版本
                motivationCache.put(childId, loaded);
                Log.d(TAG, "loadMotivationState: loaded remote state for childId=" + childId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadMotivationState: firebase cancelled for childId=" + childId
                        + ", error=" + error.getMessage());
            }
        });

        // 4. 先返回默认 state（后面异步加载会更新缓存）
        return state;
    }
    // 将 Firebase 读出来的 DTO 转成内部使用的 MotivationState
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
                    // 未知的枚举名（老版本数据）直接忽略
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
                    // 同样忽略旧数据里的未知 key
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
                    // 忽略未知 badge
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
        ref.setValue(record);   // ✅ 现在写的是 String key 的 Map，不会再崩
    }
    private MotivationStateRecord toRecord(MotivationState state) {
        MotivationStateRecord record = new MotivationStateRecord();

        // streaks:  StreakType -> int 变成 String -> int
        for (StreakType type : StreakType.values()) {
            int value = state.getStreak(type);
            record.streaks.put(type.name(), value);

            // lastStreakDates（如果你有用到）
            String date = state.getLastStreakDate(type);
            if (date != null) {
                record.lastStreakDates.put(type.name(), date);
            }
        }

        // badges: BadgeType -> boolean 变成 String -> boolean
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

    // ------------------------------------------------------------------
    // Alerts / Notifications（先简单打 log，以后可以接 UI / 通知）
    // ------------------------------------------------------------------

    @Override
    protected void handleRescueAlertsIfNeeded(Child child,
                                              MedicineLogEntry entry) {
        // 这里先只打个 log 占位。
        // 如果你在 R4 要实现 “短时间内多次使用 rescue” 的警报，
        // 可以在这里读取最近几条记录做判断。
        if (child != null) {
            Log.d(TAG, "handleRescueAlertsIfNeeded: childId=" + child.getId());
        }
    }

    @Override
    protected void notifyLowCanister(Child child, InventoryItem item) {
        // 现在先简单打印 log，将来可以改成：
        // - 发送 Notification
        // - 在 UI 上弹出对话框
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

        // 以后可以在这里：
        // - 更新 UI（比如通过 ViewModel 发事件）
        // - 显示“恭喜获得徽章”的弹窗
        // - 发送远程通知等
    }

    // ------------------------------------------------------------------
    // 规则 / 参数
    // ------------------------------------------------------------------

    @Override
    protected double getLowCanisterThresholdPercent() {
        // 剩余剂量百分比 <= 20% 时认为“快没了”
        return 20.0;
    }

    @Override
    protected int getHighQualityTechniqueBadgeThreshold() {
        // 高质量技巧 session 达到 10 次给予一个徽章
        return 10;
    }

    @Override
    protected boolean isFirstPerfectControllerWeek(Child child,
                                                   MotivationState state) {
        // 简单规则：连续 controller 天数 >= 7 算“一周完美”。
        // 如果你以后想做更复杂（比如恰好第一次达成），
        // 可以在这里联动额外的状态。
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
