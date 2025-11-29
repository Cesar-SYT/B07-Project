package com.example.smartair;  // 如果你的包名不一样，按你项目的来改

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.model.Child;
import com.example.smartair.model.Parent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ManageChildrenActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChildAdapter adapter;
    private Parent currentParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_end);

        recyclerView = findViewById(R.id.rv_children);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Firebase uid
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Database reference
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        // 从 Firebase 读取 Parent 数据
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                currentParent = snapshot.getValue(Parent.class);

                if (currentParent == null) {
                    Toast.makeText(ManageChildrenActivity.this,
                            "Error loading parent", Toast.LENGTH_SHORT).show();
                    return;
                }

                // **只有在这里读 children**
                List<Child> children = currentParent.getChildren();

                // **只有在这里设置 adapter**
                adapter = new ChildAdapter(children);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        // 其它按钮事件
        Button btnManage = findViewById(R.id.btn_manage_children);
        btnManage.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterChildActivity.class));
        });
    }
}
