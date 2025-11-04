/**
 * Copyright (C) 2025 Frank Cheung
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ajaxjs.sqlman.v1;

import com.ajaxjs.sqlman.annotation.Column;
import com.ajaxjs.sqlman.annotation.Transient;
import com.ajaxjs.sqlman.util.Utils;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.reflect.Fields;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public class BeanWriter implements JdbcConstants {
    /**
     * 将一个实体转换成插入语句的 SqlParams 对象
     *
     * @param tableName 数据库表名
     * @param entity    字段及其对应的值
     * @return 插入语句的 SqlParams 对象
     */
    public static SqlParams entity2InsertSql(String tableName, Object entity) {
        StringBuilder sb = new StringBuilder();
        List<Object> values = new ArrayList<>();
        List<String> valuesHolder = new ArrayList<>();
        sb.append("INSERT INTO ").append(tableName).append(" (");

        if (entity instanceof Map) {
            everyMapField(entity, (field, value) -> {
                sb.append(" `").append(field).append("`,");
                valuesHolder.add(" ?");
                values.add(value);
            });
        } else { // Java Bean
            everyBeanField(entity, (field, value) -> {
                sb.append(" `").append(field).append("`,");
                valuesHolder.add(" ?");
                values.add(beanValue2SqlValue(value));
            });
        }

        sb.deleteCharAt(sb.length() - 1);// 删除最后一个 ,
        sb.append(") VALUES (").append(String.join(",", valuesHolder)).append(")");

        Object[] arr = values.toArray();  // 将 List 转为数组

        SqlParams sp = new SqlParams();
        sp.sql = sb.toString();
        sp.values = arr;

        return sp;
    }

    /**
     * 将一个实体转换成更新语句的 SqlParams 对象
     *
     * @param tableName 数据库表名
     * @param entity    字段及其对应的值
     * @param idField   ID 字段名
     * @param idValue   指定记录的 ID 值
     * @return 更新语句的 SqlParams 对象
     */
    public static SqlParams entity2UpdateSql(String tableName, Object entity, String idField, Object idValue) {
        StringBuilder sb = new StringBuilder();
        List<Object> values = new ArrayList<>();
        sb.append("UPDATE ").append(tableName).append(" SET");

        if (entity instanceof Map) {
            everyMapField(entity, (field, value) -> {
                if (field.equals(idField)) // 忽略 id
                    return;

                sb.append(" `").append(field).append("` = ?,");
                values.add(beanValue2SqlValue(value));
            });
        } else { // Java Bean
            everyBeanField(entity, (field, value) -> {
                if (field.equals(idField)) // 忽略 id
                    return;

                sb.append(" `").append(field).append("` = ?,");
                values.add(beanValue2SqlValue(value));
            });
        }

        sb.deleteCharAt(sb.length() - 1);// 删除最后一个 ,
        Object[] arr = values.toArray();  // 将 List 转为数组

        if (ObjectHelper.hasText(idField) && idValue != null) {
            sb.append(" WHERE ").append(idField).append(" = ?");

            arr = Arrays.copyOf(arr, arr.length + 1);
            arr[arr.length - 1] = idValue; // 将新值加入数组末尾
        }

        SqlParams sp = new SqlParams();
        sp.sql = sb.toString();
        sp.values = arr;

        return sp;
    }

    /**
     * Bean 的值转换为符合 SQL 格式的。这个适用于 ? 会自动转换类型
     *
     * @param value Java Bean 的值
     * @return
     */
    private static Object beanValue2SqlValue(Object value) {
        if (value instanceof Enum) // 枚举类型，取其字符串保存
            return value.toString();
        else if (NULL_DATE.equals(value) || NULL_INT.equals(value) || NULL_LONG.equals(value) || NULL_STRING.equals(value)) // 如何设数据库 null 值
            return null;
        else if (value instanceof List) {
            return JsonUtil.toJson(value);// 假設數據庫是 text，於是一律轉換 json
        } else
            return value;
    }

    /**
     * 对一个 Map 类型的实体对象的每个字段进行操作
     *
     * @param entity        Java Map 实体
     * @param everyMapField 对键和值作为参数进行操作的回调函数
     */
    protected static void everyMapField(Object entity, BiConsumer<String, Object> everyMapField) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) entity;
        if (map.isEmpty())
            throw new IllegalArgumentException("This entity is a empty entity.");

        map.forEach(everyMapField);
    }

    /**
     * 对一个对象的每个字段进行操作
     *
     * @param entity         Java Bean 实体
     * @param everyBeanField 传入一个回调函数，将数据库列名和字段值作为参数进行操作
     */
    protected static void everyBeanField(Object entity, BiConsumer<String, Object> everyBeanField) {
        Class<?> clz = entity.getClass();

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(entity.getClass());

            for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
                String filedName = property.getName(); // 获取字段的名称
                if ("class".equals(filedName))
                    continue;

                Field field2 = Fields.findField(clz, filedName);
                if (field2 != null && field2.getAnnotation(Transient.class) != null)
                    continue;

                if (field2 != null && field2.getAnnotation(Column.class) != null) {
                    Column column = field2.getAnnotation(Column.class);

                    if (ObjectHelper.hasText(column.name())) // Real field name in DB
                        filedName = column.name();
                }

                Method method = property.getReadMethod(); // 获取字段对应的读取方法
                if (method.getAnnotation(Transient.class) != null) // 忽略的字段，不参与
                    continue;

                Object value = method.invoke(entity);

                if (value != null) {// 有值的才进行操作
                    String field = Utils.changeFieldToColumnName(filedName); // 将字段名转换为数据库列名
                    everyBeanField.accept(field, value);
                }
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            log.warn("WARN>>", e);
        }
    }
}
