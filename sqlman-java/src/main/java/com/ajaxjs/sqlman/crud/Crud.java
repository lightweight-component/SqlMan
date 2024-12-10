package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.sql.Sql;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;

@EqualsAndHashCode(callSuper = true)
@Data
public class Crud extends Sql implements ICrud {
    /**
     * Create a JDBC action with global connection
     */
    public Crud() {
        super();
    }

    /**
     * Create a JDBC action with specified connection
     */
    public Crud(Connection conn) {
        super(conn);
    }

    /**
     * Create a JDBC action with specified data source
     */
    public Crud(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 实体在数据库的建模信息
     */
    private TableModel tableModel;

    public Crud setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
        return this;
    }

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
    public <T extends Serializable> Crud info(T id) {
        String sql = getSql();// 尝试获取已经定义好的 SQL 语句

        // 如果已经定义了 SQL 语句且不为空，则处理查询参数的动态替换
        if (!StringUtils.hasText(sql)) {
            // 如果没有预定义SQL，则根据表名和ID字段生成一个默认的查询 SQL
            sql = String.format(SELECT_SQL, tableModel.getTableName());
            sql = sql.replace(DUMMY_STR, DUMMY_STR + " AND " + tableModel.getIdField() + " = ?");
            setParams(new Object[]{id});
        }

//        sql = limitToCurrentUser(sql);// 限制查询结果只包含当前用户的数据
//
//        if (isTenantIsolation())// 根据是否启用了租户隔离，动态添加租户ID查询条件
//            sql = TenantService.addTenantIdQuery(sql);

        setSql(sql);
        return this;
    }

    public Crud list() {
        return list(null);
    }

    public Crud list(String where) {
        String sql = getSql();

        if (StringUtils.hasText(sql)) {
//            sql = SmallMyBatis.handleSql(sql, DataServiceUtils.getQueryStringParams());
        } else {
            if (isListOrderByDate()) {
                String createDateField = getTableModel().getCreateDateField();
                String tableName = tableModel.getTableName();

                sql = String.format(SELECT_SQL + " ORDER BY " + createDateField + " DESC", tableName);
            } else
                sql = String.format(SELECT_SQL, tableModel.getTableName());
        }

        if (getTableModel().isHasIsDeleted())
            sql = sql.replace(DUMMY_STR, DUMMY_STR + " AND " + getTableModel().getDelField() + " != 1");

//        sql = limitToCurrentUser(sql);
//
//        if (isTenantIsolation())
//            sql = TenantService.addTenantIdQuery(sql);

        if (where != null)
            sql = sql.replace(DUMMY_STR, DUMMY_STR + where);

        setSql(sql);
        return this;
    }
}
