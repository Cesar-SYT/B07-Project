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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    // Alerting
    private DatabaseReference rootRef;
    private String childKey;
    private String childName = "Child"; // Default placeholder

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage_detail);

        rootRef = FirebaseDatabase.getInstance().getReference("users");
        fetchChildInfoAndSendAlert(); // Start the alert process

        initializeViews();
        setupAdapters();
        setupListeners();
        startTimer();
    }

    private void fetchChildInfoAndSendAlert() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        childKey = user.getEmail().replace(".", ",");

        // Fetch child's name and parent ID
        rootRef.child(childKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    childName = snapshot.child("displayName").getValue(String.class);
                    if (childName == null) childName = "Child";

                    String parentId = snapshot.child("parentId").getValue(String.class);
                    
                    if (parentId != null && !parentId.isEmpty()) {
                        sendAlertToParent(parentId, "TRIAGE_STARTED");
                    } else {
                        // If no parent linked, maybe log it or just ignore for now
                        // System.out.println("No parent linked for alert.");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void sendAlertToParent(String parentId, String type) {
        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("alerts").child(parentId);
        String alertId = alertsRef.push().getKey();

        if (alertId != null) {
            Alert alert = new Alert(
                    alertId,
                    childKey,
                    childName,
                    type,
                    System.currentTimeMillis(),
                    "new"
            );
            alertsRef.child(alertId).setValue(alert);
        }
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
        if (npEmergencyVisits != null) {
            npEmergencyVisits.setMinValue(0);
            npEmergencyVisits.setMaxValue(10);
            npEmergencyVisits.setValue(0);
        }
    }

    private void setupAdapters() {
        // Emergency Type Spinner
        String[] emergencyTypes = {"None", "Inhaler use", "Chest compressions", "Oxygen supply"};
        ArrayAdapter<String> emergencyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, emergencyTypes);
        emergencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerEmergencyType != null) {
            spinnerEmergencyType.setAdapter(emergencyAdapter);
        }

        // Symptom Duration Spinner
        String[] durations = {"< 1 hour", "1-3 hours", "3-6 hours", "> 6 hours"};
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, durations);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerSymptomDuration != null) {
            spinnerSymptomDuration.setAdapter(durationAdapter);
        }
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
        if (cbCantSpeak != null) cbCantSpeak.setOnCheckedChangeListener(criticalCheckListener);
        if (cbChestPulling != null) cbChestPulling.setOnCheckedChangeListener(criticalCheckListener);

        // Timer Reset
        if (btnResetTimer != null) btnResetTimer.setOnClickListener(v -> resetTimer());

        // Call Emergency Actions
        View.OnClickListener callEmergencyListener = v -> {
            // Placeholder for actual call logic
            Toast.makeText(TriageDetail.this, "Calling Emergency Services...", Toast.LENGTH_LONG).show();
            // Example integration:
            // Intent intent = new Intent(Intent.ACTION_DIAL);
            // intent.setData(android.net.Uri.parse("tel:112"));
            // startActivity(intent);
        };
        if (btnEmergencyTop != null) btnEmergencyTop.setOnClickListener(callEmergencyListener);
        if (btnCallEmergencyBottom != null) btnCallEmergencyBottom.setOnClickListener(callEmergencyListener);

        // Home Steps Action (Green Button)
        if (btnHomeSteps != null) {
            btnHomeSteps.setOnClickListener(v -> {
                // Navigate back to ChildHomeActivity (Safety & Control dashboard)
                Intent intent = new Intent(TriageDetail.this, ChildHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    private void updateEmergencyVisibility() {
        boolean isCritical = (cbCantSpeak != null && cbCantSpeak.isChecked()) || 
                             (cbChestPulling != null && cbChestPulling.isChecked());
        if (isCritical) {
            if (btnEmergencyTop != null) btnEmergencyTop.setVisibility(View.VISIBLE);
        } else {
            if (btnEmergencyTop != null) btnEmergencyTop.setVisibility(View.GONE);
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
        if (timerTextView != null) timerTextView.setText(timeFormatted);
    }

    private void checkTimerExpiryConditions() {
        boolean isCritical = (cbCantSpeak != null && cbCantSpeak.isChecked()) || 
                             (cbChestPulling != null && cbChestPulling.isChecked());
        if (isCritical) {
            showCriticalAlert();
        }
    }

    private void showCriticalAlert() {
        new AlertDialog.Builder(this)
                .setTitle("CRITICAL ALERT")
                .setMessage("Immediate attention required! Critical signs detected.")
                .setPositiveButton("CALL EMERGENCY", (dialog, which) -> {
                    if (btnEmergencyTop != null) btnEmergencyTop.performClick();
                })
                .setNegativeButton("Dismiss", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        // Highlight emergency button (ensure it's visible and distinct)
        updateEmergencyVisibility();
        if (btnCallEmergencyBottom != null) btnCallEmergencyBottom.setBackgroundColor(Color.parseColor("#B71C1C")); // Darker Red
        
        // Send Escalation Alert if parentId is known (fetched at startup)
        // For simplicity, we re-fetch or reuse fetched data.
        // Since fetch is async, we rely on the initial fetch or trigger it again.
        // Better to trigger again to be safe or store parentId globally in activity.
        // We'll assume the initial fetch handled the START alert. 
        // For ESCALATION, let's do a quick fetch-send again.
        sendEscalationAlert();
    }

    private void sendEscalationAlert() {
        if (childKey == null) return;
        rootRef.child(childKey).child("parentId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String parentId = snapshot.getValue(String.class);
                if (parentId != null) {
                    sendAlertToParent(parentId, "TRIAGE_ESCALATED");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}