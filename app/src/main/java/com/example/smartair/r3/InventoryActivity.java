package com.example.smartair.r3;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.model.Child;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * InventoryActivity
 *
 * 从 ParentHomeActivity 传入 childName：
 * - 顶部显示该孩子名字
 * - 用 childName 调用 r3.getInventory(...) 读取该孩子的库存
 * - 新增药品也写在该孩子名下
 */
public class InventoryActivity extends AppCompatActivity {

    // UI
    private TextView txtChildName;
    private RecyclerView recyclerInventory;
    private final List<InventoryItem> inventoryList = new ArrayList<>();
    private InventoryAdapter adapter;

    // 当前查看的孩子
    private String childName;      // 从 Intent 传入

    private String childKey;
    private Child currentChild;    // 封装成 Child 对象给 InventoryItem 用

    private DatabaseReference usersRef;
    private DatabaseReference inventoryRef;

    // 业务
    private static final double LOW_THRESHOLD_PERCENT = 20.0;
    private R3Service r3Service = new R3ServiceImpl();


    // （旧版遗留字段，先保留）
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_inventory);

        childKey = getIntent().getStringExtra("childKey");
        if (childKey == null || childKey.trim().isEmpty()) {
            Toast.makeText(this, "Missing childKey, cannot load inventory", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        childName = getIntent().getStringExtra("childName");
        if (childName == null || childName.trim().isEmpty()) {
            childName = childKey;  // 实在没名字就用 key 占位
        }


        currentChild = new Child(childKey, childName);

        usersRef = FirebaseDatabase
                .getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                .getReference("users");

        inventoryRef = usersRef
                .child(childKey)
                .child("inventory");

        // 2. 绑定控件
        txtChildName = findViewById(R.id.txtChildName);
        recyclerInventory = findViewById(R.id.recyclerInventory);
        Button btnAddMedicine = findViewById(R.id.btnAddMedicine);

        // 顶部显示名字
        txtChildName.setText(childName);

        // 3. 配置 RecyclerView
        recyclerInventory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(
                inventoryList,
                inventoryRef,     // ★ 第二个参数：数据库引用
                (item, newVal) -> {

                    // 写入剩余剂量
                    inventoryRef
                            .child(item.getId())
                            .child("remainingDoses")
                            .setValue(newVal);

                    // 写入百分比
                    inventoryRef
                            .child(item.getId())
                            .child("remainingPercent")
                            .setValue(item.getRemainingPercent());

                    // 更新 UI
                    refreshWarnings();
                }
        );


        recyclerInventory.setAdapter(adapter);

        // 4. 读取该孩子的库存
        loadInventoryForCurrentChild();

        // 5. 添加药品
        btnAddMedicine.setOnClickListener(v -> showAddMedicineDialog());
    }

    // === 从服务器读取该孩子的库存 ===
    private void loadInventoryForCurrentChild() {
        inventoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                inventoryList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    InventoryItem item = snap.getValue(InventoryItem.class);
                    if (item != null) {
                        item.setId(snap.getKey());
                        inventoryList.add(item);
                    }
                }

                refreshWarnings();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InventoryActivity.this,
                        "Load inventory failed: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    // === 新增药品：写到该孩子名下 ===
    private void addMedicineToFirebase(String name,
                                       String purchase,
                                       String expiry,
                                       int total,
                                       MedicineType type) {
        Date purchaseDate = parseDate(purchase);
        Date expiryDate   = parseDate(expiry);

        InventoryItem item = new InventoryItem(
                currentChild,
                name,
                type,              // ★ 使用传进来的类型
                purchaseDate,
                expiryDate,
                total,
                total
        );

        DatabaseReference newRef = inventoryRef.push();
        newRef.setValue(item)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show();
                    loadInventoryForCurrentChild();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Add failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }




    private Date parseDate(String str) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(str);
        } catch (Exception e) {
            return new Date();
        }
    }

    /** 刷新所有药品的 low / expired 状态，并通知 RecyclerView 重绘 */
    private void refreshWarnings() {
        for (InventoryItem item : inventoryList) {
            boolean low = item.isLowCanister(LOW_THRESHOLD_PERCENT);
            boolean expired = item.isExpired(new Date());
            item.setLow(low);
            item.setExpired(expired);
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddMedicineDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.date_choose_dialog, null);

        EditText editName = view.findViewById(R.id.editMedicineName);
        TextView txtPurchase = view.findViewById(R.id.txtPurchaseDate);
        TextView txtExpiry = view.findViewById(R.id.txtExpiryDate);
        RadioGroup rgType = view.findViewById(R.id.rgMedicineType);
        EditText editTotal = view.findViewById(R.id.editTotalDoses);

        final Calendar calendar = Calendar.getInstance();

        // 选择购买日期
        txtPurchase.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    InventoryActivity.this,
                    (dp, y, m, d) -> txtPurchase.setText(y + "-" + (m + 1) + "-" + d),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // 选择过期日期
        txtExpiry.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    InventoryActivity.this,
                    (dp, y, m, d) -> txtExpiry.setText(y + "-" + (m + 1) + "-" + d),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });


        // 弹窗
        new AlertDialog.Builder(this)
                .setTitle("Add Medicine")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String purchase = txtPurchase.getText().toString().trim();
                    String expiry = txtExpiry.getText().toString().trim();
                    String totalStr = editTotal.getText().toString().trim();
                    int total = parseIntSafe(totalStr, 0);


                    boolean purchaseChosen = !purchase.equals("Select Purchase Date");
                    boolean expiryChosen   = !expiry.equals("Select Expiry Date");

                    if (!name.isEmpty() && purchaseChosen && expiryChosen) {

                        MedicineType type = MedicineType.RESCUE;
                        int checkedId = rgType.getCheckedRadioButtonId();
                        if (checkedId == R.id.rbController) {
                            type = MedicineType.CONTROLLER;
                        }

                        addMedicineToFirebase(name, purchase, expiry, total, type);

                    } else {
                        Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
}

    private int parseIntSafe(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
