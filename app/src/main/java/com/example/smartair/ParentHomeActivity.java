package com.example.smartair;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentHomeActivity extends AppCompatActivity {

    private static final boolean TEST_MODE_ENABLED = true;
    private static final long TEST_CURRENT_TIME_MS = 1764576000000L;

    private DatabaseReference mChildDataRef;
    private ValueEventListener mChildDataListener;

    private DatabaseReference mAlertsRef;
    private ValueEventListener mAlertsListener;

    private ConstraintLayout rootLayout;
    private TextView txtParentTitle;
    private TextView txtCurrentChildName;


    private View cardZoneParent;
    private TextView txtZoneParent;

    private TextView txtRescueLastUsed;
    private TextView txtRescueThisWeek;

    private TextView txtControllerToday;
    private TextView txtControllerWeek;
    private TextView txtLinkedProvider;

    private RadioGroup rgTrendRange;
    private View viewTrendPlaceholder;
    private RadioButton rb7days, rb30days;
    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private final List<Object> notificationItems = new ArrayList<>();

    private Button btnAddLogParent;
    private Button btnViewReportParent;
    private Button btnSharingSettingsParent;
    private Button btnSymptomCheckin;
    private Button btnLinkProvider;
    private Button btnChildSelector;

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
        setupLinkProviderButton();

        String nameFromIntent = getIntent().getStringExtra("childName");
        if (nameFromIntent != null && !nameFromIntent.isEmpty()) {
            currentChildName = nameFromIntent;
        }

        currentChildKey = getIntent().getStringExtra("childKey");
        if (currentChildKey == null || currentChildKey.isEmpty()) {
            Log.e("ParentHomeActivity", "Error: Missing 'childKey' Intent extra.");
        }

        loadDataForChild(currentChildName);

        rgTrendRange.setOnCheckedChangeListener((group, checkedId) -> {
            int days = (checkedId == R.id.rb7days) ? 7 : 30;
            loadTrendChart(days);
        });

        btnChildSelector.setOnClickListener(v -> {
            Intent intent = new Intent(ParentHomeActivity.this, ManageChildrenActivity.class);
            startActivity(intent);
        });

        loadTrendChart(7);
        listenForAlerts();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDataListener();
        removeAlertsListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentChildKey != null && !currentChildKey.isEmpty()) {
            startRealtimeDataListener(currentChildKey);
            loadTrendChart((rb7days != null && rb7days.isChecked() ? 7 : 30));
        }
        listenForAlerts();
        listenForLinkedProvider();
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

        viewTrendPlaceholder = findViewById(R.id.viewTrendPlaceholder);
        rb7days = findViewById(R.id.rb7days);
        rb30days = findViewById(R.id.rb30days);

        btnAddLogParent = findViewById(R.id.btnAddLogParent);
        btnViewReportParent = findViewById(R.id.btnViewReportParent);
        btnSharingSettingsParent = findViewById(R.id.btnSharingSettingsParent);
        btnSymptomCheckin = findViewById(R.id.btnSymptomCheckin);
        btnLinkProvider = findViewById(R.id.btnLinkProvider);

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(notificationItems);
        rvNotifications.setAdapter(notificationAdapter);

        txtLinkedProvider = findViewById(R.id.txtLinkedProvider);

    }

    private void setupTopBar() {
        txtParentTitle.setText("SMART AIR");
        txtCurrentChildName.setText("Child: " + currentChildName);
    }
    private void loadProviderDisplayName(String providerId) {

        DatabaseReference providerRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(providerId);

        providerRef.child("displayName").get()
                .addOnSuccessListener(snapshot -> {
                    String displayName = snapshot.getValue(String.class);

                    if (displayName != null && !displayName.isEmpty()) {
                        txtLinkedProvider.setText("Linked Doctor: " + displayName);
                        txtLinkedProvider.setTextColor(Color.parseColor("#4CAF50"));
                    } else {
                        txtLinkedProvider.setText("Linked Doctor: " + providerId.replace(",", "."));
                        txtLinkedProvider.setTextColor(Color.parseColor("#4CAF50"));
                    }
                })
                .addOnFailureListener(e -> {
                    txtLinkedProvider.setText("Linked Doctor (Failed to load)");
                    txtLinkedProvider.setTextColor(Color.parseColor("#757575"));
                });
    }

    private void listenForLinkedProvider() {
        if (currentChildKey == null) return;
        DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("users").child(currentChildKey);
        childRef.child("providerId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String providerId = snapshot.getValue(String.class);
                if (providerId != null && !providerId.isEmpty()) {
                    loadProviderDisplayName(providerId);
                } else {
                    txtLinkedProvider.setText("Not linked to any doctor");
                    txtLinkedProvider.setTextColor(Color.parseColor("#757575")); // Grey
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupLinkProviderButton() {
        btnLinkProvider.setOnClickListener(v -> showLinkProviderDialog());
    }

    private void showLinkProviderDialog() {
        if (currentChildKey == null) {
            Toast.makeText(this, "Please select a child first.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Link to Doctor");
        builder.setMessage("Enter the Doctor's unique ID (email address usually):");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Link", (dialog, which) -> {
            String rawInput = input.getText().toString().trim();
            String providerId = rawInput.replace(".", ",");

            if (!providerId.isEmpty()) {
                linkChildToProvider(providerId);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void linkChildToProvider(String providerId) {
        DatabaseReference childRef = FirebaseDatabase.getInstance().getReference("users").child(currentChildKey);
        childRef.child("providerId").setValue(providerId)
                .addOnSuccessListener(aVoid -> Toast.makeText(ParentHomeActivity.this, "Linked to Doctor successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ParentHomeActivity.this, "Failed to link: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void setupTrendSelector() {
    }

    private void setupNotificationsRecycler() {
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

    private void listenForAlerts() {
        removeAlertsListener();

        FirebaseUser parent = FirebaseAuth.getInstance().getCurrentUser();
        if (parent == null || parent.getEmail() == null) return;

        String parentKey = parent.getEmail().replace(".", ",");

        mAlertsRef = FirebaseDatabase.getInstance().getReference("alerts").child(parentKey);

        mAlertsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                notificationItems.removeIf(item -> item instanceof Alert);

                for (DataSnapshot alertSnapshot : snapshot.getChildren()) {

                    Alert alert = alertSnapshot.getValue(Alert.class);
                    if (alert != null && "new".equals(alert.getStatus())) {
                        notificationItems.add(0, alert);
                    }
                }
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to listen for alerts.", error.toException());
                Toast.makeText(ParentHomeActivity.this, "Failed to listen for alerts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mAlertsRef.addValueEventListener(mAlertsListener);
    }

    private void removeAlertsListener() {
        if (mAlertsRef != null && mAlertsListener != null) {
            mAlertsRef.removeEventListener(mAlertsListener);
            Log.d("Firebase", "Removed alerts listener.");
        }
        mAlertsRef = null;
        mAlertsListener = null;
    }


    private void startRealtimeDataListener(String childKey) {
        removeDataListener();

        if (childKey == null || childKey.isEmpty()) {
            Log.e("Firebase", "Child Key is null or empty, cannot start listener.");
            return;
        }

        mChildDataRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(childKey);

        final long now = TEST_MODE_ENABLED ? TEST_CURRENT_TIME_MS : System.currentTimeMillis();

        mChildDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Firebase", "Data updated for " + childKey + ". Processing UI updates.");


                notificationItems.removeIf(item -> item instanceof NotificationItem);


                long oneWeek = 7L * 24 * 60 * 60 * 1000;
                long oneDay = 24L * 60 * 60 * 1000;
                long threeHoursAgo = now - (3 * 60 * 60 * 1000);

                int rescueThisWeek = 0;
                long lastRescueTime = 0;
                int controllerToday = 0;
                String lastRescueTimeStr = "N/A";
                int rescueCount3H = 0;


                for (DataSnapshot log : snapshot.child("medicine_logs").getChildren()) {
                    String type = log.child("type").getValue(String.class);
                    String timeString = log.child("time").getValue(String.class);
                    Long doseCount = log.child("doseCount").getValue(Long.class);
                    int dose = (doseCount != null) ? doseCount.intValue() : 0;

                    long ts = parseTime(timeString);
                    if (ts < 0) continue;

                    if ("RESCUE".equals(type)) {
                        if (ts > lastRescueTime) {
                            lastRescueTime = ts;
                            lastRescueTimeStr = timeString;
                        }
                        if (now - ts < oneWeek) {
                            rescueThisWeek += dose;
                        }

                        if (ts > threeHoursAgo) {
                            rescueCount3H += dose;
                        }
                    }

                    if ("CONTROLLER".equals(type)) {
                        if (now - ts < oneDay) {
                            controllerToday += dose;
                        }
                    }

                    String feel = log.child("feel").getValue(String.class);
                    if ("Worse".equalsIgnoreCase(feel)) {
                        notificationItems.add(new NotificationItem(
                                "Worsening Symptoms",
                                "Symptoms felt WORSE after medication."
                        ));

                    }
                }

                if (rescueCount3H >= 3) {
                    notificationItems.add(new NotificationItem(
                            "Frequent Rescue Use",
                            "More than 3 rescue uses in the past 3 hours (" + rescueCount3H + " doses)."
                    ));
                }

                String lastUsedDisplay = lastRescueTimeStr.equals("N/A") ? "N/A" : lastRescueTimeStr.substring(0, 16);
                txtRescueLastUsed.setText("Last: " + lastUsedDisplay);
                txtRescueThisWeek.setText("This week: " + rescueThisWeek + " puffs");

                txtControllerToday.setText("Today: " + controllerToday + "/2 doses");

                Long streak = snapshot.child("motivation").child("streaks").child("CONTROLLER_DAYS").getValue(Long.class);
                int controllerStreak = (streak != null) ? streak.intValue() : 0;
                txtControllerWeek.setText("Streak: " + controllerStreak + " days on track");



                Integer PB = snapshot.child("pb").getValue(Integer.class);
                String currentZone = "Unknown";

                if (PB != null && PB > 0) {
                    currentZone = determineZone(snapshot);
                    updateZoneCard(currentZone);


                    for (DataSnapshot log : snapshot.child("pef_logs").getChildren()) {

                        Integer pefValue = log.child("preValue").getValue(Integer.class);

                        if (pefValue != null && pefValue < PB * 0.5) {
                            notificationItems.add(new NotificationItem(
                                    "Red Zone Alert",
                                    "Today's peak flow dropped into the RED zone."
                            ));
                            break;
                        }
                    }
                } else {
                    updateZoneCard("Unknown (Set PB)");
                }

                if (snapshot.hasChild("events")) {
                    for (DataSnapshot d : snapshot.child("events").getChildren()) {
                        String note = d.child("note").getValue(String.class);
                        if (note != null && (note.toLowerCase().contains("emergency") || note.toLowerCase().contains("er visit"))) {
                            notificationItems.add(new NotificationItem(
                                    "Emergency Visit",
                                    "Emergency/urgent care event reported."
                            ));
                            break;
                        }
                    }
                }

                if (snapshot.hasChild("inventory")) {
                    for (DataSnapshot med : snapshot.child("inventory").getChildren()) {
                        Long totalDosesLong = med.child("totalDoses").getValue(Long.class);
                        Long remainingDosesLong = med.child("remainingDoses").getValue(Long.class);
                        String medName = med.child("name").getValue(String.class);

                        int totalDoses = (totalDosesLong != null) ? totalDosesLong.intValue() : 0;
                        int remainingDoses = (remainingDosesLong != null) ? remainingDosesLong.intValue() : 0;

                        boolean isLowCalculated = false;
                        if (totalDoses > 0) {
                            double percentRemaining = (double) remainingDoses / totalDoses;
                            if (percentRemaining <= 0.20) {
                                isLowCalculated = true;
                            }
                        }
                        Boolean lowFlag = med.child("low").getValue(Boolean.class);
                        if (isLowCalculated || Boolean.TRUE.equals(lowFlag)) {
                            notificationItems.add(new NotificationItem(
                                    "Low Inventory",
                                    medName + " is running low (Remaining: " + remainingDoses + "/" + totalDoses + ")."
                            ));
                        }

                        Boolean expiredFlagObject = med.child("expiredFlag").getValue(Boolean.class);
                        boolean isExpired = (expiredFlagObject != null) && expiredFlagObject;
                        if (!isExpired) {
                            DataSnapshot expiryDateSnapshot = med.child("expiryDate");
                            if (expiryDateSnapshot.exists()) {
                                Long expiryTimestampMs = expiryDateSnapshot.child("time").getValue(Long.class);
                                if (expiryTimestampMs != null && now > expiryTimestampMs) {
                                    isExpired = true;
                                }
                            }
                        }
                        if (isExpired) {
                            notificationItems.add(new NotificationItem(
                                    "Expired Medication",
                                    medName + " has expired."
                            ));
                        }
                    }
                }

                notificationItems.sort((o1, o2) -> {
                    if (o1 instanceof Alert && o2 instanceof NotificationItem) return -1;
                    if (o1 instanceof NotificationItem && o2 instanceof Alert) return 1;
                    return 0;
                });

                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read child data for real-time update.", error.toException());
            }
        };

        mChildDataRef.addValueEventListener(mChildDataListener);
    }

    private void removeDataListener() {
        if (mChildDataRef != null && mChildDataListener != null) {
            mChildDataRef.removeEventListener(mChildDataListener);
            Log.d("Firebase", "Removed listener for " + currentChildKey);
        }
        mChildDataRef = null;
        mChildDataListener = null;
    }

    private String determineZone(DataSnapshot snapshot) {
        Integer PB = snapshot.child("pb").getValue(Integer.class);
        if (PB == null || PB <= 0) {
            return "Unknown (Set PB)";
        }

        Integer currentPEF = null;

        DataSnapshot pefLogsSnapshot = snapshot.child("pef_logs");

        if (pefLogsSnapshot.exists()) {
            long latestTimestamp = 0;

            for (DataSnapshot log : pefLogsSnapshot.getChildren()) {
                Long timestamp = log.child("timestamp").getValue(Long.class);

                if (timestamp != null && timestamp > latestTimestamp) {
                    latestTimestamp = timestamp;

                    Integer postValue = log.child("postValue").getValue(Integer.class);
                    Integer preValue = log.child("preValue").getValue(Integer.class);

                    currentPEF = preValue;
                }
            }
        }

        if (currentPEF == null) {
            return "No PEF Data";
        }

        double ratio = (double) currentPEF / PB;

        if (ratio >= 0.8) {
            return "Green";
        } else if (ratio >= 0.5) {
            return "Yellow";
        } else {
            return "Red";
        }
    }

    private void loadDataForChild(@NonNull String childName) {
        currentChildName = childName;
        txtCurrentChildName.setText("Child: " + childName);

        startRealtimeDataListener(currentChildKey);
    }

    private void updateZoneCard(@NonNull String zoneName) {
        txtZoneParent.setText("Today: " + zoneName + " Zone\n(Click to set PB)");
    }

    private long parseTime(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            Date date = sdf.parse(timeString);
            return (date != null) ? date.getTime() : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private void drawTrendChart(int[] values, int days) {

        if (viewTrendPlaceholder.getWidth() == 0 || viewTrendPlaceholder.getHeight() == 0) {
            viewTrendPlaceholder.post(() -> drawTrendChart(values, days));
            return;
        }

        Bitmap bmp = Bitmap.createBitmap(
                viewTrendPlaceholder.getWidth(),
                viewTrendPlaceholder.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int w = canvas.getWidth();
        int h = canvas.getHeight();

        paint.setColor(0xFFECEFF1);
        canvas.drawRect(0, 0, w, h, paint);

        int max = 1;
        for (int v : values)
            if (v > max) max = v;

        paint.setColor(0xFF0277BD);
        paint.setStrokeWidth(6);

        float dx = (float) w / (days - 1);

        float prevX = 0;

        float prevY = h - (values[0] * (h / (float) max));

        for (int i = 1; i < days; i++) {
            float x = i * dx;
            float y = h - (values[i] * (h / (float) max));

            canvas.drawLine(prevX, prevY, x, y, paint);

            prevX = x;
            prevY = y;
        }

        viewTrendPlaceholder.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), bmp));
    }

    private void loadTrendChart(int days) {
        if (currentChildKey == null || currentChildKey.isEmpty()) {
            Log.e("Firebase", "Child Key is null or empty, cannot load trend chart.");
            return;
        }

        long now = TEST_MODE_ENABLED ? TEST_CURRENT_TIME_MS : System.currentTimeMillis();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentChildKey)
                .child("medicine_logs");

        ref.get().addOnSuccessListener(snapshot -> {

            long oneDay = 24L * 60 * 60 * 1000;
            int[] daily = new int[days];

            for (DataSnapshot log : snapshot.getChildren()) {

                String type = log.child("type").getValue(String.class);
                if (!"RESCUE".equals(type)) continue;

                String timeString = log.child("time").getValue(String.class);
                long ts = parseTime(timeString);
                if (ts < 0) continue;

                long diff = now - ts;

                int index = (int)(diff / oneDay);

                if (index >= 0 && index < days) {
                    Long doseCount = log.child("doseCount").getValue(Long.class);

                    daily[days - 1 - index] += (doseCount != null) ? doseCount.intValue() : 1;
                }
            }

            drawTrendChart(daily, days);
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Failed to load trend chart for child: " + currentChildKey, e);
        });
    }


    static class NotificationItem {
        final String title;
        final String message;

        NotificationItem(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }


    static class Alert {

        String getStatus() { return "new"; }
        String getChildName() { return "Child"; }
        long getTimestamp() { return System.currentTimeMillis(); }
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
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new NotificationViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            Object item = items.get(position);

            if (item instanceof Alert) {

                Alert alert = (Alert) item;
                String title = "ALERT: " + alert.getChildName();
                String message = "Triage session started at " + new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(alert.getTimestamp()));
                holder.txtTitle.setText(title);
                holder.txtMessage.setText(message);
                holder.itemView.setBackgroundColor(Color.parseColor("#FFCDD2"));
            } else if (item instanceof NotificationItem) {
                NotificationItem notification = (NotificationItem) item;
                holder.txtTitle.setText(notification.title);
                holder.txtMessage.setText(notification.message);
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                holder.txtTitle.setText("Unknown Notification Type");
                holder.txtMessage.setText("Error in data model.");
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
