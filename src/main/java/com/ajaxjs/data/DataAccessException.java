package com.ajaxjs.data;

/**
 * 数据访问异常
 */
public class DataAccessException extends RuntimeException {
    /**
     * 创建一个数据访问异常
     *
     * @param msg 数据访问异常的信息
     */
    public DataAccessException(String msg) {
        super(msg);
    }
}
