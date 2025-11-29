package com.example.smartair.r3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

/**
 * 先做一个本地步骤切换的 Demo。
 * 以后可以在最后一步调用 r3Service.completeTechniqueSession(...)
 */
public class TechniqueHelperActivity extends AppCompatActivity {

    private int currentStep = 0;

    private final String[] stepTitles = {
            "Step 1: Shake inhaler",
            "Step 2: Breathe out gently",
            "Step 3: Press and breathe in slowly"
    };

    private final String[] stepDescriptions = {
            "Shake your inhaler well before use.",
            "Breathe out fully, away from the inhaler.",
            "Put the mouthpiece in your mouth, start breathing in slowly and press once."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_technique_helper);

        //LottieAnimationView animationView = findViewById(R.id.animationView);
        TextView stepTitle       = findViewById(R.id.stepTitle);
        TextView stepDescription = findViewById(R.id.stepDescription);
        Button btnPrev           = findViewById(R.id.btnPrev);
        Button btnNext           = findViewById(R.id.btnNext);

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
                // 最后一页，之后可以在这里记录一次 TechniqueSession
                finish();
            }
        });
    }

    private void updateUI(TextView title, TextView desc) {
        title.setText(stepTitles[currentStep]);
        desc.setText(stepDescriptions[currentStep]);
    }
}

