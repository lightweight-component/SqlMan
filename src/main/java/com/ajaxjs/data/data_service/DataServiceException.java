package com.ajaxjs.data.data_service;

/**
 * 数据服务异常
 */
public class DataServiceException extends RuntimeException {
    /**
     * 创建一个数据服务异常
     *
     * @param msg 数据服务异常的信息
     */
    public DataServiceException(String msg) {
        super(msg);
    }
}
