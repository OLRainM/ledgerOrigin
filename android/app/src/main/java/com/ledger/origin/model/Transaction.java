package com.ledger.origin.model;

import java.io.Serializable;

/** 交易记录 */
public class Transaction implements Serializable {
    public long id;
    public long user_id;
    public int type;          // 1=支出 2=收入
    public double amount;
    public long category_id;
    public long account_id;
    public String note;
    public String date;
    public String created_at;

    // join 字段
    public String category_name;
    public String category_icon;
    public String account_name;

    public boolean isExpense() {
        return type == 1;
    }
}
