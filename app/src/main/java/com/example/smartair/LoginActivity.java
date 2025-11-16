package com.example.smartair;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        EditText edit_login_email = findViewById(R.id.edit_login_email);
        EditText edit_login_password = findViewById(R.id.edit_login_password);
        Button btn_login = findViewById(R.id.btn_login);

        btn_login.setOnClickListener(v -> {
            String email = edit_login_email.getText().toString().trim();
            String password = edit_login_password.getText().toString().trim();
            if (email.isEmpty()){
                Toast.makeText(this, "Please fill in your email", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (password.isEmpty()){
                Toast.makeText(this, "Please fill in your password", Toast.LENGTH_SHORT).show();
                return;
            }
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            // goto parent homepage
                        }
                        else{
                            Toast.makeText(this, "Invalid email/password", Toast.LENGTH_SHORT).show();
                            edit_login_password.setText("");
                            return;
                        }
                    });
        });

    }
}