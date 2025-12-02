package com.example.smartair.login;

public interface LoginView {

    void showEmailEmpty(String msg);
    void showPasswordEmpty(String msg);
    void showError(String msg);

    void showResetEmailSent();
    void showResetEmailError(String msg);

    void navigateToRegister();
    void navigateToOnboarding();
    void navigateToParentHome();
    void navigateToChildHome();
    void navigateToProviderHome();
}
