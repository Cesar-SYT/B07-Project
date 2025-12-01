package com.example.smartair.r3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;

public class R3MainActivity extends AppCompatActivity {
    private String childName;
    private String childKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r3_main_activity);

        childName = getIntent().getStringExtra("childName");
        childKey  = getIntent().getStringExtra("childKey");

        Button btnRecords   = findViewById(R.id.button);
        Button btnTechHelp  = findViewById(R.id.button2);
        Button btnInventory = findViewById(R.id.button4);
        Button btnMotivate  = findViewById(R.id.button5);

        btnTechHelp.setOnClickListener(v -> {
            Intent intent = new Intent(this, TechniqueHelperActivity.class);
            intent.putExtra("childName", childName);
            intent.putExtra("childKey", childKey);
            startActivity(intent);
        });

        btnInventory.setOnClickListener(v -> {
            Intent intent = new Intent(this, InventoryActivity.class);
            intent.putExtra("childName", childName);
            intent.putExtra("childKey", childKey);
            startActivity(intent);
        });

        btnMotivate.setOnClickListener(v -> {
            Intent intent = new Intent(this, MotivationActivity.class);
            intent.putExtra("childName", childName);
            intent.putExtra("childKey", childKey);
            startActivity(intent);
        });

        btnRecords.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecordsActivity.class);
            intent.putExtra("childName", childName);
            intent.putExtra("childKey", childKey);
            startActivity(intent);
        });
    }
}
