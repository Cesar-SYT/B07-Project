package com.example.smartair.r3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    public interface OnDoseChangeListener {
        void onDoseChanged(InventoryItem item, int newRemainingDoses);
    }

    private List<InventoryItem> items;
    private OnDoseChangeListener listener;
    private DatabaseReference inventoryRef;


    public InventoryAdapter(List<InventoryItem> items, DatabaseReference inventoryRef, OnDoseChangeListener listener) {
        this.items = items;
        this.inventoryRef = inventoryRef;
        this.listener = listener;

    }

    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventory_item_row, parent, false);
        return new InventoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = items.get(position);

        // ---- 显示基本信息 ----
        holder.txtMedName.setText(item.getName());
        holder.txtAmount.setText("Remaining: " + item.getRemainingDoses());

        if (item.getPurchaseDate() != null) {
            holder.txtPurchase.setText("Purchase: " + item.getPurchaseDate());
        } else {
            holder.txtPurchase.setText("Purchase: -");
        }

        if (item.getExpiryDate() != null) {
            holder.txtExpiry.setText("Expiry: " + item.getExpiryDate());
        } else {
            holder.txtExpiry.setText("Expiry: -");
        }

        // ---- 显示 warning ----
        boolean low = item.isLow();
        boolean expired = item.isExpiredFlag();

        if (!low && !expired) {
            holder.txtWarning.setVisibility(View.GONE);
        } else {
            holder.txtWarning.setVisibility(View.VISIBLE);
            if (low && expired) {
                holder.txtWarning.setText("Low canister & expired");
            } else if (low) {
                holder.txtWarning.setText("Low canister");
            } else {
                holder.txtWarning.setText("Expired");
            }
        }

        // ---- 加减按钮 ----
        holder.btnAdd.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            InventoryItem current = items.get(adapterPos);
            int newVal = current.getRemainingDoses() + 1;
            current.setRemainingDoses(newVal);

            // ★ 写回 Firebase：/users/{childKey}/inventory/{id}/remainingDoses
            if (current.getId() != null && inventoryRef != null) {
                inventoryRef
                        .child(current.getId())
                        .child("remainingDoses")
                        .setValue(newVal);
            }

            if (listener != null) listener.onDoseChanged(current, newVal);
            notifyItemChanged(adapterPos);
        });

        holder.btnSubtract.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            InventoryItem current = items.get(adapterPos);
            if (current.getRemainingDoses() <= 0) return;

            int newVal = current.getRemainingDoses() - 1;
            current.setRemainingDoses(newVal);

            if (current.getId() != null && inventoryRef != null) {
                inventoryRef
                        .child(current.getId())
                        .child("remainingDoses")
                        .setValue(newVal);
            }

            if (listener != null) listener.onDoseChanged(current, newVal);
            notifyItemChanged(adapterPos);
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedName, txtAmount, txtPurchase, txtExpiry, txtWarning;
        Button btnAdd, btnSubtract;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedName = itemView.findViewById(R.id.txtMedName);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtPurchase = itemView.findViewById(R.id.txtPurchase);
            txtExpiry = itemView.findViewById(R.id.txtExpiry);
            txtWarning = itemView.findViewById(R.id.txtWarning);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnSubtract = itemView.findViewById(R.id.btnSubtract);
        }

        public void bind(InventoryItem item, OnDoseChangeListener listener) {
            txtMedName.setText(item.getName());
            txtAmount.setText("Remaining: " + item.getRemainingDoses());

            if (item.getPurchaseDate() != null) {
                txtPurchase.setText("Purchase: " + item.getPurchaseDate().toString());
            } else {
                txtPurchase.setText("Purchase: -");
            }

            if (item.getExpiryDate() != null) {
                txtExpiry.setText("Expiry: " + item.getExpiryDate().toString());
            } else {
                txtExpiry.setText("Expiry: -");
            }

            // 调用和你原来类似的 warning 逻辑
            boolean low = item.isLowCanister(20);
            boolean expired = item.isExpired(new Date());
            if (!low && !expired) {
                txtWarning.setVisibility(View.GONE);
            } else {
                txtWarning.setVisibility(View.VISIBLE);
                if (low && expired) {
                    txtWarning.setText("Low canister & expired");
                } else if (low) {
                    txtWarning.setText("Low canister");
                } else {
                    txtWarning.setText("Expired");
                }
            }

            btnAdd.setOnClickListener(v -> {
                int newVal = item.getRemainingDoses() + 1;
                item.setRemainingDoses(newVal);
                txtAmount.setText("Remaining: " + newVal);
                if (listener != null) listener.onDoseChanged(item, newVal);
            });

            btnSubtract.setOnClickListener(v -> {
                if (item.getRemainingDoses() > 0) {
                    int newVal = item.getRemainingDoses() - 1;
                    item.setRemainingDoses(newVal);
                    txtAmount.setText("Remaining: " + newVal);
                    if (listener != null) listener.onDoseChanged(item, newVal);
                }
            });
        }
    }
}
