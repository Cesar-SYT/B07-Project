package com.example.smartair.r3;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.model.Child;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    /** 从 Firebase 读取当前 child 信息，然后再初始化界面 */
    /** 在 currentChild 加载完成后初始化 UI 组件和监听器 */
    private void initUIAfterChildLoaded() {

        Button btnRescue     = findViewById(R.id.button7);
        Button btnController = findViewById(R.id.button6);
        Button btnSave       = findViewById(R.id.btnSave2);
        doseInput            = findViewById(R.id.editTextNumber);
        logRecycler          = findViewById(R.id.logRecycler);

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

    private void loadChildFromServer() {

        // 1. 拿当前登录用户
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        // 2. 从数据库读取 fullName
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("users").child(uid).child("fullName")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String fullName = snapshot.getValue(String.class);

                    if (fullName == null || fullName.isEmpty()) {
                        fullName = "Child";   // fallback
                    }

                    // 3. 创建 Child 对象（id = uid，name = fullName）
                    currentChild = new Child(uid, fullName);

                    // 有了 currentChild 之后再继续初始化页面
                    initUIAfterChildLoaded();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load child: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // 失败也给一个占位的 child
                    currentChild = new Child(uid, "Unknown");
                    initUIAfterChildLoaded();
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records_activity);

        r3Service = new R3ServiceImpl();

        // 从服务器加载当前 child（uid + fullName），加载完再初始化 UI
        loadChildFromServer();
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
