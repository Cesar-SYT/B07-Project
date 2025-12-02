package com.example.smartair.r3;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.model.Child;

import java.util.Date;

public class TechniqueHelperActivity extends AppCompatActivity {

    private int currentStep = 0;

    private ImageView stepImage;

    private R3Service r3Service;
    private Child currentChild;

    private final String[] stepTitles = {
            "Step 1: Seal your lips or mask",
            "Step 2: Slow, deep breath in",
            "Step 3: Hold your breath ~10 seconds",
            "Step 4: Wait 30–60s between puffs",
    };

    private final String[] stepDescriptions = {
            "Put the mouthpiece (or mask) on your face and seal your lips so no air leaks out.",
            "Start a slow, deep breath in. As you begin to breathe in, press the inhaler once and keep breathing in steadily.",
            "Hold your breath for about 10 seconds, or as long as feels comfortable, then breathe out slowly.",
            "If your doctor told you to take another puff, wait 30–60 seconds before you press again.",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_technique_helper);

        r3Service = new R3ServiceImpl();

        TextView stepTitle       = findViewById(R.id.stepTitle);
        TextView stepDescription = findViewById(R.id.stepDescription);
        Button btnPrev           = findViewById(R.id.btnPrev);
        Button btnNext           = findViewById(R.id.btnNext);
        stepImage                = findViewById(R.id.stepImage);

        String childId = getIntent().getStringExtra("childKey");
        String childName = getIntent().getStringExtra("childName");
        currentChild = new Child(childId, childName);

        updateUI(stepTitle, stepDescription);

        btnPrev.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateUI(stepTitle, stepDescription);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentStep < stepTitles.length - 1) {
                currentStep++;
                updateUI(stepTitle, stepDescription);
            } else {
                onTechniqueSessionCompleted();
                finish();
            }
        });
    }

    private void updateUI(TextView title, TextView desc) {
        title.setText(stepTitles[currentStep]);
        desc.setText(stepDescriptions[currentStep]);
        playStepAnimation(currentStep);
    }

    private void playStepAnimation(int stepIndex) {
        int animResId;
        switch (stepIndex) {
            case 0:
                animResId = R.drawable.step1_anim;
                break;
            case 1:
                animResId = R.drawable.step2_anim;
                break;
            case 2:
                animResId = R.drawable.step3_anim;
                break;
            case 3:
            default:
                animResId = R.drawable.step4_anim;
                break;
        }

        stepImage.setBackgroundResource(animResId);
        AnimationDrawable animationDrawable = (AnimationDrawable) stepImage.getBackground();
        animationDrawable.stop();
        animationDrawable.start();
    }

    private void onTechniqueSessionCompleted() {
        if (r3Service == null || currentChild == null) return;

        TechniqueSession session = new TechniqueSession(currentChild, new Date());

        session.setLipsSealed(true);
        session.setSlowDeepBreath(true);
        session.setBreathHeldFor10s(true);
        session.setWaitedBetweenPuffs(true);
        session.setSpacerUsedIfNeeded(true);

        r3Service.completeTechniqueSession(session);
    }
}
