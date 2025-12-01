package com.example.smartair.ui.mvp.login;

public interface LoginView {
    void showEmailEmpty(String msg);

    void showPasswordEmpty(String msg);

    void showLoginSuccess();

    void showLoginFailure(String msg);
}
