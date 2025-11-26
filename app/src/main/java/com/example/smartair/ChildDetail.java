package com.example.smartair;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class ChildDetail extends AppCompatActivity {

    private String childId;
    private ChildHealthData healthData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_detail);

        childId = getIntent().getStringExtra("CHILD_ID");
        // Ideally, fetch data from DB using childId. For now, use dummy data.
        healthData = new ChildHealthData(childId);

        initializeCards();
    }

    private void initializeCards() {
        setupCard(R.id.card_rescue_logs, "Rescue Logs", "No recent rescue events recorded.", 
                healthData.isShareRescueLogs(), (isChecked) -> healthData.setShareRescueLogs(isChecked));

        setupCard(R.id.card_adherence, "Controller Adherence Summary", "85% adherence in last 30 days.", 
                healthData.isShareAdherence(), (isChecked) -> healthData.setShareAdherence(isChecked));

        setupCard(R.id.card_symptoms, "Symptoms", "Coughing reported 2 times this week.", 
                healthData.isShareSymptoms(), (isChecked) -> healthData.setShareSymptoms(isChecked));

        setupCard(R.id.card_triggers, "Triggers", "Pollen, Cold Air.", 
                healthData.isShareTriggers(), (isChecked) -> healthData.setShareTriggers(isChecked));

        setupCard(R.id.card_pef, "Peak-flow (PEF)", "Avg: 450 L/min. Green Zone maintained.", 
                healthData.isSharePEF(), (isChecked) -> healthData.setSharePEF(isChecked));

        setupCard(R.id.card_triage, "Triage Incidents", "1 incident on Oct 25. Resolved at home.", 
                healthData.isShareTriage(), (isChecked) -> healthData.setShareTriage(isChecked));

        setupCard(R.id.card_charts, "Summary Charts", "Visual chart placeholder.", 
                healthData.isShareCharts(), (isChecked) -> healthData.setShareCharts(isChecked));
    }

    private void setupCard(int cardId, String title, String content, boolean isInitiallyShared, OnShareToggleListener listener) {
        View cardView = findViewById(cardId);
        
        TextView tvTitle = cardView.findViewById(R.id.tv_card_title);
        TextView tvContent = cardView.findViewById(R.id.tv_content_preview);
        SwitchCompat switchShare = cardView.findViewById(R.id.switch_share);
        TextView tvSharedTag = cardView.findViewById(R.id.tv_shared_tag);

        tvTitle.setText(title);
        tvContent.setText(content);
        switchShare.setChecked(isInitiallyShared);
        updateSharedTag(tvSharedTag, isInitiallyShared);

        switchShare.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onToggle(isChecked);
            updateSharedTag(tvSharedTag, isChecked);
            
            // Simulate real-time update
            String status = isChecked ? "Shared" : "Unshared";
            // Toast.makeText(this, title + " is now " + status, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateSharedTag(TextView tagView, boolean isShared) {
        if (isShared) {
            tagView.setVisibility(View.VISIBLE);
        } else {
            tagView.setVisibility(View.GONE);
        }
    }

    interface OnShareToggleListener {
        void onToggle(boolean isChecked);
    }
}