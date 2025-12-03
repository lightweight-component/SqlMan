package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.annotation.ResultSetProcessor;
import com.ajaxjs.sqlman.model.DatabaseVendor;
import com.ajaxjs.sqlman.util.Utils;
import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.ConvertBasicValue;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.reflect.Clazz;
import com.ajaxjs.util.reflect.Methods;
import com.ajaxjs.util.reflect.Types;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base operation abstraction
 */
@Slf4j
@Data
public abstract class BaseAction {
    /**
     * An action contains many input fields.
     */
    Action action;

    /**
     * Default constructor
     *
     * @param action An action contains many input fields.
     */
    public BaseAction(Action action) {
        this.action = action;
    }

    /**
     * To log the duration of SQL operation.
     */
    public long startTime;

    void setParam2Ps(PreparedStatement ps) throws SQLException {
        Object[] params = action.getParams();

        if (ObjectHelper.isEmpty(params))
            return;

        for (int i = 0; i < params.length; i++) {
            Object ele = params[i];

            if (ele instanceof Map)
                ele = JsonUtil.toJson(ele); // Map to JSON

            if (ele instanceof List)
                throw new UnsupportedOperationException("暂不支持 List 类型参数。如果你入參用於 IN (?)，請直接拼接 SQL 語句而不是使用 PreparedStatement。這是系統的限制，無法支持 List");
            else if (ele instanceof byte[])  // for small file
                ps.setBytes(i + 1, (byte[]) ele);
            else if (ele instanceof InputStream)  // for large file
                ps.setBinaryStream(i + 1, (InputStream) ele);
            else {
//                if (NullValue.NULL_DATE.equals(ele) || NullValue.NULL_INT.equals(ele)
//                        || NullValue.NULL_LONG.equals(ele) || NullValue.NULL_STRING.equals(ele))
//                    ele = null;

                ps.setObject(i + 1, ele);
            }
        }
    }

    public static Map<String, Object> getResultMap(ResultSet rs) throws SQLException {
        // LinkedHashMap 是 HashMap 的一个子类，保存了记录的插入顺序
        Map<String, Object> map = new LinkedHashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {// 遍历结果集
            String key = Utils.changeColumnToFieldName(metaData.getColumnLabel(i));
            Object value = rs.getObject(i);
            String columnTypeName = metaData.getColumnTypeName(i);
//            log.info(key + "::" + columnTypeName);

            if (value != null) {
                if (BLOB_TYPE_MYSQL.equals(columnTypeName))
                    value = rs2Base64Str(rs, i);
                else if (JSON_TYPE_MYSQL8.equals(columnTypeName)) { /* mysql 8 json 字段对应 jdbc 的类型是？有没有办法让 jdbc 得知这个是一个 json 类型的字段？ */
                    /* JSON 类型会返回字符串 null 而不是 null */
                    if ("null".equals(value))
                        value = null;
                    else {
                        String jsonStr = value.toString();
                        value = jsonStr.startsWith("[") ? JsonUtil.json2mapList(jsonStr) : JsonUtil.json2map(jsonStr);
                    }
                }
            }

            map.put(key, value);
        }

        return map;
    }

    private static final String JSON_TYPE_MYSQL8 = "JSON";

    private static final String BLOB_TYPE_MYSQL = "BLOB";

    static String rs2Base64Str(ResultSet rs, int index) throws SQLException {
        // 获取 BLOB 数据
        Blob blob = rs.getBlob(index);
        // 将 BLOB 转为字节数组
        byte[] blobBytes = blob.getBytes(1, (int) blob.length());

        return new Base64Utils(blobBytes).encodeAsString();
    }

