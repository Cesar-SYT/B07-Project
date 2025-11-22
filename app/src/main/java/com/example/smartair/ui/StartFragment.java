package com.example.smartair.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.smartair.R;

public class StartFragment extends Fragment {

    Button btnLogin;
    Button btnRegister;

    public StartFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);

        btnLogin = view.findViewById(R.id.button_login);
        btnRegister = view.findViewById(R.id.button_register);

        btnLogin.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_startFragment_to_loginFragment);
        });

        btnRegister.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_startFragment_to_roleSelectionFragment);
        });
        return view;
    }
}