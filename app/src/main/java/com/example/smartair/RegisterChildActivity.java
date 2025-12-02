package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.smartair.model.Child;

public class RegisterChildActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etUsername, etPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference childrenRef;

    private String parentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register_child);

        etFullName = findViewById(R.id.edit_text_full_name);
        etUsername = findViewById(R.id.edit_text_email);
        etPassword = findViewById(R.id.edit_text_password);
        btnRegister = findViewById(R.id.button_submit_registration);

        mAuth = FirebaseAuth.getInstance();
        childrenRef = FirebaseDatabase.getInstance().getReference("users");
        parentUid = getIntent().getStringExtra("PARENT_UID");

        //Toast.makeText(this, "Parent ID: " + parentUid, Toast.LENGTH_LONG).show();

        btnRegister.setOnClickListener(v -> registerChild());
    }

    private void registerChild() {

        final String fullName = etFullName.getText().toString().trim();
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        final String firebaseEmail = username + "@child.com";

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name required");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(firebaseEmail, password)
                .addOnCompleteListener(RegisterChildActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            String uid = task.getResult().getUser().getEmail().replace(".",",");

                            Child child = new Child(
                                    username,
                                    fullName
                            );
                            FirebaseDatabase.getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                                    .getReference("users")
                                    .child(uid)
                                    .setValue(child)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            FirebaseDatabase.getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                                                    .getReference("users")
                                                    .child(uid)
                                                    .child("parentId")
                                                    .setValue(parentUid);
                                            FirebaseDatabase.getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                                                    .getReference("users")
                                                    .child(parentUid)
                                                    .child("childid")
                                                    .child(uid)
                                                    .setValue(true)
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            Toast.makeText(RegisterChildActivity.this,
                                                                    "Child registered successfully",
                                                                    Toast.LENGTH_SHORT).show();
                                                            android.content.SharedPreferences prefs =
                                                                    getSharedPreferences("auth_prefs", MODE_PRIVATE);
                                                            String parentEmail    = prefs.getString("parent_email", null);
                                                            String parentPassword = prefs.getString("parent_password", null);

                                                            com.google.firebase.auth.FirebaseAuth auth =
                                                                    com.google.firebase.auth.FirebaseAuth.getInstance();
                                                            auth.signOut();

                                                            if (parentEmail != null && parentPassword != null) {
                                                                auth.signInWithEmailAndPassword(parentEmail, parentPassword)
                                                                        .addOnCompleteListener(loginBackTask -> {
                                                                            if (loginBackTask.isSuccessful()) {
                                                                                Intent intent = new Intent(
                                                                                        RegisterChildActivity.this,
                                                                                        com.example.smartair.ManageChildrenActivity.class
                                                                                );
                                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            } else {
                                                                                Intent intent = new Intent(
                                                                                        RegisterChildActivity.this,
                                                                                        MainActivity.class
                                                                                );
                                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }
                                                                        });
                                                            } else {
                                                                Intent intent = new Intent(RegisterChildActivity.this, MainActivity.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        } else {
                                                            Toast.makeText(RegisterChildActivity.this,
                                                                    "Failed to link child to parent: " +
                                                                            task2.getException().getMessage(),
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(RegisterChildActivity.this,
                                                    "Database error: " +
                                                            task1.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterChildActivity.this,
                                    "Auth error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
