package com.example.smartair.r3;

public class BreathStatus {
    public enum Feeling {
        BETTER,
        SAME,
        WORSE
    }
    private final Feeling feeling;
    /**
     * Simple breathing score, such as
     * integers from 1 to 5 or 1 to 10
     */
    private final int breathRating;

    public BreathStatus(Feeling feeling, int breathRating) {
        this.feeling = feeling;
        this.breathRating = breathRating;
    }

    public Feeling getFeeling() {
        return feeling;
        /*
         * have options on the screen that
         * can only be chosen from BETTER SAME
         * and WORSE.
         */

    }

    public int getBreathRating() {
        return breathRating;
    }
}
