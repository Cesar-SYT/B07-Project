package com.example.smartair.login;

public interface LoginCallback {
    void onSuccess();
    void onFailure(String errorMessage);
}
