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

/**
 * R3: Medicines, Technique & Motivation 的核心业务逻辑入口。
 *
 * 说明：
 * - 这里不直接访问数据库 / UI，而是通过一组 protected abstract 方法交给子类实现
 *   （比如 R3ServiceImpl 里用 Firebase 实现）。
 * - 为了兼容低版本 Android，这里统一使用 java.util.Date，而不是 LocalDate / LocalDateTime。
 */
public abstract class R3Service {

    // ---------------------------------------------------------------------
    // Medicines 部分：记录 rescue / controller 用药
    // ---------------------------------------------------------------------
    private final Map<String, MotivationState> motivationCache = new HashMap<>();

    private final DatabaseReference rootRef = FirebaseDatabase.getInstance(
            "https://smart-air-61888-default-rtdb.firebaseio.com/"
    ).getReference();;

    /**
     * 记录一次 rescue 用药。
     */
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

        // 如果今天用了 rescue → streak 归零
        if (entry.getType() == MedicineType.RESCUE) {
            state.resetStreak(StreakType.RESCUE_LOW_MONTH);
            saveMotivationState(child, state);
            return;
        }

        // 到这里说明今天没有用 rescue
        int streak = state.getStreak(StreakType.RESCUE_LOW_MONTH);
        streak += 1;
        state.setStreak(StreakType.RESCUE_LOW_MONTH, streak);

        // 满 30 天 → 解锁 badge
        if (streak >= 30 && !state.hasBadge(BadgeType.LOW_RESCUE_MONTH)) {
            state.awardBadge(BadgeType.LOW_RESCUE_MONTH);
            onBadgeAwarded(child, BadgeType.LOW_RESCUE_MONTH);
        }

