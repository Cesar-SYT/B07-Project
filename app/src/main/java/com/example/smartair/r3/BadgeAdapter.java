package com.example.smartair.r3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;

import java.util.ArrayList;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private final List<BadgeType> unlockedBadges = new ArrayList<>();

    BadgeAdapter(MotivationState state) {
        // ⭐ 只把“已解锁”的 badge 放进列表
        for (BadgeType type : BadgeType.values()) {
            if (state.hasBadge(type)) {
                unlockedBadges.add(type);
            }
        }
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        BadgeType type = unlockedBadges.get(position);

        holder.badgeName.setText(getBadgeTitle(type));
        holder.badgeCheck.setVisibility(View.VISIBLE);
        holder.badgeIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return unlockedBadges.size();   // ⭐ 决定 RecyclerView 有几行
    }

    private String getBadgeTitle(BadgeType type) {
        switch (type) {
            case FIRST_PERFECT_CONTROLLER_WEEK:
                return "First perfect controller week";
            case TEN_HIGH_QUALITY_TECHNIQUE_SESSIONS:
                return "10 high-quality technique sessions";
            case LOW_RESCUE_MONTH:
                return "Low rescue medication month";
            default:
                return type.name();
        }
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        final ImageView badgeIcon;
        final TextView badgeName;
        final ImageView badgeCheck;

        BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeIcon  = itemView.findViewById(R.id.badgeIcon);
            badgeName  = itemView.findViewById(R.id.badgeName);
            badgeCheck = itemView.findViewById(R.id.badgeCheck);
        }
    }
}

