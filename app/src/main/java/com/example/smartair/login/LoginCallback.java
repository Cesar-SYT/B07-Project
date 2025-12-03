package com.example.smartair.login;

public interface LoginCallback {
    void onLoginSuccess(String role, boolean hasBeenOnboarding);
    void onLoginFailure(String errorMsg);

    void onResetSuccess();
    void onResetFailure(String errorMsg);
}
