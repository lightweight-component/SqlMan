package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.meta.DbMetaInfoCreate;
import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.sqlgenerator.Entity2WriteSql;
import com.ajaxjs.sqlman.util.PrintRealSql;
import com.ajaxjs.util.log.Trace;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

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
    private <T extends Serializable> CreateResult<T> create(boolean isAutoIns, Class<T> idType) {
        startTime = System.currentTimeMillis();
        String resultText = null;
        String sql = action.getSql();

        try (PreparedStatement ps = isAutoIns
                ? action.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : action.getConn().prepareStatement(sql)) {

            setParam2Ps(ps);
            int effectRows = ps.executeUpdate();

            CreateResult<T> result = new CreateResult<>();
            if (effectRows > 0) {// 插入成功
                result.setOk(true);

                if (isAutoIns) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {// 当保存之后会自动获得数据库返回的主键
                        if (rs.next()) {
                            Object newlyId = rs.getObject(1);

                            if (idType != null)
                                result.setNewlyId(convertGeneratedKey(newlyId, idType));
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
            } else
                result.setOk(false);

            return result;
        } catch (SQLException e) {
            log.warn("SQL insert error.", e);
            throw new RuntimeException("SQL insert error.", e);
        } finally {
            try { // avoid this exception to effect main job
                String duration = (System.currentTimeMillis() - startTime) + "ms";
                String _resultText = resultText;
                String traceId = MDC.get(Trace.TRACE_KEY);
                String bizAction = MDC.get(Trace.BIZ_ACTION);

                PrintRealSql.printLog("Create", traceId, bizAction,
                        action.getSql(), action.getParams(), PrintRealSql.printRealSql(action.getSql(), action.getParams()), duration, _resultText);
            } catch (Exception e) {
                log.warn("There's error when logging.", e);
            }
        }
    }

    /**
     * Convert a JDBC generated key to the type requested by the caller.
     * Exact numeric conversions are used so that overflow or fractional values
     * cannot be silently truncated.
     */
    static <T extends Serializable> T convertGeneratedKey(Object value, Class<T> targetType) {
        if (value == null)
            return null;
        if (targetType == null)
            throw new IllegalArgumentException("Generated key target type cannot be null.");
        if (targetType.isInstance(value))
            return targetType.cast(value);
        if (targetType == String.class)
            return targetType.cast(String.valueOf(value));
        if (targetType == Serializable.class) {
            if (value instanceof Serializable)
                return targetType.cast(value);

            throw conversionError(value, targetType, null);
        }

        try {
            BigDecimal number = toBigDecimal(value);
            Object converted;

            if (targetType == Integer.class)
                converted = number.intValueExact();
            else if (targetType == Long.class)
                converted = number.longValueExact();
            else if (targetType == Short.class)
                converted = number.shortValueExact();
            else if (targetType == Byte.class)
                converted = number.byteValueExact();
            else if (targetType == BigInteger.class)
                converted = number.toBigIntegerExact();
            else if (targetType == BigDecimal.class)
                converted = number;
            else
                throw conversionError(value, targetType, null);

            return targetType.cast(converted);
        } catch (ArithmeticException | NumberFormatException e) {
            throw conversionError(value, targetType, e);
        }
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal)
            return (BigDecimal) value;
        if (value instanceof BigInteger)
            return new BigDecimal((BigInteger) value);
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)
            return BigDecimal.valueOf(((Number) value).longValue());
        if (value instanceof Number || value instanceof CharSequence)
            return new BigDecimal(value.toString());

        throw conversionError(value, BigDecimal.class, null);
    }

    private static IllegalArgumentException conversionError(Object value, Class<?> targetType, Exception cause) {
        String message = "Cannot convert generated key [" + value + "] of type "
                + value.getClass().getName() + " to " + targetType.getName() + " without data loss.";

        return cause == null ? new IllegalArgumentException(message) : new IllegalArgumentException(message, cause);
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
