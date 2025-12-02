package com.example.smartair;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DisplayPEF extends AppCompatActivity {

    private TextView tvPBLabel;
    private RecyclerView recyclerViewPEF;
    private TextView tvEmptyState;
    private PEFLogAdapter adapter;
    private List<PEFLogModel> pefLogList = new ArrayList<>();

    private float parentConfiguredPB = 0f;
    private String currentChildKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_display_pef);
        
        setupWindowInsets();

        currentChildKey = getIntent().getStringExtra("childKey");
        if (currentChildKey == null || currentChildKey.isEmpty()) {
            Toast.makeText(this, "Error: No child selected.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        fetchData();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        tvPBLabel = findViewById(R.id.tv_pb_label);
        recyclerViewPEF = findViewById(R.id.recyclerView_pef);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        recyclerViewPEF.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PEFLogAdapter(pefLogList);
        recyclerViewPEF.setAdapter(adapter);
    }

    private void fetchData() {
        PEFDataRepository.getInstance().fetchParentConfiguredPB(currentChildKey, new OnPBFetchListener() {
            @Override
            public void onSuccess(float pbValue) {
                parentConfiguredPB = pbValue;
                updatePBDisplay(pbValue);

                loadPEFLogs(); 
            }

            @Override
            public void onFailure(String errorMsg) {
                Toast.makeText(DisplayPEF.this, "Failed to load PB: " + errorMsg, Toast.LENGTH_SHORT).show();
                loadPEFLogs();
            }
        });
    }

    private void updatePBDisplay(float pbValue) {
        String label = (pbValue > 0) ? String.format("PB : %.0f L/min", pbValue) : "PB: Not Set";
        tvPBLabel.setText(label);
    }
    private String calculateZone(int value, float pb) {
        if (pb <= 0) return "Unknown";

        float greenThreshold = 0.8f * pb;
        float yellowThreshold = 0.5f * pb;

        if (value >= greenThreshold) {
            return "Green";
        } else if (value >= yellowThreshold) {
            return "Yellow";
        } else {
            return "Red";
        }
    }
    private void loadPEFLogs() {
        PEFDataRepository.getInstance().fetchPEFLogs(currentChildKey, new PEFDataRepository.OnLogsFetchListener() {
            @Override
            public void onLogsFetched(List<PEFLogModel> logs) {
                pefLogList.clear();

                for (PEFLogModel log : logs) {

                    int pre = log.getPreValue();
                    int post = log.isComparison() ? log.getPostValue() : 0;

                    int compareValue = Math.max(pre, post);
                    if (compareValue == 0 && pre > 0) {
                        compareValue = pre;
                    }

                    String oldZone = log.getZone();
                    if (oldZone == null) oldZone = "Unknown";

                    if (oldZone.equalsIgnoreCase("Unknown")) {
                        String newZone = calculateZone(compareValue, parentConfiguredPB);

                        log.setZone(newZone);

                        if (log.getLogId() != null && !log.getLogId().isEmpty()) {
                            com.google.firebase.database.FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(currentChildKey)
                                    .child("pef_logs")
                                    .child(log.getLogId())
                                    .child("zone")
                                    .setValue(newZone);
                        }
                    }

                    pefLogList.add(log);
                }

                updateEmptyState();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(DisplayPEF.this, "Failed to load logs: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateEmptyState() {
        if (pefLogList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewPEF.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewPEF.setVisibility(View.VISIBLE);
        }
    }
}