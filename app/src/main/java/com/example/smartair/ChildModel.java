package com.example.smartair;

/**
 * Model class representing a child in the Parent's view.
 */
public class ChildModel {
    private String childId;
    private String name;
    private String dob; // Date of Birth (yyyy-MM-dd)
    private int age;
    private String avatarUrl; // For future profile picture loading

    public ChildModel(String childId, String name, String dob, int age, String avatarUrl) {
        this.childId = childId;
        this.name = name;
        this.dob = dob;
        this.age = age;
        this.avatarUrl = avatarUrl;
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}