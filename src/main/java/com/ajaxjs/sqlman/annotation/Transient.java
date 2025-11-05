package com.ajaxjs.sqlman.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于 Java Bean 里面某个字段的注解，说明这个字段是否不参与数据库的操作
 * <p>
 * Specifies that the property or field is not persistent.
 * By default, properties and fields are persistent.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}