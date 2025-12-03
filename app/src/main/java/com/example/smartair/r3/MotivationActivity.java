package com.example.smartair.r3;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MotivationActivity extends AppCompatActivity {

    private TextView controllerStreakText;
    private TextView techniqueStreakText;
    private RecyclerView badgeRecycler;

    private MotivationState motivationState;
    private DatabaseReference usersRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_motivation);

        controllerStreakText = findViewById(R.id.streakControllerValue);
        techniqueStreakText  = findViewById(R.id.streakTechniqueValue);
        badgeRecycler        = findViewById(R.id.badgesRecycler);

        badgeRecycler.setLayoutManager(new LinearLayoutManager(this));

        usersRootRef = FirebaseDatabase
                .getInstance("https://smart-air-61888-default-rtdb.firebaseio.com/")
                .getReference("users");

        String childId = getIntent().getStringExtra("childKey");
        if (childId == null || childId.isEmpty()) {
            Toast.makeText(this, "No child id passed", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadMotivationStateFromFirebase(childId);
    }

    private void loadMotivationStateFromFirebase(String childId) {
        DatabaseReference motivationRef =
                usersRootRef.child(childId).child("motivation");

        motivationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MotivationStateRecord record =
                        snapshot.getValue(MotivationStateRecord.class);

                if (record == null) {
                    motivationState = createInitialState();
                    motivationRef.setValue(toRecord(motivationState));
                } else {
                    motivationState = fromRecord(record);
                }

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MotivationActivity.this,
                        "Failed to load: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();

                motivationState = createInitialState();
                updateUI();
            }
        });
    }

    private MotivationState createInitialState() {
        MotivationState state = new MotivationState();
        state.setStreak(StreakType.CONTROLLER_DAYS, 0);
        state.setStreak(StreakType.TECHNIQUE_COMPLETED_DAYS, 0);
        return state;
    }

    private MotivationState fromRecord(MotivationStateRecord record) {
        MotivationState state = new MotivationState();

        if (record.streaks != null) {
            for (String key : record.streaks.keySet()) {
                try {
                    StreakType type = StreakType.valueOf(key);
                    Integer value = record.streaks.get(key);
                    if (value != null) state.setStreak(type, value);
                } catch (Exception ignored) {}
            }
        }

        if (record.badges != null) {
            for (String key : record.badges.keySet()) {
                try {
                    BadgeType type = BadgeType.valueOf(key);
                    Boolean unlocked = record.badges.get(key);
                    if (Boolean.TRUE.equals(unlocked)) state.awardBadge(type);
                } catch (Exception ignored) {}
            }
        }

        return state;
    }

    private MotivationStateRecord toRecord(MotivationState state) {
        MotivationStateRecord record = new MotivationStateRecord();
        for (StreakType type : StreakType.values()) {
            record.streaks.put(type.name(), state.getStreak(type));
        }
        for (BadgeType type : BadgeType.values()) {
            record.badges.put(type.name(), state.hasBadge(type));
        }
        return record;
    }

    private void updateUI() {
        controllerStreakText.setText(
                motivationState.getStreak(StreakType.CONTROLLER_DAYS) + " days"
        );

        techniqueStreakText.setText(
                motivationState.getStreak(StreakType.TECHNIQUE_COMPLETED_DAYS) + " days"
        );

        BadgeAdapter adapter = new BadgeAdapter(motivationState);
        badgeRecycler.setAdapter(adapter);

        TextView titleBadges     = findViewById(R.id.titleBadges);
        TextView noBadgesMessage = findViewById(R.id.noBadgesMessage);

        if (adapter.getItemCount() == 0) {
            titleBadges.setVisibility(View.GONE);
            badgeRecycler.setVisibility(View.GONE);
            noBadgesMessage.setVisibility(View.VISIBLE);
        } else {
            titleBadges.setVisibility(View.VISIBLE);
            badgeRecycler.setVisibility(View.VISIBLE);
            noBadgesMessage.setVisibility(View.GONE);
        }
    }
}
