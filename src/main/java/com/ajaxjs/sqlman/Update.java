package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.entity.BaseWriteSql;
import com.ajaxjs.sqlman.entity.BeanWriteSql;
import com.ajaxjs.sqlman.entity.MapWriteSql;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.model.tablemodel.IdField;
import com.ajaxjs.sqlman.util.PrintRealSql;
import com.ajaxjs.util.log.Trace;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

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
            result.setOk(true); // when CREATE TABLE executing, it'll return rows = 0, so `effectedRows > 0` is not enough
            result.setEffectedRows(effectedRows);
            resultText = result.toString();

            return result;
        } catch (SQLException e) {
            log.warn("SQL update error.", e);
            throw new RuntimeException("SQL update error.", e);
        } finally {
            try { // avoid this exception to effect main job
                String duration = (System.currentTimeMillis() - startTime) + "ms";
                String _resultText = resultText;
                String traceId = MDC.get(Trace.TRACE_KEY);
                String bizAction = MDC.get(Trace.BIZ_ACTION);

                PrintRealSql.printLog("Update", traceId, bizAction,
                        action.getSql(), action.getParams(),
                        PrintRealSql.printRealSql(action.getSql(), action.getParams()), duration, _resultText);
            } catch (Exception e) {
                log.warn("There's error when logging.", e);
            }
        }
    }

    /**
     * Execute the update with ID specified row.
     * The Default ID field is "id".
     * There is already ID in the entity, so we can take it out.
     *
     * @return The result object, contains effected rows.
     */
    public UpdateResult withId() {
        return withId("id", null);
    }

    /**
     * Execute the update with ID specified row.
     * There is already ID in the entity, so we can take it out.
     *
     * @param idField Actually, the field is already ID in the entity, tell me.
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
        BaseWriteSql generator = initWriteSql();

        if (idValue == null)
            generator.getUpdateSqlWithId(idField);
        else
            generator.getUpdateSqlWithId(idField, idValue);

        action.setSql(generator.getSql());
        action.setParams(generator.getParams());

        return update();
    }

    /**
     * Initializes a write SQL generator for the action's Map or Bean entity.
     *
     * @return the initialized write SQL generator
     */
    private BaseWriteSql initWriteSql() {
        Map<String, Object> entityMap = action.getEntityMap();
        Object entityBean = action.getEntityBean();
        String tableName = action.getTableName();

        if (entityMap != null)
            return new MapWriteSql(tableName, entityMap);
        else if (entityBean != null)
            return tableName == null ? new BeanWriteSql(entityBean) : new BeanWriteSql(tableName, entityBean);

        throw new UnsupportedOperationException("Input `entityMap` or `entityBean` is null. This method is for Entity.");
    }

    /**
     * Execute the update with where clause.
     *
     * @param where The where clause
     * @return The result object, contains effected rows.
     */
    public UpdateResult execute(String where) {
        BaseWriteSql generator = initWriteSql();
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
        if (action.getEntityBean() != null || action.getEntityMap() != null)
            throw new UnsupportedOperationException("This method only for plain SQL, not entity.");

        return update();
    }

    /**
     * Physical delete on an object.
     * The name of the field is "id" by default.
     * The value of id field comes from the entity.
     *
     * @return The result object, contains effected rows.
     */
    public UpdateResult delete() {
        return delete(IdField.ID_FIELD, null);
    }

    /**
     * Physical delete on an object. The name of the field is "id" by default.
     * This is a physical deletion.
     * If you want to delete a logical deletion, please use the update method.
     *
     * @param idValue The value of id field
     * @return The result object, contains effected rows.
     */
    public UpdateResult delete(Object idValue) {
        return delete(IdField.ID_FIELD, idValue);
    }

    /**
     * Physical delete on an object.
     * This is a physical deletion.
     * If you want to delete a logical deletion, please use the update method.
     *
     * @param idField The name of the field
     * @param idValue The value of id field
     * @return The result object, contains effected rows.
     */
    public UpdateResult delete(String idField, Object idValue) {
        BaseWriteSql generator = initWriteSql();
        generator.getDeleteSql(idField, idValue);

        action.setSql(generator.getSql());
        action.setParams(generator.getParams());

        return update();
    }

    /**
     * Physical delete by id field and id value.
     * This is a physical deletion.
     * If you want to delete a logical deletion, please use the update method.
     *
     * @param tableName Which table?
     * @param idField   The name of the field
     * @param idValue   The value of id field
     * @return The result object, contains effected rows.
     */
    public UpdateResult delete(String tableName, String idField, Serializable idValue) {
        String sql = "DELETE FROM " + tableName + " WHERE " + idField + " = ?";
        action.setSql(sql);
        action.setParams(idValue);

        return update();
    }

    /**
     * Physical delete by where clause.
     *
     * @param tableName Which table?
     * @param where     The where clause
     * @return The result object, contains effected rows.
     */
    public UpdateResult deleteWhere(String tableName, String where) {
        String sql = "DELETE FROM " + tableName + " WHERE " + where;
        action.setSql(sql);

        return update();
    }
}