        saveMotivationState(child, state);
    }

    /** 记录一次 CONTROLLER 用药 */
    public void logControllerDose(Child child, int doseCount, int breathRate, String feel) {
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());
        MedicineLogEntry entry = new MedicineLogEntry(
                doseCount, breathRate, feel, timeStr, MedicineType.CONTROLLER);
        saveMedicineLog(child, entry);
        updateControllerStreak(child, true);
    }

    // ---------------------------------------------------------------------
    // Technique Helper 部分：记录技巧练习 session
    // ---------------------------------------------------------------------

    /**
     * 完成一次 Technique Helper session。
     */
    public void completeTechniqueSession(TechniqueSession session) {
        saveTechniqueSession(session);

        if (session.isHighQuality()) {
            onHighQualityTechniqueSession(session.getChild());
        }
    }

    // ---------------------------------------------------------------------
    // Inventory 部分：库存更新 & 过期提醒
    // ---------------------------------------------------------------------

    /**
     * 更新某个孩子某个药物的剩余剂量，并根据剩余量和过期时间触发提醒。
     *
     * @param today 调用处传入 “今天” 的日期（new Date() 即可）
     */
    public void updateInventory(Child child,
                                InventoryItem item,
                                int newRemainingDoses,
                                Date today) {

        item.setRemainingDoses(newRemainingDoses);
        saveInventoryItem(item);

        // 低库存提醒
        if (item.isLowCanister(getLowCanisterThresholdPercent())) {
            notifyLowCanister(child, item);
        }

        // 过期提醒
        if (item.isExpired(today)) {
            notifyExpiredMedication(child, item);
        }
    }

    // ---------------------------------------------------------------------
    // Motivation 部分：streaks & badges 逻辑
    // ---------------------------------------------------------------------

    /**
     * 高质量技巧 session 发生时调用：
     * - 技巧 streak +1
     * - 判断是否达到 “10 high-quality technique sessions” badge
     */
    protected void onHighQualityTechniqueSession(Child child) {
        MotivationState state = loadMotivationState(child);

        // 技巧 streak +1
        int currentStreak =
                state.getStreak(StreakType.TECHNIQUE_COMPLETED_DAYS);
        int newStreak = currentStreak + 1;
        state.setStreak(StreakType.TECHNIQUE_COMPLETED_DAYS, newStreak);

        // 检查 “10 high-quality technique sessions” badge
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

        // ⭐ 同样更新缓存
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



    /**
     * 根据今天有没有吃 controller 更新 streak；并判断是否达成
     * “first perfect controller week” badge。
     *
     * @param takenToday 今天是否按计划吃 controller
     */
    protected void updateControllerStreak(Child child, boolean takenToday) {
        if (!takenToday) {
            return;
        }

        MotivationState state = loadMotivationState(child);

        LocalDate today = LocalDate.now();
        String todayStr = today.toString();  // 例如 "2025-11-30"

        // 上一次已经记录 streak 的日期
        String lastDateStr = state.getLastStreakDate(StreakType.CONTROLLER_DAYS);

        // 1. 如果今天已经记过 streak，就什么都不做（避免一天加多次）
        if (todayStr.equals(lastDateStr)) {
            return;
        }

        int newStreak;

        if (lastDateStr == null) {
            // 第一次有记录
            newStreak = 1;
        } else {
            LocalDate lastDate = LocalDate.parse(lastDateStr);

            if (lastDate.plusDays(1).isEqual(today)) {
                // 昨天也吃了，连续 +1
                int streak = state.getStreak(StreakType.CONTROLLER_DAYS);
                newStreak = streak + 1;
            } else {
                // 中间断了一天或以上，重新从 1 开始
                newStreak = 1;
            }
        }

        // 更新 streak 和“最后更新日期”
        state.setStreak(StreakType.CONTROLLER_DAYS, newStreak);
        state.setLastStreakDate(StreakType.CONTROLLER_DAYS, todayStr);

        // 是否达到 “first perfect controller week”
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

        // 内存缓存里也更新一下，保证当前进程内两边看到同一份 state
        motivationCache.put(childId, state);

        DatabaseReference baseRef = rootRef.child("users")
                .child(childId)
                .child("motivation");

        // 只更新 CONTROLLER_DAYS 这一个 streak
        int controllerDays = state.getStreak(StreakType.CONTROLLER_DAYS);
        baseRef.child("streaks")
                .child(StreakType.CONTROLLER_DAYS.name())
                .setValue(controllerDays);

        // 只更新 FIRST_PERFECT_CONTROLLER_WEEK 这个 badge
        boolean hasControllerBadge =
                state.hasBadge(BadgeType.FIRST_PERFECT_CONTROLLER_WEEK);
        baseRef.child("badges")
                .child(BadgeType.FIRST_PERFECT_CONTROLLER_WEEK.name())
                .setValue(hasControllerBadge);
    }


    // ---------------------------------------------------------------------
    // 抽象方法：交给具体实现类（比如 R3ServiceImpl）去实现
    // ---------------------------------------------------------------------

    // 持久化 / 读取

    /**
     * 保存一次用药记录（写数据库 / Firebase / 本地文件等）。
     */
    protected abstract void saveMedicineLog(Child child, MedicineLogEntry entry);

    /**
     * 保存一次技巧 session。
     */
    protected abstract void saveTechniqueSession(TechniqueSession session);

    /**
     * 保存库存信息。
     */
    protected abstract void saveInventoryItem(InventoryItem item);

    /**
     * 读取某个 child 的 MotivationState。
     * 如果没有记录，可以由实现类创建一个新的默认对象返回。
     */
    protected abstract MotivationState loadMotivationState(Child child);

    /**
     * 保存 MotivationState。
     */
    protected abstract void saveMotivationState(Child child,
                                                MotivationState state);

    /**
     * 获取某个 child 至今高质量技巧 session 的总次数。
     */
    protected abstract int getTotalHighQualityTechniqueCount(Child child);

    // Alert / 通知 / UI 更新

    /**
     * 当记录 rescue 用药后，检查是否需要触发 “重复使用 rescue” 的警报。
     */
    protected abstract void handleRescueAlertsIfNeeded(Child child,
                                                       MedicineLogEntry entry);

    /**
     * 低库存提醒：由实现类决定是 toast / notification / 更新 UI。
     */
    protected abstract void notifyLowCanister(Child child,
                                              InventoryItem item);

    /**
     * 药物过期提醒。
     */
    protected abstract void notifyExpiredMedication(Child child,
                                                    InventoryItem item);

    /**
     * 当某个 badge 被授予时调用，例如弹窗、播放动画等。
     */
    protected abstract void onBadgeAwarded(Child child,
                                           BadgeType badgeType);

    // 配置项 / 规则

    /**
     * 低库存阈值，例如 20 表示 ≤20% 时认为快没了。
     */
    protected abstract double getLowCanisterThresholdPercent();

    /**
     * 达到多少次高质量技巧 session 赠送 TEN_HIGH_QUALITY_TECHNIQUE_SESSIONS badge。
     */
    protected abstract int getHighQualityTechniqueBadgeThreshold();

    /**
     * 判断当前 MotivationState 是否代表 “第一周完美 controller 一周”。
     * 实现类可以根据 streak 或历史记录做更精细的判断。
     */
    protected abstract boolean isFirstPerfectControllerWeek(Child child,
                                                            MotivationState state);

}
