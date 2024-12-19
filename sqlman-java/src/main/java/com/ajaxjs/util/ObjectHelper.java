/*
 * Copyright (C) 2025 Frank Cheung<frank@ajaxjs.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ajaxjs.util;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper for Java Object.
 */
public class ObjectHelper {
    /**
     * 对象深度克隆
     *
     * @param <T> 对象泛型参数
     * @param obj 待克隆的对象
     * @return 克隆后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(T obj) {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bout)) {
            oos.writeObject(obj);

            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
                // 说明：调用 ByteArrayInputStream 或 ByteArrayOutputStream 对象的 close 方法没有任何意义
                // 这两个基于内存的流只要垃圾回收器清理对象就能够释放资源，这一点不同于对外部资源（如文件流）的释放
                return (T) ois.readObject();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("对象深度克隆 Error.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("对象深度克隆 Error. 找不到类", e);
        }
    }

    /**
     * Dummy Map
     */
    public static final Map<String, Object> EMPTY_PARAMS_MAP = Collections.unmodifiableMap(new HashMap<>());

    /**
     * 创建一个新的 HashMap
     *
     * @param k1  键1
     * @param v1  值1
     * @param <K> k1 类型
     * @param <V> v1 类型
     * @return 新创建的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);

        return map;
    }

    /**
     * 创建一个新的 HashMap
     *
     * @param k1  键1
     * @param v1  值1
     * @param k2  键2
     * @param v2  值2
     * @param <K> k1 类型
     * @param <V> v1 类型
     * @return 新创建的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);

        return map;
    }

    /**
     * 创建一个新的 HashMap
     *
     * @param k1  键1
     * @param v1  值1
     * @param k2  键2
     * @param v2  值2
     * @param k3  键3
     * @param v3  值3
     * @param <K> k1 类型
     * @param <V> v1 类型
     * @return 新创建的 HashMap
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = mapOf(3);
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);

        return map;
    }

    /**
     * Creates a HashMap with a specified expected number of entries.
     * The initial capacity and load factor are calculated to minimize resizing.
     *
     * @param expectedSize the expected number of entries in the map
     * @return a new HashMap with optimal initial capacity and load factor
     */
    public static <K, V> Map<K, V> mapOf(int expectedSize) {
        // Calculate the initial capacity as the next power of two greater than or equal to expectedSize / default_load_factor
        float defaultLoadFactor = 0.75f;
        int initialCapacity = (int) Math.ceil(expectedSize / defaultLoadFactor);
        initialCapacity = Integer.highestOneBit(initialCapacity - 1) << 1;

        // Ensure that the initial capacity is at least 16 (the default capacity)
        if (initialCapacity < 16)
            initialCapacity = 16;

        return new HashMap<>(initialCapacity, defaultLoadFactor);// Create and return the HashMap with the calculated initial capacity and default load factor
    }
}
