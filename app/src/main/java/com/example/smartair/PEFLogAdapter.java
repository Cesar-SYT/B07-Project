package com.example.smartair;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying PEF Logs in a RecyclerView.
 */
public class PEFLogAdapter extends RecyclerView.Adapter<PEFLogAdapter.PEFLogViewHolder> {

    private List<PEFLogModel> pefLogList;

    public PEFLogAdapter(List<PEFLogModel> pefLogList) {
        this.pefLogList = pefLogList;
    }

    @NonNull
    @Override
    public PEFLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pef_log, parent, false);
        return new PEFLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PEFLogViewHolder holder, int position) {
        PEFLogModel log = pefLogList.get(position);

        holder.tvDate.setText(log.getDateTime());
        holder.tvPefValue.setText(String.format("%s L/min", (int) log.getPefValue()));
        
        // Zone Logic
        String zone = log.getZone();
        holder.tvZone.setText(zone);

        // Set background based on zone
        if ("Green".equalsIgnoreCase(zone)) {
            holder.tvZone.setBackgroundResource(R.drawable.bg_zone_green);
            holder.tvZone.setTextColor(Color.WHITE);
        } else if ("Yellow".equalsIgnoreCase(zone)) {
            holder.tvZone.setBackgroundResource(R.drawable.bg_zone_yellow);
            holder.tvZone.setTextColor(Color.WHITE);
        } else if ("Red".equalsIgnoreCase(zone)) {
            holder.tvZone.setBackgroundResource(R.drawable.bg_zone_red);
            holder.tvZone.setTextColor(Color.WHITE);
        } else {
            holder.tvZone.setBackgroundColor(Color.GRAY);
            holder.tvZone.setTextColor(Color.WHITE);
        }

        // Zebra striping for rows (Alternate background colors)
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF")); // White
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5")); // Light Gray
        }
    }

    @Override
    public int getItemCount() {
        return pefLogList != null ? pefLogList.size() : 0;
    }

    public static class PEFLogViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvPefValue, tvZone;

        public PEFLogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvPefValue = itemView.findViewById(R.id.tv_pef_value);
            tvZone = itemView.findViewById(R.id.tv_zone);
        }
    }
}