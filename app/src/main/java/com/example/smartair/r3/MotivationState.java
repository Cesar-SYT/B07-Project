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

    private final Map<StreakType, Integer> streaks =
            new EnumMap<>(StreakType.class);

    private final Map<BadgeType, Boolean> badges =
            new EnumMap<>(BadgeType.class);

    private final Map<StreakType, String> lastStreakDates =
            new EnumMap<>(StreakType.class);

    /**
     * all streaks are 0, all badges are false
     */
    public MotivationState() {
        for (StreakType type : StreakType.values()) {
            streaks.put(type, 0);
        }
        for (BadgeType type : BadgeType.values()) {
            badges.put(type, false);
        }
    }


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


    public Map<StreakType, Integer> getAllStreaks() {
        return Collections.unmodifiableMap(streaks);
    }

    public Map<BadgeType, Boolean> getAllBadges() {
        return Collections.unmodifiableMap(badges);
    }

    public void resetAllStreaks() {
        for (StreakType type : StreakType.values()) {
            streaks.put(type, 0);
        }
    }

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
