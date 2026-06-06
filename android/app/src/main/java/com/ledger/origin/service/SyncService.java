package com.ledger.origin.service;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ledger.origin.model.ApiResponse;
import com.ledger.origin.model.Dtos;
import com.ledger.origin.net.ApiClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

/**
 * 数据同步服务：后台拉取本月概览，
 * 完成后通过本地广播通知界面刷新。
 */
public class SyncService extends IntentService {

    public static final String ACTION_SYNC_DONE = "com.ledger.origin.SYNC_DONE";
    public static final String EXTRA_BALANCE = "balance";

    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String month = intent != null ? intent.getStringExtra("month") : null;
        if (month == null) return;

        try {
            Map<String, String> q = new HashMap<>();
            q.put("month", month);
            Response<ApiResponse<Dtos.Overview>> resp =
                    ApiClient.get().getOverview(q).execute();

            double balance = 0;
            if (resp.body() != null && resp.body().data != null) {
                balance = resp.body().data.balance;
            }

            Intent broadcast = new Intent(ACTION_SYNC_DONE);
            broadcast.putExtra(EXTRA_BALANCE, balance);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
        } catch (Exception ignored) {
        }
    }
}
