package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.model.Child;
import com.example.smartair.model.UserRole;
import com.example.smartair.r3.SimpleMedicineLog;
import com.example.smartair.r5model.SymptomEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProviderHomeActivity extends AppCompatActivity {

    private Spinner spinnerPatientSelector;
    private TextView txtProviderId; // To-Do 4: Provider ID TextView

    private View cardRescueOverviewProvider;
    private View cardControllerOverviewProvider;
    private View cardSymptomsTriggersProvider;
    private View cardZoneDistributionProvider;

    private TextView txtSymptomsSummaryProvider;
    private TextView txtTopTriggersProvider;
    private TextView txtEventSample1;

    private Button btnExportReportProvider;
    private Button btnSignout;

    private DatabaseReference usersRef;
    private final List<Child> patientList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private final List<String> patientNames = new ArrayList<>();

    private String currentProviderId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_page);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        // To-Do 5: Get Current Provider ID (email as key logic)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            currentProviderId = user.getEmail().replace(".", ","); // Simplified ID derived from email
        } else {
            // Fallback or redirect to login
            currentProviderId = "unknown_provider";
        }

        initViews();
        setupPatientSpinner();
        loadPatients(); // Fetch real patients
        setupClickListeners();
    }

    private void initViews() {
        spinnerPatientSelector = findViewById(R.id.spinnerPatientSelector);
        txtProviderId = findViewById(R.id.txtProviderId); // To-Do 4: Bind View

        if (txtProviderId != null) {
            txtProviderId.setText("My Provider ID: " + currentProviderId);
        }

        cardRescueOverviewProvider = findViewById(R.id.cardRescueOverviewProvider);
        cardControllerOverviewProvider = findViewById(R.id.cardControllerOverviewProvider);
        cardSymptomsTriggersProvider = findViewById(R.id.cardSymptomsTriggersProvider);
        cardZoneDistributionProvider = findViewById(R.id.cardZoneDistributionProvider);

        txtSymptomsSummaryProvider = findViewById(R.id.txtSymptomsSummaryProvider);
        txtTopTriggersProvider = findViewById(R.id.txtTopTriggersProvider);
        txtEventSample1 = findViewById(R.id.txtEventSample1);

        btnExportReportProvider = findViewById(R.id.btnExportReportProvider);
        btnSignout = findViewById(R.id.btnProviderSignOut);

        btnSignout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProviderHomeActivity.this, MainActivity.class));
            finish();
        });
    }

    private void setupPatientSpinner() {
        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                patientNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatientSelector.setAdapter(spinnerAdapter);

        // Note: The listener logic is also updated inside loadPatients to handle the dynamic keys
    }

    private void loadPatients() {
        // To-Do 5: Filter patients by providerId
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientList.clear();
                patientNames.clear();
                final List<String> keys = new ArrayList<>();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    
                    // Check if role is CHILD and if providerId matches current provider
                    // FIX: Use Enum comparison instead of String equals
                    if (child != null && child.getRole() == UserRole.CHILD) {
                        if (currentProviderId != null && currentProviderId.equals(child.getProviderId())) {
                             patientList.add(child);
                             patientNames.add(child.getDisplayName());
                             keys.add(childSnapshot.getKey());
                        }
                    }
                }
                
                spinnerAdapter.notifyDataSetChanged();
                
                if (patientList.isEmpty()) {
                    Toast.makeText(ProviderHomeActivity.this, "No patients linked to your ID.", Toast.LENGTH_LONG).show();
                } else {
                    // Automatically load the first patient if available
                    loadPatientData(keys.get(0));
                }
                
                spinnerPatientSelector.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                         if (position >= 0 && position < keys.size()) {
                             loadPatientData(keys.get(position));
                         }
                    }
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProviderHomeActivity.this, "Failed to load patients", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientData(String childKey) {
        // 1. Check Sharing Settings first
        PEFDataRepository.getInstance().fetchSharingSettings(childKey, new PEFDataRepository.OnSharingSettingsFetchListener() {
            @Override
            public void onSuccess(SharingSettings settings) {
                updateUIBasedOnSettings(childKey, settings);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProviderHomeActivity.this, "Error loading settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIBasedOnSettings(String childKey, SharingSettings settings) {
        // Rescue Logs
        if (settings.isShareRescueLogs()) {
            cardRescueOverviewProvider.setVisibility(View.VISIBLE);
            loadRescueData(childKey);
        } else {
            cardRescueOverviewProvider.setVisibility(View.GONE);
        }

        // Controller Adherence
        if (settings.isShareAdherence()) {
            cardControllerOverviewProvider.setVisibility(View.VISIBLE);
            // loadControllerData(childKey); // Placeholder
        } else {
            cardControllerOverviewProvider.setVisibility(View.GONE);
        }

        // Symptoms & Triggers
        if (settings.isShareSymptoms() || settings.isShareTriggers()) {
            cardSymptomsTriggersProvider.setVisibility(View.VISIBLE);
            loadSymptomsAndTriggers(childKey, settings.isShareSymptoms(), settings.isShareTriggers());
        } else {
            cardSymptomsTriggersProvider.setVisibility(View.GONE);
        }
        
        // Zone Distribution (PEF)
        if (settings.isSharePEF()) {
            cardZoneDistributionProvider.setVisibility(View.VISIBLE);
        } else {
            cardZoneDistributionProvider.setVisibility(View.GONE);
        }
    }

    private void loadRescueData(String childKey) {
        usersRef.child(childKey).child("medicine_logs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int rescueCount = 0;
                long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 3600 * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                for (DataSnapshot child : snapshot.getChildren()) {
                    SimpleMedicineLog log = child.getValue(SimpleMedicineLog.class);
                    if (log != null && "RESCUE".equalsIgnoreCase(log.getType())) {
                        try {
                            Date date = sdf.parse(log.getTime());
                            if (date != null && date.getTime() > oneWeekAgo) {
                                rescueCount += log.getDoseCount();
                            }
                        } catch (Exception e) {}
                    }
                }
                txtEventSample1.setText("Rescue use: " + rescueCount + " puffs this week.");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadSymptomsAndTriggers(String childKey, boolean shareSymptoms, boolean shareTriggers) {
        usersRef.child(childKey).child("symptomCheckins").limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int symptomCount = 0;
                Set<String> triggers = new HashSet<>();
                long twoWeeksAgo = System.currentTimeMillis() - (14L * 24 * 3600 * 1000);

                for (DataSnapshot child : snapshot.getChildren()) {
                    SymptomEntry entry = child.getValue(SymptomEntry.class);
                    if (entry != null && entry.timestamp > twoWeeksAgo) {
                        symptomCount++;
                        if (entry.triggers != null) triggers.addAll(entry.triggers);
                    }
                }

                if (shareSymptoms) {
                    txtSymptomsSummaryProvider.setText("Symptom entries: " + symptomCount + " in last 14 days");
                } else {
                    txtSymptomsSummaryProvider.setText("Symptoms not shared.");
                }

                if (shareTriggers) {
                    String t = triggers.isEmpty() ? "None" : String.join(", ", triggers);
                    txtTopTriggersProvider.setText("Recent triggers: " + t);
                } else {
                    txtTopTriggersProvider.setText("Triggers not shared.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupClickListeners() {
        btnExportReportProvider.setOnClickListener(v ->
                Toast.makeText(this, "Export report functionality to be implemented", Toast.LENGTH_SHORT).show()
        );
    }
}