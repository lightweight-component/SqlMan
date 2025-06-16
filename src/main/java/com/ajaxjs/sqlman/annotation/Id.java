package com.ajaxjs.sqlman.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 表示 id 字段
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    /**
     * Id 是哪个字段？
     *
     * @return v
     */
    String value();
}
