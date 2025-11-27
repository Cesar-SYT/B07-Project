package com.example.smartair.ui.symptoms;

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
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.smartair.R;
import com.example.smartair.ui.r5model.SymptomEntry;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SymptomHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private SymptomHistoryAdapter adapter;
    private DatabaseReference db;
    private FirebaseAuth auth;
    private Button btnFilter;
    private ExtendedFloatingActionButton fabExport;
    List<SymptomEntry> fullHistoryList;

    public SymptomHistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.symptom_history_fragment, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_history);
        btnFilter = view.findViewById(R.id.button_filter);
        fabExport = view.findViewById(R.id.fab_export);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("Checkins");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SymptomHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        fullHistoryList = new ArrayList<>();

        // load full history from database
        String userId = auth.getCurrentUser().getUid();
        db.child(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    fullHistoryList.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        SymptomEntry entry = child.getValue(SymptomEntry.class);
                        if (entry != null) {
                            fullHistoryList.add(entry);
                        }
                    }

                    // sort the symptom entries with respect to timestamp
                    fullHistoryList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

                    adapter.updateList(fullHistoryList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        btnFilter.setOnClickListener(v ->
                openFilters());

        fabExport.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_symptomHistoryFragment_to_exportReportFragment);
        });

        return view;
    }

    private void openFilters() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View filterView = getLayoutInflater().inflate(
                R.layout.symptom_history_filter_sheet,
                null
        );

        dialog.setContentView(filterView);
        dialog.show();

        // find views inside the filter sheet
        CheckBox cbSleep = filterView.findViewById(R.id.filter_symptom_sleep);
        CheckBox cbActivity = filterView.findViewById(R.id.filter_symptom_activity);
        CheckBox cbCough = filterView.findViewById(R.id.filter_symptom_cough);

        CheckBox cbChild = filterView.findViewById(R.id.filter_author_child);
        CheckBox cbParent = filterView.findViewById(R.id.filter_author_parent);

        Button btnApply = filterView.findViewById(R.id.button_apply_filters);

        // --- apply filters ---
        btnApply.setOnClickListener(v -> {

            List<SymptomEntry> filteredHistory = new ArrayList<>();

            for (SymptomEntry e : fullHistoryList) {
                boolean ok = true;
                // --- symptom filters ---
                if (cbSleep.isChecked()) {
                    ok &= (e.sleep != null && !e.sleep.equals("Good"));
                }
                if (cbActivity.isChecked()) {
                    ok &= (e.activity != null && !e.activity.equals("Normal"));
                }
                if (cbCough.isChecked()) {
                    ok &= (e.cough != null && !e.cough.equals("No"));
                }

                // --- trigger filters ---
                // trigger filters
                CheckBox cbEx = filterView.findViewById(R.id.filter_trigger_exercise);
                CheckBox cbCold = filterView.findViewById(R.id.filter_trigger_coldair);
                CheckBox cbPets = filterView.findViewById(R.id.filter_trigger_pets);
                CheckBox cbSmoke = filterView.findViewById(R.id.filter_trigger_smoke);
                CheckBox cbIllness = filterView.findViewById(R.id.filter_trigger_illness);
                CheckBox cbPerfume = filterView.findViewById(R.id.filter_trigger_perfume);

                boolean triggerFilterUsed =
                        cbEx.isChecked() ||
                                cbCold.isChecked() ||
                                cbPets.isChecked() ||
                                cbSmoke.isChecked() ||
                                cbIllness.isChecked() ||
                                cbPerfume.isChecked();

                if (triggerFilterUsed) {
                    boolean matchTrigger = false;
                    List<String> t = e.triggers; // triggers of this entry

                    if (t != null) {
                        if (cbEx.isChecked() && t.contains("Exercise")) matchTrigger = true;
                        if (cbCold.isChecked() && t.contains("Cold air")) matchTrigger = true;
                        if (cbPets.isChecked() && t.contains("Pets")) matchTrigger = true;
                        if (cbSmoke.isChecked() && t.contains("Smoke")) matchTrigger = true;
                        if (cbIllness.isChecked() && t.contains("Illness")) matchTrigger = true;
                        if (cbPerfume.isChecked() && t.contains("Perfume/cleaners/strong odors")) matchTrigger = true;
                    }
                    if (!matchTrigger) {
                        ok = false;
                    }
                }

                // --- author filters ---
                if (cbChild.isChecked() && !cbParent.isChecked()) {
                    ok &= "child".equals(e.enteredBy);
                }
                if (cbParent.isChecked() && !cbChild.isChecked()) {
                    ok &= "parent".equals(e.enteredBy);
                }
                if (ok) {
                    filteredHistory.add(e);
                }
            }
            adapter.updateList(filteredHistory);
            dialog.dismiss();
        });
    }
}