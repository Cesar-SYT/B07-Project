package com.example.smartair;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SharingActivity extends AppCompatActivity {

    private CheckBox cbShareRescueLogs;
    private CheckBox cbShareAdherence;
    private CheckBox cbShareSymptoms;
    private CheckBox cbShareTriggers;
    private CheckBox cbSharePEF;
    private CheckBox cbShareTriage;
    private CheckBox cbShareCharts;
    private Button btnSaveSharing;

    private String childKey;
    private DatabaseReference sharingRef;

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

        sharingRef = FirebaseDatabase.getInstance().getReference("users").child(childKey).child("sharing_settings");

        loadSettings();

        btnSaveSharing.setOnClickListener(v -> saveSettings());
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
}
