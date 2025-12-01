package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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

public class ChildHomeActivity extends AppCompatActivity {

    private TextView txtChildGreeting;
    private Button btnDailyCheckChild;
    private Button btnLogMedicineChild;
    private Button btnTechniquePracticeChild;
    private Button btnBadgesandStreakChild;
    private Button btnSymptomCheckin;
    private String name = "";
    private Button btnSignout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_page);  // 对应 child_page.xml

        findViewById(R.id.nav_host_fragment_child_home).setVisibility(View.GONE);

        initViews();
        loadChildName();
        setupClickListeners();
    }

    private void initViews() {
        txtChildGreeting = findViewById(R.id.txtChildGreeting);
        btnDailyCheckChild = findViewById(R.id.btnDailyCheckChild);
        btnLogMedicineChild = findViewById(R.id.btnLogMedicineChild);
        btnTechniquePracticeChild = findViewById(R.id.btnTechniquePracticeChild);
        btnBadgesandStreakChild = findViewById(R.id.btnBadgesandStreakChild);
        btnSymptomCheckin = findViewById(R.id.btnSymptomCheckin);
        btnSignout = findViewById(R.id.btnChildSignOut);

        btnSignout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChildHomeActivity.this, MainActivity.class));
            finish();
        });
    }

    /**
     * 现在：users/{childId} 的 key 就是 FirebaseUser.getUid()
     * 所以这里直接用 uid，当成 childId 来查名字
     */
    private void loadChildName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            txtChildGreeting.setText("Hi!");
            return;
        }

        String childId = currentUser.getEmail().replace(".", ",");

        DatabaseReference childRef = FirebaseDatabase
                .getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                .getReference("users")
                .child(childId);

        childRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child("displayName")
                        .getValue(String.class);

                if (name == null || name.isEmpty()) {
                    name = "there";
                }
                txtChildGreeting.setText("Hi, " + name + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                txtChildGreeting.setText("Hi!");
                Toast.makeText(ChildHomeActivity.this,
                        "Failed to load name: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        btnDailyCheckChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecordsActivity.class);
            intent.putExtra("childKey", getCurrentChildKey());
            intent.putExtra("childName", name);
            startActivity(intent);
        });

        btnLogMedicineChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, InventoryActivity.class);
            intent.putExtra("childKey", getCurrentChildKey());
            intent.putExtra("childName", name);
            startActivity(intent);
        });

        btnTechniquePracticeChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, TechniqueHelperActivity.class);
            intent.putExtra("childKey", getCurrentChildKey());
            intent.putExtra("childName", name);
            startActivity(intent);
        });

        btnBadgesandStreakChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, MotivationActivity.class);
            intent.putExtra("childKey", getCurrentChildKey());
            intent.putExtra("childName", name);
            startActivity(intent);
        });
        btnSymptomCheckin.setOnClickListener(v -> {
            View container = findViewById(R.id.nav_host_fragment_child_home);
            container.setVisibility(View.VISIBLE);
            container.requestLayout();
            findViewById(R.id.nav_host_fragment_child_home).setVisibility(View.VISIBLE);
            findViewById(R.id.childHomeRoot).setVisibility(View.GONE);

            NavController navController = Navigation.findNavController(
                    ChildHomeActivity.this,
                    R.id.nav_host_fragment_child_home
            );

            navController.navigate(R.id.symptomCheckinChildFragment);
        });
    }

    private String getCurrentChildKey() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return "";
        return currentUser.getEmail().replace(".", ",");
    }



}

