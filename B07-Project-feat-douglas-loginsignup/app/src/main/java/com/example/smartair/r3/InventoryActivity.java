package com.example.smartair.r3;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

public class InventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_inventory);
        // TODO: 以后在这里用 R3Service.updateInventory(...) 更新库存
    }
}
