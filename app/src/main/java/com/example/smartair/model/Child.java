package com.example.smartair.model;

public class Child extends User{
    private String notes;
    private String parentId;

    // To-Do 1: Add providerId field to store the linked doctor's UID
    private String providerId;

    public Child() {
        super();
    }
    public Child(String id, String displayName) {
        super(id, displayName, UserRole.CHILD);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}