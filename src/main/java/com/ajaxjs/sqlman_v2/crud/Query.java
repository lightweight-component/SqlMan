package com.ajaxjs.sqlman_v2.crud;

import com.ajaxjs.sqlman.Sql;
import com.ajaxjs.sqlman.annotation.ResultSetProcessor;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.BaseAction;
import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.ConvertBasicValue;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Do CRUD action on the database.
 */
@Slf4j
public class Query extends BaseAction {
    public Query(Action action) {
        super(action);
    }

    protected <T> T query(ResultSetProcessor<T> processor) {
        String resultText = null;

        try (PreparedStatement ps = action.getConn().prepareStatement(action.getSql(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            setParam2Ps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    T _result = processor.process(rs);
                    resultText = _result.toString();

                    return _result;
                } else {
                    resultText = "[Empty result]";
                    log.info("Queried SQL：{}, data not found.", action.getSql());

                    return null;
                }
            }
        } catch (SQLException e) {
            log.warn("SQL Error when doing read action.", e);
            throw new RuntimeException("SQL Error when doing read action.", e);
        } finally {
            String _resultText = resultText;
            String traceId = MDC.get(BoxLogger.TRACE_KEY);
            String bizAction = MDC.get(BoxLogger.BIZ_ACTION);

//            CompletableFuture.runAsync(() -> PrettyLogger.printLog("Query", traceId, bizAction, sql, params, PrintRealSql.printRealSql(sql, params), this, _resultText, true));
        }
    }

    public Map<String, Object> query() {
        return query(Sql::getResultMap);
    }

    public <T> T query(Class<T> beanClz) {
        return query(getResultBean(beanClz));
    }

    public List<Map<String, Object>> queryList() {
        return query(rs -> forEachRs(rs, Sql::getResultMap));
    }

    public <T> List<T> queryList(Class<T> beanClz) {
        return query(rs -> forEachRs(rs, getResultBean(beanClz)));
    }

    public <T> T queryOne(Class<T> clz) {
        Map<String, Object> map = query();

        if (map != null) {
            for (String key : map.keySet()) {// 有且只有一个记录
                Object obj = map.get(key);

                return obj == null ? null : ConvertBasicValue.basicCast(obj, clz);
            }
        }

        return null;
    }

    /**
     * ResultSet 迭代器
     *
     * @param rs        结果集合
     * @param processor 单行处理器
     * @return 多行记录列表集合
     * @throws SQLException 异常
     */
    static <T> List<T> forEachRs(ResultSet rs, ResultSetProcessor<T> processor) throws SQLException {
        List<T> list = new ArrayList<>();

        do {
            T d = processor.process(rs);
            list.add(d);
        } while (rs.next());

//        return list.size() > 0 ? list : null; // 找不到记录返回 null，不返回空的 list
        return list; // 找不到记录返回 null，不返回空的 list
    }
}
