package com.example.smartair;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register_child); // 使用你的 XML

        // 绑定 UI
        etFullName = findViewById(R.id.edit_text_full_name);
        etUsername = findViewById(R.id.edit_text_email);  // 这里改当 username 输入框
        etPassword = findViewById(R.id.edit_text_password);
        btnRegister = findViewById(R.id.button_submit_registration);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        childrenRef = FirebaseDatabase.getInstance().getReference("users");

        btnRegister.setOnClickListener(v -> registerChild());
    }

    private void registerChild() {
        String parentUid = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        final String fullName = etFullName.getText().toString().trim();
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        // 使用 @child.com
        final String firebaseEmail = username + "@child.com";

        // 输入校验
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

        // Firebase Auth 注册
        mAuth.createUserWithEmailAndPassword(firebaseEmail, password)
                .addOnCompleteListener(RegisterChildActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            String uid = task.getResult().getUser().getEmail().replace(".",",");

                            // database 存孩子信息（不存 email）
                            Child child = new Child(
                                    username,
                                    fullName
                            );
                            FirebaseDatabase.getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                                    .getReference("users")
                                    .child(uid)                // ★ 孩子的 uid
                                    .setValue(child)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            FirebaseDatabase.getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                                                    .getReference("users")
                                                    .child(uid)
                                                    .child("parentId")
                                                    .setValue(parentUid);
                                            FirebaseDatabase.getInstance()
                                                    .getReference("users")
                                                    .child(parentUid)
                                                    .child("childid")
                                                    .child(uid)     // ★ 每个 child 一个 key
                                                    .setValue(true)
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            Toast.makeText(RegisterChildActivity.this,
                                                                    "Child registered successfully",
                                                                    Toast.LENGTH_SHORT).show();
                                                            finish();
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

    // 孩子数据结构
}
