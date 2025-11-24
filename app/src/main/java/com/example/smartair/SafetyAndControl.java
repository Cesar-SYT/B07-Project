package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SafetyAndControl extends AppCompatActivity {

    // UI Components for PEF Log
    private SwitchCompat switchPefMode;
    private EditText etPefValue;
    private LinearLayout layoutPostPef;
    private EditText etPostPefValue;
    private Button btnSavePef;
    
    // Top Bar Button
    private ImageButton btnPefHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Commenting out to prevent potential issues if theme/styles aren't perfectly aligned
        setContentView(R.layout.activity_safety_and_control);
        
        setupWindowInsets();
        initializeTopBarActions();
        initializeTriageButton();
        initializePefLogging();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void initializeTopBarActions() {
        btnPefHistory = findViewById(R.id.btn_pef_history);
        if (btnPefHistory != null) {
            btnPefHistory.setOnClickListener(v -> {
                // Navigate to PEF History (DisplayPEF activity)
                Intent intent = new Intent(SafetyAndControl.this, DisplayPEF.class);
                startActivity(intent);
            });
        }
    }

    private void initializeTriageButton() {
        Button btnOneTapTriage = findViewById(R.id.btn_one_tap_triage);
        if (btnOneTapTriage != null) {
             btnOneTapTriage.setOnClickListener(v -> {
                Intent intent = new Intent(SafetyAndControl.this, TriageDetail.class);
                startActivity(intent);
            });
        }
    }

    private void initializePefLogging() {
        switchPefMode = findViewById(R.id.switch_pef_mode);
        etPefValue = findViewById(R.id.et_pef_value);
        layoutPostPef = findViewById(R.id.layout_post_pef);
        etPostPefValue = findViewById(R.id.et_post_pef_value);
        btnSavePef = findViewById(R.id.btn_save_pef);

        if (switchPefMode != null) {
            // Switch change listener to toggle modes
            switchPefMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Mode: Compare Pre/Post Meds
                    if (etPefValue != null) etPefValue.setHint("Pre-Med Value");
                    if (layoutPostPef != null) layoutPostPef.setVisibility(View.VISIBLE);
                } else {
                    // Mode: Single Value
                    if (etPefValue != null) etPefValue.setHint("Enter Value");
                    if (layoutPostPef != null) layoutPostPef.setVisibility(View.GONE);
                }
            });
        }

        if (btnSavePef != null) {
            // Save button logic
            btnSavePef.setOnClickListener(v -> savePefLog());
        }
    }

    private void savePefLog() {
        if (etPefValue == null) return;

        String pefVal = etPefValue.getText().toString().trim();
        boolean isCompareMode = switchPefMode != null && switchPefMode.isChecked();

        if (pefVal.isEmpty()) {
            etPefValue.setError("Please enter a value");
            return;
        }

        if (isCompareMode) {
            if (etPostPefValue == null) return;
            
            String postPefVal = etPostPefValue.getText().toString().trim();
            if (postPefVal.isEmpty()) {
                etPostPefValue.setError("Please enter post-med value");
                return;
            }
            // Save Pre & Post logic here
            Toast.makeText(this, "Saved: Pre=" + pefVal + ", Post=" + postPefVal, Toast.LENGTH_SHORT).show();
        } else {
            // Save Single logic here
            Toast.makeText(this, "Saved: PEF=" + pefVal, Toast.LENGTH_SHORT).show();
        }
        
        // Clear fields after save
        etPefValue.setText("");
        if (etPostPefValue != null) etPostPefValue.setText("");
    }
}