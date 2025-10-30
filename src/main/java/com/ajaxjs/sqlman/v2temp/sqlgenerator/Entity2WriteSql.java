package com.ajaxjs.sqlman.v2temp.sqlgenerator;

import com.ajaxjs.sqlman.JdbcConstants;
import com.ajaxjs.sqlman.annotation.Column;
import com.ajaxjs.sqlman.annotation.Table;
import com.ajaxjs.sqlman.annotation.Transient;
import com.ajaxjs.sqlman.util.Utils;
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.reflect.Fields;
import lombok.Data;
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
@Data
public class Entity2WriteSql implements JdbcConstants {
    /**
     * A data entity in Map format.
     */
    Map<String, Object> entityMap;

    /**
     * A data entity in Java Bean format.
     */
    Object entityBean;

    /**
     * Which table to write in.
     */
    String tableName;

    public Entity2WriteSql(Map<String, Object> entityMap) {
        this.entityMap = entityMap;
    }

    public Entity2WriteSql(Object entityBean) {
        this.entityBean = entityBean;
        this.tableName = getTableNameByBean(entityBean);
    }

    /**
     * 给 PrepareStatement 用的 SQL 语句
     */
    String sql;

    /**
     * 参数值列表
     */
    Object[] params;

    public void getInsertSql() {
        StringBuilder sb = new StringBuilder();
        List<Object> values = new ArrayList<>();
        List<String> valuesHolder = new ArrayList<>();
        sb.append("INSERT INTO ").append(tableName).append(" (");

        if (entityMap != null)
            entityMap.forEach((field, value) -> {
                sb.append(" `").append(field).append("`,");
                valuesHolder.add(" ?");
                values.add(value);
            });
        else if (entityBean != null)
            everyBeanField(entityBean, (field, value) -> {
                sb.append(" `").append(field).append("`,");
                valuesHolder.add(" ?");
                values.add(beanValue2SqlValue(value));
            });

        sb.deleteCharAt(sb.length() - 1);// 删除最后一个 ,
        sb.append(") VALUES (").append(String.join(",", valuesHolder)).append(")");

        sql = sb.toString();
        params = values.toArray();
    }

    public void getUpdateSql(String idField, Object idValue) {
        StringBuilder sb = new StringBuilder();
        List<Object> values = new ArrayList<>();
        sb.append("UPDATE ").append(tableName).append(" SET");

        if (entityMap != null)
            entityMap.forEach((field, value) -> {
                if (field.equals(idField)) // 忽略 id
                    return;

                sb.append(" `").append(field).append("` = ?,");
                values.add(beanValue2SqlValue(value));
            });
        else if (entityBean != null)
            everyBeanField(entityBean, (field, value) -> {
                if (field.equals(idField)) // 忽略 id
                    return;

                sb.append(" `").append(field).append("` = ?,");
                values.add(beanValue2SqlValue(value));
            });


        sb.deleteCharAt(sb.length() - 1);// 删除最后一个 ,
        Object[] arr = values.toArray();  // 将 List 转为数组

        if (ObjectHelper.isEmptyText(idField) && idValue == null) {
            log.warn("You're going to update ALL rows on the table {}, which is SO dangerous! " +
                    "All records will be effected!", tableName);
            return;
        }

        sb.append(" WHERE ").append(idField).append(" = ?");

        arr = Arrays.copyOf(arr, arr.length + 1);
        arr[arr.length - 1] = idValue; // 将新值加入数组末尾

        sql = sb.toString();
        params = values.toArray();
    }

    /**
     * Do the iteration of a Java Bean
     *
     * @param entity         Entity
     * @param everyBeanField An iterator for a Java Bean
     */
    static void everyBeanField(Object entity, BiConsumer<String, Object> everyBeanField) {
        Class<?> clz = entity.getClass();

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(entity.getClass());

            for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
                String filedName = property.getName();

                if (CommonConstant.CLASS.equals(filedName))
                    continue;

                Field field = Fields.findField(clz, filedName); // field info. on the entity

                if (field != null) {
                    if (field.isAnnotationPresent(Transient.class))// ignore transient fields
                        continue;

                    Column column = field.getAnnotation(Column.class); // mapping another field name

                    if (ObjectHelper.hasText(column.name())) // Real field name in DB
                        filedName = column.name();
                }

                Method method = property.getReadMethod();

                if (method.isAnnotationPresent(Transient.class)) // ignore transient fields
                    continue;

                Object value = method.invoke(entity);

                if (value != null) // only has value to do so
                    everyBeanField.accept(Utils.changeFieldToColumnName(filedName), value);
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Error occurred when iterating a Java bean.", e);
        }
    }

    /**
     * Translate Java Bean value to SQL value. This is suitable for ? will automatically convert a type.
     *
     * @param value The value on the Java Bean.
     * @return The value for SQL.
     */
    private static Object beanValue2SqlValue(Object value) {
        if (value instanceof Enum) // 枚举类型，取其字符串保存
            return value.toString();
        else if (NULL_DATE.equals(value) || NULL_INT.equals(value) || NULL_LONG.equals(value) || NULL_STRING.equals(value)) // 如何设数据库 null 值
            return null;
        else if (value instanceof List)
            return JsonUtil.toJson(value);// 假設數據庫是 text，於是一律轉換 json
        else
            return value;
    }

    public static String getTableNameByBean(Object javaBean) {
        Table annotation = javaBean.getClass().getAnnotation(Table.class);

        return annotation == null ? null : annotation.value();
    }
}
