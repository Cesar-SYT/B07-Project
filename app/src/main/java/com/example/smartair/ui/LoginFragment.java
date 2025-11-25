package com.example.smartair.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    EditText editEmail;
    EditText editLoginPassword;
    Button btnLogin;
    TextView textNoAccount;
    TextView textForgotPasswd;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        editEmail = view.findViewById(R.id.edit_text_email);
        editLoginPassword = view.findViewById(R.id.edit_text_password);
        btnLogin = view.findViewById(R.id.button_login);
        textNoAccount = view.findViewById(R.id.text_view_register_prompt);
        textForgotPasswd = view.findViewById(R.id.text_view_forgot_password);

        textNoAccount.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_loginFragment_to_roleSelectionFragment);
        });

        textForgotPasswd.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            if (email.isEmpty()){
                Toast.makeText(requireContext(), "Please enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Toast.makeText(requireContext(), "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(requireContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editLoginPassword.getText().toString().trim();
            if (email.isEmpty()){
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (password.isEmpty()){
                Toast.makeText(requireContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            // sign in successful, goto different homepage based on userType
                            String uid = firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference ref = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(uid);
                            ref.get().addOnSuccessListener(snapshot -> {
                                String usertype = snapshot.child("userType").getValue(String.class);

                                if (usertype.equals("parent")) {
                                    // TODO: goto parent homepage
                                }
                                else if (usertype.equals("child")){
                                    // TODO: goto child homepage
                                }
                                else{
                                    // TODO: goto provider homepage
                                }
                            });
                        }
                        else{
                            Toast.makeText(requireContext(), "Invalid email/password", Toast.LENGTH_SHORT).show();
                            editLoginPassword.setText("");
                        }
                    });
        });
        return view;
    }
}