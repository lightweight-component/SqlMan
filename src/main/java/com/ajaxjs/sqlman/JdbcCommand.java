package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.annotation.ResultSetProcessor;
import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.util.PrettyLogger;
import com.ajaxjs.sqlman.util.PrintRealSql;
import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * To execute basic JDBC commands, read and write data to database.
 * Usually we don't use this class directly, use class Sql instead.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class JdbcCommand extends JdbcConnection implements JdbcConstants {
    /**
     * SQL 语句，可以带有 ? 的占位符
     */
    private String sql;

    /**
     * 插入到 SQL 中的参数，可单个可多个可不填
     */
    private Object[] params;

    /**
     * 通过 key 替换 SQL 中的参数，可不填
     */
    private Map<String, Object> keyParams;

    /**
     * Create a JDBC action with global connection
     */
    public JdbcCommand() {
        super();
    }

    /**
     * Create a JDBC action with specified connection
     */
    public JdbcCommand(Connection conn) {
        super(conn);
    }

    /**
     * Create a JDBC action with specified data source
     */
    public JdbcCommand(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 执行查询
     *
     * @param <T>       结果的类型
     * @param processor 结果处理器
     * @return 查询结果，如果为 null 表示没有数据
     */
    protected <T> T query(ResultSetProcessor<T> processor) {
        sql = SmallMyBatis.handleSql(sql, keyParams);
        String resultText = null;

        try (PreparedStatement ps = getConn().prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            if (!ObjectHelper.isEmpty(params))
                setParam2Ps(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    T _result = processor.process(rs);
                    resultText = _result.toString();

                    return _result;
                } else {
                    resultText = "[Empty result]";
                    log.info("Queried SQL：{}, data not found.", sql);

                    return null;
                }
            }
        } catch (SQLException e) {
            log.warn(e.getMessage());
            throw new RuntimeException("SQL query error.", e);
        } finally {
            String _resultText = resultText;
            String traceId = MDC.get(BoxLogger.TRACE_KEY);
            String bizAction = MDC.get(BoxLogger.BIZ_ACTION);

            CompletableFuture.runAsync(() -> PrettyLogger.printLog("Query", traceId, bizAction, sql, params, PrintRealSql.printRealSql(sql, params), this, _resultText, true));
        }
    }

    public static final Long INSERT_OK_LONG = -1L;
    public static final Integer INSERT_OK_INT = -1;
    public static final String INSERT_OK_STR = "INSERT_OK";

    /**
     * 新建记录
     * 也可以作为执行任意 SQL 的方法，例如执行 CreateTable
     *
     * @param isAutoIns 是否自增 id
     * @param idType    id 字段类型，可以雪花 id（Long）、自增（Integer）、UUID（String）
     * @return 新增主键，为兼顾主键类型，返回的类型设为同时兼容 int/long/string 的 Serializable
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> CreateResult<T> create(boolean isAutoIns, Class<T> idType) {
//        if (keyParams != null)
        sql = SmallMyBatis.handleSql(sql, keyParams);
        String resultText = null;

        try (PreparedStatement ps = isAutoIns ? getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : getConn().prepareStatement(sql)) {
            setParam2Ps(ps, params);
            int effectRows = ps.executeUpdate();

            if (effectRows > 0) {// 插入成功
                CreateResult<T> result = new CreateResult<>();
                result.setOk(true);

                if (isAutoIns) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {// 当保存之后会自动获得数据库返回的主键
                        if (rs.next()) {
                            Object newlyId = rs.getObject(1);

                            if (newlyId instanceof BigInteger)
                                newlyId = ((BigInteger) newlyId).longValue();

                            if (idType != null)
                                result.setNewlyId((T) newlyId);

//                            if (idType.equals(Long.class))
//                                return (Long) newlyId;
//                            else if (idType.equals(Integer.class))
//                                return (Integer) newlyId;
//                            else if (idType.equals(String.class))
//                                return (String) newlyId;
                        }
                    }
                } else {
                    // 不是自增，但不能返回 null，返回 null 就表示没插入成功
                    if (idType != null) {
                        T v = null;

                        if (idType.equals(Long.class)) {
                            v = (T) INSERT_OK_LONG;
                        } else if (idType.equals(Integer.class))
                            v = (T) INSERT_OK_INT;
                        else if (idType.equals(String.class))
                            v = (T) INSERT_OK_STR;

                        result.setNewlyId(v);
                    }
                }

                resultText = result.toString();
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL insert error.", e);
        } finally {
            String _resultText = resultText;
            String traceId = MDC.get(BoxLogger.TRACE_KEY);
            String bizAction = MDC.get(BoxLogger.BIZ_ACTION);

            CompletableFuture.runAsync(() -> PrettyLogger.printLog("Create", traceId, bizAction, sql, params, PrintRealSql.printRealSql(sql, params), this, _resultText, true));
        }

        return null;
    }

    /**
     * 执行 SQL UPDATE 更新
     *
     * @return 成功修改的行数
     */
    public UpdateResult update() {
//        if (keyParams != null)
        sql = SmallMyBatis.handleSql(sql, keyParams);
        String resultText = null;

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            setParam2Ps(ps, params);

            int effectedRows = ps.executeUpdate();
            UpdateResult result = new UpdateResult();
            result.setOk(effectedRows > 0);
            result.setEffectedRows(effectedRows);

            resultText = result.toString();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("SQL update error.", e);
        } finally {
            String _resultText = resultText;
            String traceId = MDC.get(BoxLogger.TRACE_KEY);
            String bizAction = MDC.get(BoxLogger.BIZ_ACTION);

            CompletableFuture.runAsync(() -> PrettyLogger.printLog("Update", traceId, bizAction, sql, params, PrintRealSql.printRealSql(sql, params), this, _resultText, true));
        }
    }

    /**
     * 对 PreparedStatement 设置值
     *
     * @param ps     PreparedStatement
     * @param params 插入到 SQL 中的参数，可单个可多个可不填
     * @throws SQLException 异常
     */
    private static void setParam2Ps(PreparedStatement ps, Object... params) throws SQLException {
        if ((params == null || params.length == 0))
            return;

        for (int i = 0; i < params.length; i++) {
            Object ele = params[i];

            if (ele instanceof Map)
                ele = com.ajaxjs.util.JsonUtil.toJson(ele); // Map to JSON
            if (ele instanceof List)
                throw new UnsupportedOperationException("暂不支持 List 类型参数。如果你入參用於 IN (?)，請直接拼接 SQL 語句而不是使用 PreparedStatement。這是系統的限制，無法支持 List");

            if (ele instanceof InputStream) {
                InputStream in = (InputStream) ele;
                ps.setBinaryStream(i + 1, in);
            } else
                ps.setObject(i + 1, ele);
//            ps.setBinaryStream(i + 1, );        }
        }
    }

    /**
     * 物理删除
     *
     * @param id 实体 ID
     * @return 是否成功
     */
    @Deprecated
    public UpdateResult delete(String tableName, String idField, Serializable id) {
        String sql = "DELETE FROM " + tableName + " WHERE " + idField + " = ?";
        setSql(sql);
        setParams(new Object[]{id});

        return update();
    }

    public static String toSqlValues(List<java.lang.String> ele) {
        List<String> result = new ArrayList<>(ele.size());
        ele.forEach(el -> result.add("'" + el + "'"));

        return String.join(",", result);
    }

}
