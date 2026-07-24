package com.ajaxjs.sqlman.sqlgenerator;

import com.ajaxjs.sqlman.annotation.Column;
import com.ajaxjs.sqlman.annotation.Table;
import com.ajaxjs.sqlman.annotation.Transient;
import com.ajaxjs.sqlman.meta.DbMetaInfoUpdate;
import com.ajaxjs.sqlman.model.NullValue;
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

/**
 * No matter which type of entity you have, they finally are generated to SQL.
 */
@Slf4j
@Data
public class Entity2WriteSql {
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
     * The result of SQL.
     */
    String sql;

    /**
     * The result of parameters.
     */
    Object[] params;

    public void getInsertSql() {
        StringBuilder sb = new StringBuilder();
        List<Object> values = new ArrayList<>();
        List<String> valuesHolder = new ArrayList<>();
        sb.append("INSERT INTO ").append(tableName).append(" (");

        if (entityMap != null)
            entityMap.forEach((field, value) -> {
                sb.append("`").append(field).append("`, ");
                valuesHolder.add("?");
                values.add(value);
            });
        else if (entityBean != null)
            everyBeanField(entityBean, (field, value) -> {
                sb.append("`").append(field).append("`, ");
                valuesHolder.add("?");
                values.add(beanValue2SqlValue(value));
            });

        sb.deleteCharAt(sb.length() - 2);// 删除最后一个 ,
        sb.append(") VALUES (").append(String.join(", ", valuesHolder)).append(")");

        sql = sb.toString();
        params = values.toArray();
    }

    /**
     * Generate SQL for update.
     */
    public void getUpdateSql(boolean isUpdateAllRow, String idField) {
        if (isUpdateAllRow)
            log.warn("You're going to update ALL rows on the table {}, which is SO dangerous! " +
                    "All records will be effected!", tableName);

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

        sql = sb.toString();
        params = values.toArray();
    }

    /**
     * Generate SQL for update with ID specified row.
     *
     * @param idField Which row to update, we need the name of that field.
     * @param idValue The value of ID.
     */
    public void getUpdateSqlWithId(String idField, Object idValue) {
        if (ObjectHelper.isEmptyText(idField) && idValue == null) {
            log.warn("You're going to update ALL rows on the table {}, which is SO dangerous! " +
                    "All records will be effected!", tableName);
            return;
        }

        getUpdateSql(false, idField);
        sql += " WHERE " + idField + " = ?";

        params = Arrays.copyOf(params, params.length + 1);
        params[params.length - 1] = idValue; // 将新值加入数组末尾
    }

    /**
     * Generate SQL for update with ID specified row.
     * There is already ID in the entity, so we can take it out.
     *
     * @param idField Which row to update, we need the name of that field. Actually, the field is already ID in the entity, just tell me.
     */
    public void getUpdateSqlWithId(String idField) {
        Object idValue = null;

        if (entityMap != null)
            idValue = entityMap.get(idField);
        else if (entityBean != null) {
            DbMetaInfoUpdate meta = new DbMetaInfoUpdate(entityBean, idField);
            idValue = meta.getIdValue();
        }

        if (idValue == null)
            throw new NullPointerException("Since you didn't pass the id value, that means it's already in the entity, however it's not...");

        getUpdateSqlWithId(idField, idValue);
    }

    public void getUpdateSql(String where) {
        if (ObjectHelper.isEmptyText(where)) {
            log.warn("You're going to update ALL rows on the table {}, which is SO dangerous! " +
                    "All records will be effected!", tableName);
            return;
        }

        getUpdateSql(false, null);
        sql += " WHERE " + where;
    }

    public void getDeleteSql(String idField, Object idValue) {
        if (idValue == null) {
            if (entityMap != null)
                idValue = entityMap.get(idField);
            else if (entityBean != null) {
                DbMetaInfoUpdate meta = new DbMetaInfoUpdate(entityBean, idField);
                idValue = meta.getIdValue();
            }
        }

        if (idValue == null)
            throw new NullPointerException("Since you didn't pass the id value, that means it's already in the entity, however it's not...");

        sql = "DELETE FROM " + tableName + " WHERE " + idField + " = ?";
        params = new Object[]{idValue};
    }

    /**
     * Do the iteration of a Java Bean
     *
     * @param entity         Entity
     * @param everyBeanField An iterator for a Java Bean
     */
    public static void everyBeanField(Object entity, BiConsumer<String, Object> everyBeanField) {
        everyBeanField(entity, false, everyBeanField);
    }

    /**
     * Do the iteration of a Java Bean.
     *
     * @param entity         Entity
     * @param includeNull    Whether null-valued properties should be included
     * @param everyBeanField An iterator for a Java Bean
     */
    public static void everyBeanField(Object entity, boolean includeNull, BiConsumer<String, Object> everyBeanField) {
        Class<?> clz = entity.getClass();
        BeanInfo beanInfo;

        try {
            beanInfo = Introspector.getBeanInfo(clz);
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot inspect Java bean " + clz.getName() + ".", e);
        }

        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
            String propertyName = property.getName();

            if (CommonConstant.CLASS.equals(propertyName))
                continue;

            Field field = Fields.findField(clz, propertyName); // field info. on the entity
            String fieldName = propertyName;

            if (field != null) {
                if (field.isAnnotationPresent(Transient.class))// ignore transient fields
                    continue;

                Column column = field.getAnnotation(Column.class); // mapping another field name

                if (column != null && ObjectHelper.hasText(column.name())) // Real field name in DB
                    fieldName = column.name();
            }

            Method method = property.getReadMethod();
            if (method == null) {
                log.debug("Skip unreadable property {}.{} because it has no getter.", clz.getName(), propertyName);
                continue;
            }

            if (method.isAnnotationPresent(Transient.class)) // ignore transient fields
                continue;

            Object value;
            try {
                value = method.invoke(entity);
            } catch (IllegalAccessException e) {
                throw beanPropertyReadException(clz, propertyName, e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException() == null ? e : e.getTargetException();
                throw beanPropertyReadException(clz, propertyName, cause);
            }

            if (includeNull || value != null)
                everyBeanField.accept(Utils.changeFieldToColumnName(fieldName), value);
        }
    }

    private static IllegalStateException beanPropertyReadException(Class<?> beanClass, String propertyName, Throwable cause) {
        return new IllegalStateException("Cannot read Java bean property "
                + beanClass.getName() + "." + propertyName + " via its getter.", cause);
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
        else if (NullValue.NULL_DATE.equals(value) || NullValue.NULL_INT.equals(value)
                || NullValue.NULL_LONG.equals(value) || NullValue.NULL_STRING.equals(value)) // 如何设数据库 null 值
            return null;
        else if (value instanceof List || value instanceof Map)
            return JsonUtil.toJson(value);// 假設數據庫是 text，於是一律轉換 json
        else
            return value;
    }

    public static String getTableNameByBean(Object javaBean) {
        Table annotation = javaBean.getClass().getAnnotation(Table.class);

        return annotation == null ? null : annotation.value();
    }
}
