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

/**
 * RecordsActivity
 *
 * - Rescue vs Controller logging kept distinct
 * - Each entry captures timestamp, dose count, breath rating, feel
 * - Logs are stored under /users/{childKey}/medicine_logs
 */
public class RecordsActivity extends AppCompatActivity {

    // --- R3 core service ---
    private R3Service r3Service;

    // 当前 child（从 R3MainActivity 传进来的）
    private Child currentChild;
    private String childKey;
    private String childName;

    // --- UI 控件 ---
    private Button btnRescue;
    private Button btnController;
    private Button btnSave;

    private EditText doseInput;
    private RadioGroup preGroup;
    private SeekBar breathSeek;
    private RecyclerView logRecycler;

    // 当前选中的类型 / 心情
    private MedicineType selectedType = null;
    private String selectedFeel = "Same";   // 默认 same

    // 日志列表
    private final List<MedicineLogEntry> logs = new ArrayList<>();
    private MedicineLogAdapter adapter;

    // Firebase 直接读取 logs 用
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

        // 1. 从 Intent 拿 child 信息
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

        // 2. 初始化 UI
        initViews();
        setupTypeButtons();
        setupFeelGroup();
        setupRecycler();

        // 3. 读取数据库中的历史日志
        loadLogsFromDatabase();
    }

    // ------------------------ UI 初始化 ------------------------

    private void initViews() {
        btnRescue     = findViewById(R.id.button7);
        btnController = findViewById(R.id.button6);
        btnSave       = findViewById(R.id.btnSave2);

        doseInput  = findViewById(R.id.editTextNumber);
        preGroup   = findViewById(R.id.preGroup);
        breathSeek = findViewById(R.id.breathSeek3);
        logRecycler = findViewById(R.id.logRecycler);

        // 初始：两个类型按钮都半透明，Save 不可点
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

    // ------------------------ 保存 Log ------------------------

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

        // 1) 调用 R3Service，把 log 写入数据库
        if (selectedType == MedicineType.RESCUE) {
            r3Service.logRescueDose(currentChild, dose, breathRating, feel);
        } else {
            r3Service.logControllerDose(currentChild, dose, breathRating, feel);
        }

        // 2) 本地也插入一条，刷新 UI（不必重新读数据库）
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

    // ------------------------ 读取历史 Log ------------------------

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

                        // 按时间倒序（如果 timeStr 是升序存的，这里简单 reverse 一下）
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

    // ------------------------ RecyclerView Adapter ------------------------

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

            // 第一行：时间
            holder.logTime.setText(
                    entry.getTime() != null ? entry.getTime() : "--"
            );

            // 类型：RESCUE / CONTROLLER
            holder.logType.setText(entry.getType() != null ? entry.getType().name() : "");

            // 剂量：右侧
            holder.logDose.setText("Dose: " + entry.getDoseCount() + " puffs");

            // 额外信息：呼吸+感觉 -> 放在第二行 TextView，下方
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

                // 你原来的 layout 只有三行，如果你在 layout 里额外加了一个 TextView
                // （比如 id = logExtra），这里就绑定它。
                // 如果你还没加，可以把 logType 改为 extra 信息也行。
                android.widget.TextView extra;
                try {
                    extra = itemView.findViewById(R.id.logExtra);
                } catch (Exception e) {
                    // 如果没有这个 id，就把 type 当成 extra 行使用
                    extra = logType;
                }
                extraLine = extra;
            }
        }
    }
}
