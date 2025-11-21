package com.example.smartair.mvp.login;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {
    @Mock
    LoginView view;

    @Mock
    LoginModel model;

    LoginPresenter presenter;
}
