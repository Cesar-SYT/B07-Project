package com.example.smartair.r3;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.model.Child;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;


import java.util.List;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {

    private List<Child> children;


    public interface OnChildClickListener {
        void onChildClick(Child child);
    }

    private OnChildClickListener listener;
    public ChildrenAdapter(List<Child> children, OnChildClickListener listener) {
        this.children = children;
        this.listener = listener;
    }
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_card, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = children.get(position);

        holder.tvName.setText(child.getDisplayName());

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChildClick(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnAction;
        ChildViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_child_name);
            btnAction = itemView.findViewById(R.id.btn_child_action);
        }
    }

}
