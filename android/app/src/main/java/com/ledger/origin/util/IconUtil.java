package com.ledger.origin.util;

import java.util.HashMap;
import java.util.Map;

/** 分类图标到 emoji 的映射，简化资源管理 */
public class IconUtil {
    private static final Map<String, String> ICONS = new HashMap<>();

    static {
        ICONS.put("food", "🍜");
        ICONS.put("transport", "🚗");
        ICONS.put("shopping", "🛍️");
        ICONS.put("entertainment", "🎮");
        ICONS.put("housing", "🏠");
        ICONS.put("medical", "💊");
        ICONS.put("education", "📚");
        ICONS.put("communication", "📱");
        ICONS.put("salary", "💰");
        ICONS.put("bonus", "🎁");
        ICONS.put("investment", "📈");
        ICONS.put("parttime", "💼");
        ICONS.put("cash", "💵");
        ICONS.put("alipay", "🅰️");
        ICONS.put("wechat", "💚");
        ICONS.put("bank", "🏦");
        ICONS.put("credit", "💳");
        ICONS.put("other", "📌");
    }

    public static String emoji(String key) {
        if (key == null) return "📌";
        String e = ICONS.get(key);
        return e != null ? e : "📌";
    }
}
