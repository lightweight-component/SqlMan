package com.ajaxjs.sqlman.v1;

/**
 * 给 PrepareStatement 用的 SQL 语句和参数值列表
 */
public class SqlParams {
    /**
     * 给 PrepareStatement 用的 SQL 语句
     */
    public String sql;

    /**
     * 参数值列表
     */
    public Object[] values;
}