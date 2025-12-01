package com.example.smartair.mvp.login;

import com.example.smartair.ui.mvp.login.LoginModel;
import com.example.smartair.ui.mvp.login.LoginPresenter;
import com.example.smartair.ui.mvp.login.LoginView;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {
    @Mock
    LoginView view;

    @Mock
    LoginModel model;

    LoginPresenter presenter;
}
