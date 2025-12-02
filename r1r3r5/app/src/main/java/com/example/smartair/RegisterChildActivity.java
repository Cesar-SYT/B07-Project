package com.example.smartair;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.model.Child;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterChildActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etUsername, etPassword;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef; // 统一使用这个引用

    // 显式指定数据库 URL，防止实例不一致
    private static final String DATABASE_URL = "https://smart-air-61888-default-rtdb.firebaseio.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register_child);

        // 绑定 UI
        etFullName = findViewById(R.id.edit_text_full_name);
        etUsername = findViewById(R.id.edit_text_email);
        etPassword = findViewById(R.id.edit_text_password);
        btnRegister = findViewById(R.id.button_submit_registration);

        // Firebase 初始化
        mAuth = FirebaseAuth.getInstance();
        // 统一使用带 URL 的实例，确保读写同一个库
        usersRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users");

        btnRegister.setOnClickListener(v -> registerChild());
    }

    private void registerChild() {
        FirebaseUser parentUser = mAuth.getCurrentUser();
        if (parentUser == null) {
            Toast.makeText(this, "Parent not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 提前获取父账号 ID，因为 createUser 后 mAuth 会切换到子账号
        String parentUid = parentUser.getEmail().replace(".", ",");

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
        // 警告：成功后，当前 App 的登录状态会切换为新注册的孩子账号！
        mAuth.createUserWithEmailAndPassword(firebaseEmail, password)
                .addOnCompleteListener(RegisterChildActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            FirebaseUser childUser = task.getResult().getUser();
                            if (childUser == null) return;
                            
                            String uid = childUser.getEmail().replace(".", ",");

                            // database 存孩子信息
                            Child child = new Child(
                                    username,
                                    fullName
                            );
                            
                            // Store parent ID in child's node (Requires Child.java update)
                            child.setParentId(parentUid);

                            // 1. 写入孩子信息 (users -> childUid)
                            usersRef.child(uid)
                                    .setValue(child)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // 2. 将孩子关联到父账号 (users -> parentUid -> childid -> childUid)
                                            // 注意：如果 Security Rules 限制只能写自己的节点，这里可能会失败，
                                            // 因为此时 Auth 已经是 Child 了。
                                            usersRef.child(parentUid)
                                                    .child("childid")
                                                    .child(uid)
                                                    .setValue(true)
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            Toast.makeText(RegisterChildActivity.this,
                                                                    "Child registered successfully",
                                                                    Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        } else {
                                                            String error = task2.getException() != null ? task2.getException().getMessage() : "Unknown error";
                                                            Toast.makeText(RegisterChildActivity.this,
                                                                    "Failed to link child to parent: " + error,
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        } else {
                                            String error = task1.getException() != null ? task1.getException().getMessage() : "Unknown error";
                                            Toast.makeText(RegisterChildActivity.this,
                                                    "Database error: " + error,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(RegisterChildActivity.this,
                                    "Auth error: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}