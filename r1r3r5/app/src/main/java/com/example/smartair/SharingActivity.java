package com.example.smartair;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class SharingActivity extends AppCompatActivity {

    private CheckBox cbShareRescueLogs;
    private CheckBox cbShareAdherence;
    private CheckBox cbShareSymptoms;
    private CheckBox cbShareTriggers;
    private CheckBox cbSharePEF;
    private CheckBox cbShareTriage;
    private CheckBox cbShareCharts;
    private Button btnSaveSharing;

    // Invite Code UI
    private TextView tvInviteCode;
    private Button btnGenerateCode;
    private Button btnRevokeCode;

    private String childKey;
    private DatabaseReference sharingRef;
    private DatabaseReference invitesRef;
    private DatabaseReference userInviteRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);

        childKey = getIntent().getStringExtra("childKey");
        if (childKey == null) {
            Toast.makeText(this, "Error: No child selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        cbShareRescueLogs = findViewById(R.id.cbShareRescueLogs);
        cbShareAdherence = findViewById(R.id.cbShareAdherence);
        cbShareSymptoms = findViewById(R.id.cbShareSymptoms);
        cbShareTriggers = findViewById(R.id.cbShareTriggers);
        cbSharePEF = findViewById(R.id.cbSharePEF);
        cbShareTriage = findViewById(R.id.cbShareTriage);
        cbShareCharts = findViewById(R.id.cbShareCharts);
        btnSaveSharing = findViewById(R.id.btnSaveSharing);

        tvInviteCode = findViewById(R.id.tvInviteCode);
        btnGenerateCode = findViewById(R.id.btnGenerateCode);
        btnRevokeCode = findViewById(R.id.btnRevokeCode);

        sharingRef = FirebaseDatabase.getInstance().getReference("users").child(childKey).child("sharing_settings");
        invitesRef = FirebaseDatabase.getInstance().getReference("invites");
        userInviteRef = FirebaseDatabase.getInstance().getReference("users").child(childKey).child("invite_code");

        loadSettings();
        loadInviteCode();

        btnSaveSharing.setOnClickListener(v -> saveSettings());
        btnGenerateCode.setOnClickListener(v -> generateInviteCode());
        btnRevokeCode.setOnClickListener(v -> revokeInviteCode());
    }

    private void loadSettings() {
        sharingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SharingSettings settings = snapshot.getValue(SharingSettings.class);
                if (settings == null) {
                    settings = new SharingSettings(); // Default false
                }
                updateUI(settings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SharingActivity.this, "Failed to load settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(SharingSettings settings) {
        cbShareRescueLogs.setChecked(settings.isShareRescueLogs());
        cbShareAdherence.setChecked(settings.isShareAdherence());
        cbShareSymptoms.setChecked(settings.isShareSymptoms());
        cbShareTriggers.setChecked(settings.isShareTriggers());
        cbSharePEF.setChecked(settings.isSharePEF());
        cbShareTriage.setChecked(settings.isShareTriage());
        cbShareCharts.setChecked(settings.isShareCharts());
    }

    private void saveSettings() {
        SharingSettings settings = new SharingSettings();
        settings.setShareRescueLogs(cbShareRescueLogs.isChecked());
        settings.setShareAdherence(cbShareAdherence.isChecked());
        settings.setShareSymptoms(cbShareSymptoms.isChecked());
        settings.setShareTriggers(cbShareTriggers.isChecked());
        settings.setSharePEF(cbSharePEF.isChecked());
        settings.setShareTriage(cbShareTriage.isChecked());
        settings.setShareCharts(cbShareCharts.isChecked());

        sharingRef.setValue(settings).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SharingActivity.this, "Sharing settings saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(SharingActivity.this, "Failed to save settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Invite Code Logic ---

    private void loadInviteCode() {
        userInviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String code = snapshot.getValue(String.class);
                if (code != null) {
                    // Check expiry details from /invites/{code}
                    checkInviteDetails(code);
                } else {
                    tvInviteCode.setText("No code generated.");
                    btnRevokeCode.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // ignore
            }
        });
    }

    private void checkInviteDetails(String code) {
        invitesRef.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                    if (timestamp != null) {
                        long expiry = timestamp + (7L * 24 * 60 * 60 * 1000); // 7 days
                        long now = System.currentTimeMillis();
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        String expiryStr = sdf.format(new Date(expiry));

                        if (now > expiry) {
                             tvInviteCode.setText("Code: " + code + " (EXPIRED on " + expiryStr + ")");
                        } else {
                             tvInviteCode.setText("Code: " + code + "\nExpires: " + expiryStr);
                        }
                        btnRevokeCode.setEnabled(true);
                    }
                } else {
                    // Stale reference in user node?
                     tvInviteCode.setText("Code: " + code + " (Invalid/Removed)");
                     btnRevokeCode.setEnabled(true); // Allow clearing it
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();

        long now = System.currentTimeMillis();

        // 1. Save to /invites/{code}
        Map<String, Object> inviteData = new HashMap<>();
        inviteData.put("childKey", childKey);
        inviteData.put("timestamp", now);

        invitesRef.child(code).setValue(inviteData).addOnSuccessListener(aVoid -> {
            // 2. Save to /users/{childKey}/invite_code
            userInviteRef.setValue(code).addOnSuccessListener(aVoid1 -> {
                Toast.makeText(SharingActivity.this, "Code generated!", Toast.LENGTH_SHORT).show();
                loadInviteCode();
            });
        });
    }

    private void revokeInviteCode() {
        userInviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String code = snapshot.getValue(String.class);
                if (code != null) {
                    // Remove from /invites
                    invitesRef.child(code).removeValue();
                    // Remove from user
                    userInviteRef.removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(SharingActivity.this, "Code revoked.", Toast.LENGTH_SHORT).show();
                        tvInviteCode.setText("No code generated.");
                        btnRevokeCode.setEnabled(false);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
