package com.example.smartair.r3;

import java.util.HashMap;
import java.util.Map;

/**
 * 这个类专门用来和 Firebase 读写。
 * 所有 Map 的 key 都是 String（枚举名），Firebase 才能接受。
 */
public class MotivationStateRecord {

    public Map<String, Integer> streaks = new HashMap<>();
    public Map<String, Boolean> badges = new HashMap<>();
    public Map<String, String> lastStreakDates = new HashMap<>();

    // 必须有无参构造函数，Firebase 反序列化用
    public MotivationStateRecord() {
    }
}
