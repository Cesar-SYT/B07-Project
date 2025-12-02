package com.example.smartair;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private final List<NotificationItem> items;

    public NotificationAdapter(List<NotificationItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationItem item = items.get(position);
        holder.txtTitle.setText(item.title);
        holder.txtMessage.setText(item.message);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtMessage;

        NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtNotifTitle);
            txtMessage = itemView.findViewById(R.id.txtNotifMessage);
        }
    }
}

