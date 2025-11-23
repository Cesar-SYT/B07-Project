package com.example.smartair.r3;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.model.Child;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordsActivity extends AppCompatActivity {

    private MedicineType selectedType = MedicineType.RESCUE;

    private R3Service r3Service;
    private Child currentChild;

    private EditText doseInput;
    private RecyclerView logRecycler;
    private MedicineLogAdapter adapter;
    private final List<MedicineLogEntry> logs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records_activity);

        currentChild = new Child("child001");
        r3Service = new R3ServiceImpl();

        Button btnRescue     = findViewById(R.id.button7);
        Button btnController = findViewById(R.id.button6);
        Button btnSave       = findViewById(R.id.btnSave2);
        doseInput = findViewById(R.id.editTextNumber);
        logRecycler = findViewById(R.id.logRecycler);

        btnRescue.setEnabled(false);
        btnController.setEnabled(true);

        btnRescue.setOnClickListener(v -> {
            selectedType = MedicineType.RESCUE;
            btnRescue.setEnabled(false);
            btnController.setEnabled(true);
        });

        btnController.setOnClickListener(v -> {
            selectedType = MedicineType.CONTROLLER;
            btnController.setEnabled(false);
            btnRescue.setEnabled(true);
        });

        logRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineLogAdapter(logs);
        logRecycler.setAdapter(adapter);

        btnSave.setOnClickListener(v -> onSaveClicked());
    }

    private void onSaveClicked() {
        String doseText = doseInput.getText().toString().trim();
        if (TextUtils.isEmpty(doseText)) {
            doseInput.setError("Please enter dose");
            return;
        }

        int dose = Integer.parseInt(doseText);

        if (selectedType == MedicineType.RESCUE) {
            r3Service.logRescueDose(currentChild, dose, null, null);
        } else {
            r3Service.logControllerDose(currentChild, dose, null, null);
        }

        MedicineLogEntry entry = new MedicineLogEntry(
                currentChild,
                selectedType,
                dose,
                new Date(),
                null,
                null
        );
        logs.add(0, entry);
        adapter.notifyItemInserted(0);
        logRecycler.scrollToPosition(0);

        doseInput.setText("");
    }

    private static class MedicineLogAdapter
            extends RecyclerView.Adapter<MedicineLogAdapter.LogViewHolder> {

        private final List<MedicineLogEntry> data;
        private final SimpleDateFormat timeFormat =
                new SimpleDateFormat("HH:mm", Locale.getDefault());

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
            holder.logDose.setText(entry.getDoseCount() + " puffs");

            if (entry.getTime() != null)
                holder.logTime.setText(timeFormat.format(entry.getTime()));
            else
                holder.logTime.setText("--:--");

            holder.logType.setText(
                    entry.getType() == MedicineType.RESCUE ? "Rescue" : "Controller"
            );
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class LogViewHolder extends RecyclerView.ViewHolder {
            final android.view.View colorDot;
            final android.widget.TextView logTime;
            final android.widget.TextView logType;
            final android.widget.TextView logDose;

            LogViewHolder(@NonNull android.view.View itemView) {
                super(itemView);
                colorDot = itemView.findViewById(R.id.colorDot);
                logTime  = itemView.findViewById(R.id.logTime);
                logType  = itemView.findViewById(R.id.logType);
                logDose  = itemView.findViewById(R.id.logDose);
            }
        }
    }
}
