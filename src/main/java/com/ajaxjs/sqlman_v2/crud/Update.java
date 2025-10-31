package com.ajaxjs.sqlman_v2.crud;

import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.util.PrintRealSql;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.meta.DbMetaInfoCreate;
import com.ajaxjs.sqlman_v2.sqlgenerator.Entity2WriteSql;
import com.ajaxjs.sqlman_v2.util.PrettyLogger;
import com.ajaxjs.util.BoxLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Update operation and Delete operation.
 */
@Slf4j
public class Update extends BaseAction {
    /**
     * Do the update operation by an action.
     *
     * @param action an action object with input Sql, data and config.
     */
    public Update(Action action) {
        super(action);
    }

    /**
     * Update by any SQL.
     * This is the low-level API.
     *
     * @return The result object, contains effected rows.
     */
    public UpdateResult update() {
        startTime = System.currentTimeMillis();
        String resultText = null;

        try (PreparedStatement ps = action.getConn().prepareStatement(action.getSql())) {
            setParam2Ps(ps);

            int effectedRows = ps.executeUpdate();
            UpdateResult result = new UpdateResult();
            result.setOk(effectedRows > 0);
            result.setEffectedRows(effectedRows);
            resultText = result.toString();

            return result;
        } catch (SQLException e) {
            log.warn("SQL update error.", e);
            throw new RuntimeException("SQL update error.", e);
        } finally {
            String _resultText = resultText;
            String traceId = MDC.get(BoxLogger.TRACE_KEY);
            String bizAction = MDC.get(BoxLogger.BIZ_ACTION);

            CompletableFuture.runAsync(() -> PrettyLogger.printLog("Update", traceId, bizAction,
                    action.getSql(), action.getParams(),
                    PrintRealSql.printRealSql(action.getSql(), action.getParams()), this, _resultText, true));
        }
    }

    /**
     * Execute the update with ID specified row.
     * There is already ID in the entity, so we can take it out.
     *
     * @param idField Actually, the field is already ID in the entity, just tell me.
     * @return The result object, contains effected rows.
     */
    public UpdateResult withId(String idField) {
        return withId(idField, null);
    }

    /**
     * Execute the update with ID specified row.
     *
     * @param idField Which row to update, we need the name of that field.
     * @param idValue The value of ID.
     * @return The result object, contains effected rows.
     */
    public UpdateResult withId(String idField, Object idValue) {
        Entity2WriteSql generator = initEntity2WriteSql();

        if (idValue == null)
            generator.getUpdateSqlWithId(idField);
        else
            generator.getUpdateSqlWithId(idField, idValue);

        action.setSql(generator.getSql());
        action.setParams(generator.getParams());

        return update();
    }

    /**
     * Inner function for initializing Entity2WriteSql.
     *
     * @return Entity2WriteSql
     */
    private Entity2WriteSql initEntity2WriteSql() {
        Map<String, Object> entityMap = action.getEntityMap();
        Object entityBean = action.getEntityBean();
        String tableName = action.getTableName();
        Entity2WriteSql generator;

        if (entityMap != null)
            generator = new Entity2WriteSql(entityMap);
        else if (entityBean != null)
            generator = new Entity2WriteSql(entityBean);
        else
            throw new UnsupportedOperationException("Input `entityMap` or `entityBean` is null. This method is for Entity.");

        if (entityBean != null && tableName == null)
            tableName = new DbMetaInfoCreate<>(entityBean).getTableNameByAnnotation();

        generator.setTableName(tableName);

        return generator;
    }

    /**
     * Execute the update with where clause.
     *
     * @param where The where clause
     * @return The result object, contains effected rows.
     */
    public UpdateResult execute(String where) {
        Entity2WriteSql generator = initEntity2WriteSql();
        generator.getUpdateSql(where);

        action.setSql(generator.getSql());
        action.setParams(generator.getParams());

        return update();
    }

    /**
     * Execute the update.
     * This method is executed only for plain SQL input, not entity.
     *
     * @return The result object, contains effected rows.
     */
    public UpdateResult execute() {
        if (action.getEntityBean() != null | action.getEntityMap() != null)
            throw new UnsupportedOperationException("This method only for plain SQL, not entity.");

        return update();
    }

    /**
     * Physical delete by id field and id value.
     *
     * @param tableName Which table?
     * @param idField   The name of the field
     * @param id        The value of id field
     * @return The result object, contains effected rows.
     */
    public UpdateResult delete(String tableName, String idField, Serializable id) {
        String sql = "DELETE FROM " + tableName + " WHERE " + idField + " = ?";
        action.setSql(sql);
        action.setParams(id);

        return update();
    }

    /**
     * Physical delete by where clause.
     *
     * @param tableName Which table?
     * @param where     The where clause
     * @return The result object, contains effected rows.
     */
    public UpdateResult delete(String tableName, String where) {
        String sql = "DELETE FROM " + tableName + " WHERE " + where;
        action.setSql(sql);

        return update();
    }
}
