package com.example.smartair.r3;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.model.Child;
import android.view.View;

import java.util.List;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {

    private List<Child> children;

    public ChildrenAdapter(List<Child> children) {
        this.children = children;
    }

    @Override
    public ChildViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_card, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChildViewHolder holder, int position) {
        Child child = children.get(position);

        // TODO: 这里根据你 Child 类的实际字段名修改
        holder.tvName.setText(child.getDisplayName());
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        ChildViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_child_name);
        }
    }
}
