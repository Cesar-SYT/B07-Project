package com.example.smartair.ui.data;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String userType; // can either be "child", "parent", or "provider"

    public User(){
    }

    public User(String uid, String fullName, String email, String userType){
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
    }

    public String getUid() {
        return uid;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getUserType() {
        return userType;
    }
}
