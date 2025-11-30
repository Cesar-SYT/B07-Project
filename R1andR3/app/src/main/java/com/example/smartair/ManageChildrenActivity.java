package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.model.Child;
import com.example.smartair.model.User;
import com.example.smartair.r3.ChildrenAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageChildrenActivity extends AppCompatActivity {

    private RecyclerView rvChildren;
    private TextView tvNoChildren;
    private ChildrenAdapter childrenAdapter;
    private final List<Child> childList = new ArrayList<>();

    private DatabaseReference childrenRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_end);

        Button btnManage = findViewById(R.id.btn_manage_children);
        rvChildren = findViewById(R.id.rv_children);
        tvNoChildren = findViewById(R.id.tv_no_children);

        // 设置 RecyclerView
        rvChildren.setLayoutManager(new LinearLayoutManager(this));
        childrenAdapter = new ChildrenAdapter(childList, child -> {
            Intent intent = new Intent(ManageChildrenActivity.this, ParentHomeActivity.class);
            //intent.putExtra("childId", child.getId());              如果有 getId()
            intent.putExtra("childName", child.getDisplayName());   //传名字
            startActivity(intent);
        });

        rvChildren.setAdapter(childrenAdapter);

        // 跳转到添加 child 的页面
        btnManage.setOnClickListener(view -> {
            Intent intent = new Intent(ManageChildrenActivity.this, RegisterChildActivity.class);
            startActivity(intent);
        });

        // 指向 parent 的 children 目录
        String parentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        childrenRef = FirebaseDatabase
                .getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                .getReference("users")
                .child(parentUid)
                .child("children");

        // ★ 保持原来的 Firebase 实时监听
        addRealtimeListener();
    }

    private void addRealtimeListener() {
        childrenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childList.clear();

                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    Child child = childSnap.getValue(Child.class);
                    if (child != null) {
                        childList.add(child);
                    }
                }

                childrenAdapter.notifyDataSetChanged();

                // 显示/隐藏提示
                if (childList.isEmpty()) {
                    rvChildren.setVisibility(View.GONE);
                    tvNoChildren.setVisibility(View.VISIBLE);
                    tvNoChildren.setText("You currently have no registered children.");
                } else {
                    rvChildren.setVisibility(View.VISIBLE);
                    tvNoChildren.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                rvChildren.setVisibility(View.GONE);
                tvNoChildren.setVisibility(View.VISIBLE);
                tvNoChildren.setText("Failed to load children.");
            }
        });
    }
}
