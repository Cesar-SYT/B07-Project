package com.example.smartair.r3;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Store the streak and badge status for a specific Child.
 * True persistence may be handled by another module;
 * this is merely an in-memory model.
 */
public class MotivationState {

    // 用 EnumMap 性能会比较好，key 是枚举
    private final Map<StreakType, Integer> streaks =
            new EnumMap<>(StreakType.class);

    private final Map<BadgeType, Boolean> badges =
            new EnumMap<>(BadgeType.class);

    private final Map<StreakType, String> lastStreakDates =
            new EnumMap<>(StreakType.class);

    /**
     * 默认构造：把所有 streak 初始化为 0，所有 badge 初始化为 false
     * 这样外面直接 new MotivationState() 就是一个干净的初始状态
     */
    public MotivationState() {
        for (StreakType type : StreakType.values()) {
            streaks.put(type, 0);
        }
        for (BadgeType type : BadgeType.values()) {
            badges.put(type, false);
        }
    }

    /* ------------ 你原来的 API：保持不变 ------------ */

    public Integer getStreak(StreakType type) {
        return streaks.getOrDefault(type, 0);
    }

    public void setStreak(StreakType type, int value) {
        if (value < 0) value = 0;
        streaks.put(type, value);
    }

    public void resetStreak(StreakType type) {
        streaks.put(type, 0);
    }

    public Boolean hasBadge(BadgeType type) {
        return badges.getOrDefault(type, false);
    }

    public void awardBadge(BadgeType type) {
        badges.put(type, true);
    }

    /* ------------ 新增的辅助方法（不破坏旧功能） ------------ */

    /**
     * 只读地拿到所有 streak，给 UI / 调试用
     */
    public Map<StreakType, Integer> getAllStreaks() {
        return Collections.unmodifiableMap(streaks);
    }

    /**
     * 只读地拿到所有 badge 状态
     */
    public Map<BadgeType, Boolean> getAllBadges() {
        return Collections.unmodifiableMap(badges);
    }

    /**
     * 把所有 streak 清零（比如用户断更很久的时候重置）
     */
    public void resetAllStreaks() {
        for (StreakType type : StreakType.values()) {
            streaks.put(type, 0);
        }
    }

    /**
     * 清除所有 badge（通常不会用到，只是工具方法）
     */
    public void clearAllBadges() {
        for (BadgeType type : BadgeType.values()) {
            badges.put(type, false);
        }
    }
    public String getLastStreakDate(StreakType type) {
        return lastStreakDates.get(type);
    }

    public void setLastStreakDate(StreakType type, String dateIso) {
        lastStreakDates.put(type, dateIso);
    }
}
