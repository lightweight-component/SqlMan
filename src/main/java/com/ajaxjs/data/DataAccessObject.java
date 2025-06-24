package com.ajaxjs.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 数据访问接口 DAO
 */
public interface DataAccessObject {
    /**
     * 查询单行单列的记录
     *
     * @param clz    返回的类型
     * @param sql    执行的 SQL
     * @param params SQL 参数列表（选填项，能对应 SQL 里面的`?`的插值符）
     * @param <T>    返回的类型
     * @return 单行单列记录
     */
    <T> T queryOne(Class<T> clz, String sql, Object... params);

    /**
     * 查询单笔记录，以 Java Bean 格式返回
     *
     * @param beanClz 返回的 Bean 类型
     * @param sql     SQL 语句
     * @param params  SQL 参数列表（选填项，能对应 SQL 里面的`?`的插值符）
     * @param <T>     返回的 Bean 类型
     * @return 查询单笔记录，以 Java Bean 格式返回
     */
    <T> T info(Class<T> beanClz, String sql, Object... params);

    /**
     * 查询单笔记录，以 Java Bean 格式返回
     *
     * @param sqlId     SQL Id，于 XML 里的索引
     * @param beanClz   返回的 Bean 类型
     * @param mapParams Map 格式的参数（若没有可传 null）
     * @param params    SQL 参数列表（选填项，能对应 SQL 里面的`?`的插值符）
     * @param <T>       返回的 Bean 类型
     * @return 查询单笔记录，以 Java Bea 格式返回
     */
    <T> T infoBySqlId(Class<T> beanClz, String sqlId, Map<String, Object> mapParams, Object... params);

    /**
     * 查询单笔记录，以 Map 格式返回
     *
     * @param sql    SQL 语句
     * @param params SQL 参数列表（选填项，能对应 SQL 里面的`?`的插值符）
     * @return 查询结果，如果为 null 表示没数据
     */
    Map<String, Object> infoMap(String sql, Object... params);

    /**
     * 查询单笔记录，以 Map 格式返回
     *
     * @param sqlId     SQL Id，于 XML 里的索引
     * @param mapParams Map 格式的参数（若没有可传 null）
     * @param params    SQL 参数列表（选填项，能对应 SQL 里面的`?`的插值符）
     * @return 查询结果，如果为 null 表示没数据
     */
    Map<String, Object> infoMapBySqlId(String sqlId, Map<String, Object> mapParams, Object... params);

    /**
     * 查询列表记录，以 List Java Bean 格式返回
     *
     * @param beanClz 实体 Bean 类型
     * @param sql     SQL 语句
     * @param params  SQL 参数列表（选填项，能对应 SQL 里面的`?`的插值符）
     * @param <T>     实体类引用
     * @return 查询结果，如果没数据返回一个空 List
     */
    <T> List<T> list(Class<T> beanClz, String sql, Object... params);

    /**
     * 查询列表记录，以 List Java Bean 格式返回
     *
     * @param beanClz   实体 Bean 类型
     * @param sqlId     SQL Id，于 XML 里的索引
     * @param mapParams Map 格式的参数（若没有可传 null）
     * @param params    SQL 参数列表（选填项，能对应 SQL 里面的`?`的插值符）
     * @param <T>       实体 Bean 类型
     * @return 查询结果，如果没数据返回一个空 List
     */
    <T> List<T> listById(Class<T> beanClz, String sqlId, Map<String, Object> mapParams, Object... params);

    /**
     * 查询列表记录，以 List Map 格式返回
     *
     * @param sql    SQL 语句
     * @param params SQL 参数列表（选填项，能对应 SQL 里面的`?`的插值符）
     * @return 查询结果，如果没数据返回一个空 List
     */
    List<Map<String, Object>> listMap(String sql, Object... params);

    /**
     * 查询列表记录，以 List Map 格式返回
     *
     * @param sqlId     SQL Id，于 XML 里的索引
     * @param mapParams Map 格式的参数（若没有可传 null）
     * @param params    SQL 参数列表（选填项，能对应 SQL 里面的`?`的插值符）
     * @return 查询结果，如果没数据返回一个空 List
     */
    List<Map<String, Object>> listMapBySqlId(String sqlId, Map<String, Object> mapParams, Object... params);

    /**
     * 分页查询列表记录，以 List Java Bean 格式返回
     *
     * @param beanClz   实体 Bean 类型
     * @param sql       SQL 语句
     * @param paramsMap Map 格式的参数（若没有可传 null）
     * @param <T>       实体 Bean 类型
     * @return 查询结果，如果没数据返回一个空 List
     */
    <T> PageResult<T> page(Class<T> beanClz, String sql, Map<String, Object> paramsMap);

