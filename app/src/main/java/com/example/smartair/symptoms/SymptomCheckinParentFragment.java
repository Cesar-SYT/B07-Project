package com.example.smartair.symptoms;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.smartair.R;
import com.example.smartair.r5model.SymptomEntry;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SymptomCheckinParentFragment extends Fragment {

    private ChipGroup chipSleep;
    private ChipGroup chipActivity;
    private ChipGroup chipCough;
    private LinearLayout layoutTriggers;
    private ChipGroup chipGroupTriggers;
    private TextInputEditText editOtherTrigger;
    private Button btnSave;
    private Button btnExport;
    private FirebaseAuth auth;
    private String childEmail;

    private RecyclerView recyclerViewHistory;
    private SymptomHistoryAdapter historyAdapter;
    private List<SymptomEntry> historyList = new ArrayList<>();


    public SymptomCheckinParentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.symptom_checkin_fragment, container, false);

        auth = FirebaseAuth.getInstance();

        chipSleep = view.findViewById(R.id.chip_group_q1_sleep);
        chipActivity = view.findViewById(R.id.chip_group_q2_activity);
        chipCough = view.findViewById(R.id.chip_group_q3_cough);
        layoutTriggers = view.findViewById(R.id.layout_triggers);
        chipGroupTriggers = view.findViewById(R.id.chip_group_triggers);
        editOtherTrigger = view.findViewById(R.id.edit_text_other_trigger);
        btnSave = view.findViewById(R.id.button_save);
        btnExport = view.findViewById(R.id.button_export);

        recyclerViewHistory = view.findViewById(R.id.recycler_view_history);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new SymptomHistoryAdapter(historyList);
        recyclerViewHistory.setAdapter(historyAdapter);

        // if at least one of the symptoms is selected, triggers becomes visible
        chipSleep.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateTriggersVisibility();
        });
        chipActivity.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateTriggersVisibility();
        });
        chipCough.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateTriggersVisibility();
        });

        childEmail = getArguments().getString("childEmail");
        if (childEmail == null) {
            Toast.makeText(getContext(), "Error: No child selected.", Toast.LENGTH_LONG).show();
            return view;
        }
        // TODO: I need parent homepage pass me childEmail, before navigating here
        /*
        Bundle args = new Bundle();
        args.putString("childEmail", selectedChildEmail);

        navController.navigate(R.id.symptomParentCheckinFragment, args);
         */
        String key = childEmail.replace(".", ",");
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(key)
                .child("symptomCheckins");
        ref.get().addOnSuccessListener(snapshot -> {
            historyList.clear();
            for (DataSnapshot child : snapshot.getChildren()) {
                SymptomEntry entry = child.getValue(SymptomEntry.class);
                if (entry != null) {
                    historyList.add(entry);
                }
            }

            historyList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
            historyAdapter.updateList(historyList);
        });

        // final choices when button save is clicked
        btnSave.setOnClickListener(v -> {

            // role
            String enteredBy = "PARENT";

            // sleep
            int sleepId = chipSleep.getCheckedChipId();
            String sleep = null;
            if (sleepId != View.NO_ID) {
                if (sleepId == R.id.chip_sleep_good) {
                    sleep = "Sleep well";
                } else if (sleepId == R.id.chip_sleep_bad) {
                    sleep = "Night waking";
                }
            }

            // activity
            int activityId = chipActivity.getCheckedChipId();
            String activity = null;
            if (activityId != View.NO_ID) {
                if (activityId == R.id.chip_activity_normal) {
                    activity = "Activity normal";
                } else if (activityId == R.id.chip_activity_limited) {
                    activity = "Activity a bit challenging";
                } else if (activityId == R.id.chip_activity_hard) {
                    activity = "Activity very difficult";
                }
            }

            // cough
            int coughId = chipCough.getCheckedChipId();
            String cough = null;
            if (coughId != View.NO_ID) {
                if (coughId == R.id.chip_cough_no) {
                    cough = "No coughing";
                } else if (coughId == R.id.chip_cough_yes) {
                    cough = "Coughing";
                }
            }

            // triggers
            List<String> triggers = new ArrayList<>();
            int count = chipGroupTriggers.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = chipGroupTriggers.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (chip.isChecked()) {
                        triggers.add(chip.getText().toString());
                    }
                }
            }

            if (editOtherTrigger != null) {
                String otherTrigger = editOtherTrigger.getText().toString().trim();
                if (!otherTrigger.isEmpty()) {
                    triggers.add(otherTrigger);
                }
            }

            // save everything
            long timestamp = System.currentTimeMillis();

            SymptomEntry entry = new SymptomEntry(
                    sleep,
                    activity,
                    cough,
                    triggers,
                    timestamp,
                    enteredBy
            );

            ref.push().setValue(entry)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Daily check-in saved for your child.", Toast.LENGTH_SHORT).show();
                        historyList.add(0, entry);
                        historyAdapter.notifyItemInserted(0);
                        recyclerViewHistory.scrollToPosition(0);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        btnExport.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("childEmail", childEmail);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.symptomHistoryParentFragment, args);
        });
        return view;
    }

    private void updateTriggersVisibility() {
        boolean sleepSymptom = false;
        boolean activitySymptom = false;
        boolean coughSymptom = false;
        // Night waking
        int sleepId = chipSleep.getCheckedChipId();
        if (sleepId != View.NO_ID) {
            sleepSymptom = (sleepId != R.id.chip_sleep_good);
        }
        // Activity limit
        int activityId = chipActivity.getCheckedChipId();
        if (activityId != View.NO_ID) {
            activitySymptom = (activityId != R.id.chip_activity_normal);
        }
        // Cough
        int coughId = chipCough.getCheckedChipId();
        if (coughId != View.NO_ID) {
            coughSymptom = (coughId != R.id.chip_cough_no);
        }

        if (sleepSymptom || activitySymptom || coughSymptom){
            layoutTriggers.setVisibility(View.VISIBLE);
        }
        else{
            layoutTriggers.setVisibility(View.GONE);
        }
    }
}