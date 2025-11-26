package com.example.smartair;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private Context context;
    private List<ChildModel> childList;

    public ChildAdapter(Context context, List<ChildModel> childList) {
        this.context = context;
        this.childList = childList;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_child_card, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        ChildModel child = childList.get(position);
        holder.tvName.setText(child.getName());
        holder.tvAge.setText("Age: " + child.getAge());
        holder.tvDob.setText("DOB: " + child.getDob());

        // TODO: Load avatar image using Glide or Picasso if avatarUrl is present
        // For now, using placeholder in XML

        holder.itemView.setOnClickListener(v -> {
            // Navigate to ChildDetail Activity
            Intent intent = new Intent(context, ChildDetail.class);
            intent.putExtra("CHILD_ID", child.getChildId()); // Pass ID to detail view
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAge, tvDob;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_child_name);
            tvAge = itemView.findViewById(R.id.tv_child_age);
            tvDob = itemView.findViewById(R.id.tv_child_dob);
        }
    }
}