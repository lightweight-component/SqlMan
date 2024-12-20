package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestObjectHelper {

    // 用于测试的非序列化类
    private static class SerializableObject implements Serializable {
        private String data = "data";
    }

    // 用于测试的非序列化类
    private static class NonSerializableObject {
        private String data = "data";
    }

    @Test
    public void clone_SuccessfulClone_ReturnsClonedObject() {
        SerializableObject serializableObject = new SerializableObject();
        SerializableObject clone = ObjectHelper.clone(serializableObject);
        assertNotSame(clone, serializableObject);
    }

    @Test
    public void mapOf_ValidInputs_ReturnsMapWithOneEntry() {
        String key = "key";
        String value = "value";
        Map<String, String> map = ObjectHelper.mapOf(key, value);

        assertNotNull(map, "The map should not be null");
        assertEquals(1, map.size(), "The map should contain one entry");
        assertTrue(map.containsKey(key), "The map should contain the key");
        assertEquals(value, map.get(key), "The map should contain the correct value for the key");

        Map<String, Object> map2 = ObjectHelper.mapOf("key", "value", "key2", 2);
        assertNotNull(map2);

        Map<String, Object> map3 = ObjectHelper.mapOf("key", "value", "key2", 2, "key3", true);
        assertNotNull(map3);
    }

    @Test
    public void mapOf_NullKey_ReturnsMapWithNullKey() {
        String value = "value";
        Map<String, String> map = ObjectHelper.mapOf(null, value);

        assertNotNull(map, "The map should not be null");
        assertEquals(1, map.size(), "The map should contain one entry");
        assertTrue(map.containsKey(null), "The map should contain the null key");
        assertEquals(value, map.get(null), "The map should contain the correct value for the null key");
    }

}
