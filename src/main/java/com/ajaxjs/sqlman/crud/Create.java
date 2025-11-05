package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.meta.DbMetaInfoCreate;
import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.sqlgenerator.Entity2WriteSql;
import com.ajaxjs.sqlman.util.PrettyLogger;
import com.ajaxjs.sqlman.util.PrintRealSql;
import com.ajaxjs.util.BoxLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Create operation.
 */
@Slf4j
public class Create extends BaseAction {
    /**
     * Do the create operation by an action.
     *
     * @param action an action object with input Sql, data and config.
     */
    public Create(Action action) {
        super(action);
    }

    /**
     * Create by SQL.
     * This is the low-level API.
     *
     * @param isAutoIns Is this auto increment id?
     * @param idType    The type of newly id. If you provided, it'll avoid a type case.
     * @param <T>       The type of id. It can be Long, Integer, String, or their type in common: Serializable
     * @return The result object.
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> CreateResult<T> create(boolean isAutoIns, Class<T> idType) {
        startTime = System.currentTimeMillis();
        String resultText = null;
        String sql = action.getSql();

        try (PreparedStatement ps = isAutoIns
                ? action.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : action.getConn().prepareStatement(sql)) {

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
                    } else
                        log.warn("Nothing returns from newly create.");
                }

                resultText = result.toString();
                return result;
            }
        } catch (SQLException e) {
            log.warn("SQL insert error.", e);
            throw new RuntimeException("SQL insert error.", e);
        } finally {
            String _resultText = resultText;
            String traceId = MDC.get(BoxLogger.TRACE_KEY);
            String bizAction = MDC.get(BoxLogger.BIZ_ACTION);

            CompletableFuture.runAsync(() -> PrettyLogger.printLog("Create", traceId, bizAction,
                    action.getSql(), action.getParams(), PrintRealSql.printRealSql(action.getSql(), action.getParams()), this, _resultText, true));
        }

        return null;
    }

    public static final Long INSERT_OK_LONG = -1L;
    public static final Integer INSERT_OK_INT = -1;
    public static final String INSERT_OK_STR = "INSERT_OK";

    /**
     * Execute the creation
     *
     * @param isAutoIns Is this auto increment id?
     * @param idType    The type of newly id. If you provided, it'll avoid a type case.
     * @param <T>       The type of id. It can be Long, Integer, String, or their type in common: Serializable
     * @return The result object.
     */
    public <T extends Serializable> CreateResult<T> execute(boolean isAutoIns, Class<T> idType) {
        Map<String, Object> entityMap = action.getEntityMap();
        Object entityBean = action.getEntityBean();
        String tableName = action.getTableName();

        if (entityMap != null || entityBean != null) {
            Entity2WriteSql generator;

            if (entityMap != null)
                generator = new Entity2WriteSql(entityMap);
            else
                generator = new Entity2WriteSql(entityBean);

            if (entityBean != null && tableName == null)
                tableName = new DbMetaInfoCreate<T>(entityBean).getTableNameByAnnotation();

            generator.setTableName(tableName);
            generator.getInsertSql();

            action.setSql(generator.getSql());
            action.setParams(generator.getParams());
        }

        return create(isAutoIns, idType);
    }

    public CreateResult<Serializable> execute(boolean isAutoIns) {
        return execute(isAutoIns, Serializable.class);
    }
}
