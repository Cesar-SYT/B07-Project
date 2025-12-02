package com.example.smartair.login;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

public class LoginModel {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private String normalizeEmail(String email) {
        if (email == null) return "";
        email = email.trim();
        if (!email.contains("@")) {
            email += "@child.com";
        }
        return email;
    }

    public void sendResetEmail(String emailInput, LoginCallback callback) {
        String email = normalizeEmail(emailInput);

        if (email.isEmpty()) {
            callback.onResetFailure("Please enter your email first");
            return;
        }

        firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResetSuccess();
                    } else {
                        callback.onResetFailure(task.getException().getMessage());
                    }
                });
    }

    public void login(String emailInput, String passwordInput, LoginCallback callback) {
        String email = normalizeEmail(emailInput);
        String password = passwordInput.trim();

        firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onLoginFailure("Invalid email/password");
                        return;
                    }

                    String uid = email.replace(".", ",");
                    DatabaseReference ref = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid);

                    ref.get().addOnSuccessListener(snapshot -> {
                        String role = snapshot.child("role").getValue(String.class);
                        Boolean hasBeenOnboarding =
                                snapshot.child("hasBeenOnboarding").getValue(Boolean.class);

                        if (hasBeenOnboarding == null) hasBeenOnboarding = false;

                        callback.onLoginSuccess(role, hasBeenOnboarding);
                    });
                });
    }
}
