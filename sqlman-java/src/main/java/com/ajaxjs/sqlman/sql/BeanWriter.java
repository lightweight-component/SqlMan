package com.ajaxjs.sqlman.sql;

import com.ajaxjs.sqlman.annotation.IgnoreDB;
import com.ajaxjs.sqlman.annotation.TableName;
import com.ajaxjs.sqlman.model.*;
import com.ajaxjs.sqlman.util.Utils;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.reflect.Methods;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public class BeanWriter extends TableInfo implements JdbcConstants {
    /**
     * 新建记录
     *
     * @param entity 实体，可以是 Map or Java Bean
     * @return 新增主键，为兼顾主键类型，返回的类型设为同时兼容 int/long/string 的 Serializable
     */
    public Serializable create(Object entity) {
        SqlParams sp = entity2InsertSql(getTableName(), entity);
        JdbcCommand crud = getCrud();
        crud.setSql(sp.sql);
        crud.setParams(sp.values);
        IdField idField = getIdField();

        Serializable newlyId = crud.create(idField.isAutoIns(), idField.getIdTypeClz()).getNewlyId();

        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) entity;
            map.put(idField.getIdField(), newlyId); // id 一开始是没有的，保存之后才有，现在增加到实体
        } else { // bean
            TableName a = entity.getClass().getAnnotation(TableName.class);

            if (a != null && a.isReturnNewlyId()) {
                try {
                    Method getId = entity.getClass().getMethod(Utils.changeColumnToFieldName("get_" + getIdField()));

                    if (newlyId == null)
                        return null; // 创建失败

                    if (newlyId.equals(-1))  // 插入成功 但没有自增
                        return (Serializable) getId.invoke(entity);

                    Class<?> idClz = getId.getReturnType();// 根据 getter 推断 id 类型
                    String setIdMethod = Utils.changeColumnToFieldName("set_" + getIdField());

                    if (Long.class == idClz && newlyId instanceof Integer) {
                        newlyId = (long) (int) newlyId;
                        Methods.executeMethod(entity, setIdMethod, newlyId);
                    } else if (Long.class == idClz && newlyId instanceof BigInteger) {
                        newlyId = ((BigInteger) newlyId).longValue();
                        Methods.executeMethod(entity, setIdMethod, newlyId);
                    } else Methods.executeMethod(entity, setIdMethod, newlyId); // 直接保存
                } catch (Throwable e) {
                    log.warn("WARN>>", e);
                }
            }
        }

        return newlyId;
    }

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
     * 修改实体
     *
     * @param tableName 数据库表名
     * @param entity    实体，可以是 Map or Java Bean
     * @return 成功修改的行数，一般为 1
     */
    public Update updateBean(String tableName, Object entity) {
        SqlParams sp;
        IdField idField = getIdField();

        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) entity;
            Object id = map.get(idField.getIdField());

            if (id == null)
                throw new DataAccessException("未指定 id，这将会是批量全体更新！");

            sp = entity2UpdateSql(tableName, map, idField.getIdField(), id);
        } else {
            String getId = Utils.changeColumnToFieldName("get_" + getIdField());
            Object id = Methods.executeMethod(entity, getId);

            if (id == null)
                throw new DataAccessException("未指定 id，这将会是批量全体更新！");

            sp = entity2UpdateSql(tableName, entity, idField.getIdField(), id);
        }

        JdbcCommand crud = getCrud();
        crud.setSql(sp.sql);
        crud.setParams(sp.values);

        return crud.update();
    }

    /**
     * 将一个实体转换成更新语句的 SqlParams 对象
     *
     * @param tableName 数据库表名
     * @param entity    字段及其对应的值
     * @param idField   ID 字段名
     * @param where     指定记录的 ID 值
     * @return 更新语句的 SqlParams 对象
     */
    public static SqlParams entity2UpdateSql(String tableName, Object entity, String idField, Object where) {
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

        if (StrUtil.hasText(idField) && where != null) {
            sb.append(" WHERE ").append(idField).append(" = ?");

            arr = Arrays.copyOf(arr, arr.length + 1);
            arr[arr.length - 1] = where; // 将新值加入数组末尾
        }

        SqlParams sp = new SqlParams();
        sp.sql = sb.toString();
        sp.values = arr;

        return sp;
    }


    /**
     * Bean 的值转换为符合 SQL 格式的。这个适用于 ? 会自动转换类型
     */
    private static Object beanValue2SqlValue(Object value) {
        if (value instanceof Enum) // 枚举类型，取其字符串保存
            return value.toString();
        else if (NULL_DATE.equals(value) || NULL_INT.equals(value) || NULL_LONG.equals(value) || NULL_STRING.equals(value)) // 如何设数据库 null 值
            return null;
        else
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
            throw new NullPointerException("该实体没有任何字段和数据");

        map.forEach(everyMapField);
    }

    /**
     * 对一个对象的每个字段进行操作
     *
     * @param entity         Java Bean 实体
     * @param everyBeanField 传入一个回调函数，将数据库列名和字段值作为参数进行操作
     */
    protected static void everyBeanField(Object entity, BiConsumer<String, Object> everyBeanField) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(entity.getClass());

            for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
                String filedName = property.getName(); // 获取字段的名称
                if ("class".equals(filedName)) continue;

                Method method = property.getReadMethod(); // 获取字段对应的读取方法
                if (method.getAnnotation(IgnoreDB.class) != null) // 忽略的字段，不参与
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
