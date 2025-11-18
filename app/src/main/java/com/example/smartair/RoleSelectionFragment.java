package com.example.smartair;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RoleSelectionFragment extends Fragment {
    Button btnSelectChild;
    Button btnSelectParent;
    Button btnSelectProvider;
    public RoleSelectionFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_role_selection, container, false);

        btnSelectChild = view.findViewById(R.id.button_select_child);
        btnSelectParent = view.findViewById(R.id.button_select_parent);
        btnSelectProvider = view.findViewById(R.id.button_select_provider);

        btnSelectChild.setOnClickListener(v -> {
            // TODO: goto child explanation page
        });
        btnSelectParent.setOnClickListener(v -> {
            // TODO: goto parent explanation page
        });
        btnSelectProvider.setOnClickListener(v -> {
            // TODO: goto provider explanation page
        });
        return view;
    }
}
