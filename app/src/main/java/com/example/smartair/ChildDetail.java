package com.example.smartair;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChildDetail extends AppCompatActivity {

    private String childId;
    private SharingSettings currentSettings = new SharingSettings();
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_detail);

        childId = getIntent().getStringExtra("CHILD_ID");
        if (childId == null || childId.isEmpty()) {
            Toast.makeText(this, "Error: Child ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initializeCards();
        fetchAndListenForSharingSettings();
        loadHealthData();
    }

    private void fetchAndListenForSharingSettings() {
        PEFDataRepository.getInstance().fetchSharingSettings(childId, new PEFDataRepository.OnSharingSettingsFetchListener() {
            @Override
            public void onSuccess(SharingSettings settings) {
                currentSettings = settings;
                updateAllSwitches();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ChildDetail.this, "Failed to load settings: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeCards() {
        setupCard(R.id.card_rescue_logs, "Rescue Logs", "Loading...", 
            (isChecked) -> {
                currentSettings.setShareRescueLogs(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_adherence, "Controller Adherence Summary", "Loading...", 
            (isChecked) -> {
                currentSettings.setShareAdherence(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_symptoms, "Symptoms", "Loading...", 
            (isChecked) -> {
                currentSettings.setShareSymptoms(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_triggers, "Triggers", "Loading...", 
            (isChecked) -> {
                currentSettings.setShareTriggers(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_pef, "Peak-flow (PEF)", "Loading...", 
            (isChecked) -> {
                currentSettings.setSharePEF(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_triage, "Triage Incidents", "Loading...", 
            (isChecked) -> {
                currentSettings.setShareTriage(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_charts, "Summary Charts", "Charts generated from available data.", 
            (isChecked) -> {
                currentSettings.setShareCharts(isChecked);
                saveSharingSettings();
            });
    }

    private void loadHealthData() {
        loadMedicineData();
        loadSymptomData();
        loadPEFData();
        loadTriageData();
    }

    private void loadMedicineData() {
        usersRef.child(childId).child("medicine_logs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int rescueCountWeek = 0;
                int controllerCountMonth = 0;
                long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 3600 * 1000);
                long oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 3600 * 1000);
                String lastRescueDate = "None";

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                for (DataSnapshot child : snapshot.getChildren()) {
                    SimpleMedicineLog log = child.getValue(SimpleMedicineLog.class);
                    if (log != null && log.getTime() != null) {
                        try {
                            Date date = sdf.parse(log.getTime());
                            if (date != null) {
                                long time = date.getTime();
                                if ("RESCUE".equalsIgnoreCase(log.getType())) {
                                    if (time > oneWeekAgo) rescueCountWeek += log.getDoseCount();
                                    // Find latest
                                    if (lastRescueDate.equals("None") || time > sdf.parse(lastRescueDate).getTime()) {
                                        lastRescueDate = log.getTime();
                                    }
                                } else if ("CONTROLLER".equalsIgnoreCase(log.getType())) {
                                    if (time > oneMonthAgo) controllerCountMonth++;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                updateCardContent(R.id.card_rescue_logs, "Last used: " + lastRescueDate + "\nThis week: " + rescueCountWeek + " puffs");
                updateCardContent(R.id.card_adherence, "Last 30 days: " + controllerCountMonth + " doses recorded.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateCardContent(R.id.card_rescue_logs, "Error loading data.");
                updateCardContent(R.id.card_adherence, "Error loading data.");
            }
        });
    }

    private void loadSymptomData() {
        usersRef.child(childId).child("symptomCheckins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int symptomsWeek = 0;
                Set<String> distinctTriggers = new HashSet<>();
                long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 3600 * 1000);

                for (DataSnapshot child : snapshot.getChildren()) {
                    SymptomEntry entry = child.getValue(SymptomEntry.class);
                    if (entry != null) {
                        if (entry.timestamp > oneWeekAgo) {
                            symptomsWeek++;
                        }
                        if (entry.triggers != null) {
                            distinctTriggers.addAll(entry.triggers);
                        }
                    }
                }

                updateCardContent(R.id.card_symptoms, "Reported " + symptomsWeek + " times in the last 7 days.");
                
                String triggersText = distinctTriggers.isEmpty() ? "No triggers recorded recently." : String.join(", ", distinctTriggers);
                updateCardContent(R.id.card_triggers, triggersText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateCardContent(R.id.card_symptoms, "Error loading data.");
                updateCardContent(R.id.card_triggers, "Error loading data.");
            }
        });
    }

    private void loadPEFData() {
        usersRef.child(childId).child("pef_logs").limitToLast(50).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int sum = 0;
                int count = 0;
                String lastZone = "N/A";
                long latestTimestamp = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    PEFLogModel log = child.getValue(PEFLogModel.class);
                    if (log != null) {
                        // Prefer postValue if comparison, else preValue
                        int val = log.isComparison() && log.getPostValue() > 0 ? log.getPostValue() : log.getPreValue();
                        sum += val;
                        count++;
                        if (log.getTimestamp() > latestTimestamp) {
                            latestTimestamp = log.getTimestamp();
                            lastZone = log.getZone();
                        }
                    }
                }

                String avgText = count > 0 ? (sum / count) + " L/min" : "N/A";
                updateCardContent(R.id.card_pef, "Avg (last 50): " + avgText + ". Latest Zone: " + lastZone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateCardContent(R.id.card_pef, "Error loading data.");
            }
        });
    }

    private void loadTriageData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
             updateCardContent(R.id.card_triage, "Parent info not found.");
             return;
        }
        
        String parentKey = user.getEmail().replace(".", ",");
        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("alerts").child(parentKey);
        
        alertsRef.orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long lastIncidentTime = 0;
                int incidentCount = 0;
                
                for (DataSnapshot child : snapshot.getChildren()) {
                    Alert alert = child.getValue(Alert.class);
                    if (alert != null && childId.equals(alert.getChildKey())) {
                        incidentCount++;
                        if (alert.getTimestamp() > lastIncidentTime) {
                            lastIncidentTime = alert.getTimestamp();
                        }
                    }
                }
                
                if (incidentCount == 0) {
                    updateCardContent(R.id.card_triage, "No incidents recorded.");
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                    updateCardContent(R.id.card_triage, "Last incident: " + sdf.format(new Date(lastIncidentTime)) + "\nTotal recorded: " + incidentCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateCardContent(R.id.card_triage, "Error loading data.");
            }
        });
    }

    private void updateCardContent(int cardId, String content) {
        View cardView = findViewById(cardId);
        if (cardView != null) {
            TextView tvContent = cardView.findViewById(R.id.tv_content_preview);
            if (tvContent != null) {
                tvContent.setText(content);
            }
        }
    }

    private void setupCard(int cardId, String title, String content, OnShareToggleListener listener) {
        View cardView = findViewById(cardId);
        if (cardView == null) return;
        
        TextView tvTitle = cardView.findViewById(R.id.tv_card_title);
        TextView tvContent = cardView.findViewById(R.id.tv_content_preview);
        SwitchCompat switchShare = cardView.findViewById(R.id.switch_share);

        tvTitle.setText(title);
        tvContent.setText(content);

        // Remove previous listener to avoid loops when updating UI
        switchShare.setOnCheckedChangeListener(null);
        
        // Set initial state from currentSettings (will be updated by fetch)
        updateSwitchState(cardId, switchShare);
        
        // Add new listener
        switchShare.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onToggle(isChecked);
        });
    }

    private void updateAllSwitches() {
        updateSwitchForCard(R.id.card_rescue_logs);
        updateSwitchForCard(R.id.card_adherence);
        updateSwitchForCard(R.id.card_symptoms);
        updateSwitchForCard(R.id.card_triggers);
        updateSwitchForCard(R.id.card_pef);
        updateSwitchForCard(R.id.card_triage);
        updateSwitchForCard(R.id.card_charts);
    }

    private void updateSwitchForCard(int cardId) {
        View cardView = findViewById(cardId);
        if (cardView == null) return;
        SwitchCompat switchShare = cardView.findViewById(R.id.switch_share);
        if (switchShare != null) {
            updateSwitchState(cardId, switchShare);
        }
    }

    private void updateSwitchState(int cardId, SwitchCompat switchView) {
        boolean isChecked = false;
        if (cardId == R.id.card_rescue_logs) {
            isChecked = currentSettings.isShareRescueLogs();
        } else if (cardId == R.id.card_adherence) {
            isChecked = currentSettings.isShareAdherence();
        } else if (cardId == R.id.card_symptoms) {
            isChecked = currentSettings.isShareSymptoms();
        } else if (cardId == R.id.card_triggers) {
            isChecked = currentSettings.isShareTriggers();
        } else if (cardId == R.id.card_pef) {
            isChecked = currentSettings.isSharePEF();
        } else if (cardId == R.id.card_triage) {
            isChecked = currentSettings.isShareTriage();
        } else if (cardId == R.id.card_charts) {
            isChecked = currentSettings.isShareCharts();
        }
        switchView.setChecked(isChecked);
        
        // Update the "Shared" tag visibility
        TextView tvSharedTag = ((View)switchView.getParent().getParent()).findViewById(R.id.tv_shared_tag);
        if (tvSharedTag != null) {
            tvSharedTag.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
    }

    private void saveSharingSettings() {
        PEFDataRepository.getInstance().saveSharingSettings(childId, currentSettings, new PEFDataRepository.OnSaveListener() {
            @Override
            public void onSuccess() {
                // Toast.makeText(ChildDetail.this, "Settings updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ChildDetail.this, "Failed to save settings: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnShareToggleListener {
        void onToggle(boolean isChecked);
    }
}