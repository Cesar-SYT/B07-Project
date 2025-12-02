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
    private DatabaseReference userRef;
    private DatabaseReference childrenRef;
    private String parentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_end);

        Button btnManage = findViewById(R.id.btn_manage_children);
        rvChildren = findViewById(R.id.rv_children);
        tvNoChildren = findViewById(R.id.tv_no_children);
        // Sign Out button
        Button btnSignOut = findViewById(R.id.btnParentSignOut);

        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            // jump to MainActivity
            Intent intent = new Intent(ManageChildrenActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish();
        });


        // set RecyclerView
        rvChildren.setLayoutManager(new LinearLayoutManager(this));
        childrenAdapter = new ChildrenAdapter(childList, child -> {
            Intent intent = new Intent(ManageChildrenActivity.this, ParentHomeActivity.class);
            intent.putExtra("childKey", child.getId());              //send id
            intent.putExtra("childName", child.getDisplayName());   //send name
            startActivity(intent);
        });

        rvChildren.setAdapter(childrenAdapter);


        userRef = FirebaseDatabase
                .getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                .getReference("users");

        String parentEmail = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail();

        if (parentEmail == null) {
            rvChildren.setVisibility(View.GONE);
            tvNoChildren.setVisibility(View.VISIBLE);
            tvNoChildren.setText("No parent logged in.");
            return;
        }

        parentUid = parentEmail.replace(".", ",");

        btnManage.setOnClickListener(view -> {
            Intent intent = new Intent(ManageChildrenActivity.this, RegisterChildActivity.class);
            intent.putExtra("PARENT_UID", parentUid);
            startActivity(intent);
        });


        childrenRef = userRef
                .child(parentUid)
                .child("childid");

        addRealtimeListener();
    }

    private void addRealtimeListener() {

        childrenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                childList.clear();

                if (!snapshot.hasChildren()) {
                    updateUiAfterLoad();
                    return;
                }

                final int total = (int) snapshot.getChildrenCount();
                final int[] loaded = {0};

                for (DataSnapshot childSnap : snapshot.getChildren()) {

                    String childId = childSnap.getKey();

                    if (childId == null) {
                        loaded[0]++;
                        if (loaded[0] == total) updateUiAfterLoad();
                        continue;
                    }

                    // read child data
                    userRef.child(childId).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot childData) {

                                    if (childData.exists()) {
                                        Child child = childData.getValue(Child.class);
                                        if (child != null) {
                                            // child.setId(childId);
                                            child.setId(childId);
                                            childList.add(child);
                                        }
                                    }

                                    loaded[0]++;
                                    if (loaded[0] == total) updateUiAfterLoad();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    loaded[0]++;
                                    if (loaded[0] == total) updateUiAfterLoad();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                rvChildren.setVisibility(View.GONE);
                tvNoChildren.setVisibility(View.VISIBLE);
                tvNoChildren.setText("Error loading children.");
            }
        });
    }

    /** refresh UI */
    private void updateUiAfterLoad() {
        childrenAdapter.notifyDataSetChanged();
        if (childList.isEmpty()) {
            rvChildren.setVisibility(View.GONE);
            tvNoChildren.setVisibility(View.VISIBLE);
            tvNoChildren.setText("You currently have no registered children.");
        } else {
            rvChildren.setVisibility(View.VISIBLE);
            tvNoChildren.setVisibility(View.GONE);
        }
    }

}
