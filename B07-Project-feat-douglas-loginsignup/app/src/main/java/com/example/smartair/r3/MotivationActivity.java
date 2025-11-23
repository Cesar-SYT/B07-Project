package com.example.smartair.r3;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;

import java.util.Arrays;
import java.util.List;

public class MotivationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_motivation);

        TextView streakValue = findViewById(R.id.streakControllerValue);
        RecyclerView badgeRecycler = findViewById(R.id.badgesRecycler);

        // TODO: 以后从 R3Service / MotivationState 读取真实 streak
        streakValue.setText("0 days");

        badgeRecycler.setLayoutManager(new LinearLayoutManager(this));
        badgeRecycler.setAdapter(new BadgeAdapter(
                Arrays.asList("10 high-quality techniques", "Perfect controller week")
        ));
    }

    private static class BadgeAdapter
            extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

        private final List<String> names;

        BadgeAdapter(List<String> names) {
            this.names = names;
        }

        @Override
        public BadgeViewHolder onCreateViewHolder(android.view.ViewGroup parent,
                                                  int viewType) {
            android.view.View view = android.view.LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_badge, parent, false);
            return new BadgeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BadgeViewHolder holder, int position) {
            holder.badgeName.setText(names.get(position));
            // 先默认全部未获得，后面你可以根据 MotivationState 设置图标可见性
        }

        @Override
        public int getItemCount() {
            return names.size();
        }

        static class BadgeViewHolder extends RecyclerView.ViewHolder {
            final android.widget.ImageView badgeIcon;
            final android.widget.TextView badgeName;
            final android.widget.ImageView badgeCheck;

            BadgeViewHolder(android.view.View itemView) {
                super(itemView);
                badgeIcon  = itemView.findViewById(R.id.badgeIcon);
                badgeName  = itemView.findViewById(R.id.badgeName);
                badgeCheck = itemView.findViewById(R.id.badgeCheck);
            }
        }
    }
}
