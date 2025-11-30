package com.example.smartair.r3;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.model.Child;

import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import android.app.DatePickerDialog;
import android.view.LayoutInflater;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;


import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 简单版本的库存页面：
 *
 * - 显示两个药品（卡片 1 = Rescue，卡片 2 = Controller）
 * - 点击 + / - 更新剩余剂量
 * - 每次变化都会调用 R3Service.updateInventory(...) 写入 Firebase
 * - 根据剩余百分比和过期情况在 txtWarning1 / txtWarning2 里显示提示
 */
public class InventoryActivity extends AppCompatActivity {
    // 新增
    private TextView txtChildName;
    private RecyclerView recyclerInventory;

    private final List<InventoryItem> inventoryList = new ArrayList<>();
    private InventoryAdapter adapter;

    private String userUid;   // 当前登录用户 uid

    private static final double LOW_THRESHOLD_PERCENT = 20.0; // 和 R3ServiceImpl 保持一致

    private R3Service r3Service;
    private Child currentChild; // 先用假数据，之后可以从登录信息中拿
    private ListView listView;          // 或 RecyclerView

    // UI 控件
    private TextView txtMedName1, txtAmount1, txtPurchase1, txtExpiry1, txtWarning1;
    private TextView txtMedName2, txtAmount2, txtPurchase2, txtExpiry2, txtWarning2;
    private Button btnAdd1;
    private Button btnSubtract1;
    private Button btnAdd2;
    private Button btnSubtract2;