    /**
     * 分页查询列表记录，以 List Java Bean 格式返回
     *
     * @param beanClz   实体 Bean 类型
     * @param sqlId     SQL Id，于 XML 里的索引
     * @param paramsMap Map 格式的参数（若没有可传 null）
     * @param <T>       实体 Bean 类型
     * @return 查询结果，如果没数据返回一个空 List
     */
    <T> PageResult<T> pageBySqlId(Class<T> beanClz, String sqlId, Map<String, Object> paramsMap);

    /**
     * 创建数据并返回创建的记录数量
     *
     * @param talebName 数据表名
     * @param entity    待创建的实体对象
     * @param idField   ID 字段名
     * @return 创建的记录数量，类型为 Long
     */
    Long create(String talebName, Object entity, String idField);

    /**
     * 根据实体对象、实体类的 ID 字段名称，创建对应的数据记录。
     *
     * @param entity  实体对象，包含待保存的数据
     * @param idField 实体对象中表示ID的字段名称，用于唯一标识数据记录
     * @return 返回创建数据记录的操作结果，通常是一个自增的ID或其他形式的唯一标识
     */
    Long createWithIdField(Object entity, String idField);

    /**
     * 根据实体对象创建对应的数据记录，对象已经包含 id 字段
     *
     * @param entity 实体对象，包含待保存的数据
     * @return 返回创建数据记录的操作结果，通常是一个自增的ID或其他形式的唯一标识
     */
    Long createWithIdField(Object entity);

    /**
     * 根据实体对象创建对应的数据记录
     *
     * @param entity 实体对象，包含待保存的数据
     * @return 返回创建数据记录的操作结果，通常是一个自增的ID或其他形式的唯一标识
     */
    Long create(Object entity);

    /**
     * 更新数据库中的实体数据
     *
     * @param talebName 表名，用于指定要更新数据的数据库表
     * @param entity    要更新的实体对象，其中的数据将被写入到数据库表中
     * @param idField   主键字段名，用于指定实体对象的主键字段。如果未指定，则抛出异常，因为没有主键将导致无法唯一标识要更新的记录
     * @return 如果更新操作成功影响了至少一条记录，则返回 true；否则返回 false
     */
    boolean update(String talebName, Object entity, String idField);

    /**
     * 更新数据库中的实体数据
     *
     * @param talebName 表名，用于指定要更新数据的数据库表
     * @param entity    要更新的实体对象，其中的数据将被写入到数据库表中
     * @return 如果更新操作成功影响了至少一条记录，则返回 true；否则返回 false
     */
    boolean update(String talebName, Object entity);

    /**
     * 更新数据库中的实体数据
     *
     * @param entity 要更新的实体对象，其中的数据将被写入到数据库表中
     * @return 如果更新操作成功影响了至少一条记录，则返回 true；否则返回 false
     */
    boolean update(Object entity);

    /**
     * 更新数据库中的实体数据，对象已经包含 id 字段
     *
     * @param entity 要更新的实体对象，其中的数据将被写入到数据库表中
     * @return 如果更新操作成功影响了至少一条记录，则返回 true；否则返回 false
     */
    boolean updateWithIdField(Object entity);

    /**
     * 更新数据库中的实体数据
     *
     * @param entity  要更新的实体对象，其中的数据将被写入到数据库表中
     * @param idField 主键字段名，用于指定实体对象的主键字段
     * @return 如果更新操作成功影响了至少一条记录，则返回 true；否则返回 false
     */
    boolean updateWithIdField(Object entity, String idField);

    /**
     * 根据条件更新数据库中的实体数据
     *
     * @param entity 要更新的实体对象，其类必须对应数据库中的一个表
     * @param where  SQL查询中的 WHERE 子句，用于指定更新的条件
     * @return 如果更新操作影响的行数大于0，则返回 true；否则返回 false
     */
    boolean updateWithWhere(Object entity, String where);

    /**
     * 删除指定实体的指定 id 对应的记录
     *
     * @param entity 实体对象
     * @param id     实体的 id
     * @return 如果删除成功则返回 true，否则返回 false
     */
    boolean delete(Object entity, Serializable id);

    /**
     * 根据给定的使用者姓名和 ID 删除数据
     *
     * @param talebName 数据表名
     * @param id        实体的 id
     * @return 如果删除成功则返回 true，否则返回 false
     */
    boolean delete(String talebName, Serializable id);

    /**
     * 删除实体对象
     *
     * @param entity 实体对象
     * @return 如果删除成功则返回 true，否则返回 false
     */
    boolean delete(Object entity);
}