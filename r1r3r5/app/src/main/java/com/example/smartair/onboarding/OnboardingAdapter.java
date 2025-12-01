package com.example.smartair.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class OnboardingAdapter extends FragmentStateAdapter {

    private final List<OnboardingScreen> screens;
    private final Runnable onFinished;

    public OnboardingAdapter(Fragment fragment, List<OnboardingScreen> screens, Runnable onFinished) {
        super(fragment);
        this.screens = screens;
        this.onFinished = onFinished;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        boolean isLastScreen = position == screens.size() - 1;
        return OnboardingPageFragment.newInstance(
                screens.get(position),
                position,
                screens.size(),
                isLastScreen,
                onFinished
        );
    }

    @Override
    public int getItemCount() {
        return screens.size();
    }
}