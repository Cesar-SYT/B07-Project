package com.example.smartair.model;

public class Child extends User{
    private String notes;
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

}
