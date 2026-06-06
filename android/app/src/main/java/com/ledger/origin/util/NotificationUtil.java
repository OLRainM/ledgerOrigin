package com.ledger.origin.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/** 通知工具：用于预算超支提醒 */
public class NotificationUtil {

    public static final String CHANNEL_ID = "budget_alert";
    private static int sId = 1000;

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "预算提醒", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("预算超支或接近上限时提醒");
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    public static void notifyBudget(Context ctx, String title, String content) {
        ensureChannel(ctx);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager nm =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            try {
                nm.notify(sId++, builder.build());
            } catch (SecurityException ignored) {
            }
        }
    }
}
