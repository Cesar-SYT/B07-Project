package com.example.b07project.model;
import java.util.UUID;
public abstract class User {
    private final String id;
    private String displayName;
    private final UserRole role;
    protected User(String displayName, UserRole role) {
        this.id = UUID.randomUUID().toString();
        this.displayName = displayName;
        this.role = role;
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
