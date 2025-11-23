package com.example.smartair.ui;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.smartair.R;

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
            //goto register child page
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_roleSelectionFragment_to_registerChildFragment);
        });
        btnSelectParent.setOnClickListener(v -> {
            //goto register parent page
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_roleSelectionFragment_to_registerParentFragment);
        });
        btnSelectProvider.setOnClickListener(v -> {
            //goto register provider age
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_roleSelectionFragment_to_registerProviderFragment);
        });
        return view;
    }
}
