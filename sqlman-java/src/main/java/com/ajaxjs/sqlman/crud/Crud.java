package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.sql.Sql;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class Crud<T, K extends Serializable> extends Sql implements ICrud<T, K> {
    /**
     * 实体在数据库的建模信息
     */
    private TableModel tableModel;

    /**
     * 命名空间，标识
     */
    private String namespace;

    /**
     * 查询列表的时候，是否自动加上按照日期排序
     */
    private boolean listOrderByDate = true;

    /**
     * 实体类引用名称
     */
    private String clzName;

    private T beanClz;

    /**
     * 是否加入租户数据隔离
     */
    private boolean isTenantIsolation;

    /**
     * 当前用户的约束
     */
    private boolean isCurrentUserOnly;

    public final static String DUMMY_STR = "1=1";

    private final static String SELECT_SQL = "SELECT * FROM %s WHERE " + DUMMY_STR;

    @Override
    public T info(K id) {
        String sql = getSql();// 尝试获取已经定义好的 SQL 语句

        // 如果已经定义了 SQL 语句且不为空，则处理查询参数的动态替换
        if (StringUtils.hasText(sql)) {
            Map<String, Object> queryStringParams = DataServiceUtils.getQueryStringParams();// 获取查询字符串中的参数
            setKeyParams(queryStringParams);
        } else {
            // 如果没有预定义SQL，则根据表名和ID字段生成一个默认的查询 SQL
            sql = String.format(SELECT_SQL, getTableName()).replace(DUMMY_STR, DUMMY_STR + " AND " + getTableModel().getIdField() + " = ?");
            setParams(new Object[]{id});
        }

        sql = limitToCurrentUser(sql);// 限制查询结果只包含当前用户的数据

        if (isTenantIsolation())// 根据是否启用了租户隔离，动态添加租户ID查询条件
            sql = TenantService.addTenantIdQuery(sql);

        setSql(sql);

        return query(beanClz);
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
}
