package com.example.smartair.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class OnboardingFragment extends Fragment {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.onboarding_view_pager);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        ref.child("role").get().addOnSuccessListener(snapshot -> {
            String rawType = snapshot.getValue(String.class);
            if (rawType == null) {
                rawType = "PROVIDER"; // default, prevent crashing in case database accidentally didn't write type
            }
            String userType = rawType.toUpperCase();

            List<OnboardingScreen> screens = getOnboardingScreens(userType);

            adapter = new OnboardingAdapter(this, screens, () -> {
                ref.child("hasBeenOnboarding").setValue(true);
                if ("PARENT".equals(userType)){
                    Intent intent = new Intent(requireContext(),
                            com.example.smartair.ParentHomeActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
                else if ("CHILD".equals(userType)){
                    Intent intent = new Intent(requireContext(),
                            com.example.smartair.ChildHomeActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
                else if ("PROVIDER".equals(userType)){
                    Intent intent = new Intent(requireContext(),
                            com.example.smartair.ProviderHomeActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                } else {
                    Toast.makeText(requireContext(),
                            "Unknown user type: " + userType,
                            Toast.LENGTH_SHORT).show();
                }
            });
            viewPager.setAdapter(adapter);
        });

    }

    private List<OnboardingScreen> getOnboardingScreens(String userType) {
        if ("CHILD".equals(userType)) {
            return OnboardingRepo.getChildOnboardingScreens();
        }
        else if ("PARENT".equals(userType)) {
            return OnboardingRepo.getParentOnboardingScreens();
        }
        else {
            return OnboardingRepo.getProviderOnboardingScreens();
        }
    }
}