package com.example.smartair.r3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;

public class R3MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r3_main_activity);

        Button btnRecords   = findViewById(R.id.button);
        Button btnTechHelp  = findViewById(R.id.button2);
        Button btnInventory = findViewById(R.id.button4);
        Button btnMotivate  = findViewById(R.id.button5);

        btnRecords.setOnClickListener(v ->
                startActivity(new Intent(this, RecordsActivity.class)));

        btnTechHelp.setOnClickListener(v ->
                startActivity(new Intent(this, TechniqueHelperActivity.class)));

        btnInventory.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));

        btnMotivate.setOnClickListener(v ->
                startActivity(new Intent(this, MotivationActivity.class)));
    }
}
