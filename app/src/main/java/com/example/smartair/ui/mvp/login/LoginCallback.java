package com.example.smartair.ui.mvp.login;

public interface LoginCallback {
    void onSuccess();
    void onFailure(String errorMessage);
}
