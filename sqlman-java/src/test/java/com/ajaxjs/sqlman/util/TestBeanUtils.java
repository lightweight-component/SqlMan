package com.ajaxjs.sqlman.util;


import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

public class TestBeanUtils {
    @Test
    public void testEachFields() {
        // Given
        TestClass testClass = new TestClass();
        BiConsumer<String, Object> fn = (fieldName, fieldValue) -> {
            // Do something
        };

        BeanUtils.eachFields(testClass, fn);

    }
    @Test
    public void testEachField() {
        // Given
        TestClass testClass = new TestClass();
        BeanUtils.EachFieldArg fn = (key, value, property) -> {
            // Do something
        };

        // When
       BeanUtils.eachField(testClass, fn);
    }

    @Test
    public void testEachFields2() {
        // Given
        Class<?> clz = TestClass.class;
        BiConsumer<String, Field> fn = (fieldName, field) -> {
            // Do something
        };

        // When
        BeanUtils.eachFields2(clz, fn);
    }
}
