package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.smartair.r3.InventoryActivity;
import com.example.smartair.r3.MotivationActivity;
import com.example.smartair.r3.RecordsActivity;
import com.example.smartair.r3.TechniqueHelperActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChildHomeActivity extends AppCompatActivity {

    // Drawer & Menu
    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private TextView txtChildGreetingMenu; // In the side menu

    // Side Menu Buttons
    private View btnDailyCheckChild;
    private View btnLogMedicineChild;
    private View btnTechniquePracticeChild;
    private View btnBadgesandStreakChild;
    private View btnSymptomCheckin;
    private View btnSignout;

    // Main Content - Safety & Control Elements
    private View btnOneTapTriage;
    private ImageButton btnPefHistory;
    
    // PEF Logging Components
    private SwitchCompat switchPefMode;
    private EditText etPefValue;
    private LinearLayout layoutPostPef;
    private EditText etPostPefValue;
    private Button btnSavePef;

    private String name = "";
    private float currentPB = 0f; // Store locally to calculate Zone

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Explicitly request no title feature before setting content
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.child_page);

        // Double check: hide Action Bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        loadChildName();
        fetchBaselinePEF(); // Load PB for zone calculation
        setupMenuListeners();
        setupSafetyControlLogic();
    }

    private void initViews() {
        // Drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        
        // Side Menu Items (TextViews acting as buttons)
        txtChildGreetingMenu = findViewById(R.id.txtChildGreetingMenu); // "Hi, User" inside menu or just "Menu"
        btnDailyCheckChild = findViewById(R.id.btnDailyCheckChild);
        btnLogMedicineChild = findViewById(R.id.btnLogMedicineChild);
        btnTechniquePracticeChild = findViewById(R.id.btnTechniquePracticeChild);
        btnBadgesandStreakChild = findViewById(R.id.btnBadgesandStreakChild);
        btnSymptomCheckin = findViewById(R.id.btnSymptomCheckin);
        btnSignout = findViewById(R.id.btnChildSignOut);

        // Main Content - Safety & Control
        btnOneTapTriage = findViewById(R.id.btn_one_tap_triage);
        btnPefHistory = findViewById(R.id.btn_pef_history);
        
        // PEF Log
        switchPefMode = findViewById(R.id.switch_pef_mode);
        etPefValue = findViewById(R.id.et_pef_value);
        layoutPostPef = findViewById(R.id.layout_post_pef);
        etPostPefValue = findViewById(R.id.et_post_pef_value);
        btnSavePef = findViewById(R.id.btn_save_pef);
    }

    private void loadChildName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String childId = currentUser.getEmail().replace(".", ",");

        DatabaseReference childRef = FirebaseDatabase
                .getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                .getReference("users")
                .child(childId);

        childRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child("displayName").getValue(String.class);
                if (name == null || name.isEmpty()) name = "User";
                
                // Update Greeting in Menu
                if (txtChildGreetingMenu != null) {
                    txtChildGreetingMenu.setText("Hi, " + name + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ignore
            }
        });
    }

    private void fetchBaselinePEF() {
        String childKey = getCurrentChildKey();
        PEFDataRepository.getInstance().fetchParentConfiguredPB(childKey, new PEFDataRepository.OnPBFetchListener() {
            @Override
            public void onSuccess(float pbValue) {
                currentPB = pbValue;
            }

            @Override
            public void onFailure(String errorMsg) {
                // Log error or just leave currentPB as 0
            }
        });
    }

    private void setupMenuListeners() {
        // Open Drawer
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Navigation Logic
        View.OnClickListener navListener = v -> {
            Intent intent = null;
            int id = v.getId();

            if (id == R.id.btnDailyCheckChild) { // Medicine Record
                intent = new Intent(this, RecordsActivity.class);
            } else if (id == R.id.btnLogMedicineChild) {
                intent = new Intent(this, InventoryActivity.class);
            } else if (id == R.id.btnTechniquePracticeChild) {
                intent = new Intent(this, TechniqueHelperActivity.class);
            } else if (id == R.id.btnBadgesandStreakChild) {
                intent = new Intent(this, MotivationActivity.class);
            }

            if (intent != null) {
                intent.putExtra("childKey", getCurrentChildKey());
                intent.putExtra("childName", name);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        };

        btnDailyCheckChild.setOnClickListener(navListener);
        btnLogMedicineChild.setOnClickListener(navListener);
        btnTechniquePracticeChild.setOnClickListener(navListener);
        btnBadgesandStreakChild.setOnClickListener(navListener);

        // Symptom Checkin (Fragment navigation)
        btnSymptomCheckin.setOnClickListener(v -> {
            View container = findViewById(R.id.nav_host_fragment_child_home);
            if (container != null) {
                container.setVisibility(View.VISIBLE);
                drawerLayout.closeDrawer(GravityCompat.START);
                
                try {
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment_child_home);
                    
                    if (navHostFragment != null) {
                        NavController navController = navHostFragment.getNavController();
                        try {
                             // Dynamically set graph if start destination is empty
                             navController.setGraph(R.navigation.nav_graph_child);
                        } catch (Exception e) {
                            // Graph might be already set
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Sign Out
        btnSignout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChildHomeActivity.this, MainActivity.class));
            finish();
        });
    }

    private void setupSafetyControlLogic() {
        // Triage Button
        if (btnOneTapTriage != null) {
            btnOneTapTriage.setOnClickListener(v -> {
                Intent intent = new Intent(this, TriageDetail.class);
                startActivity(intent);
            });
        }

        // PEF History
        if (btnPefHistory != null) {
            btnPefHistory.setOnClickListener(v -> {
                Intent intent = new Intent(this, DisplayPEF.class);
                // Pass childKey
                intent.putExtra("childKey", getCurrentChildKey());
                startActivity(intent);
            });
        }

        // PEF Logging Logic
        if (switchPefMode != null) {
            switchPefMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (etPefValue != null) etPefValue.setHint("Pre-Med Value");
                    if (layoutPostPef != null) layoutPostPef.setVisibility(View.VISIBLE);
                } else {
                    if (etPefValue != null) etPefValue.setHint("Enter Value");
                    if (layoutPostPef != null) layoutPostPef.setVisibility(View.GONE);
                }
            });
        }

        if (btnSavePef != null) {
            btnSavePef.setOnClickListener(v -> savePefLog());
        }
    }

    private void savePefLog() {
        if (etPefValue == null) return;

        String preValStr = etPefValue.getText().toString().trim();
        boolean isCompareMode = switchPefMode != null && switchPefMode.isChecked();

        if (preValStr.isEmpty()) {
            etPefValue.setError("Please enter a value");
            return;
        }

        int preValue = Integer.parseInt(preValStr);
        int postValue = 0;

        if (isCompareMode) {
            if (etPostPefValue == null) return;
            String postValStr = etPostPefValue.getText().toString().trim();
            if (postValStr.isEmpty()) {
                etPostPefValue.setError("Please enter post-med value");
                return;
            }
            postValue = Integer.parseInt(postValStr);
        }

        // Calculate Zone
        // Use the higher value for zone calculation if comparison
        int compareValue = Math.max(preValue, postValue);
        if (compareValue == 0 && preValue > 0) compareValue = preValue;
        
        String zone = calculateZone(compareValue, currentPB);

        // Create Model
        long timestamp = System.currentTimeMillis();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(timestamp));
        
        PEFLogModel log = new PEFLogModel(
                null, // ID generated by repo
                dateTime,
                timestamp,
                preValue,
                postValue,
                isCompareMode,
                zone,
                getCurrentChildKey()
        );

        // Save to Firebase
        PEFDataRepository.getInstance().savePEFLogToFirebase(getCurrentChildKey(), log, new PEFDataRepository.OnSaveListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(ChildHomeActivity.this, "PEF Log Saved!", Toast.LENGTH_SHORT).show();
                etPefValue.setText("");
                if (etPostPefValue != null) etPostPefValue.setText("");
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ChildHomeActivity.this, "Failed to save: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Calculate Zone based on PB (Personal Best).
     * Green: >= 80%
     * Yellow: 50% - 80%
     * Red: < 50%
     */
    private String calculateZone(int value, float pb) {
        if (pb <= 0) return "Unknown"; // No PB set

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

    private String getCurrentChildKey() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return "";
        return currentUser.getEmail().replace(".", ",");
    }

    @Override
    public void onBackPressed() {
        // Close drawer if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Also handle hiding the fragment container if it's visible
            View container = findViewById(R.id.nav_host_fragment_child_home);
            if (container != null && container.getVisibility() == View.VISIBLE) {
                container.setVisibility(View.GONE);
                return; // Don't finish activity
            }
            super.onBackPressed();
        }
    }
}