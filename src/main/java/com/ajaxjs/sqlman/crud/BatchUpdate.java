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

import com.ajaxjs.sqlman.crud.model.TableModel;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.JdbcCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 批量更新
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class BatchUpdate extends TableModel {
    private JdbcCommand crud;

    /**
     * 批量插入数据
     *
     * @param fields 数据库表的字段列表，多个字段用逗号分隔，例如："id,name,age"
     * @param values 批量插入的数据列表，每个元素代表一条记录，格式为"('value1', 'value2', 'value3', ... , 'valueN')"
     */
    public void createBatch(String fields, List<String> values) {
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
    public void createBatch(String fields, String values) {
        long start = System.currentTimeMillis();
        String sql = "INSERT INTO " + getTableName() + " (" + fields + ") VALUE " + values;
        log.info(sql);

        int[] result = null;
        Connection conn = getCrud().getConn();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);// 取消自动提交

            ps.addBatch();
            result = ps.executeBatch();
            ps.clearBatch();

            conn.commit();// 所有语句都执行完毕后才手动提交sql语句
        } catch (Throwable e) {
            try {
                conn.rollback();// 回滚事务
            } catch (SQLException ex) {
                log.warn("WARN>>>", ex);
            }

            log.warn("WARN>>>", e);
        }

        log.info("result>>{}", Arrays.toString(result));
        log.info("批量插入完毕 {}ms", System.currentTimeMillis() - start);
    }

    /**
     * 批量插入
     *
     * @param entities  Map 列表或 Map 数组
     * @param tableName 表名
     */
    @SuppressWarnings("unchecked")
    public void createBatchMap(Object entities, String tableName) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object>[] arr;

        if (entities instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) entities;
            arr = new Map[list.size()];

            for (int i = 0; i < list.size(); i++)
                arr[i] = list.get(i);

        } else if (entities instanceof Map[])  // Arrays
            arr = (Map<String, Object>[]) entities;
        else
            throw new IllegalArgumentException("不支持参数");

        Map<String, Object> firstEntity = arr[0];
        sb.append("INSERT INTO ").append(tableName).append(" (");
        BeanWriter.everyMapField(firstEntity, (field, value) -> sb.append(" `").append(field).append("`,"));
        sb.deleteCharAt(sb.length() - 1);// 删除最后一个
        sb.append(") VALUES");

        for (Map<String, Object> entity : arr) {
            sb.append(" (");
            BeanWriter.everyMapField(entity, (field, value) -> sb.append(toSqlValue(value)).append(", "));
            sb.deleteCharAt(sb.length() - 1);// 删除最后一个
            sb.deleteCharAt(sb.length() - 1);// 删除最后一个
            sb.append("),");
        }

        insertBatch(sb);
    }

    /**
     * 批量插入
     *
     * @param entities Bean 列表或 Bean 数组
     */
    public void createBatch(Object entities) {
        StringBuilder sb = new StringBuilder();
        Object[] arr;

        if (entities instanceof List) {
            List<?> list = (List<?>) entities;
            arr = list.toArray();
        } else if (entities instanceof Object[])  // Arrays
            arr = (Object[]) entities;
        else
            throw new IllegalArgumentException("不支持参数");

        Object firstEntity = arr[0];
        sb.append("INSERT INTO ").append(getTableName()).append(" (");
        BeanWriter.everyBeanField(firstEntity, (field, value) -> sb.append(" `").append(field).append("`,"));
        sb.deleteCharAt(sb.length() - 1);// 删除最后一个
        sb.append(") VALUES");

        for (Object entity : arr) {
            sb.append(" (");
            BeanWriter.everyBeanField(entity, (field, value) -> sb.append(toSqlValue(value)).append(", "));
            sb.deleteCharAt(sb.length() - 1);// 删除最后一个
            sb.deleteCharAt(sb.length() - 1);// 删除最后一个
            sb.append("),");
        }

        insertBatch(sb);
    }

    /**
     * 执行批量插入操作
     * <p>
     * 该方法根据传入的 StringBuilder 对象构建 SQL 语句，并执行批量插入操作
     * 它首先移除 StringBuilder 中最后一个字符，通常是移除SQL语句中的多余逗号或类似字符
     * 然后，它将 StringBuilder 的内容转换为字符串并执行SQL批量插入操作
     *
     * @param sb StringBuilder 对象，用于构建 SQL 批量插入语句
     */
    private void insertBatch(StringBuilder sb) {
        sb.deleteCharAt(sb.length() - 1);// 删除最后一个

        String sql = sb.toString();
        log.info("批量插入：：{}", sql);
        int[] result;

        Connection conn = getCrud().getConn(); // TODO close this connection? above also
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.addBatch();
            result = ps.executeBatch();
            ps.clearBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.info("批量插入完成。{}", Arrays.toString(result));
    }

    /**
     * 物理批量删除
     *
     * @param ids 实体 ID 列表
     * @return 是否成功
     */
    public UpdateResult deleteBatch(List<? extends Serializable> ids) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(getTableName()).append(" WHERE ").append(getIdField()).append(" IN (");

        List<String> valueHolders = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        ids.forEach(id -> {
            valueHolders.add("?");
            params.add(id);
        });

        sb.append(String.join(",", valueHolders));
        sb.append(")");

        getCrud().setSql(sb.toString());
        getCrud().setParams(params.toArray());

        return getCrud().update();
    }

    /**
     * 转换为符合 SQL 的类型
     */
    static Object toSqlValue(Object value) {
        if (value instanceof String)
            return "'" + value + "'";
//        else if (value instanceof Boolean)
//            return ((Boolean) value) ? 1 : 0;
//        else if (value instanceof Date)
//            return "'" + DateHelper.formatDateTime((Date) value) + "'";
//        else if (value instanceof LocalDateTime)
//            return "'" + DateHelper.formatDateTime((LocalDateTime) value) + "'";

        return value.toString();
    }
}
