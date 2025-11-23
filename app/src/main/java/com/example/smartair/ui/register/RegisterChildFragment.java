package com.example.smartair.ui.register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.smartair.R;
import com.example.smartair.ui.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterChildFragment extends Fragment {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    EditText fullName;
    EditText editTextEmail;
    EditText editTextPassword;
    Button btnSubmitRegistration;
    TextView loginPrompt;

    public RegisterChildFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_child, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        fullName = view.findViewById(R.id.edit_text_full_name);
        editTextEmail = view.findViewById(R.id.edit_text_email);
        editTextPassword = view.findViewById(R.id.edit_text_password);
        btnSubmitRegistration = view.findViewById(R.id.button_submit_registration);
        loginPrompt = view.findViewById(R.id.text_view_login_prompt);

        loginPrompt.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_registerChildFragment_to_loginFragment);
        });

        btnSubmitRegistration.setOnClickListener(v -> {
            String name = fullName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Please enter your full name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Please enter your email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6){
                Toast.makeText(requireContext(),
                        "Your password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Registration successful", Toast.LENGTH_SHORT).show();
                            String uid = firebaseAuth.getCurrentUser().getUid();
                            User myuser = new User(uid, name, email, "child");
                            FirebaseDatabase.getInstance
                                    ("https://smart-air-61888-default-rtdb.firebaseio.com/").getReference("users")
                                    .child(uid)
                                    .setValue(myuser);

                            Bundle bundle = new Bundle();
                            bundle.putString("userType", "child");
                            NavController navController = Navigation.findNavController(v);
                            navController.navigate(R.id.action_registerChildFragment_to_onboardingFragment, bundle);
                        }
                        else {
                            Toast.makeText(requireContext(),
                                    "Registration failed:" + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
        return view;
    }
}