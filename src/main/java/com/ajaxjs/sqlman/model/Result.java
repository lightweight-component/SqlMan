package com.ajaxjs.sqlman.model;

import lombok.Data;

@Data
public abstract class Result {
    /**
     * 是否成功
     */
    private boolean isOk;

    /**
     * 信息
     */
    private String message;
}
