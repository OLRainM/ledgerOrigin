package com.ledger.origin.model;

import java.io.Serializable;
import java.util.List;

/** 各类辅助响应模型 */
public class Dtos {

    public static class LoginData implements Serializable {
        public String token;
        public long user_id;
        public String nickname;
        public String avatar;
    }

    public static class PageData<T> implements Serializable {
        public List<T> list;
        public int total;
        public int page;
    }

    public static class Overview implements Serializable {
        public double income;
        public double expense;
        public double balance;
    }

    public static class CategoryStat implements Serializable {
        public String category_name;
        public String category_icon;
        public double amount;
        public int count;
    }

    public static class DailyStat implements Serializable {
        public String date;
        public double income;
        public double expense;
    }
}
