package com.ajaxjs.sqlman.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 表名
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * 表名
     *
     * @return 表名
     */
    String value();

    /**
     * 创建实体后是否返回新建的 id
     *
     * @return v 是否返回新建的 id
     */
    boolean isReturnNewlyId() default false;

    /**
     * 是否自增 id
     *
     * @return 是否自增 id
     */
    boolean isAutoIns() default false;
}
