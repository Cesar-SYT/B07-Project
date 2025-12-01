package com.example.smartair.model;

public abstract class User {
    private String id;
    private String displayName;
    private UserRole role;
    private boolean hasBeenOnboarding;
    protected User(String id, String displayName, UserRole role) {
        this.id = id;
        this.displayName = displayName;
        this.role = role;
        this.hasBeenOnboarding = false;
    }

    public User(){
    }
    public void setId(String id){
        this.id = id;
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

    public boolean isHasBeenOnboarding() {
        return hasBeenOnboarding;
    }

    public void setHasBeenOnboarding(boolean hasBeenOnboarding) {
        this.hasBeenOnboarding = hasBeenOnboarding;
    }

}