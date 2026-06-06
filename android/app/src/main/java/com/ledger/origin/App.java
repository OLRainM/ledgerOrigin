package com.ledger.origin;

import android.app.Application;

import com.ledger.origin.util.PrefManager;

/** 全局 Application，初始化 SharedPreferences */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PrefManager.init(this);
    }
}
