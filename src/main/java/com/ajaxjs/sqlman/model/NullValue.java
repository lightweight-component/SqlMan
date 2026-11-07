package com.ajaxjs.sqlman.model;

import java.util.Date;
import java.time.LocalDateTime;

/**
 * When a user really wants to insert a null value into a database, we can use this interface to replace the value.
 * Because we can't use 'null' simply.
 */
public interface NullValue {
    /**
     * 表示数据库里面的 null 值（ for Date 类型）
     * 1970 年 1 月 1 日 00:00:00 GMT
     */
    Date NULL_DATE = new Date(0);

    /**
     * Explicit SQL NULL marker for LocalDateTime Bean properties.
     * <p>Uses LocalDateTime.MIN as a reserved value. Entity write generation compares
     * by value, so an equal LocalDateTime is also treated as SQL NULL.</p>
     */
    LocalDateTime NULL_LOCAL_DATE_TIME = LocalDateTime.MIN;

    /**
     * 表示数据库里面的 null 值（for String 类型）
     */
    String NULL_STRING = "__NULL_STRING__";

    /**
     * 表示数据库里面的 null 值（ for int 类型）
     */
    Integer NULL_INT = Integer.MAX_VALUE;

    /**
     * 表示数据库里面的 null 值（ for long 类型）
     */
    Long NULL_LONG = Long.MAX_VALUE;
}
