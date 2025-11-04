package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.model.DatabaseVendor;
import com.ajaxjs.sqlman.crud.Create;
import com.ajaxjs.sqlman.crud.Query;
import com.ajaxjs.sqlman.crud.Update;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Map;

/**
 * The gateway for database actions.
 * A value object with input Sql, data and config.
 */
@Data
public class Action {
    /**
     * The connection to be used to execute the sql
     */
    Connection conn;

    /**
     * Create a new action.
     *
     * @param dataSource The data source to be used to obtain the connection.
     */
    public Action(DataSource dataSource) {
        this(JdbcConnection.getConnection(dataSource));
    }

    public Action(Connection conn) {
        this.conn = conn;
    }

    public Action(Map<String, Object> entity, String tableName) {
        this(JdbcConnection.getConnection(), entity, tableName);
    }

    public Action(Connection conn, Map<String, Object> entity, String tableName) {
        this(conn);
        entityMap = entity;
        this.tableName = tableName;
    }

    public Action(Object entity, String tableName) {
        this(JdbcConnection.getConnection(), entity, tableName);
    }

    public Action(Connection conn, Object entity, String tableName) {
        this(conn);
        entityBean = entity;
        this.tableName = tableName;
    }

    public Action(Object entity) {
        this(JdbcConnection.getConnection(), entity);
    }

    public Action(Connection conn, Object entity) {
        this(conn);
        entityBean = entity;
    }

    /**
     * Create a new action.
     * Default way to initialize the connection. Obtained a connection from local thread.
     *
     * @param sql The input sql.
     */
    public Action(String sql) {
        this(JdbcConnection.getConnection(), sql);
    }

    public Action(Connection conn, String sql) {
        this.conn = conn;
        this.sql = sql;
    }

    /**
     * The sql to be executed
     */
    String sql;

    /**
     * The parameters to be bound to the sql
     */
    Object[] params;

    @SuppressWarnings("unchecked")
    public Action setParams(Object... params) {
        if (!ObjectHelper.isEmpty(params)) {
            if (params[0] instanceof Map) {
//                sql = SmallMyBatis.getValuedSQL(sql, (Map<String, Object>) params[0]);
                sql = SmallMyBatis.handleSql(sql, (Map<String, Object>) params[0]);// high-cost processing
                params = Arrays.copyOfRange(params, 1, params.length);
            }

            this.params = params;
        }

        return this;
    }

    /**
     * The table name of the entity
     */
    String tableName;

    Map<String, Object> entityMap;

    Object entityBean;

    /**
     * The vendor of the database that using, default is MYSQL.
     */
    DatabaseVendor databaseVendor = DatabaseVendor.MYSQL;

    /**
     * Initialize a query action.
     *
     * @param params The parameters to be bound to the sql
     * @return The query action.
     */
    public Query query(Object... params) {
        setParams(params);

        return new Query(this);
    }

    /**
     * Initialize a create action.
     *
     * @param params The parameters to be bound to the sql
     * @return The creation action.
     */
    public Create create(Object... params) {
        setParams(params);

        return new Create(this);
    }

    /**
     * Initialize an update action.
     *
     * @param params The parameters to be bound to the sql
     * @return The update action.
     */
    public Update update(Object... params) {
        setParams(params);

        return new Update(this);
    }

    /**
     * Execute delete operation.
     * The only argument needed for delete operation is the `connection` when constructing the action,
     * and then is: tableName, idField and id.
     * This is a physical deletion.
     * If you want to delete a logical deletion, please use the update method.
     *
     * @param tableName Which table?
     * @param idField   The name of the field
     * @param id        The value of id field
     * @return The result object, contains effected rows.
     */
    public UpdateResult delete(String tableName, String idField, Serializable id) {
        return new Update(this).delete(tableName, idField, id);
    }

    /**
     * Execute delete operation.
     * The only argument needed for delete operation is the `connection` when constructing the action,
     * and then is: tableName, where clause.
     * This is a physical deletion.
     * If you want to delete a logical deletion, please use the update method.
     *
     * @param tableName Which table?
     * @param where     The where clause
     * @return The result object, contains effected rows.
     */
    public UpdateResult delete(String tableName, String where) {
        return new Update(this).delete(tableName, where);
    }
}
