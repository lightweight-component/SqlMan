package com.ajaxjs.data.crud;

import com.ajaxjs.data.DataAccessObject;
import com.ajaxjs.data.PageResult;
import com.ajaxjs.data.SmallMyBatis;
import com.ajaxjs.data.data_service.BaseEntityConstants;
import com.ajaxjs.data.data_service.DataServiceUtils;
import com.ajaxjs.data.data_service.TenantService;
import com.ajaxjs.data.jdbc_helper.JdbcWriter;
import com.ajaxjs.data.util.SnowflakeId;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 通用实体快速的 CRUD。这个服务无须 DataService
 * 提供默认 CRUD 的逻辑，包含常见情况的 SQL。
 *
 * @param <T> 实体类型，可以是 Bean 或者 Map
 * @param <K> Id 类型，一般为 Long
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FastCRUD<T, K extends Serializable> extends FastCRUD_Config {
    /**
     * 实体类引用
     */
    private Class<T> clz;

    private DataAccessObject dao;

    private JdbcWriter jdbcWriter;

    /**
     * 1=自增；2=雪花；3=UUID
     */
    private Integer idType = BaseEntityConstants.IdType.AUTO_INC;

    public final static String DUMMY_STR = "1=1";

    private final static String SELECT_SQL = "SELECT * FROM %s WHERE " + DUMMY_STR;

    /**
     * 根据当前的业务上下文，构造用于查询托管信息的 SQL 语句。
     * 此方法主要用于处理动态 SQL 的生成，根据不同的条件拼接适合当前业务场景的查询语句。
     *
     * @return 返回构造好的 SQL 查询语句
     */
    private String getManagedInfoSql() {
        String sql = getInfoSql();// 尝试获取已经定义好的 SQL 语句

        // 如果已经定义了 SQL 语句且不为空，则处理查询参数的动态替换
        if (StringUtils.hasText(sql)) {
            Map<String, Object> queryStringParams = DataServiceUtils.getQueryStringParams();// 获取查询字符串中的参数
            sql = SmallMyBatis.handleSql(sql, queryStringParams);   // 使用 SmallMyBatis 框架处理 SQL中的参数替换
        } else
            // 如果没有预定义SQL，则根据表名和ID字段生成一个默认的查询 SQL
            sql = String.format(SELECT_SQL, getTableName()).replace(DUMMY_STR, DUMMY_STR + " AND " + getTableModel().getIdField() + " = ?");

        sql = limitToCurrentUser(sql);// 限制查询结果只包含当前用户的数据

        return isTenantIsolation() ? TenantService.addTenantIdQuery(sql) : sql;// 根据是否启用了租户隔离，动态添加租户ID查询条件
    }

    /**
     * 获取单笔记录
     *
     * @param id 主键值，用于唯一标识一个实体
     * @return 实体记录
     */
    public T info(K id) {
        Objects.requireNonNull(clz, "Please give Bean Class");
        return dao.info(clz, getManagedInfoSql(), id);
    }

    /**
     * 获取单笔记录
     *
     * @param id 主键值，用于唯一标识一个实体
     * @return 包含实体详细信息的 Map 对象，键为字段名，值为字段值
     */
    public Map<String, Object> infoMap(K id) {
        return dao.infoMap(getManagedInfoSql(), id);
    }

    /**
     * 获取列表
     *
     * @return 列表
     */
    public List<T> list() {
        return list(null);
    }

    /**
     * 获取列表（Map 格式）
     *
     * @param where 查询条件，用于筛选数据
     * @return 列表（Map 格式）
     */
    public List<Map<String, Object>> listMap(String where) {
        String sql = getListSql(where);

        return dao.listMap(sql);
    }

    private String getListSql(String where) {
        String sql = getListSql();

        if (StringUtils.hasText(sql))
            sql = SmallMyBatis.handleSql(sql, DataServiceUtils.getQueryStringParams());
        else {
            if (isListOrderByDate()) {
                String createDateField = getTableModel().getCreateDateField();
                String tableName = getTableName();

                sql = String.format(SELECT_SQL + " ORDER BY " + createDateField + " DESC", tableName);
            } else
                sql = String.format(SELECT_SQL, getTableName());
        }

        if (getTableModel().isHasIsDeleted())
            sql = sql.replace(DUMMY_STR, DUMMY_STR + " AND " + getTableModel().getDelField() + " != 1");

        sql = limitToCurrentUser(sql);

        if (isTenantIsolation())
            sql = TenantService.addTenantIdQuery(sql);

        if (where != null)
            sql = sql.replace(DUMMY_STR, DUMMY_STR + where);

        return sql;
    }

    /**
     * 根据条件查询列表数据
     *
     * @param where 查询条件，用于筛选数据
     * @return 返回查询结果列表，列表元素类型为泛型 T
     */
    public List<T> list(String where) {
        String sql = getListSql(where);  // 构造查询SQL语句
        Objects.requireNonNull(clz, "Please give Bean Class");

        return dao.list(clz, sql); // 执行查询操作，并返回结果列表
    }

    /**
     * 根据指定的查询条件进行分页查询
     *
     * @param where 查询条件，用于筛选数据
     * @return PageResult 分页查询结果，包含查询到的数据及分页信息
     */
    public PageResult<T> page(String where) {
        String sql = getListSql(where); // 构造查询 SQL 语句

        return dao.page(clz, sql, null); // 执行分页查询，并返回结果
    }

    /**
     * 根据指定的查询条件进行分页查询
     *
     * @param where 查询条件，用于筛选数据
     * @return Map 分页查询结果，包含查询到的数据及分页信息
     */
    public PageResult<Map<String, Object>> pageMap(String where) {
        String sql = getListSql(where); // 构造查询 SQL 语句

        return dao.page(null, sql, null); // 执行分页查询，并返回结果
    }

    /**
     * 更新之前的执行的回调函数，可以设置 updateBy 等的字段
     */
    private BiFunction<Boolean, String, String> beforeDelete;

    /**
     * 根据给定的 ID 删除记录。
     * 如果表中有标记删除字段（isDeleted），则更新该字段为删除状态（通常为 1）；
     * 否则，直接从表中删除对应的记录。
     *
     * @param id 要删除的记录的 ID，类型为泛型 K。
     * @return 总是返回 true，表示删除操作已执行。
     */
    public boolean delete(K id) {
        String sql;

        // 根据是否有删除标记字段来构造不同的 SQL 语句
        if (getTableModel().isHasIsDeleted())
            sql = "UPDATE " + getTableName() + " SET " + getTableModel().getDelField() + " = 1";
        else sql = "DELETE FROM " + getTableName();

        sql += " WHERE " + DUMMY_STR + " AND " + getTableModel().getIdField() + " = ?";
        sql = limitToCurrentUser(sql); // 对 SQL 语句添加当前用户限制，确保操作的安全性

        if (beforeDelete != null)
            sql = beforeDelete.apply(getTableModel().isHasIsDeleted(), sql);

        if (jdbcWriter == null && getDao() != null)
            jdbcWriter = ((CRUD_Service) getDao()).getWriter();

        jdbcWriter.write(sql, id);// 执行 SQL 语句

        return true;
    }

    /**
     * 对给定的 SQL 查询语句进行限制，确保只查询当前用户的数据。
     * 如果当前配置为只查询当前用户的数据，将在 SQL 语句中添加条件“user_id = 当前用户 ID”。
     * 如果提供的 SQL 语句中已包含特定的占位符（DUMMY_STR），则会将条件追加到该占位符之后，否则，将条件直接追加到 SQL 语句末尾。
     *
     * @param sql 初始的 SQL 查询语句
     * @return 经过限制条件添加后的 SQL 查询语句
     */
    private String limitToCurrentUser(String sql) {
        if (isCurrentUserOnly()) { // 检查是否配置为只查询当前用户的数据
            String add = " AND user_id = " + DataServiceUtils.getCurrentUserId(); // 构造添加的查询条件

            if (sql.contains(DUMMY_STR)) // 检查SQL语句中是否已包含占位符
                sql = sql.replace(DUMMY_STR, DUMMY_STR + add); // 将条件插入到占位符之后
            else
                sql += add; // 直接将条件追加到SQL语句末尾
        }

        return sql; // 返回修改后的SQL语句
    }

    /**
     * 创建之前的执行的回调函数，可以设置 createDate, createBy 等字段
     */
    private Consumer<Map<String, Object>> beforeCreate;

    /**
     * 创建实体
     *
     * @param params 实体
     * @return NewlyId
     */
    @SuppressWarnings("unchecked")
    public K create(Map<String, Object> params) {
        String idField = getTableModel().getIdField();

        if (idType != null) { // auto increment by default
            if (idType == 2)
                params.put(getTableModel().getIdField(), SnowflakeId.get());

            if (idType == 3)
                params.put(idField, StrUtil.uuid());
        }

        if (beforeCreate != null)
            beforeCreate.accept(params);

        Integer tenantId = TenantService.getTenantId();

        if (tenantId != null)
            params.put("tenant_id", tenantId);

        if (isCurrentUserOnly())
            params.put("user_id", DataServiceUtils.getCurrentUserId());

        return (K) dao.create(getTableName(), params, idField);
    }

    /**
     * 根据给定的 bean 实例创建一个新的 bean。
     * 该方法通过将 bean 实例转换为 Map，然后使用该 Map 创建一个新的 bean。
     *
     * @param bean 需要被转换并用于创建新实例的原始 bean
     * @return id
     */
    public K createBean(T bean) {
        return create(JsonUtil.pojo2map(bean));
    }

    /**
     * 更新之前的执行的回调函数，可以设置 updateDate, updateBy 等字段
     */
    private Consumer<Map<String, Object>> beforeUpdate;

    /**
     * 更新数据库中的实体信息
     *
     * @param params 包含要更新的字段及其新值的 Map
     * @return 返回更新操作的成功与否
     * @throws SecurityException 如果尝试修改不存在的实体或仅允许当前用户修改时，会抛出此异常
     */
    public Boolean update(Map<String, Object> params) {
        String tableName = getTableName(); // 获取表名
        String idField = getTableModel().getIdField(); // 获取主键字段名

        // 检查是否仅允许当前用户修改
        if (isCurrentUserOnly()) {
            Object id = params.get(idField); // 获取尝试修改的实体的 ID
            T info = info((K) id); // 根据 ID 获取实体信息

            if (info == null) // 如果尝试修改的实体不存在，抛出安全异常
                throw new SecurityException("不能修改实体，id：" + id);
        }

        if (beforeUpdate != null)
            beforeUpdate.accept(params);

        return dao.update(tableName, params, idField);// 执行更新操作
    }

    /**
     * 更新数据库中的实体对象。
     * 本方法通过将传入的实体对象转换为Map格式，然后调用 update方法来更新数据库中对应的记录。
     *
     * @param bean 待更新的实体对象，它必须是一个非空的实例
     * @return 更新操作的结果，通常是一个表示操作是否成功的 Boolean 值
     */
    public Boolean updateBean(T bean) {
        return update(JsonUtil.pojo2map(bean));
    }
}