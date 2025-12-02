package com.example.smartair.r3;

import java.util.HashMap;
import java.util.Map;


public class MotivationStateRecord {

    public Map<String, Integer> streaks = new HashMap<>();
    public Map<String, Boolean> badges = new HashMap<>();
    public Map<String, String> lastStreakDates = new HashMap<>();

    public MotivationStateRecord() {
    }
}
