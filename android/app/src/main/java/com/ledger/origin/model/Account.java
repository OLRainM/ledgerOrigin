package com.ledger.origin.model;

import java.io.Serializable;

/** 资金账户 */
public class Account implements Serializable {
    public long id;
    public long user_id;
    public String name;
    public String type;
    public double balance;
    public String icon;

    @Override
    public String toString() {
        return name;
    }
}
