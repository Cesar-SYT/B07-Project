package com.example.smartair.ui.symptoms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.ui.r5model.SymptomEntry;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SymptomHistoryAdapter extends RecyclerView.Adapter<SymptomHistoryAdapter.EntryViewHolder> {

    private List<SymptomEntry> entries;

    public SymptomHistoryAdapter(List<SymptomEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.symptom_entry_template, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        SymptomEntry entry = entries.get(position);

        // date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.textDate.setText(sdf.format(entry.timestamp));

        // enteredBy
        holder.textAuthor.setText("Entered by " + entry.enteredBy);

        // symptoms
        StringBuilder sb = new StringBuilder();
        if (entry.sleep != null) sb.append("• ").append(entry.sleep).append("\n");
        if (entry.activity != null) sb.append("• ").append(entry.activity).append("\n");
        if (entry.cough != null) sb.append("• ").append(entry.cough);

        holder.textSummary.setText(sb.toString());

        // triggers
        holder.chipGroup.removeAllViews();

        if (entry.triggers != null) {
            for (String t : entry.triggers) {
                Chip chip = new Chip(holder.chipGroup.getContext());
                chip.setText(t);
                chip.setClickable(false);
                chip.setCheckable(false);
                holder.chipGroup.addView(chip);
            }
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {

        TextView textDate, textAuthor, textSummary;
        ChipGroup chipGroup;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_view_date);
            textAuthor = itemView.findViewById(R.id.text_view_author);
            textSummary = itemView.findViewById(R.id.text_view_symptoms_summary);
            chipGroup = itemView.findViewById(R.id.chip_group_triggers_display);
        }
    }

    public void updateList(List<SymptomEntry> newList) {
        this.entries = newList;
        notifyDataSetChanged();
    }
}
