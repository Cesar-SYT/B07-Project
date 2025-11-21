package com.example.smartair.ui.mvp.login;

import android.widget.Toast;

public class LoginPresenter {
    LoginView loginview;
    LoginModel loginmodel;

    public LoginPresenter(LoginView view, LoginModel model) {
        this.loginview = view;
        this.loginmodel = model;
    }

    public void onLoginClicked(String email, String password) {

        if (email.isEmpty()) {
            loginview.showEmailEmpty("Please fill in your email");
            return;
        }

        if (password.isEmpty()) {
            loginview.showPasswordEmpty("Please fill in your password");
            return;
        }

        loginmodel.login(email, password, new LoginCallback() {
            @Override
            public void onSuccess() {
                loginview.showLoginSuccess();
            }

            @Override
            public void onFailure(String errorMessage) {
                loginview.showLoginFailure(errorMessage);
            }
        }.toString());
    }
}
