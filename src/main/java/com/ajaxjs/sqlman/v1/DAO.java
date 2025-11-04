package com.ajaxjs.sqlman.v1;

import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object
 */
public interface DAO {
    /**
     * 执行 SQL 并返回 DAO 对象
     *
     * @param sql    SQL 语句
     * @param params 可变参数，用于替换 SQL 语句中的占位符
     * @return 返回一个 DAO 对象，该对象包含执行 SQL 后从数据库获取的数据
     */
    DAO input(String sql, Object... params);

    /**
     * 执行 SQL 并返回 DAO 对象
     *
     * @param sql       SQL 语句
     * @param keyParams 键值对参数，用于替换 SQL 语句中的变量
     * @param params    可变参数，用于替换 SQL 语句中的占位符
     * @return 返回一个 DAO 对象，该对象包含执行 SQL 后从数据库获取的数据
     */
    DAO input(String sql, Map<String, Object> keyParams, Object... params);

    /**
     * 输入 XML 里面 SQL 片段的 id，执行 SQL 并返回 DAO 对象
     *
     * @param sqlId  SQL标识符，用于定位特定的 SQL 语句
     * @param params 可变参数，用于替换 SQL 语句中的占位符
     * @return 返回一个 DAO 对象，该对象包含执行 SQL 后从数据库获取的数据
     */
    DAO inputXml(String sqlId, Object... params);

    /**
     * 输入 XML 里面 SQL 片段的 id，执行 SQL 并返回 DAO 对象
     *
     * @param sqlId     SQL标识符，用于定位特定的 SQL 语句
     * @param keyParams 键值对参数，用于替换 SQL 语句中的变量
     * @param params    可变参数，用于替换 SQL 语句中的占位符
     * @return 返回一个 DAO 对象，该对象包含执行 SQL 后从数据库获取的数据
     */
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

    /**
     * 查询单行记录(单个结果)，保存为 Java Bean 结构。如果查询不到任何数据返回 null。
     *
     * @param clz Bean 实体的类
     * @return Java Bean 的结果。如果查询不到任何数据返回 null。
     */
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

    <T extends Serializable> CreateResult<T> create(boolean isAutoIns, Class<T> idType);

    UpdateResult update();

    UpdateResult delete();
}
