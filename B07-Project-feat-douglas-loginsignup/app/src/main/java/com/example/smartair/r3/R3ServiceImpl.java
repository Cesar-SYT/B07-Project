package com.example.smartair.r3;

import android.util.Log;

import com.example.smartair.model.Child;
import com.example.smartair.model.Parent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
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
            FirebaseDatabase.getInstance().getReference();

    // -------- 内存缓存（简单可用；如果 app 被杀会丢，但作业足够） --------

    // childId -> MotivationState
    private final Map<String, MotivationState> motivationCache = new HashMap<>();

    // childId -> total high-quality technique count
    private final Map<String, Integer> highQualityCountCache = new HashMap<>();

    // ------------------------------------------------------------------
    // 持久化相关
    // ------------------------------------------------------------------

    @Override
    protected void saveMedicineLog(MedicineLogEntry entry) {
        Child child = entry.getChild();
        if (child == null) {
            Log.w(TAG, "saveMedicineLog: child is null");
            return;
        }
        String childId = child.getId();

        // /medicine_logs/{childId}/{autoKey}
        rootRef.child("medicine_logs")
                .child(childId)
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
        if (child == null) {
            Log.w(TAG, "loadMotivationState: child is null, return new state");
            return new MotivationState();
        }
        String childId = child.getId();

        MotivationState state = motivationCache.get(childId);
        if (state == null) {
            state = new MotivationState();
            motivationCache.put(childId, state);
        }

        // 这里没有从 Firebase 异步读取，只用内存缓存；
        // 如果以后要从云端恢复，可以在这里加 listener。
        return state;
    }

    @Override
    protected void saveMotivationState(Child child, MotivationState state) {
        if (child == null) {
            Log.w(TAG, "saveMotivationState: child is null");
            return;
        }
        String childId = child.getId();

        motivationCache.put(childId, state);

        // /motivation/{childId}
        rootRef.child("motivation")
                .child(childId)
                .setValue(state);

        Log.d(TAG, "saveMotivationState: saved for childId=" + childId);
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
    protected void notifyLowCanister(Parent parent, InventoryItem item) {
        // 现在先简单打印 log，将来可以改成：
        // - 发送 Notification
        // - 在 UI 上弹出对话框
        String parentName = parent != null ? parent.getDisplayName() : "unknown";
        String medName = item != null ? item.getName() : "unknown";
        Log.d(TAG, "notifyLowCanister: parent=" + parentName + ", med=" + medName);
    }

    @Override
    protected void notifyExpiredMedication(Parent parent, InventoryItem item) {
        String parentName = parent != null ? parent.getDisplayName() : "unknown";
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
}
