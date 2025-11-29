package com.example.smartair;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

public class ChildHomeActivity extends AppCompatActivity {

    private TextView txtChildGreeting;
    private Button btnDailyCheckChild;
    private Button btnLogMedicineChild;
    private Button btnTechniquePracticeChild;
    private Button btnBadgesandStreakChild;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_page);  // 对应 child_page.xml

        initViews();
        loadDemoData();
        setupClickListeners();
    }

    private void initViews() {
        txtChildGreeting = findViewById(R.id.txtChildGreeting);
        btnDailyCheckChild = findViewById(R.id.btnDailyCheckChild);
        btnLogMedicineChild = findViewById(R.id.btnLogMedicineChild);
        btnTechniquePracticeChild = findViewById(R.id.btnTechniquePracticeChild);
        btnBadgesandStreakChild = findViewById(R.id.btnBadgesandStreakChild);
    }

    /** 先放一点假数据，之后你可以从 Firebase 里拿 child 名字 */
    private void loadDemoData() {
        // 可以从 Intent extra 里拿名字：getIntent().getStringExtra("childName")
        String childName = "Alex";
        txtChildGreeting.setText("Hi, " + childName + "!");
    }

    private void setupClickListeners() {
        btnDailyCheckChild.setOnClickListener(v ->
                Toast.makeText(this, "Daily Check (to be implemented)", Toast.LENGTH_SHORT).show()
        );

        btnLogMedicineChild.setOnClickListener(v ->
                Toast.makeText(this, "Log Medicine (to be implemented)", Toast.LENGTH_SHORT).show()
        );

        btnTechniquePracticeChild.setOnClickListener(v ->
                Toast.makeText(this, "Practice Inhaler Technique (to be implemented)", Toast.LENGTH_SHORT).show()
        );

        btnBadgesandStreakChild.setOnClickListener(v ->
                Toast.makeText(this, "Badges and Streak (to be implemented)", Toast.LENGTH_SHORT).show()
        );
    }
}