    private final R3ServiceImpl r3 = new R3ServiceImpl();
    private Date parseDate(String str) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(str);
        } catch (Exception e) {
            return new Date();
        }
    }
    private void loadInventory() {

        // 1. 拿当前登录用户
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();  // FirebaseAuth uid

        // 2. 从数据库读 fullName，当作 childId
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("users").child(uid).child("fullName")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String childName = snapshot.getValue(String.class);

                    if (childName == null || childName.isEmpty()) {
                        Toast.makeText(this, "Child name not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 更新 UI 显示名字
                    txtChildName.setText(childName);

                    // 3. 用 childName 去后端拿 inventory
                    r3.getInventory(
                            childName,   // ★ 用名字当作 key
                            list -> {
                                Log.d("InventoryActivity", "Loaded items: " + list.size());

                                // 完成 TODO：把 list 显示到 RecyclerView
                                inventoryList.clear();
                                inventoryList.addAll(list);
                                adapter.notifyDataSetChanged();
                            },
                            e -> Toast.makeText(this,
                                    "Load failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Load child name failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }




    private void addMedicineToFirebase(String name, String purchase, String expiry) {


        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 用 uid 当作 childId（现在假设一个用户对应一个 child）
        String childuid = user.getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = db.getReference();

        // 2. 先从 /users/<uid> 读这个用户的信息
        rootRef.child("users").child(childuid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    // 从服务器拿 fullName，当成 Child 的 displayName
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    if (fullName == null) {
                        Toast.makeText(this, "User fullName not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 3. 用服务器的名字构造 Child（你的构造是 Child(String displayName)）
                    Child child = new Child(childuid,fullName);

                    // 4. 解析日期
                    Date purchaseDate = parseDate(purchase);
                    Date expiryDate   = parseDate(expiry);

                    // 5. 构造 InventoryItem（用带 Child 的构造）
                    InventoryItem item = new InventoryItem(
                            child,
                            name,
                            MedicineType.RESCUE,   // 先默认 RESCUE，有需要再加参数选择类型
                            purchaseDate,
                            expiryDate,
                            0,      // totalDoses
                            0       // remainingDoses
                    );

                    // 6. 写入 /inventory/<uid>/<autoKey>
                    rootRef.child("inventory")
                            .child(userUid)
                            .push()
                            .setValue(item)
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show();
                                loadInventory();   // 写入成功后刷新列表
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this,
                                        "Add failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Load user failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }



    // 读取服务器上的孩子名字，并显示在 txtChildName，再加载库存
    private void loadChildNameFromServer() {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 用 uid 当作 childId（现在假设一个用户对应一个 child）
        String childuid = user.getUid();

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("users").child(childuid).child("fullName")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String fullName = snapshot.getValue(String.class);
                    if (fullName == null || fullName.isEmpty()) {
                        fullName = "Child";
                    }
                    // 显示名字
                    txtChildName.setText(fullName);
                    // 用这个名字建一个 Child 对象
                    currentChild = new Child(childuid,fullName);

                    // 有了 child 之后再加载库存
                    loadInventory();
                })
                .addOnFailureListener(e -> {
                    txtChildName.setText("Child");
                    currentChild = new Child(childuid,"Unknown");
                    Toast.makeText(this,
                            "Load child failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // 失败了也可以继续尝试加载库存（看你需求）
                    loadInventory();
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_inventory);

        r3Service = new R3ServiceImpl();

        // 1. 获取当前登录用户 uid（后面读名字 & 库存用）
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userUid = null;
        }

        // 2. 绑定新 XML 里的控件
        txtChildName = findViewById(R.id.txtChildName);
        recyclerInventory = findViewById(R.id.recyclerInventory);
        Button btnAddMedicine = findViewById(R.id.btnAddMedicine);

        // 3. 设置 RecyclerView
        recyclerInventory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(inventoryList, (item, newDoses) -> {
            // 当某个药品在列表里被 + / - 时，更新到后端
            r3Service.updateInventory(null, item, newDoses, new Date());
        });
        recyclerInventory.setAdapter(adapter);

        // 4. 读取孩子名字，然后加载库存列表
        loadChildNameFromServer();

        // 5. “添加药品”按钮
        btnAddMedicine.setOnClickListener(v -> showAddMedicineDialog());
    }

    /**
     * 根据当前 TextView 里的内容初始化两个 InventoryItem。
     * 为了简单，purchaseDate / expiryDate 先设为 null，
     * totalDoses = 初始 remainingDoses。
     */

    /*
    private void setupInventoryItems() {
        // 卡片 1：当成 Rescue
        String name1 = txtMedName1.getText().toString();
        int remaining1 = parseIntSafe(txtAmount1.getText().toString(), 0);
        rescueItem = new InventoryItem(
                currentChild,
                name1,
                MedicineType.RESCUE,
                null,         // purchaseDate 暂时不用
                null,         // expiryDate 暂时不用
                remaining1,   // totalDoses 先和剩余一样
                remaining1
        );

        // 卡片 2：当成 Controller
        String name2 = txtMedName2.getText().toString();
        int remaining2 = parseIntSafe(txtAmount2.getText().toString(), 0);
        controllerItem = new InventoryItem(
                currentChild,
                name2,
                MedicineType.CONTROLLER,
                null,
                null,
                remaining2,
                remaining2
        );

        refreshWarnings();
    }
     */
    /*
    private void setupListeners() {
        btnAddMedicine.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add a new medicine");

            // 输入框
            final EditText inputName = new EditText(this);
            inputName.setHint("Medicine name");

            final EditText inputDoses = new EditText(this);
            inputDoses.setHint("Total doses");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);
            layout.addView(inputName);
            layout.addView(inputDoses);

            builder.setView(layout);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String name = inputName.getText().toString().trim();
                int doses = parseIntSafe(inputDoses.getText().toString(), 0);

                if (name.isEmpty() || doses <= 0) {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 新增药物（默认 rescue，后续可加入选择）
                InventoryItem newItem = new InventoryItem(
                        currentChild,
                        name,
                        MedicineType.RESCUE,   // TODO: 未来可以加 radio button 让用户选
                        null,
                        null,
                        doses,
                        doses
                );

                // 保存到数据库
                r3Service.updateInventory(null, newItem, doses, new Date());

                Toast.makeText(this, "Medicine added", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        // 卡片 1 加一
        btnAdd1.setOnClickListener(v -> {
            int current = rescueItem.getRemainingDoses();
            rescueItem.setRemainingDoses(current + 1);
            txtAmount1.setText(String.valueOf(rescueItem.getRemainingDoses()));
            // Parent 先传 null，R3ServiceImpl 会处理
            r3Service.updateInventory(null, rescueItem,
                    rescueItem.getRemainingDoses(), new Date());
            refreshWarnings();
        });

        // 卡片 1 减一
        btnSubtract1.setOnClickListener(v -> {
            int current = rescueItem.getRemainingDoses();
            if (current > 0) {
                rescueItem.setRemainingDoses(current - 1);
                txtAmount1.setText(String.valueOf(rescueItem.getRemainingDoses()));
                r3Service.updateInventory(null, rescueItem,
                        rescueItem.getRemainingDoses(), new Date());
                refreshWarnings();
            }
        });

        // 卡片 2 加一
        btnAdd2.setOnClickListener(v -> {
            int current = controllerItem.getRemainingDoses();
            controllerItem.setRemainingDoses(current + 1);
            txtAmount2.setText(String.valueOf(controllerItem.getRemainingDoses()));
            r3Service.updateInventory(null, controllerItem,
                    controllerItem.getRemainingDoses(), new Date());
            refreshWarnings();
        });

        // 卡片 2 减一
        btnSubtract2.setOnClickListener(v -> {
            int current = controllerItem.getRemainingDoses();
            if (current > 0) {
                controllerItem.setRemainingDoses(current - 1);
                txtAmount2.setText(String.valueOf(controllerItem.getRemainingDoses()));
                r3Service.updateInventory(null, controllerItem,
                        controllerItem.getRemainingDoses(), new Date());
                refreshWarnings();
            }
        });

        // Add Medicine 先占位，后面你可以改成 “新增第三个药”
        btnAddMedicine.setOnClickListener(v -> {
            // 现在先什么都不做，或者你可以弹一个 Toast
            // Toast.makeText(this, "Add medicine coming soon", Toast.LENGTH_SHORT).show();
        });
    }
    */


    /** 刷新所有药品的 low / expired 状态，并通知 RecyclerView 重绘 */
    private void refreshWarnings() {
        for (InventoryItem item : inventoryList) {
            boolean low = item.isLowCanister(LOW_THRESHOLD_PERCENT);
            boolean expired = item.isExpired(new Date());
            item.setLow(low);
            item.setExpired(expired);
        }
        adapter.notifyDataSetChanged(); // 让 RecyclerView 重新 bind 每个 item
    }


    private void updateWarningForItem(InventoryItem item, TextView warningView) {
        if (item == null) {
            warningView.setVisibility(View.GONE);
            return;
        }

        boolean low = item.isLowCanister(LOW_THRESHOLD_PERCENT);
        boolean expired = item.isExpired(new Date());

        if (!low && !expired) {
            warningView.setVisibility(View.GONE);
            warningView.setText("");
            return;
        }

        String msg;
        if (low && expired) {
            msg = "Low canister & expired";
        } else if (low) {
            msg = "Low canister";
        } else { // expired only
            msg = "Expired";
        }

        warningView.setVisibility(View.VISIBLE);
        warningView.setText(msg);
    }
    private void showAddMedicineDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.date_choose_dialog, null);

        EditText editName = view.findViewById(R.id.editMedicineName);
        TextView txtPurchase = view.findViewById(R.id.txtPurchaseDate);
        TextView txtExpiry = view.findViewById(R.id.txtExpiryDate);

        final Calendar calendar = Calendar.getInstance();

        // --- 选择购买日期 ---
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

        // --- 选择过期日期 ---
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

        // --- 弹窗 ---
        new AlertDialog.Builder(this)
                .setTitle("Add Medicine")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String purchase = txtPurchase.getText().toString();
                    String expiry = txtExpiry.getText().toString();

                    if (!name.isEmpty() && purchase.contains("-") && expiry.contains("-")) {
                        addMedicineToFirebase(name, purchase, expiry);
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
