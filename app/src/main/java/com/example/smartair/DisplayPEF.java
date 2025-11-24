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
import java.util.Collections;
import java.util.List;

public class DisplayPEF extends AppCompatActivity {

    private TextView tvPBLabel;
    private RecyclerView recyclerViewPEF;
    private TextView tvEmptyState;
    private PEFLogAdapter adapter;
    private List<PEFLogModel> pefLogList = new ArrayList<>();

    private float parentConfiguredPB = 0f; // Baseline PEF from Parent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_display_pef);
        
        setupWindowInsets();
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
        String currentUserId = "user123"; // TODO: Replace with actual User ID from Auth

        // 1. Fetch Parent Configured PB
        PEFDataRepository.getInstance().fetchParentConfiguredPB(currentUserId, new OnPBFetchListener() {
            @Override
            public void onSuccess(float pbValue) {
                parentConfiguredPB = pbValue;
                updatePBDisplay(pbValue);
                
                // 2. After fetching PB, fetch logs (so we can calculate Zones correctly if needed locally)
                // In a real app, Zone might be stored in DB, but we can also re-calculate.
                loadDummyPEFLogs(); 
            }

            @Override
            public void onFailure(String errorMsg) {
                Toast.makeText(DisplayPEF.this, "Failed to load PB: " + errorMsg, Toast.LENGTH_SHORT).show();
                // Even if PB fails, load logs (Zone calculation might default to Grey)
                loadDummyPEFLogs();
            }
        });
    }

    private void updatePBDisplay(float pbValue) {
        tvPBLabel.setText(String.format("PB (基准值): %.0f L/min", pbValue));
    }

    private void loadDummyPEFLogs() {
        // Simulate fetching data from Firebase/DB
        pefLogList.clear();

        // Mock Data
        // Note: Zone calculation logic is technically here for demo, 
        // but ideally "Zone" is saved in the object when created or calculated on fly.
        // We will use the parentConfiguredPB to determine zone if needed, 
        // or just use pre-filled data for this mock.
        
        pefLogList.add(createMockLog("1", "2023-10-27 08:00", 480));
        pefLogList.add(createMockLog("2", "2023-10-26 20:00", 350));
        pefLogList.add(createMockLog("3", "2023-10-26 08:00", 200));
        pefLogList.add(createMockLog("4", "2023-10-25 08:00", 500));

        // Sort by Date Descending (Newest first)
        // NOTE: If you want Ascending, swap o2 and o1
        Collections.sort(pefLogList, (o1, o2) -> o2.getDateTime().compareTo(o1.getDateTime()));

        updateEmptyState();
        adapter.notifyDataSetChanged();
    }

    private PEFLogModel createMockLog(String id, String date, float value) {
        String zone = calculateZone(value, parentConfiguredPB);
        return new PEFLogModel(id, date, value, zone, "user123");
    }

    /**
     * Core Business Logic for Zone Calculation
     * Green: >= 80% of PB
     * Yellow: 50% <= value < 80% of PB
     * Red: < 50% of PB
     */
    private String calculateZone(float value, float pb) {
        if (pb == 0) return "Unknown"; // Safety check

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