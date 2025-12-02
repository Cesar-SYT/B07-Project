package com.example.smartair;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParentHomeActivity extends AppCompatActivity {

    private ConstraintLayout rootLayout;
    private TextView txtParentTitle;
    private TextView txtCurrentChildName;
    private Button btnChildSelector;
    private View cardZoneParent;
    private TextView txtZoneParent;
    private TextView txtRescueLastUsed;
    private TextView txtRescueThisWeek;
    private TextView txtControllerToday;
    private TextView txtControllerWeek;
    private RadioGroup rgTrendRange;
    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private final List<Object> notificationItems = new ArrayList<>();
    private Button btnAddLogParent;
    private Button btnViewReportParent;
    private Button btnSharingSettingsParent;
    private Button btnSymptomCheckin;
    private String currentChildName = "Child";
    private String currentChildKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_page);

        findViewById(R.id.nav_host_fragment_parent_home).setVisibility(View.GONE);

        initViews();
        setupTopBar();
        setupTrendSelector();
        setupNotificationsRecycler();
        setupQuickActions();
        setupZoneCardListener();

        String nameFromIntent = getIntent().getStringExtra("childName");
        if (nameFromIntent != null && !nameFromIntent.isEmpty()) {
            currentChildName = nameFromIntent;
        }

        currentChildKey = getIntent().getStringExtra("childKey");
        loadDataForChild(currentChildName);
        listenForAlerts(); // Start listening for alerts

        btnChildSelector.setOnClickListener(v -> {
            Intent intent = new Intent(ParentHomeActivity.this, ManageChildrenActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        rootLayout = findViewById(R.id.parentHomeRoot);
        txtParentTitle = findViewById(R.id.txtParentTitle);
        txtCurrentChildName = findViewById(R.id.txtCurrentChildName);
        btnChildSelector = findViewById(R.id.btnChildSelector);
        cardZoneParent = findViewById(R.id.cardZoneParent);
        txtZoneParent = findViewById(R.id.txtZoneParent);
        txtRescueLastUsed = findViewById(R.id.txtRescueLastUsed);
        txtRescueThisWeek = findViewById(R.id.txtRescueThisWeek);
        txtControllerToday = findViewById(R.id.txtControllerToday);
        txtControllerWeek = findViewById(R.id.txtControllerWeek);
        rgTrendRange = findViewById(R.id.rgTrendRange);
        rvNotifications = findViewById(R.id.rvNotifications);
        btnAddLogParent = findViewById(R.id.btnAddLogParent);
        btnViewReportParent = findViewById(R.id.btnViewReportParent);
        btnSharingSettingsParent = findViewById(R.id.btnSharingSettingsParent);
        btnSymptomCheckin = findViewById(R.id.btnSymptomCheckin);
    }

    private void setupTopBar() {
        txtParentTitle.setText("SMART AIR");
        txtCurrentChildName.setText("Child: " + currentChildName);
    }

    private void setupTrendSelector() {
        rgTrendRange.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb7days) {
                onTrendRangeChanged(7);
            } else if (checkedId == R.id.rb30days) {
                onTrendRangeChanged(30);
            }
        });
    }

    private void onTrendRangeChanged(int days) {
        // Placeholder
    }

    private void setupNotificationsRecycler() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(notificationItems);
        rvNotifications.setAdapter(notificationAdapter);
    }

    private void setupQuickActions() {
        btnAddLogParent.setOnClickListener(v -> {
            Intent intent = new Intent(ParentHomeActivity.this, com.example.smartair.r3.R3MainActivity.class);
            intent.putExtra("childName", currentChildName);
            intent.putExtra("childKey", currentChildKey);
            startActivity(intent);
        });

        btnViewReportParent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChildDetail.class);
            intent.putExtra("CHILD_ID", currentChildKey);
            startActivity(intent);
        });

        btnSharingSettingsParent.setOnClickListener(v -> {
            Intent intent = new Intent(ParentHomeActivity.this, SharingActivity.class);
            intent.putExtra("childKey", currentChildKey);
            intent.putExtra("childName", currentChildName);
            startActivity(intent);
        });
        
        btnSymptomCheckin.setOnClickListener(v -> {
            findViewById(R.id.nav_host_fragment_parent_home).setVisibility(View.VISIBLE);
            findViewById(R.id.parentTopBar).setVisibility(View.GONE);
            findViewById(R.id.scrollParentHome).setVisibility(View.GONE);
            findViewById(R.id.parentHomeRoot).setBackgroundColor(android.graphics.Color.TRANSPARENT);
            
            Bundle bundle = new Bundle();
            bundle.putString("childKey", currentChildKey);
            NavController navController = Navigation.findNavController(ParentHomeActivity.this, R.id.nav_host_fragment_parent_home);
            navController.navigate(R.id.symptomCheckinParentFragment, bundle);
        });
    }

    private void setupZoneCardListener() {
        cardZoneParent.setOnClickListener(v -> showSetPBDialog());
    }

    private void showSetPBDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Baseline PEF (PB)");
        builder.setMessage("Enter the child's personal best Peak Expiratory Flow (L/min):");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String pbStr = input.getText().toString();
            if (!pbStr.isEmpty()) {
                try {
                    float pbVal = Float.parseFloat(pbStr);
                    savePBToFirebase(pbVal);
                } catch (NumberFormatException e) {
                    Toast.makeText(ParentHomeActivity.this, "Invalid number format", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void savePBToFirebase(float pbVal) {
        if (currentChildKey == null) {
            Toast.makeText(this, "Error: No child selected", Toast.LENGTH_SHORT).show();
            return;
        }
        PEFDataRepository.getInstance().saveParentConfiguredPB(currentChildKey, pbVal, new PEFDataRepository.OnSaveListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ParentHomeActivity.this, "Baseline PEF updated!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(ParentHomeActivity.this, "Failed to save: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDataForChild(@NonNull String childName) {
        currentChildName = childName;
        txtCurrentChildName.setText("Child: " + childName);
        updateZoneCard("Green");
        txtRescueLastUsed.setText("Last: N/A");
        txtRescueThisWeek.setText("This week: 0 puffs");
        txtControllerToday.setText("Today: 0/0 doses");
        txtControllerWeek.setText("This week: 0/7 days");
        loadDummyNotifications();
    }

    private void updateZoneCard(@NonNull String zoneName) {
        txtZoneParent.setText("Today: " + zoneName + " Zone\n(Click to set PB)");
    }

    private void loadDummyNotifications() {
        notificationItems.clear();
        notificationItems.add(new NotificationItem("New report available", "Weekly asthma control report is ready to view."));
        notificationAdapter.notifyDataSetChanged();
    }
    
    private void listenForAlerts() {
        FirebaseUser parent = FirebaseAuth.getInstance().getCurrentUser();
        if (parent == null || parent.getEmail() == null) return;
        String parentKey = parent.getEmail().replace(".", ",");

        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("alerts").child(parentKey);
        alertsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationItems.clear(); // Clear existing to refresh
                loadDummyNotifications(); // Reload static notifications
                for (DataSnapshot alertSnapshot : snapshot.getChildren()) {
                    Alert alert = alertSnapshot.getValue(Alert.class);
                    if (alert != null && "new".equals(alert.getStatus())) {
                        notificationItems.add(0, alert); // Add new alerts to the top
                    }
                }
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentHomeActivity.this, "Failed to listen for alerts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Notification & Alert Handling Classes ---

    static class NotificationItem {
        final String title;
        final String message;
        NotificationItem(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtMessage;
        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(android.R.id.text1);
            txtMessage = itemView.findViewById(android.R.id.text2);
        }
    }

    static class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

        private final List<Object> items;

        NotificationAdapter(List<Object> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new NotificationViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            Object item = items.get(position);
            if (item instanceof Alert) {
                Alert alert = (Alert) item;
                String title = "🚨 ALERT: " + alert.getChildName();
                String message = "Triage session started at " + new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(alert.getTimestamp()));
                holder.txtTitle.setText(title);
                holder.txtMessage.setText(message);
                holder.itemView.setBackgroundColor(Color.parseColor("#FFCDD2")); // Light red for alerts
            } else if (item instanceof NotificationItem) {
                NotificationItem notification = (NotificationItem) item;
                holder.txtTitle.setText(notification.title);
                holder.txtMessage.setText(notification.message);
                holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Default background
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
