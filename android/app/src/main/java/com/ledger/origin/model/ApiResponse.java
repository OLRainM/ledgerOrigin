package com.ledger.origin.model;

import java.io.Serializable;

/** 通用API响应包装 */
public class ApiResponse<T> implements Serializable {
    public int code;
    public String msg;
    public T data;

    public boolean isSuccess() {
        return code == 200;
    }
}
