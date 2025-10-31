package com.ajaxjs.sqlman_v2;

import com.ajaxjs.sqlman.JdbcConnection;
import com.ajaxjs.sqlman.SmallMyBatis;
import com.ajaxjs.sqlman_v2.constant.DatabaseVendor;
import com.ajaxjs.sqlman_v2.crud.Create;
import com.ajaxjs.sqlman_v2.crud.Query;
import com.ajaxjs.sqlman_v2.crud.Update;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;

import javax.sql.DataSource;
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
}
