package com.ajaxjs.sqlman.sql;

import com.ajaxjs.sqlman.model.Create;
import com.ajaxjs.sqlman.model.PageResult;
import com.ajaxjs.sqlman.model.Update;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface DAO {

    DAO input(String sql, Object... params);

    DAO input(String sql, Map<String, Object> keyParams, Object... params);

    DAO inputXml(String sqlId, Object... params);

    DAO inputXml(String sqlId, Map<String, Object> keyParams, Object... params);

    /**
     * 有且只有一行记录，并只返回第一列的字段。可指定字段的数据类型
     *
     * @param clz 期望的结果类型
     * @param <T> 值的类型
     * @return 数据库里面的值作为 T 出现
     */
    <T> T queryOne(Class<T> clz);

    /**
     * 查询单行记录(单个结果)，保存为 Map&lt;String, Object&gt; 结构。如果查询不到任何数据返回 null。
     *
     * @return Map&lt;String, Object&gt; 结构的结果。如果查询不到任何数据返回 null。
     */
    Map<String, Object> query();

    <T> T query(Class<T> clz);

    /**
     * 查询一组结果，保存为 List&lt;Map&lt;String, Object&gt;&gt; 结构。如果查询不到任何数据返回 null。
     *
     * @return List&lt;Map&lt;String, Object&gt;&gt; 结构的结果。如果查询不到任何数据返回 null。
     */
    List<Map<String, Object>> queryList();

    /**
     * 查询一组结果，保存为 List&lt;Bean&gt; 结构。如果查询不到任何数据返回 null。
     *
     * @param beanClz Bean 实体的类
     * @param <T>     bean 的类型
     * @return List&lt;Bean&gt; 结构的结果。如果查询不到任何数据返回 null。
     */
    <T> List<T> queryList(Class<T> beanClz);

    <T> PageResult<T> page();

    <T> PageResult<T> page(Integer start, Integer limit);

    <T> PageResult<T> page(Class<T> beanClz);

    <T> PageResult<T> page(Class<T> beanClz, Integer start, Integer limit);

    <T extends Serializable> Create<T> create(boolean isAutoIns, Class<T> idType);

    Update update();

    Update delete();
}
