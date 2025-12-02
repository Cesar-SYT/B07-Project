package com.example.smartair.r3;

public class Badge {
    private String id;
    private String title;
    private boolean unlocked;

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
