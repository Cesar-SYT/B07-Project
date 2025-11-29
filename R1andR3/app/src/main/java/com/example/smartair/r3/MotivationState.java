package com.example.smartair.r3;
import java.util.EnumMap;
import java.util.Map;

/**
 * Store the streak and badge status for a specific Child.
 * True persistence may be handled by another
 * module; this is merely a memory model.
 */
public class MotivationState {

    private final Map<StreakType, Integer> streaks =
            new EnumMap<>(StreakType.class);

    private final Map<BadgeType, Boolean> badges =
            new EnumMap<>(BadgeType.class);

    public Integer getStreak(StreakType type) {
        return streaks.getOrDefault(type, 0);
    }

    public void setStreak(StreakType type, int value) {
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
}
