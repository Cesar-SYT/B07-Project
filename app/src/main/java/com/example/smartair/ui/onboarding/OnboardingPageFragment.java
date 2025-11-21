package com.example.smartair.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.smartair.R;

public class OnboardingPageFragment extends Fragment {

    private static final String ARG_SCREEN = "screen";
    private static final String ARG_POSITION = "position";
    private static final String ARG_TOTAL_SCREENS = "total_screens";
    private static final String ARG_IS_LAST_SCREEN = "is_last_screen";

    private OnboardingScreen screen;
    private int position;
    private int totalScreens;
    private boolean isLastScreen;
    private Runnable onFinished;

    public static OnboardingPageFragment newInstance(OnboardingScreen screen, int position, int totalScreens, boolean isLastScreen, Runnable onFinished) {
        OnboardingPageFragment fragment = new OnboardingPageFragment();
        Bundle args = new Bundle();
        // Note: OnboardingScreen is not serializable, so we can't pass it directly in a bundle.
        // Instead, we would typically pass an identifier and retrieve the screen object from a repository.
        // For this example, we will pass the fields directly.
        args.putString("title", screen.title);
        args.putString("content", screen.content);
        args.putString("buttonText", screen.buttonText);
        args.putInt(ARG_POSITION, position);
        args.putInt(ARG_TOTAL_SCREENS, totalScreens);
        args.putBoolean(ARG_IS_LAST_SCREEN, isLastScreen);
        fragment.setArguments(args);
        fragment.onFinished = onFinished;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String title = getArguments().getString("title");
            String content = getArguments().getString("content");
            String buttonText = getArguments().getString("buttonText");
            screen = new OnboardingScreen(title, content, buttonText);
            position = getArguments().getInt(ARG_POSITION);
            totalScreens = getArguments().getInt(ARG_TOTAL_SCREENS);
            isLastScreen = getArguments().getBoolean(ARG_IS_LAST_SCREEN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_onboarding_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView title = view.findViewById(R.id.onboarding_page_title);
        TextView content = view.findViewById(R.id.onboarding_page_content);
        TextView progress = view.findViewById(R.id.onboarding_page_progress);
        Button button = view.findViewById(R.id.onboarding_page_button);

        title.setText(screen.title);
        content.setText(screen.content);
        progress.setText(position + " of " + totalScreens);
        button.setText(screen.buttonText);

        button.setOnClickListener(v -> {
            if (isLastScreen) {
                if (onFinished != null) {
                    onFinished.run();
                }
            } else {
                ((ViewPager2) requireView().getParent().getParent()).setCurrentItem(position);
            }
        });
    }
}