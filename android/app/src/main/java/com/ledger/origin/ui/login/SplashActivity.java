package com.ledger.origin.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.ledger.origin.ui.main.MainActivity;
import com.ledger.origin.util.PrefManager;

/** 启动页，使用 Handler 延迟跳转，判断登录状态 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (PrefManager.isLogin()) {
                intent = new Intent(this, MainActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1200);
    }
}
