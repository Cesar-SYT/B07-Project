package com.example.smartair.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartair.R;

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

        String userType = getArguments().getString("role");
        List<OnboardingScreen> screens = getOnboardingScreens(userType);

        adapter = new OnboardingAdapter(this, screens, () -> {
            if (userType.equals("PARENT")){
                // TODO: goto parent homepage
            }
            else if (userType.equals("CHILD")){
                // TODO: goto child homepage
            }
            else if (userType.equals("PROVIDER")){
                // TODO: goto provider homepage
            }
        });

        viewPager.setAdapter(adapter);
    }

    private List<OnboardingScreen> getOnboardingScreens(String userType) {
        if ("child".equals(userType)) {
            return OnboardingRepo.getChildOnboardingScreens();
        }
        else if ("parent".equals(userType)) {
            return OnboardingRepo.getParentOnboardingScreens();
        }
        else {
            return OnboardingRepo.getProviderOnboardingScreens();
        }
    }
}