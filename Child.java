package com.example.b07project.model;
import java.time.LocalDate;
public class Child extends User{
    private String notes;

    public Child(String displayName) {
        super(displayName, UserRole.CHILD);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
