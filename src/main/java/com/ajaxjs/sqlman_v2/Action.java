package com.ajaxjs.sqlman_v2;

import com.ajaxjs.sqlman.JdbcConnection;
import com.ajaxjs.sqlman.SmallMyBatis;
import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman_v2.constant.DatabaseVendor;
import com.ajaxjs.sqlman_v2.crud.Create;
import com.ajaxjs.sqlman_v2.meta.DbMetaInfoCreate;
import com.ajaxjs.sqlman_v2.sqlgenerator.Entity2WriteSql;
import lombok.Data;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;

@Data
public class Action {
    /**
     * The connection to be used to execute the sql
     */
    Connection conn;

    public Action() {
    }

    public Action(Map<String, Object> entity, String tableName) {
        setConn();
        entityMap = entity;
        this.tableName = tableName;
    }

    public Action(Connection conn, Map<String, Object> entity, String tableName) {
        this.conn = conn;
        entityMap = entity;
        this.tableName = tableName;
    }

    public Action(Object entity, String tableName) {
        setConn();
        entityBean = entity;
        this.tableName = tableName;
    }

    public Action(Connection conn, Object entity, String tableName) {
        this.conn = conn;
        entityBean = entity;
        this.tableName = tableName;
    }

    public Action(Object entity) {
        setConn();
        entityBean = entity;
    }

    public Action(Connection conn, Object entity) {
        this.conn = conn;
        entityBean = entity;
    }

    String tableName;

    Map<String, Object> entityMap;

    Object entityBean;

    /**
     * The data source to be used to obtain the connection
     *
     * @param dataSource DataSource
     */
    public void setDataSource(DataSource dataSource) {
        conn = JdbcConnection.getConnection(dataSource);
    }

    /**
     * Default way to initialize the connection. Obtained a connection from local thread.
     */
    public void setConn() {
        conn = JdbcConnection.getConnection();
    }

    /**
     * The sql to be executed
     */
    String sql;

    public void setSql(String sql, Map<String, Object> keyParams) {
        this.sql = sql; // TODO 模板替换
    }

    public void setDynamicSql(String sql, Map<String, Object> keyParams) {
        this.sql = SmallMyBatis.handleSql(sql, keyParams);
    }

    /**
     * The parameters to be bound to the sql
     */
    Object[] params;

    /**
     * The vendor of the database that using, default is MYSQL.
     */
    DatabaseVendor databaseVendor = DatabaseVendor.MYSQL;

    public <T extends Serializable> CreateResult<T> create(boolean isAutoIns, Class<T> idType) {
        Entity2WriteSql generator;

        if (entityMap != null)
            generator = new Entity2WriteSql(entityMap);
        else if (entityBean != null)
            generator = new Entity2WriteSql(entityBean);
        else
            throw new UnsupportedOperationException("entityMap or entityBean is null.");

        if (entityBean != null && tableName == null)
            tableName = new DbMetaInfoCreate<T>(entityBean).getTableNameByAnnotation();

        generator.setTableName(tableName);
        generator.getInsertSql();

        sql = generator.getSql();
        params = generator.getParams();

        return new Create(this).create(isAutoIns, idType);
    }

    public UpdateResult update(String where) {
        sql += " WHERE " + where;

        return null;
    }
}
