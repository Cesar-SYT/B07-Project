package com.example.smartair.r3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.model.Child;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class RecordsActivity extends AppCompatActivity {

    private R3Service r3Service;

    private Child currentChild;
    private String childKey;
    private String childName;

    private Button btnRescue;
    private Button btnController;
    private Button btnSave;

    private EditText doseInput;
    private RadioGroup preGroup;
    private SeekBar breathSeek;
    private RecyclerView logRecycler;

    private MedicineType selectedType = null;
    private String selectedFeel = "Same";

    private final List<MedicineLogEntry> logs = new ArrayList<>();
    private MedicineLogAdapter adapter;

    private DatabaseReference rootRef;

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records_activity);

        r3Service = new R3ServiceImpl();
        rootRef = FirebaseDatabase.getInstance(
                "https://smart-air-61888-default-rtdb.firebaseio.com/"
        ).getReference();

        childKey = getIntent().getStringExtra("childKey");
        childName = getIntent().getStringExtra("childName");

        if (childKey == null || childKey.trim().isEmpty()) {
            Toast.makeText(this, "Missing child info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (childName == null || childName.trim().isEmpty()) {
            childName = childKey;
        }

        currentChild = new Child(childKey, childName);

        initViews();
        setupTypeButtons();
        setupFeelGroup();
        setupRecycler();

        loadLogsFromDatabase();
    }


    private void initViews() {
        btnRescue     = findViewById(R.id.button7);
        btnController = findViewById(R.id.button6);
        btnSave       = findViewById(R.id.btnSave2);

        doseInput  = findViewById(R.id.editTextNumber);
        preGroup   = findViewById(R.id.preGroup);
        breathSeek = findViewById(R.id.breathSeek3);
        logRecycler = findViewById(R.id.logRecycler);

        btnRescue.setAlpha(0.5f);
        btnController.setAlpha(0.5f);
        btnSave.setEnabled(false);
    }

    private void setupTypeButtons() {
        btnRescue.setOnClickListener(v -> {
            selectedType = MedicineType.RESCUE;
            btnRescue.setAlpha(1f);
            btnController.setAlpha(0.5f);
            btnSave.setEnabled(true);
        });

        btnController.setOnClickListener(v -> {
            selectedType = MedicineType.CONTROLLER;
            btnController.setAlpha(1f);
            btnRescue.setAlpha(0.5f);
            btnSave.setEnabled(true);
        });

        btnSave.setOnClickListener(v -> onSaveClicked());
    }

    private void setupFeelGroup() {
        preGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb != null) {
                selectedFeel = rb.getText().toString(); // "Better" / "Same" / "Worse"
            }
        });
    }

    private void setupRecycler() {
        logRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineLogAdapter(logs);
        logRecycler.setAdapter(adapter);
    }

    // save log

    private void onSaveClicked() {
        if (selectedType == null) {
            Toast.makeText(this, "Please choose Rescue or Controller", Toast.LENGTH_SHORT).show();
            return;
        }

        String doseText = doseInput.getText().toString().trim();
        if (doseText.isEmpty()) {
            doseInput.setError("Please enter dose");
            return;
        }

        int dose;
        try {
            dose = Integer.parseInt(doseText);
        } catch (NumberFormatException e) {
            doseInput.setError("Invalid number");
            return;
        }

        int breathRating = breathSeek.getProgress() + 1; // SeekBar 0–4 -> 1–5
        String feel = selectedFeel;

        Date now = new Date();
        String timeStr = timeFormat.format(now);

        if (selectedType == MedicineType.RESCUE) {
            r3Service.logRescueDose(currentChild, dose, breathRating, feel);
        } else {
            r3Service.logControllerDose(currentChild, dose, breathRating, feel);
        }

        MedicineLogEntry entry = new MedicineLogEntry(
                dose,
                breathRating,
                feel,
                timeStr,
                selectedType  // "RESCUE" / "CONTROLLER"
        );
        logs.add(0, entry);
        adapter.notifyItemInserted(0);
        logRecycler.scrollToPosition(0);

        doseInput.setText("");
    }

    // history log

    private void loadLogsFromDatabase() {
        rootRef.child("users")
                .child(childKey)
                .child("medicine_logs")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        logs.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            MedicineLogEntry entry = snap.getValue(MedicineLogEntry.class);
                            if (entry != null) {
                                logs.add(entry);
                            }
                        }

                        Collections.reverse(logs);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(RecordsActivity.this,
                                "Failed to load logs: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private static class MedicineLogAdapter
            extends RecyclerView.Adapter<MedicineLogAdapter.LogViewHolder> {

        private final List<MedicineLogEntry> data;

        MedicineLogAdapter(List<MedicineLogEntry> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent,
                                                int viewType) {
            android.view.View view = android.view.LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.medicine_log_layout, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            MedicineLogEntry entry = data.get(position);

            holder.logTime.setText(
                    entry.getTime() != null ? entry.getTime() : "--"
            );

            holder.logType.setText(entry.getType() != null ? entry.getType().name() : "");

            holder.logDose.setText("Dose: " + entry.getDoseCount() + " puffs");

            String extra = "Breath: " + entry.getBreathRate() + "/5, " +
                    "Feel: " + (entry.getFeel() != null ? entry.getFeel() : "--");
            holder.extraLine.setText(extra);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class LogViewHolder extends RecyclerView.ViewHolder {
            final View colorDot;
            final android.widget.TextView logTime;
            final android.widget.TextView logType;
            final android.widget.TextView logDose;
            final android.widget.TextView extraLine;

            LogViewHolder(@NonNull android.view.View itemView) {
                super(itemView);
                colorDot  = itemView.findViewById(R.id.colorDot);
                logTime   = itemView.findViewById(R.id.logTime);
                logType   = itemView.findViewById(R.id.logType);
                logDose   = itemView.findViewById(R.id.logDose);

                android.widget.TextView extra;
                try {
                    extra = itemView.findViewById(R.id.logExtra);
                } catch (Exception e) {
                    extra = logType;
                }
                extraLine = extra;
            }
        }
    }
}
