package com.ajaxjs.sqlman.model.tablemodel;

/**
 * ID 类型，可以是自增、雪花算法、UUID
 */
public enum IdType {
    /**
     * 自增
     */
    AUTO_INC,

    /**
     * 雪花 id（Long）
     */
    SNOW,

    /**
     * UUID
     */
    UUID
}
