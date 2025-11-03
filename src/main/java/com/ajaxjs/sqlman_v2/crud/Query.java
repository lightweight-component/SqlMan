package com.ajaxjs.sqlman_v2.crud;

import com.ajaxjs.sqlman.Sql;
import com.ajaxjs.sqlman.annotation.ResultSetProcessor;
import com.ajaxjs.sqlman.util.PrintRealSql;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.page.PageQuery;
import com.ajaxjs.sqlman_v2.page.PageResult;
import com.ajaxjs.sqlman_v2.util.PrettyLogger;
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
import java.util.concurrent.CompletableFuture;

/**
 * Do query on the database.
 */
@Slf4j
public class Query extends BaseAction {
    /**
     * Do the query by an action.
     *
     * @param action an action object with input Sql, data and config.
     */
    public Query(Action action) {
        super(action);
    }

    /**
     * Query by any SQL.
     * This is the low-level API.
     *
     * @param processor How to transform the result set to a target entity.
     * @param <T>       Map or bean.
     * @return The result object.
     */
    protected <T> T query(ResultSetProcessor<T> processor) {
        startTime = System.currentTimeMillis();
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

            CompletableFuture.runAsync(() -> PrettyLogger.printLog("Query", traceId, bizAction,
                    action.getSql(), action.getParams(), PrintRealSql.printRealSql(action.getSql(), action.getParams()),
                    this, _resultText, true));
        }
    }

    public <T> T oneValue(Class<T> clz) {
        Map<String, Object> map = one();

        if (map != null) {
            for (String key : map.keySet()) {// 有且只有一个记录
                Object obj = map.get(key);

                return obj == null ? null : ConvertBasicValue.basicCast(obj, clz);
            }
        }

        return null;
    }

    public Map<String, Object> one() {
        return query(Sql::getResultMap);
    }

    public <T> T one(Class<T> beanClz) {
        return query(getResultBean(beanClz));
    }

    public List<Map<String, Object>> list() {
        return query(rs -> forEachRs(rs, Sql::getResultMap));
    }

    public <T> List<T> list(Class<T> beanClz) {
        return query(rs -> forEachRs(rs, getResultBean(beanClz)));
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

    public <T> PageResult<T> page() {
        return page(null, null, null);
    }

    public <T> PageResult<T> page(Integer start, Integer limit) {
        return page(null, start, limit);
    }

    public <T> PageResult<T> page(Class<T> beanClz) {
        return page(beanClz, null, null);
    }


    // pag
    public static boolean isPageByPageNo;

//    public <T> PageResult<T> page(Class<T> beanClz) {
//        return isPageByPageNo ? pageByPageNo(beanClz, null, null) : pageByStartLimit(beanClz, null, null);
//    }
//
//    public <T> PageResult<T> page(Class<T> beanClz, Integer int1, Integer int2) {
//        return isPageByPageNo ? pageByPageNo(beanClz, int1, int2) : pageByStartLimit(beanClz, int1, int2);
//    }

    public <T> PageResult<T> pageByStartLimit(Class<T> beanClz, Integer start, Integer limit) {
        return PageQuery.page(this, beanClz, start, limit);
    }

    public <T> PageResult<T> pageByPageNo(Class<T> beanClz, Integer pageNo, Integer pageSize) {
        return pageByStartLimit(beanClz, pageNo2start(pageNo, pageSize), pageSize);
    }

    /**
     * 将页码和每页数量转换为起始位置
     * pageSize 转换为 MySQL 的 start 分页
     *
     * @param pageNo 页码
     * @param limit  每页数量
     * @return 起始位置
     */
    public static int pageNo2start(int pageNo, int limit) {
        int start = (pageNo - 1) * limit;

        return Math.max(start, 0);
    }
}
