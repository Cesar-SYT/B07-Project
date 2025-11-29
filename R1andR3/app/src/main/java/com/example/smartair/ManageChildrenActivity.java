package com.example.smartair;  // 如果你的包名不一样，按你项目的来改

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ManageChildrenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用你给的这个 XML：activity_parent_end.xml
        setContentView(R.layout.activity_parent_end);
        Button btnManage = findViewById(R.id.btn_manage_children);

        btnManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageChildrenActivity.this, RegisterChildActivity.class);
                startActivity(intent);
            }
        });

    }
}
