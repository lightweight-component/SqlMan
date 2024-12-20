package com.ajaxjs.util.reflect;


import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTypes {
    @Test
    public void testGetActualType() {
        Type type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };

        Type[] actualType = Types.getActualType(type);

        assert actualType != null;
        assertEquals(actualType.length, 1);
        assertEquals(actualType[0], String.class);
    }

    @Test
    public void testGetGenericReturnType() {
        Method method = TestTypes.class.getMethods()[0];
        Type[] actualType = Types.getGenericReturnType(method);

        assertEquals(actualType.length, 1);
        assertEquals(actualType[0], String.class);
    }

    @Test
    public void testGetGenericFirstReturnType() {
        Method method = TestTypes.class.getMethods()[0];
        Class<?> actualType = Types.getGenericFirstReturnType(method);

        assertEquals(actualType, String.class);
    }

    @Test
    public void testGetActualType2() {
        Class<?> clz = TestTypes.class;
        Type[] actualType = Types.getActualType(clz);

        assertEquals(actualType.length, 1);
        assertEquals(actualType[0], String.class);
    }

    @Test
    public void testGetActualClass() {
        Class<?> clz = TestTypes.class;
        Class<?> actualClass = Types.getActualClass(clz);

        assertEquals(actualClass, String.class);
    }

    @Test
    public void testType2class() {
        Type type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };

        Class<?> actualClass = Types.type2class(type);

        assertEquals(actualClass, List.class);
    }
}
