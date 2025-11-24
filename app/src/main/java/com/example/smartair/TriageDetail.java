package com.example.smartair;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class TriageDetail extends AppCompatActivity {

    // UI Components
    private Button btnEmergencyTop;
    private TextView timerTextView;
    private Button btnResetTimer;
    private CheckBox cbCantSpeak, cbChestPulling;
    private NumberPicker npEmergencyVisits;
    private Spinner spinnerEmergencyType;
    private EditText etCurrentPEF;
    private Spinner spinnerSymptomDuration;
    private Button btnCallEmergencyBottom;
    private Button btnHomeSteps;

    // Timer
    private CountDownTimer countDownTimer;
    private static final long START_TIME_IN_MILLIS = 600000; // 10 minutes
    private long timeLeftInMillis = START_TIME_IN_MILLIS;
    private boolean isTimerRunning;
    private boolean isTimerFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage_detail);

        initializeViews();
        setupAdapters();
        setupListeners();
        startTimer();
    }

    private void initializeViews() {
        btnEmergencyTop = findViewById(R.id.btnEmergencyTop);
        timerTextView = findViewById(R.id.timerTextView);
        btnResetTimer = findViewById(R.id.btnResetTimer);
        cbCantSpeak = findViewById(R.id.cbCantSpeak);
        cbChestPulling = findViewById(R.id.cbChestPulling);
        npEmergencyVisits = findViewById(R.id.npEmergencyVisits);
        spinnerEmergencyType = findViewById(R.id.spinnerEmergencyType);
        etCurrentPEF = findViewById(R.id.etCurrentPEF);
        spinnerSymptomDuration = findViewById(R.id.spinnerSymptomDuration);
        btnCallEmergencyBottom = findViewById(R.id.btnCallEmergencyBottom);
        btnHomeSteps = findViewById(R.id.btnHomeSteps);

        // Setup NumberPicker: 0-10
        npEmergencyVisits.setMinValue(0);
        npEmergencyVisits.setMaxValue(10);
        npEmergencyVisits.setValue(0);
    }

    private void setupAdapters() {
        // Emergency Type Spinner
        String[] emergencyTypes = {"None", "Inhaler use", "Chest compressions", "Oxygen supply"};
        ArrayAdapter<String> emergencyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, emergencyTypes);
        emergencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmergencyType.setAdapter(emergencyAdapter);

        // Symptom Duration Spinner
        String[] durations = {"< 1 hour", "1-3 hours", "3-6 hours", "> 6 hours"};
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, durations);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSymptomDuration.setAdapter(durationAdapter);
    }

    private void setupListeners() {
        // CheckBoxes for Critical Signs
        CompoundButton.OnCheckedChangeListener criticalCheckListener = (buttonView, isChecked) -> {
            updateEmergencyVisibility();
            // If timer is finished and user checks a critical flag, alert immediately
            if (isChecked && isTimerFinished) {
                showCriticalAlert();
            }
        };
        cbCantSpeak.setOnCheckedChangeListener(criticalCheckListener);
        cbChestPulling.setOnCheckedChangeListener(criticalCheckListener);

        // Timer Reset
        btnResetTimer.setOnClickListener(v -> resetTimer());

        // Call Emergency Actions
        View.OnClickListener callEmergencyListener = v -> {
            // Placeholder for actual call logic
            Toast.makeText(TriageDetail.this, "Calling Emergency Services...", Toast.LENGTH_LONG).show();
            // Example integration:
            // Intent intent = new Intent(Intent.ACTION_DIAL);
            // intent.setData(android.net.Uri.parse("tel:112"));
            // startActivity(intent);
        };
        btnEmergencyTop.setOnClickListener(callEmergencyListener);
        btnCallEmergencyBottom.setOnClickListener(callEmergencyListener);

        // Home Steps Action (Green Button)
        btnHomeSteps.setOnClickListener(v -> {
            // Navigate to SafetyAndControl (Home Nursing Page)
            Intent intent = new Intent(TriageDetail.this, SafetyAndControl.class);
            startActivity(intent);
        });
    }

    private void updateEmergencyVisibility() {
        boolean isCritical = cbCantSpeak.isChecked() || cbChestPulling.isChecked();
        if (isCritical) {
            btnEmergencyTop.setVisibility(View.VISIBLE);
        } else {
            btnEmergencyTop.setVisibility(View.GONE);
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                isTimerFinished = true;
                timeLeftInMillis = 0;
                updateCountDownText();
                checkTimerExpiryConditions();
            }
        }.start();
        isTimerRunning = true;
        isTimerFinished = false;
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        startTimer();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    private void checkTimerExpiryConditions() {
        boolean isCritical = cbCantSpeak.isChecked() || cbChestPulling.isChecked();
        if (isCritical) {
            showCriticalAlert();
        }
    }

    private void showCriticalAlert() {
        new AlertDialog.Builder(this)
                .setTitle("CRITICAL ALERT")
                .setMessage("Immediate attention required! Critical signs detected.")
                .setPositiveButton("CALL EMERGENCY", (dialog, which) -> {
                    btnEmergencyTop.performClick();
                })
                .setNegativeButton("Dismiss", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        // Highlight emergency button (ensure it's visible and distinct)
        updateEmergencyVisibility();
        btnCallEmergencyBottom.setBackgroundColor(Color.parseColor("#B71C1C")); // Darker Red
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}