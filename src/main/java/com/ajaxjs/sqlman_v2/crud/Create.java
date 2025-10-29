package com.ajaxjs.sqlman_v2.crud;

import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.util.PrettyLogger;
import com.ajaxjs.sqlman.util.PrintRealSql;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.BaseAction;
import com.ajaxjs.util.BoxLogger;
import org.slf4j.MDC;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class Create extends BaseAction {
    public Create(Action action) {
        super(action);
    }

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
        String resultText = null;

        try (PreparedStatement ps = isAutoIns
                ? action.getConn().prepareStatement(action.getSql(), Statement.RETURN_GENERATED_KEYS)
                : action.getConn().prepareStatement(action.getSql())) {

            setParam2Ps(ps);
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

//            CompletableFuture.runAsync(() -> PrettyLogger.printLog("Create", traceId, bizAction, sql, params, PrintRealSql.printRealSql(sql, params), this, _resultText, true));
        }

        return null;
    }

    public static final Long INSERT_OK_LONG = -1L;
    public static final Integer INSERT_OK_INT = -1;
    public static final String INSERT_OK_STR = "INSERT_OK";

}
