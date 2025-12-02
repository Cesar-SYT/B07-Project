package com.example.smartair;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class ChildDetail extends AppCompatActivity {

    private String childId;
    private SharingSettings currentSettings = new SharingSettings();

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

        initializeCards();
        fetchAndListenForSharingSettings();
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
        setupCard(R.id.card_rescue_logs, "Rescue Logs", "No recent rescue events recorded.", 
            (isChecked) -> {
                currentSettings.setShareRescueLogs(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_adherence, "Controller Adherence Summary", "85% adherence in last 30 days.", 
            (isChecked) -> {
                currentSettings.setShareAdherence(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_symptoms, "Symptoms", "Coughing reported 2 times this week.", 
            (isChecked) -> {
                currentSettings.setShareSymptoms(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_triggers, "Triggers", "Pollen, Cold Air.", 
            (isChecked) -> {
                currentSettings.setShareTriggers(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_pef, "Peak-flow (PEF)", "Avg: 450 L/min. Green Zone maintained.", 
            (isChecked) -> {
                currentSettings.setSharePEF(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_triage, "Triage Incidents", "1 incident on Oct 25. Resolved at home.", 
            (isChecked) -> {
                currentSettings.setShareTriage(isChecked);
                saveSharingSettings();
            });

        setupCard(R.id.card_charts, "Summary Charts", "Visual chart placeholder.", 
            (isChecked) -> {
                currentSettings.setShareCharts(isChecked);
                saveSharingSettings();
            });
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