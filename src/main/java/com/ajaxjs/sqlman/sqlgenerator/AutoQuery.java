package com.ajaxjs.sqlman.sqlgenerator;

import com.ajaxjs.sqlman.model.tablemodel.TableModel;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class AutoQuery {
    public final static String DUMMY_STR = "1=1";

    private final static String SELECT_SQL = "SELECT * FROM %s WHERE " + DUMMY_STR;
    private final static String SELECT_LIST_SQL = "SELECT %s.* FROM %s WHERE " + DUMMY_STR;

    final TableModel tableModel;

    final AutoQueryBusiness autoQueryBusiness;

    public String info() {
        String sql = String.format(SELECT_SQL, tableModel.getTableName());
        sql = sql.replace(DUMMY_STR, DUMMY_STR + " AND " + tableModel.getIdField() + " = ?");
        sql = filterDeleted(sql);
        sql = filterDeleted(sql);
        sql = limitToCurrentUser(sql);// 限制查询结果只包含当前用户的数据

        return sql;
    }

    public String list() {
        return list(null);
    }

    public String list(String where) {
        String sql;
        String tableName = tableModel.getTableName();

        if (autoQueryBusiness.isListOrderByDate()) {

            String createDateField = tableName + "." + getTableModel().getCreateDateField();

            sql = String.format(SELECT_LIST_SQL + " ORDER BY " + createDateField + " DESC", tableName, tableName);
        } else
            sql = String.format(SELECT_LIST_SQL, tableName, tableName);

        sql = filterDeleted(sql);
        sql = limitToCurrentUser(sql);
        sql = addTenantIdQuery(sql);

        if (where != null)
            sql = sql.replace(DUMMY_STR, DUMMY_STR + where);

        TableJoin tableJoin = autoQueryBusiness.getTableJoin();

        if (tableJoin != null) {
            sql = TableJoinModifier.addLeftJoinWithAutoAlias(sql, tableJoin);
        }

        return sql;
    }

    public String deletePhysicalById() {
        return deletePhysical(tableModel.getIdField() + " = ?");
    }

    public String deletePhysical(String where) {
        if (ObjectHelper.isEmptyText(where))
            throw new UnsupportedOperationException("Please add the arguments of where cause, otherwise all rows will be deleted!");

        return "DELETE FROM " + tableModel.getTableName() + " WHERE " + DUMMY_STR + " AND " + where;
    }

    public String deleteLogicalById() {
        return deleteLogical(tableModel.getIdField() + " = ?");
    }

    public String deleteLogical(String where) {
        String field = getTableModel().isHasIsDeleted() ? getTableModel().getDelField() : tableModel.getStateField();
        String sql = "UPDATE " + tableModel.getTableName() + " SET " + field + " = 1 WHERE " + DUMMY_STR + " AND " + where;
        sql = limitToCurrentUser(sql);

        return sql;
    }

    private String filterDeleted(String sql) {
        if (autoQueryBusiness.isFilterDeleted()) {
            String tableName = tableModel.getTableName() + ".";

            if (getTableModel().isHasIsDeleted())
                sql = sql.replace(DUMMY_STR, DUMMY_STR + " AND " + tableName + getTableModel().getDelField() + " != 1");
            else
                sql = sql.replace(DUMMY_STR, DUMMY_STR + " AND " + tableName + tableModel.getStateField() + " != 1");
        }

        return sql;
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
        if (autoQueryBusiness.isCurrentUserOnly()) { // 检查是否配置为只查询当前用户的数据
            String add = " AND user_id = " + autoQueryBusiness.getCurrentUserId(); // 构造添加的查询条件

            if (sql.contains(DUMMY_STR)) // 检查SQL语句中是否已包含占位符
                sql = sql.replace(DUMMY_STR, DUMMY_STR + add); // 将条件插入到占位符之后
            else
                sql += add; // 直接将条件追加到SQL语句末尾
        }

        return sql; // 返回修改后的SQL语句
    }

    /**
     * Add SQL filter of tenant
     *
     * @param sql SQL
     * @return SQL
     */
    public String addTenantIdQuery(String sql) {
        if (autoQueryBusiness.isTenantIsolation()) {
            Serializable tenantId = autoQueryBusiness.getTenantId();

            if (tenantId != null) {
                if (sql.contains(DUMMY_STR))
                    sql = sql.replace(DUMMY_STR, DUMMY_STR + " AND tenant_id = " + tenantId);
                else
                    sql += " AND　tenant_id = " + tenantId;
            }
        }

        return sql;
    }
}
