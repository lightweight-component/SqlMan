package com.ajaxjs.sqlman.entity;

import com.ajaxjs.sqlman.model.NullValue;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Base class for generating SQL statements used in write operations.
 * <p>
 * Implementations provide entity-specific field traversal, while this class
 * builds INSERT, UPDATE, and DELETE statements and their corresponding parameters.
 * </p>
 */
@Slf4j
public abstract class BaseWriteSql {
    /**
     * The generated SQL statement.
     */
    @Getter
    @Setter
    String sql;

    /**
     * The parameter values bound to the generated SQL statement.
     */
    @Getter
    @Setter
    Object[] params;

    /**
     * The target database table.
     */
    @Getter
    String tableName;

    public void setTableName(String tableName) {
        this.tableName = SqlIdentifier.checkTableName(tableName);
    }

    /**
     * Appends writable fields and parameter placeholders for an INSERT statement.
     *
     * @param sb           the SQL builder
     * @param values       the collected parameter values
     * @param valuesHolder the collected value placeholders
     */
    abstract void everyFieldInsert(StringBuilder sb, List<Object> values, List<String> valuesHolder);

    /**
     * Generates an INSERT statement and its parameter values.
     *
     * @throws IllegalArgumentException if no writable values are available
     */
    public void getInsertSql() {
        StringBuilder sb = new StringBuilder();
        List<Object> values = new ArrayList<>();
        List<String> valuesHolder = new ArrayList<>();
        sb.append("INSERT INTO ").append(tableName).append(" (");

        everyFieldInsert(sb, values, valuesHolder);

        if (values.isEmpty())
            throw new IllegalArgumentException("Cannot generate INSERT for table " + tableName + ": no writable values.");

        sb.deleteCharAt(sb.length() - 2);// 删除最后一个 ,
        sb.append(") VALUES (").append(String.join(", ", valuesHolder)).append(")");

        sql = sb.toString();
        params = values.toArray();
    }

    /**
     * Appends writable fields and parameter values for an UPDATE statement.
     *
     * @param sb      the SQL builder
     * @param values  the collected parameter values
     * @param idField the identifier field to exclude from the SET clause
     */
    abstract void everyFieldUpdate(StringBuilder sb, List<Object> values, String idField);

    /**
     * Generates an UPDATE statement.
     * <p>
     * When {@code isUpdateAllRow} is {@code true}, no WHERE clause is added and
     * the generated statement may affect all rows in the target table.
     * </p>
     *
     * @param isUpdateAllRow whether an update affecting all rows is explicitly allowed
     * @param idField        the identifier field to exclude from the SET clause
     * @throws IllegalArgumentException if no writable values are available
     */
    protected void getUpdateSql(boolean isUpdateAllRow, String idField) {
        if (idField != null)
            idField = SqlIdentifier.check(idField);

        if (isUpdateAllRow)
            log.warn("You're going to update ALL rows on the table {}, which is SO dangerous! All records will be effected!", tableName);

        StringBuilder sb = new StringBuilder();
        List<Object> values = new ArrayList<>();
        sb.append("UPDATE ").append(tableName).append(" SET");

        everyFieldUpdate(sb, values, idField);

        if (values.isEmpty())
            throw new IllegalArgumentException("Cannot generate UPDATE for table " + tableName + ": no writable values.");

        sb.deleteCharAt(sb.length() - 1);// 删除最后一个 ,

        sql = sb.toString();
        params = values.toArray();
    }

    /**
     * Generates an UPDATE statement for a row identified by the specified field and value.
     *
     * @param idField the identifier field name
     * @param idValue the identifier value
     */
    public void getUpdateSqlWithId(String idField, Object idValue) {
        idField = SqlIdentifier.check(idField);

        if (ObjectHelper.isEmptyText(idField) && idValue == null) {
            log.warn("You're going to update ALL rows on the table {}, which is SO dangerous! All records will be effected!", tableName);
            return;
        }

        getUpdateSql(false, idField);
        sql += " WHERE " + idField + " = ?";

        params = Arrays.copyOf(params, params.length + 1);
        params[params.length - 1] = idValue; // 将新值加入数组末尾
    }

    /**
     * Generates an UPDATE statement for a row identified by a value obtained from the entity.
     *
     * @param idField the identifier field name whose value should be read from the entity
     * @throws NullPointerException if the identifier value cannot be obtained from the entity
     */
    public void getUpdateSqlWithId(String idField) {
        idField = SqlIdentifier.check(idField);
        Object idValue = getIdValueByIdField(idField);

        if (idValue == null)
            throw new NullPointerException("No identifier value was provided or found in the entity.");

        getUpdateSqlWithId(idField, idValue);
    }

    /**
     * Generates an UPDATE statement with the specified WHERE predicate.
     *
     * @param where the WHERE predicate without the {@code WHERE} keyword
     */
    public void getUpdateSql(String where) {
        if (ObjectHelper.isEmptyText(where)) {
            log.warn("You're going to update ALL rows on the table {}, which is SO dangerous! All records will be effected!", tableName);
            return;
        }

        getUpdateSql(false, null);
        sql += " WHERE " + where;
    }

    /**
     * Generates a DELETE statement for a row identified by the specified field and value.
     * <p>
     * If {@code idValue} is {@code null}, the identifier value is read from the entity.
     * </p>
     *
     * @param idField the identifier field name
     * @param idValue the identifier value, or {@code null} to obtain it from the entity
     * @throws NullPointerException if no identifier value is provided or available from the entity
     */
    public void getDeleteSql(String idField, Object idValue) {
        idField = SqlIdentifier.check(idField);

        if (idValue == null)
            idValue = getIdValueByIdField(idField);

        if (idValue == null)
            throw new NullPointerException("No identifier value was provided or found in the entity.");

        sql = "DELETE FROM " + tableName + " WHERE " + idField + " = ?";
        params = new Object[]{idValue};
    }

    /**
     * Returns the value of the specified identifier field from the entity.
     *
     * @param idField the identifier field name
     * @return the identifier value, or {@code null} if it is not available
     */
    abstract Object getIdValueByIdField(String idField);

    /**
     * Converts a Java value to a value suitable for use as a SQL parameter.
     * <p>
     * Enum values are converted to strings, supported {@link NullValue} markers
     * are converted to {@code null}, and {@link List} and {@link Map} values are
     * serialized as JSON.
     * </p>
     *
     * @param value the Java value
     * @return the converted SQL parameter value
     */
    static Object toSqlValue(Object value) {
        if (value instanceof Enum) // 枚举类型，取其字符串保存
            return value.toString();
        else if (NullValue.NULL_DATE.equals(value) || NullValue.NULL_INT.equals(value)
                || NullValue.NULL_LONG.equals(value) || NullValue.NULL_STRING.equals(value)) // 如何设数据库 null 值
            return null;
        else if (value instanceof List || value instanceof Map)
            return JsonUtil.toJson(value);// 假設數據庫是 text，於是一律轉換 json
        else
            return value;
    }
}
