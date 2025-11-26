package com.example.smartair;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ParentEnd extends AppCompatActivity {

    private RecyclerView rvChildren;
    private ChildAdapter adapter;
    private List<ChildModel> childList;
    private Button btnManageChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_end);

        rvChildren = findViewById(R.id.rv_children);
        btnManageChildren = findViewById(R.id.btn_manage_children);

        // Initialize data
        childList = new ArrayList<>();
        // Dummy data for demonstration
        childList.add(new ChildModel("1", "Alice", "2015-05-20", 8, ""));
        childList.add(new ChildModel("2", "Bob", "2013-08-15", 10, ""));
        childList.add(new ChildModel("3", "Charlie", "2018-01-10", 5, ""));

        // Setup RecyclerView
        rvChildren.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChildAdapter(this, childList);
        rvChildren.setAdapter(adapter);

        // Manage Button Listener
        btnManageChildren.setOnClickListener(v -> {
            // TODO: Open Dialog or Activity to Add/Edit Children
            // For now, showing a toast placeholder
            manageChildren();
        });
    }

    /**
     * Interface to handle adding/editing children logic.
     * This could launch a dialog or a new activity.
     */
    private void manageChildren() {
        // Placeholder for management logic
        Toast.makeText(this, "Manage Children: Add/Edit/Bind Account", Toast.LENGTH_SHORT).show();
        // Example:
        // Intent intent = new Intent(this, ManageChildrenActivity.class);
        // startActivity(intent);
    }
}