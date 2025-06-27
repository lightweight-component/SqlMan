package com.ajaxjs.sqlman;


import com.ajaxjs.sqlman.annotation.ResultSetProcessor;
import com.ajaxjs.sqlman.model.PageResult;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.util.JsonUtil;
import com.ajaxjs.sqlman.util.Utils;
import com.ajaxjs.util.ConvertBasicValue;
import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.reflect.Clazz;
import com.ajaxjs.util.reflect.Methods;
import com.ajaxjs.util.reflect.Types;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class Sql extends JdbcCommand implements DAO {
    /**
     * Create a JDBC action with global connection
     */
    public Sql() {
        super();
    }

    /**
     * Create a JDBC action with specified connection
     */
    public Sql(Connection conn) {
        super(conn);
    }

    /**
     * Create a JDBC action with specified data source
     */
    public Sql(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public DAO input(String sql, Object... params) {
        setSql(sql);
        setParams(params);

        return this;
    }

    @Override
    public DAO input(String sql, Map<String, Object> keyParams, Object... params) {
        setSql(sql);
        setParams(params);
        setKeyParams(keyParams);

        return this;
    }

    @Override
    public <T> T queryOne(Class<T> clz) {
        Map<String, Object> map = query();

        if (map == null)
            return null;

        for (String key : map.keySet()) {// 有且只有一个记录
            Object obj = map.get(key);

            if (obj == null)
                return null;
            else {
                return ConvertBasicValue.basicCast(obj, clz);
//                if (obj instanceof Long && clz == int.class) {
//                    Object _int = ((Long) obj).intValue();
//                    return (T) _int;
//                }
//
//                if (obj instanceof Integer && (clz == long.class || clz == Long.class)) {
//                    Object _int = ((Integer) obj).longValue();
//                    return (T) _int;
//                }
//
//                return (T) obj;
            }
        }

        return null;
    }

    @Override
    public Map<String, Object> query() {
        return query(Sql::getResultMap);
    }

    @Override
    public <T> T query(Class<T> beanClz) {
        return query(getResultBean(beanClz));
    }

    @Override
    public List<Map<String, Object>> queryList() {
        return query(rs -> forEachRs(rs, Sql::getResultMap));
    }

    @Override
    public <T> List<T> queryList(Class<T> beanClz) {
        return query(rs -> forEachRs(rs, getResultBean(beanClz)));
    }

    @Override
    public <T> PageResult<T> page() {
        return page(null, null, null);
    }
    // TODO make page() returns Map, not the T

    @Override
    public <T> PageResult<T> page(Integer start, Integer limit) {
        return page(null, start, limit);
    }

    @Override
    public <T> PageResult<T> page(Class<T> beanClz) {
        return page(beanClz, null, null);
    }

    @SuppressWarnings("unchecked")
    public <T> PageResult<T> page(Class<T> beanClz, Integer start, Integer limit) {
        Pager pager = new Pager();
        pager.setDatabaseVendor(getDatabaseVendor());

        if (start != null && limit != null) {
            pager.setStart(start);
            pager.setLimit(limit);
        }

        pager.parse(getSql());
        setSql(pager.getCountTotal());
        Long total = queryOne(Long.class);

        PageResult<T> result = new PageResult<>();

        if (total != null && total > 0) {
            List<T> list;
            setSql(pager.getPageSql());

            // 如果 beanCls 为 null，则将查询结果作为 Map 列表返回
            // 否则将查询结果转换为指定实体类的列表
            if (beanClz == null)
                list = (List<T>) queryList();
            else
                list = queryList(beanClz);

            if (list != null) {
                result.setCurrentPage(pager.getCurrentPage());
                result.setTotalCount(total.intValue());
                result.addAll(list);

                return result;
            }
        }

        result.setTotalCount(0);
        result.setZero(true);

        return result;
    }

    @Override
    public UpdateResult delete() {
        return null;
    }

    /**
     * 记录集合转换为 Map
     *
     * @param rs 记录集合
     * @return Map 结果
     * @throws SQLException 转换时的 SQL 异常
     */
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
                if ("BLOB".equals(columnTypeName)) {
                    // 获取 BLOB 数据
                    Blob blob = rs.getBlob(i);
                    // 将 BLOB 转为字节数组
                    byte[] blobBytes = blob.getBytes(1, (int) blob.length());

                    value = EncodeTools.base64EncodeToString(blobBytes);
                } else if (JSON_TYPE_MYSQL8.equals(columnTypeName)) { /* mysql 8 json 字段对应 jdbc 的类型是？有没有办法让 jdbc 得知这个是一个 json 类型的字段？ */
                    /* JSON 类型会返回字符串 null 而不是 null */
                    if ("null".equals(value))
                        value = null;
                    else {
                        String jsonStr = value.toString();
                        value = jsonStr.startsWith("[") ? JsonUtil.INSTANCE.json2mapList(jsonStr) : JsonUtil.INSTANCE.json2map(jsonStr);
                    }
                }
            }

            map.put(key, value);
        }

        return map;
    }

    private static final String JSON_TYPE_MYSQL8 = "JSON";

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
                if (getDatabaseVendor() == JdbcConstants.DatabaseVendor.H2)  // H2 的数据库字段名称是大写的，需要转换为小写
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