    /**
     * 记录集合转换为 bean 的高阶函数
     *
     * @param beanClz 实体类
     * @param <T>     bean 的类型
     * @return ResultSet 处理器，传入 ResultSet 类型对象返回 T 类型的 bean
     */
    @SuppressWarnings({"unchecked"})
    public <T> ResultSetProcessor<T> getResultBean(Class<T> beanClz) {
        return rs -> {
            ResultSetMetaData metaData = rs.getMetaData();

            if (beanClz == Integer.class || beanClz == Long.class || beanClz == String.class || beanClz == Double.class || beanClz == Float.class || beanClz == BigDecimal.class) {
                Object v = rs.getObject(1);

                return (T) v;
//                for (int i = 1; i <= columnLength; i++) {// 遍历结果集
//                    Object v = rs.getObject(i);
//
//                    return (T) v;
//                }
            }

            T bean = Clazz.newInstance(beanClz);

//            if (beanClz.toString().contains("xxx")) {
//                System.out.println();
//            }

            for (int i = 1; i <= metaData.getColumnCount(); i++) {// 遍历结果集
                String key = metaData.getColumnLabel(i);
                String columnTypeName = metaData.getColumnTypeName(i);

                if (action.getDatabaseVendor() == DatabaseVendor.H2)  // H2 的数据库字段名称是大写的，需要转换为小写
                    key = key.toLowerCase();

                Object _value = rs.getObject(i); // Real value in DB

//                if (key.startsWith("table_model") && _value != null) {
//                    log.debug(key + ":v:" + _value);
//                    log.debug(key + "::" + metaData.getColumnTypeName(i));
//                }
                if (key.contains("_")) // 将以下划线分隔的数据库字段转换为驼峰风格的字符串
                    key = Utils.changeColumnToFieldName(key);

                try {
                    PropertyDescriptor property = new PropertyDescriptor(key, beanClz);
                    Method method = property.getWriteMethod();
                    Object value;
                    Class<?> propertyType = property.getPropertyType();

                    if (key.startsWith("birthday"))
                        log.debug(key + "::" + metaData.getColumnTypeName(i));

                    // 枚举类型的支持
//					if (propertyType.isEnum()) // Enum.class.isAssignableFrom(propertyType) 这个方法也可以
//						value = dbValue2Enum(propertyType, _value);
//					else {

                    if (_value != null && JSON_TYPE_MYSQL8.equals(columnTypeName)) {
                        /* JSON 类型会返回字符串 null 而不是 null */
                        if ("null".equals(_value))
                            value = null;
                        else {
                            String jsonStr = _value.toString();

                            if (jsonStr.startsWith("{"))
//                              value = ConvertComplexValue.getConvertValue().convert(jsonStr, propertyType);
//                              value = JsonUtil.INSTANCE.fromJson(jsonStr, propertyType);
                                value = JsonUtil.fromJson(jsonStr, propertyType);
                            else if (jsonStr.startsWith("[")) {
//                            Class<?> listType =  propertyType; // it might be a List
                                Class<?> _beanClz = Types.getGenericFirstReturnType(property.getReadMethod());

                                if (_beanClz == Integer.class || _beanClz == Long.class || _beanClz == String.class)
                                    value = JsonUtil.json2list(jsonStr, _beanClz);
                                else
                                    value = _beanClz == null ? JsonUtil.json2mapList(jsonStr) : JsonUtil.json2list(jsonStr, _beanClz);
                            } else {
                                value = null;
                                log.warn("非法 JSON 字符串： {}，字段：{}", jsonStr, key);
                            }
                        }
                    } else if (_value != null && BLOB_TYPE_MYSQL.equals(columnTypeName))
                        value = rs2Base64Str(rs, i);
                    else
                        try {
                            value = ConvertBasicValue.basicConvert(_value, propertyType);
                        } catch (NumberFormatException e) {
//                        String input = value.getClass().toString();
//                        String expect = property.getPropertyType().toString();
//                        LOGGER.warning(e, "保存数据到 bean 的 {0} 字段时，转换失败，输入值：{1}，输入类型 ：{2}， 期待类型：{3}", key, "", "", expect);
                            continue; // 转换失败，继续下一个字段
                        }
//					}

                    Methods.executeMethod(bean, method, value);
                } catch (IntrospectionException e) {
                    // 数据库返回这个字段，但是 bean 没有对应的方法
//						LOGGER.info("数据库返回这个字段 {0}，但是 bean {1} 没有对应的方法", key, beanClz);
                    try {
                        if (_value != null) {
                            Object obj = Methods.executeMethod(bean, "getExtractData");

//								LOGGER.info(":::::::::key::"+ key +":::v:::" + _value);
                            if (obj == null) {
                                Map<String, Object> extractData = new HashMap<>();
                                Methods.executeMethod(bean, "setExtractData", extractData);
                                obj = Methods.executeMethod(bean, "getExtractData");
                            }

                            if (obj instanceof Map) {
                                Map<String, Object> map = (Map<String, Object>) obj;
                                map.put(key, _value);
                            }
                        }
                    } catch (SecurityException ignored) {
//                        log.warn("ERROR>>", e2);
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("记录集合转换为 bean 异常。", e);
                }
            }

            return bean;
        };
    }
}
