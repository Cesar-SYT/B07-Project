package com.example.smartair.ui.usermodel;

import java.util.UUID;
public abstract class User {
    private final String id;
    private String displayName;
    private final UserRole role;
    private boolean hasBeenOnboarding;
    protected User(String id, String displayName, UserRole role) {
        this.id = id;
        this.displayName = displayName;
        this.role = role;
        this.hasBeenOnboarding = false;
    }
    public String getId() {
        return id;
    }
    public UserRole getRole() {
        return role;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}