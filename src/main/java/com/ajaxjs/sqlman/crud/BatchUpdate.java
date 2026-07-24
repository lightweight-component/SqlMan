/**
 * Copyright (C) 2025 Frank
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.JdbcConnection;
import com.ajaxjs.sqlman.model.NullValue;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.model.tablemodel.TableModel;
import com.ajaxjs.sqlman.sqlgenerator.Entity2WriteSql;
import com.ajaxjs.util.JsonUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 批量更新
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class BatchUpdate extends TableModel {
    /**
     * 批量插入数据
     * <p>
     * 此兼容接口接收的是完整 SQL 值片段，无法使用参数绑定。新代码应优先使用
     * {@link #createBatchMap(Object, String)} 或 {@link #createBatch(Object)}。
     *
     * @param fields 数据库表的字段列表，多个字段用逗号分隔，例如："id,name,age"
     * @param values 批量插入的数据列表，每个元素代表一条记录，格式为"('value1', 'value2', 'value3', ... , 'valueN')"
     */
    @Deprecated
    public void createBatch(String fields, List<String> values) {
        requireNotEmpty(values, "values 不能为空");
        for (String value : values)
            requireText(value, "values 不能包含空值");

        log.info("批量插入 {} 条数据", values.size());
        createBatch(fields, String.join(",", values));
    }

    /**
     * 批量插入数据
     * <a href="https://blog.csdn.net/C3245073527/article/details/122071045">参考链接</a>
     *
     * @param fields 数据库表的字段列表，多个字段用逗号分隔，例如："id,name,age"
     * @param values 批量插入的数据，格式为"('value1', 'value2', 'value3', ... , 'valueN')"，每个元素代表一条记录
     */
    @Deprecated
    public void createBatch(String fields, String values) {
        requireText(getTableName(), "tableName 不能为空");
        requireText(fields, "fields 不能为空");
        requireText(values, "values 不能为空");

        long start = System.currentTimeMillis();
        Connection conn = JdbcConnection.getConnection();
        String sql;

        try {
            List<String> fieldList = new ArrayList<>();
            for (String field : fields.split(",", -1))
                fieldList.add(field.trim());

            sql = "INSERT INTO " + quoteIdentifier(conn, getTableName()) + " ("
                    + quoteIdentifiers(conn, fieldList) + ") VALUES " + values;
        } catch (SQLException e) {
            throw new RuntimeException("生成批量插入 SQL 失败", e);
        }

        int effectedRows;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            effectedRows = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("批量插入失败", e);
        }

        log.info("批量插入完成，影响 {} 行，耗时 {}ms", effectedRows, System.currentTimeMillis() - start);
    }

    /**
     * 批量插入
     *
     * @param entities  Map 列表或 Map 数组
     * @param tableName 表名
     */
    @SuppressWarnings("unchecked")
    public void createBatchMap(Object entities, String tableName) {
        requireText(tableName, "tableName 不能为空");
        List<Map<String, Object>> rows = new ArrayList<>();

        if (entities instanceof List) {
            for (Object entity : (List<?>) entities) {
                if (!(entity instanceof Map))
                    throw new IllegalArgumentException("entities 只能包含 Map");

                rows.add((Map<String, Object>) entity);
            }
        } else if (entities instanceof Map[])
            rows.addAll(Arrays.asList((Map<String, Object>[]) entities));
        else
            throw new IllegalArgumentException("entities 必须是 Map 列表或 Map 数组");

        insertRows(tableName, rows, true);
    }

    /**
     * 批量插入
     *
     * @param entities Bean 列表或 Bean 数组
     */
    public void createBatch(Object entities) {
        requireText(getTableName(), "tableName 不能为空");
        List<?> entitiesList;

        if (entities instanceof List)
            entitiesList = (List<?>) entities;
        else if (entities instanceof Object[])
            entitiesList = Arrays.asList((Object[]) entities);
        else
            throw new IllegalArgumentException("entities 必须是 Bean 列表或 Bean 数组");

        requireNotEmpty(entitiesList, "entities 不能为空");
        List<Map<String, Object>> rows = new ArrayList<>(entitiesList.size());

        for (Object entity : entitiesList) {
            if (entity == null)
                throw new IllegalArgumentException("entities 不能包含 null");

            Map<String, Object> row = new LinkedHashMap<>();
            Entity2WriteSql.everyBeanField(entity, true, row::put);
            rows.add(row);
        }

        // 延续旧行为：以第一条 Bean 的非 null 字段决定 INSERT 列；后续相同字段可为 null。
        Set<String> selectedFields = new LinkedHashSet<>();
        rows.get(0).forEach((field, value) -> {
            if (value != null)
                selectedFields.add(field);
        });

        if (selectedFields.isEmpty())
            throw new IllegalArgumentException("第一条 Bean 没有可插入的非 null 字段");

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getValue() != null && !selectedFields.contains(entry.getKey()))
                    throw new IllegalArgumentException("第 " + (i + 1) + " 条 Bean 的非 null 字段与第一条不一致: " + entry.getKey());
            }

            row.keySet().retainAll(selectedFields);
        }

        insertRows(getTableName(), rows, false);
    }

    /**
     * 校验每一行的字段并执行参数化批量插入。
     */
    private void insertRows(String tableName, List<Map<String, Object>> rows, boolean requireSameFields) {
        requireNotEmpty(rows, "entities 不能为空");

        Map<String, Object> first = rows.get(0);
        if (first == null || first.isEmpty())
            throw new IllegalArgumentException("第一条记录没有可插入字段");

        List<String> fields = new ArrayList<>(first.keySet());
        Set<String> expectedFields = new LinkedHashSet<>(fields);

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            if (row == null)
                throw new IllegalArgumentException("第 " + (i + 1) + " 条记录为 null");

            if (requireSameFields && !expectedFields.equals(row.keySet()))
                throw new IllegalArgumentException("第 " + (i + 1) + " 条记录的字段与第一条不一致");
        }

        Connection conn = JdbcConnection.getConnection();
        String sql;

        try {
            sql = buildInsertSql(conn, tableName, fields);
        } catch (SQLException e) {
            throw new RuntimeException("生成批量插入 SQL 失败", e);
        }

        int[] result = executeBatch(conn, sql, rows, fields);
        log.info("批量插入完成，共 {} 条。{}", rows.size(), Arrays.toString(result));
    }

    private static String buildInsertSql(Connection conn, String tableName, List<String> fields) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(quoteIdentifier(conn, tableName)).append(" (")
                .append(quoteIdentifiers(conn, fields)).append(") VALUES (");
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0)
                sql.append(", ");
            sql.append("?");
        }

        return sql.append(")").toString();
    }

    private static String quoteIdentifiers(Connection conn, List<String> identifiers) throws SQLException {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < identifiers.size(); i++) {
            if (i > 0)
                result.append(", ");
            result.append(quoteIdentifier(conn, identifiers.get(i)));
        }

        return result.toString();
    }

    private static String quoteIdentifier(Connection conn, String identifier) throws SQLException {
        requireText(identifier, "数据库标识符不能为空");
        java.sql.DatabaseMetaData metadata = conn.getMetaData();
        String quote = metadata.getIdentifierQuoteString();
        if (quote == null || quote.trim().isEmpty())
            quote = "";
        else
            quote = quote.trim();

        String[] parts = identifier.split("\\.", -1);
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            requireText(part, "数据库标识符不能为空");
            if (result.length() > 0)
                result.append('.');

            if (metadata.storesUpperCaseIdentifiers())
                part = part.toUpperCase(Locale.ROOT);
            else if (metadata.storesLowerCaseIdentifiers())
                part = part.toLowerCase(Locale.ROOT);

            result.append(quote)
                    .append(quote.isEmpty() ? part : part.replace(quote, quote + quote))
                    .append(quote);
        }

        return result.toString();
    }

    private static int[] executeBatch(Connection conn, String sql, List<Map<String, Object>> rows, List<String> fields) {
        final boolean localTransaction;

        try {
            localTransaction = conn.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException("无法读取数据库连接的事务状态", e);
        }

        SQLException failure = null;
        int[] result = null;

        try {
            if (localTransaction)
                conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Map<String, Object> row : rows) {
                    for (int i = 0; i < fields.size(); i++)
                        setParameter(ps, i + 1, row.get(fields.get(i)));

                    ps.addBatch();
                }

                result = ps.executeBatch();
            }

            if (localTransaction)
                conn.commit();
        } catch (SQLException e) {
            failure = e;

            if (localTransaction) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }

        } finally {
            if (localTransaction) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    if (failure == null)
                        failure = e;
                    else
                        failure.addSuppressed(e);
                }
            }
        }

        if (failure != null)
            throw new RuntimeException("批量插入失败", failure);

        return result;
    }

    private static void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        if (NullValue.NULL_DATE.equals(value) || NullValue.NULL_INT.equals(value)
                || NullValue.NULL_LONG.equals(value) || NullValue.NULL_STRING.equals(value))
            value = null;
        else if (value instanceof Map || value instanceof List)
            value = JsonUtil.toJson(value);
        else if (value instanceof Enum)
            value = value.toString();

        if (value instanceof byte[])
            ps.setBytes(index, (byte[]) value);
        else if (value instanceof InputStream)
            ps.setBinaryStream(index, (InputStream) value);
        else
            ps.setObject(index, value);
    }

    /**
     * 物理批量删除
     *
     * @param ids 实体 ID 列表
     * @return 是否成功
     */
    public UpdateResult deleteBatch(List<? extends Serializable> ids) {
        requireNotEmpty(ids, "ids 不能为空");
        if (ids.contains(null))
            throw new IllegalArgumentException("ids 不能包含 null");
        requireText(getTableName(), "tableName 不能为空");
        requireText(getIdField(), "idField 不能为空");

        Connection conn = JdbcConnection.getConnection();
        String tableName;
        String idField;

        try {
            tableName = quoteIdentifier(conn, getTableName());
            idField = quoteIdentifier(conn, getIdField());
        } catch (SQLException e) {
            throw new RuntimeException("生成批量删除 SQL 失败", e);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(tableName).append(" WHERE ").append(idField).append(" IN (");

        List<String> valueHolders = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        ids.forEach(id -> {
            valueHolders.add("?");
            params.add(id);
        });

        sb.append(String.join(",", valueHolders));
        sb.append(")");

        return new Action(conn, sb.toString()).update(params.toArray()).execute();
    }

    private static void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(message);
    }

    private static void requireNotEmpty(Collection<?> values, String message) {
        if (values == null || values.isEmpty())
            throw new IllegalArgumentException(message);
    }
}
