package com.example.smartair;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    EditText editEmail;
    EditText editLoginPassword;
    Button btnLogin;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        editEmail = view.findViewById(R.id.edit_text_email);
        editLoginPassword = view.findViewById(R.id.edit_text_password);
        btnLogin = view.findViewById(R.id.button_login);

        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editLoginPassword.getText().toString().trim();
            if (email.isEmpty()){
                Toast.makeText(requireContext(), "Please fill in your email", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (password.isEmpty()){
                Toast.makeText(requireContext(), "Please fill in your password", Toast.LENGTH_SHORT).show();
                return;
            }
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            // TODO:goto parent homepage
                        }
                        else{
                            Toast.makeText(requireContext(), "Invalid email/password", Toast.LENGTH_SHORT).show();
                            editLoginPassword.setText("");
                        }
                    });
        });
        return view;
    }
}