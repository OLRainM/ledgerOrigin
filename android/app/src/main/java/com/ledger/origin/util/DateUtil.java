package com.ledger.origin.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** 日期工具 */
public class DateUtil {

    public static String currentMonth() {
        return new SimpleDateFormat("yyyy-MM", Locale.CHINA).format(Calendar.getInstance().getTime());
    }

    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Calendar.getInstance().getTime());
    }

    public static String monthStart(String month) {
        return month + "-01";
    }

    public static String monthEnd(String month) {
        // month: yyyy-MM
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        Calendar c = Calendar.getInstance();
        c.set(year, m - 1, 1);
        int last = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        return String.format(Locale.CHINA, "%s-%02d", month, last);
    }
}
