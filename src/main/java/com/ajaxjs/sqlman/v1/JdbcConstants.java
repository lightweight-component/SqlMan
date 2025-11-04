package com.ajaxjs.sqlman.v1;

import java.util.Date;

/**
 * 常量
 */
public interface JdbcConstants {
    /**
     * 表示数据库里面的 null 值（ for Date 类型）
     * 1970 年 1 月 1 日 00:00:00 GMT
     */
    Date NULL_DATE = new Date(0);

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

    /**
     * 数据库厂商的常量集合
     */
    enum DatabaseVendor {
        ORACLE,
        MYSQL,
        MARIADB,
        POSTGRESQL,
        SQL_SERVER,
        DB2,
        SQL_LITE,
        H2,
        DERBY,
        HSQLDB
    }

    /**
     * ID 类型，可以是自增、雪花算法、UUID
     */
    enum IdType {
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
}