//                    if (key.startsWith("table_model"))
//                        log.debug(key + "::" + metaData.getColumnTypeName(i));

                    // 枚举类型的支持
//					if (propertyType.isEnum()) // Enum.class.isAssignableFrom(propertyType) 这个方法也可以
//						value = dbValue2Enum(propertyType, _value);
//					else {

                    if (_value != null && JSON_TYPE_MYSQL8.equals(metaData.getColumnTypeName(i))) {
                        /* JSON 类型会返回字符串 null 而不是 null */
                        if ("null".equals(_value))
                            value = null;
                        else {
                            String jsonStr = _value.toString();

                            if (jsonStr.startsWith("{"))
//                        value = ConvertComplexValue.getConvertValue().convert(jsonStr, propertyType);

                                value = JsonUtil.INSTANCE.fromJson(jsonStr, propertyType);
                            else if (jsonStr.startsWith("[")) {
//                            Class<?> listType =  propertyType; // it might be a List

                                Class<?> _beanClz = Types.getGenericFirstReturnType(property.getReadMethod());

                                if (_beanClz == Integer.class || _beanClz == Long.class || _beanClz == String.class) {
                                    value = com.ajaxjs.util.JsonUtil.json2list(jsonStr, _beanClz);
                                } else
//                                value="foo";
                                    value = JsonUtil.INSTANCE.json2list(jsonStr, _beanClz);
                            } else {
                                value = null;
                                log.warn("非法 JSON 字符串： {}", jsonStr);
                            }
                        }
                    } else
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
                        if ((_value != null) && beanClz.getField("extractData") != null) {
                            Object obj = Methods.executeMethod(bean, "getExtractData");

//								LOGGER.info(":::::::::key::"+ key +":::v:::" + _value);
                            if (obj == null) {
                                Map<String, Object> extractData = new HashMap<>();
                                Methods.executeMethod(bean, "setExtractData", extractData);
                                obj = Methods.executeMethod(bean, "getExtractData");
                            }

                            Map<String, Object> map = (Map<String, Object>) obj;
                            assert map != null;
                            map.put(key, _value);
                        }
                    } catch (NoSuchFieldException | SecurityException ignored) {
//                        log.warn("ERROR>>", e2);
                    }
                } catch (IllegalArgumentException e) {
                    throw new DataAccessException("记录集合转换为 bean 异常。", e);
                }
            }

            return bean;
        };
    }

    /**
     * ResultSet 迭代器
     *
     * @param rs        结果集合
     * @param processor 单行处理器
     * @return 多行记录列表集合
     * @throws SQLException 异常
     */
    static <T> List<T> forEachRs(ResultSet rs, ResultSetProcessor<T> processor) throws SQLException {
        List<T> list = new ArrayList<>();

        do {
            T d = processor.process(rs);
            list.add(d);
        } while (rs.next());

//        return list.size() > 0 ? list : null; // 找不到记录返回 null，不返回空的 list
        return list; // 找不到记录返回 null，不返回空的 list
    }

    public static Sql sql(String sql, Object... params) {
        Sql _sql = new Sql();
        _sql.setSql(sql);
        _sql.setParams(params);

        return _sql;
    }

    public static Sql sql(Connection conn, String sql, Object... params) {
        Sql _sql = new Sql(conn);
        _sql.setSql(sql);
        _sql.setParams(params);

        return _sql;
    }

    public static Sql sql(DataSource dataSource, String sql, Object... params) {
        Connection conn = getConnection(dataSource);

        return sql(conn, sql, params);
    }

    @Override
    public DAO inputXml(String sqlId, Object... params) {
        setParams(params);

        SmallMyBatis smallMyBatis = new SmallMyBatis();
        smallMyBatis.loadBySqlLocations("classpath*:sql/**/*.xml");
        String sql = smallMyBatis.getSqlById(sqlId);
        setSql(sql);

        return this;
    }

    @Override
    public DAO inputXml(String sqlId, Map<String, Object> keyParams, Object... params) {
        setKeyParams(keyParams);

        return inputXml(sqlId, params);
    }

    public static Sql newInstance() {
        return new Sql(JdbcConnection.getConnection());
    }

    // shorthand
    public static Sql instance() {
        return newInstance();
    }
}
