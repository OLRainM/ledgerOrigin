package com.ledger.origin.util;

import android.content.Context;
import android.content.SharedPreferences;

/** 使用 SharedPreferences 保存登录状态和用户信息 */
public class PrefManager {
    private static final String PREF_NAME = "ledger_pref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NICKNAME = "nickname";

    private static SharedPreferences sp;

    public static void init(Context ctx) {
        if (sp == null) {
            sp = ctx.getApplicationContext()
                    .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void saveLogin(String token, long userId, String nickname) {
        sp.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_NICKNAME, nickname)
                .apply();
    }

    public static String getToken() {
        return sp.getString(KEY_TOKEN, "");
    }

    public static long getUserId() {
        return sp.getLong(KEY_USER_ID, 0);
    }

    public static String getNickname() {
        return sp.getString(KEY_NICKNAME, "");
    }

    public static boolean isLogin() {
        return !getToken().isEmpty();
    }

    public static void logout() {
        sp.edit().clear().apply();
    }
}
