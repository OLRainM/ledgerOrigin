package com.ledger.origin.model;

import java.io.Serializable;

/** 分类 */
public class Category implements Serializable {
    public long id;
    public long user_id;
    public String name;
    public String icon;
    public int type;        // 1=支出 2=收入
    public int sort_order;

    public Category() {}

    public Category(String name, String icon, int type) {
        this.name = name;
        this.icon = icon;
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }
}
