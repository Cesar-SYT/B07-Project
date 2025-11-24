package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Simulate checking for logged in user
        // If logged in -> navigate to SafetyAndControl directly
        // For demo purposes, we wait for button click

        btnLogin = findViewById(R.id.btn_login); // Note: btn_login is in content_main, but included in activity_main via include
        // Since activity_main uses CoordinatorLayout and includes content_main, normal findViewById works 
        // if the ID is unique in the hierarchy. 
        
        // However, since we modified content_main.xml in the previous step to have btn_login, 
        // and we must ensure activity_main.xml includes it correctly.
        
        // To be safe and simple given the previous edits:
        // We will assume the view hierarchy is flattened or accessible.
        
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                // Simulate Login Logic
                Toast.makeText(MainActivity.this, "Logging in...", Toast.LENGTH_SHORT).show();
                
                // Navigate to SafetyAndControl
                Intent intent = new Intent(MainActivity.this, SafetyAndControl.class);
                startActivity(intent);
                // finish(); // Optional: close Main so user can't go back to login easily
            });
        }
    }
}