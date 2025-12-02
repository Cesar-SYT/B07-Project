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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    
    // Zone Logic
    private float personalBest = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage_detail);

        rootRef = FirebaseDatabase.getInstance().getReference("users");
        fetchChildInfoAndSendAlert(); // Start the alert process and fetch PB

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
                    
                    // Log the Triage Incident persistenty
                    saveTriageLog(childKey, "TRIAGE_STARTED");

                    if (parentId != null && !parentId.isEmpty()) {
                        sendAlertToParent(parentId, "TRIAGE_STARTED");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        
        // Fetch Personal Best for Zone Logic
        PEFDataRepository.getInstance().fetchParentConfiguredPB(childKey, new OnPBFetchListener() {
            @Override
            public void onSuccess(float pbValue) {
                personalBest = pbValue;
            }
            @Override
            public void onFailure(String errorMsg) {
                // Ignore error, will default to generic advice if PB missing
            }
        });
    }

    private void saveTriageLog(String childKey, String action) {
        DatabaseReference logsRef = rootRef.child(childKey).child("triage_logs");
        String logId = logsRef.push().getKey();
        if (logId != null) {
            Map<String, Object> log = new HashMap<>();
            log.put("logId", logId);
            log.put("action", action);
            log.put("timestamp", System.currentTimeMillis());
            
            logsRef.child(logId).setValue(log);
        }
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
            Toast.makeText(TriageDetail.this, "Calling Emergency Services...", Toast.LENGTH_LONG).show();
        };
        if (btnEmergencyTop != null) btnEmergencyTop.setOnClickListener(callEmergencyListener);
        if (btnCallEmergencyBottom != null) btnCallEmergencyBottom.setOnClickListener(callEmergencyListener);

        // Home Steps Action (Green Button)
        if (btnHomeSteps != null) {
            btnHomeSteps.setOnClickListener(v -> showHomeStepsDialog());
        }
    }

    private void showHomeStepsDialog() {
        String title;
        String steps;
        int icon;
        
        // Determine Zone
        float currentPEF = 0;
        String input = etCurrentPEF.getText().toString().trim();
        if (!input.isEmpty()) {
            try {
                currentPEF = Float.parseFloat(input);
            } catch (NumberFormatException e) {
                currentPEF = 0;
            }
        }

        if (personalBest > 0 && currentPEF > 0) {
            float percentage = (currentPEF / personalBest) * 100;
            
            if (percentage >= 80) {
                // Green Zone
                title = "Green Zone (Doing Well)";
                steps = "Even though your PEF is good, since you are here, you might have mild symptoms:\n\n" +
                        "1. Take your controller medicine as prescribed.\n" +
                        "2. If you have cough/wheeze, use Rescue Inhaler (2 puffs).\n" +
                        "3. Monitor symptoms closely for 24 hours.";
                icon = android.R.drawable.ic_dialog_info;
            } else if (percentage >= 50) {
                // Yellow Zone
                title = "Yellow Zone (Caution)";
                steps = "Your asthma is getting worse:\n\n" +
                        "1. Take 2-4 puffs of Rescue Inhaler every 20 minutes for up to 1 hour.\n" +
                        "2. If symptoms improve, continue monitoring.\n" +
                        "3. If symptoms persist, call your doctor.";
                icon = android.R.drawable.ic_dialog_alert;
            } else {
                // Red Zone
                title = "Red Zone (Medical Alert)";
                steps = "Your asthma is severe:\n\n" +
                        "1. Use Rescue Inhaler immediately (4-6 puffs).\n" +
                        "2. Call Emergency immediately if not better in 15 mins.\n" +
                        "3. Stay calm and sit up.";
                icon = android.R.drawable.ic_delete;
            }
        } else {
            // Default / Unknown Zone (assume Yellow/Caution as fallback for Triage)
            title = "Home Action Plan (Caution)";
            steps = "Standard steps for worsening symptoms:\n\n" +
                    "1. Sit upright and stay calm.\n" +
                    "2. Take 2-4 puffs of Rescue Inhaler every 20 minutes for up to 1 hour.\n" +
                    "3. If symptoms improve, continue to monitor closely.\n" +
                    "4. If symptoms worsen or do not improve, call emergency immediately.";
            icon = android.R.drawable.ic_dialog_alert;
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(steps)
                .setIcon(icon)
                .setPositiveButton("Done / Return Home", (dialog, which) -> {
                    // Navigate back to ChildHomeActivity (Safety & Control dashboard)
                    Intent intent = new Intent(TriageDetail.this, ChildHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        // Log Escalation
        if (childKey != null) saveTriageLog(childKey, "TRIAGE_ESCALATED");
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
