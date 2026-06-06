package com.ledger.origin.model;

import java.io.Serializable;

/** 预算 */
public class Budget implements Serializable {
    public long id;
    public long user_id;
    public long category_id;
    public double amount;
    public String month;

    // 计算字段
    public String category_name;
    public double spent;
    public double remaining;
    public double percentage;
}
