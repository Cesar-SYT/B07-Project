package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;

import java.util.ArrayList;
import java.util.List;

/**
 * ParentHomeActivity
 *
 * 家长端首页 Dashboard：
 * - 选择当前查看的 Child
 * - 显示今日 Zone
 * - Rescue / Controller 用药概览
 * - 最近 7/30 天趋势（现在只是占位）
 * - 通知列表
 * - 快速操作按钮（Add Log / Report / Sharing）
 */
public class ParentHomeActivity extends AppCompatActivity {

    // 根布局
    private ConstraintLayout rootLayout;

    // 顶部栏
    private TextView txtParentTitle;
    private TextView txtCurrentChildName;
    private Spinner spinnerChildSelector;

    // Zone 卡片
    private View cardZoneParent;
    private TextView txtZoneParent;

    // Rescue 卡片
    private TextView txtRescueLastUsed;
    private TextView txtRescueThisWeek;

    // Controller 卡片
    private TextView txtControllerToday;
    private TextView txtControllerWeek;

    // 趋势
    private RadioGroup rgTrendRange;

    // 通知列表
    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private final List<NotificationItem> notificationItems = new ArrayList<>();

    // 快速操作按钮
    private Button btnAddLogParent;
    private Button btnViewReportParent;
    private Button btnSharingSettingsParent;

    // 当前 child（后面可以改成真正的 Child model）
    private String currentChildName = "Alex"; // 先写死一份 demo 数据

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 布局文件名：parent_page.xml
        setContentView(R.layout.parent_page);

        initViews();
        setupTopBar();
        setupTrendSelector();
        setupNotificationsRecycler();
        setupQuickActions();

        // 初始加载当前 child 的数据（后面可接 Firebase）
        loadDataForChild(currentChildName);
    }

    private void initViews() {
        rootLayout = findViewById(R.id.parentHomeRoot);

        txtParentTitle = findViewById(R.id.txtParentTitle);
        txtCurrentChildName = findViewById(R.id.txtCurrentChildName);
        spinnerChildSelector = findViewById(R.id.spinnerChildSelector);

        cardZoneParent = findViewById(R.id.cardZoneParent);
        txtZoneParent = findViewById(R.id.txtZoneParent);

        txtRescueLastUsed = findViewById(R.id.txtRescueLastUsed);
        txtRescueThisWeek = findViewById(R.id.txtRescueThisWeek);

        txtControllerToday = findViewById(R.id.txtControllerToday);
        txtControllerWeek = findViewById(R.id.txtControllerWeek);

        rgTrendRange = findViewById(R.id.rgTrendRange);

        rvNotifications = findViewById(R.id.rvNotifications);

        btnAddLogParent = findViewById(R.id.btnAddLogParent);
        btnViewReportParent = findViewById(R.id.btnViewReportParent);
        btnSharingSettingsParent = findViewById(R.id.btnSharingSettingsParent);
    }

    /**
     * 顶部栏基本设置：标题 + 当前 Child 名称
     * 这里先不做 Spinner 的数据绑定，后面接真正列表再加。
     */
    private void setupTopBar() {
        txtParentTitle.setText("SMART AIR – Parent");

        // 默认显示当前 child 名字
        txtCurrentChildName.setText("Child: " + currentChildName);

        // TODO: 以后这里给 spinner 绑定真正的 child 列表
        // spinnerChildSelector.setAdapter(...);
        // spinnerChildSelector.setOnItemSelectedListener(...);
    }

    /**
     * 趋势 7d / 30d 切换
     */
    private void setupTrendSelector() {
        rgTrendRange.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb7days) {
                onTrendRangeChanged(7);
            } else if (checkedId == R.id.rb30days) {
                onTrendRangeChanged(30);
            }
        });
    }

    private void onTrendRangeChanged(int days) {
        // TODO: 根据 days = 7 / 30 刷新趋势图数据
        // 目前只是占位，你可以在这里调用 presenter / viewModel
        // 比如：presenter.loadTrend(currentChildId, days);
    }

    /**
     * 通知列表 RecyclerView 基本设置
     */
    private void setupNotificationsRecycler() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(notificationItems);
        rvNotifications.setAdapter(notificationAdapter);
    }

    /**
     * 快速操作按钮
     */
    private void setupQuickActions() {
        btnAddLogParent.setOnClickListener(v -> {
            Intent intent = new Intent(ParentHomeActivity.this, com.example.smartair.r3.R3MainActivity.class);
            startActivity(intent);
        });

        btnViewReportParent.setOnClickListener(v -> {
            // TODO: 打开报告页面
            // Intent intent = new Intent(this, ParentReportActivity.class);
            // startActivity(intent);
        });

        btnSharingSettingsParent.setOnClickListener(v -> {
            // TODO: 打开 sharing 设置页面
            // Intent intent = new Intent(this, SharingSettingsActivity.class);
            // startActivity(intent);
        });
    }

    /**
     * 加载当前 child 的所有数据
     * 后面接 Firebase 时可以在这里统一调用 repository。
     */
    private void loadDataForChild(@NonNull String childName) {
        currentChildName = childName;
        txtCurrentChildName.setText("Child: " + childName);

        // Zone demo：实际应该根据 log 计算 Zone
        updateZoneCard("Green");

        // Rescue demo 数据
        txtRescueLastUsed.setText("Last: 2025-11-28 14:30");
        txtRescueThisWeek.setText("This week: 2 puffs");

        // Controller demo 数据
        txtControllerToday.setText("Today: 2/2 doses taken");
        txtControllerWeek.setText("This week: 6/7 days on track");

        // Notification demo 数据
        loadDummyNotifications();
    }

    private void updateZoneCard(@NonNull String zoneName) {
        // 这里先只改文字，背景你可以用不同 drawable 来表示颜色
        txtZoneParent.setText("Today: " + zoneName + " Zone");

        // TODO: 根据 zoneName 设置不同背景，比如：
        // if ("Green".equals(zoneName)) {
        //     cardZoneParent.setBackgroundResource(R.drawable.bg_zone_green);
        // } else if ("Yellow".equals(zoneName)) {
        //     cardZoneParent.setBackgroundResource(R.drawable.bg_zone_yellow);
        // } else if ("Red".equals(zoneName)) {
        //     cardZoneParent.setBackgroundResource(R.drawable.bg_zone_red);
        // }
    }

    /**
     * 临时的假数据，方便你先看到界面效果。
     * 真正接 Firebase 的时候，把这段删掉，换成网络数据。
     */
    private void loadDummyNotifications() {
        notificationItems.clear();
        notificationItems.add(new NotificationItem(
                "Missed controller dose",
                "Alex missed the evening controller dose yesterday."
        ));
        notificationItems.add(new NotificationItem(
                "Rescue inhaler used 3 times this week",
                "Consider checking symptoms or contacting your doctor."
        ));
        notificationItems.add(new NotificationItem(
                "New report available",
                "Weekly asthma control report is ready to view."
        ));
        notificationAdapter.notifyDataSetChanged();
    }

    // ---------------- Notification model + adapter ----------------

    static class NotificationItem {
        final String title;
        final String message;

        NotificationItem(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtMessage;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(android.R.id.text1);
            txtMessage = itemView.findViewById(android.R.id.text2);
        }
    }

    /**
     * 一个非常简单的通知列表 Adapter，使用系统自带的双行布局。
     * 以后你可以换成自己定义的 card layout。
     */
    static class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

        private final List<NotificationItem> items;

        NotificationAdapter(List<NotificationItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new NotificationViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            NotificationItem item = items.get(position);
            holder.txtTitle.setText(item.title);
            holder.txtMessage.setText(item.message);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
