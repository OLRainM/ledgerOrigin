package com.ledger.origin.ui.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ledger.origin.R;
import com.ledger.origin.service.SyncService;
import com.ledger.origin.ui.budget.BudgetFragment;
import com.ledger.origin.ui.record.AddRecordActivity;
import com.ledger.origin.ui.record.RecordListFragment;
import com.ledger.origin.ui.stats.StatsFragment;
import com.ledger.origin.util.DateUtil;

/** 主界面：底部导航 + Fragment 切换 */
public class MainActivity extends AppCompatActivity {

    private final Fragment recordFragment = new RecordListFragment();
    private final Fragment statsFragment = new StatsFragment();
    private final Fragment budgetFragment = new BudgetFragment();
    private final Fragment mineFragment = new MineFragment();
    private Fragment active = recordFragment;
    private final FragmentManager fm = getSupportFragmentManager();

    /** 接收 SyncService 同步完成广播 */
    private final BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 同步完成，可在此触发界面刷新，这里仅作演示
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNotificationPermission();

        // 预加载全部 Fragment，提升切换性能
        fm.beginTransaction()
                .add(R.id.fragment_container, mineFragment, "mine").hide(mineFragment)
                .add(R.id.fragment_container, budgetFragment, "budget").hide(budgetFragment)
                .add(R.id.fragment_container, statsFragment, "stats").hide(statsFragment)
                .add(R.id.fragment_container, recordFragment, "record")
                .commit();

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(this::onNavSelected);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddRecordActivity.class)));

        // 注册广播接收器并启动后台同步服务
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(syncReceiver, new IntentFilter(SyncService.ACTION_SYNC_DONE));
        Intent sync = new Intent(this, SyncService.class);
        sync.putExtra("month", DateUtil.currentMonth());
        startService(sync);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    private boolean onNavSelected(@NonNull android.view.MenuItem item) {
        Fragment target;
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            target = recordFragment;
        } else if (id == R.id.nav_stats) {
            target = statsFragment;
        } else if (id == R.id.nav_budget) {
            target = budgetFragment;
        } else {
            target = mineFragment;
        }
        if (target != active) {
            fm.beginTransaction().hide(active).show(target).commit();
            active = target;
        }
        return true;
    }
}
