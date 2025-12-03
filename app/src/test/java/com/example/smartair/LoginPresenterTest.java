package com.example.smartair;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.smartair.login.LoginCallback;
import com.example.smartair.login.LoginModel;
import com.example.smartair.login.LoginPresenter;
import com.example.smartair.login.LoginView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {

    @Mock
    LoginView view;

    @Mock
    LoginModel model;

    LoginPresenter presenter;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        presenter = new LoginPresenter(view, model);
    }

    @Test
    public void testOnResetSuccess_ShowsResetEmailSent() {
        presenter.onResetSuccess();

        verify(view).showResetEmailSent();
    }

    @Test
    public void testOnResetFailure_ShowsResetEmailError() {
        presenter.onResetFailure("Network error");

        verify(view).showResetEmailError("Network error");
    }

    @Test
    public void testOnRegisterClicked_NavigateToRegister() {
        presenter.onRegisterClicked();

        verify(view).navigateToRegister();
    }

    @Test
    public void testForgotPassword_CallsModelSendResetEmail() {
        presenter.onForgotPasswordClicked("user");

        verify(model).sendResetEmail(eq("user"), eq(presenter));
    }

    @Test
    public void testLogin_EmailEmpty_ShowEmailEmpty() {
        presenter.onLoginClicked("", "123");

        verify(view).showEmailEmpty("Please fill in your email");
        verify(model, never()).login(anyString(), anyString(), any(LoginCallback.class));
    }

    @Test
    public void testLogin_PasswordEmpty_ShowPasswordEmpty() {
        presenter.onLoginClicked("group19@mail.utoronto.ca", "");

        verify(view).showPasswordEmpty("Please fill in your password");
        verify(model, never()).login(anyString(), anyString(), any(LoginCallback.class));
    }

    @Test
    public void testLoginSuccess() {
        presenter.onLoginClicked("test@outlook.com", "123456");

        verify(model).login(eq("test@outlook.com"), eq("123456"), eq(presenter));
    }

    @Test
    public void testLoginFailure() {
        presenter.onLoginFailure("error");

        verify(view).showError("error");
    }

    @Test
    public void testLoginParentFirstTime_NavigateOnboarding() {
        presenter.onLoginSuccess("PARENT", false);

        verify(view).navigateToOnboarding();
    }

    @Test
    public void testLoginParent_NavigateParentHome() {
        presenter.onLoginSuccess("PARENT", true);

        verify(view).navigateToParentHome();
    }

    @Test
    public void testLoginChild_NavigateChildHome() {
        presenter.onLoginSuccess("CHILD", true);

        verify(view).navigateToChildHome();
    }

    @Test
    public void testLoginProvider_NavigateChildHome() {
        presenter.onLoginSuccess("PROVIDER", true);

        verify(view).navigateToProviderHome();
    }

    @Test
    public void testUnknownRole_ShowsError() {
        presenter.onLoginSuccess("BANANA", true);

        verify(view).showError("Unknown user role");
    }


}
