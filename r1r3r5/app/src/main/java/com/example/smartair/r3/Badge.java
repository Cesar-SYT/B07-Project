package com.example.smartair.r3;

public class Badge {
    private String id;       // 用来区分不同 badge，比如 "badge_10_technique"
    private String title;    // UI 显示的名字
    private boolean unlocked;

    // Firebase 反序列化需要无参构造
    public Badge() {
    }

    public Badge(String id, String title, boolean unlocked) {
        this.id = id;
        this.title = title;
        this.unlocked = unlocked;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
