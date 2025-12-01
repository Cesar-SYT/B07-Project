package com.example.smartair.r3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.model.Child;

import java.time.LocalDateTime;


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

    // ⭐ 你需要根据自己的结构拿到这两个
    private R3Service r3Service;
    private Child currentChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        r3Service = new R3ServiceImpl();
        setContentView(R.layout.fragment_technique_helper);

        TextView stepTitle       = findViewById(R.id.stepTitle);
        TextView stepDescription = findViewById(R.id.stepDescription);
        Button btnPrev           = findViewById(R.id.btnPrev);
        Button btnNext           = findViewById(R.id.btnNext);

        String childId = getIntent().getStringExtra("childKey");
        String childName = getIntent().getStringExtra("childName");
        currentChild = new Child (childId, childName );


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
                // ⭐ 最后一页：记录一次 technique session，然后关闭
                onTechniqueSessionCompleted();
                finish();
            }
        });
    }

    private void updateUI(TextView title, TextView desc) {
        title.setText(stepTitles[currentStep]);
        desc.setText(stepDescriptions[currentStep]);
    }

    /** 完成一次技巧练习：给 TECHNIQUE_COMPLETED_DAYS +1 */
    private void onTechniqueSessionCompleted() {
        if (r3Service == null || currentChild == null) return;

        TechniqueSession session = new TechniqueSession(currentChild, LocalDateTime.now());

        // 如果以后你会真的填写质量项，可以从 UI 读取；现在先用默认全部 true
        session.setLipsSealed(true);
        session.setSlowDeepBreath(true);
        session.setBreathHeldFor10s(true);
        session.setWaitedBetweenPuffs(true);
        session.setSpacerUsedIfNeeded(true);

        r3Service.completeTechniqueSession(session);
    }

}


