package com.example.smartair.login;

public class LoginPresenter implements LoginCallback {

    private LoginView loginview;
    private LoginModel loginmodel;

    public LoginPresenter(LoginView view, LoginModel model) {
        this.loginview = view;
        this.loginmodel = model;
    }

    @Override
    public void onResetSuccess() {
        loginview.showResetEmailSent();
    }

    @Override
    public void onResetFailure(String errorMsg) {
        loginview.showResetEmailError(errorMsg);
    }

    @Override
    public void onLoginSuccess(String role, boolean hasBeenOnboarding) {
        if (!hasBeenOnboarding) {
            loginview.navigateToOnboarding();
            return;
        }

        if ("PARENT".equals(role)) {
            loginview.navigateToParentHome();
        }

        else if ("CHILD".equals(role)) {
            loginview.navigateToChildHome();
        }

        else if ("PROVIDER".equals(role)) {
            loginview.navigateToProviderHome();
        }

        else {
            loginview.showError("Unknown user role");
        }
    }

    @Override
    public void onLoginFailure(String errorMsg) {
        loginview.showError(errorMsg);
    }

    public void onRegisterClicked() {
        loginview.navigateToRegister();
    }

    public void onForgotPasswordClicked(String email) {
        loginmodel.sendResetEmail(email, this);
    }

    public void onLoginClicked(String email, String password) {

        if (email.trim().isEmpty()) {
            loginview.showEmailEmpty("Please fill in your email");
            return;
        }

        if (password.trim().isEmpty()) {
            loginview.showPasswordEmpty("Please fill in your password");
            return;
        }

        // correct: pass THIS (presenter), not toString()!!
        loginmodel.login(email, password, this);
    }

    public void detach() {
        loginview = null;
    }
}
