package com.ajaxjs.sqlman.annotation;

import java.lang.annotation.*;

/**
 * Specifies that the property or field is not persistent.
 * By default, properties and fields are persistent.
 *
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}