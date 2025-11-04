package com.ajaxjs.sqlman_v2.crud;

import com.ajaxjs.sqlman.Sql;
import com.ajaxjs.sqlman.annotation.ResultSetProcessor;
import com.ajaxjs.sqlman.util.PrintRealSql;
import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.crud.page.PageQuery;
import com.ajaxjs.sqlman_v2.crud.page.PageResult;
import com.ajaxjs.sqlman_v2.util.PrettyLogger;
import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.ConvertBasicValue;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
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

    /**
     * Do the pagination by start/limit.
     *
     * @param start The start position
     * @param limit The limit of records, equals to page size
     * @return The page result in Java Bean format.
     */
    public <T> PageResult<T> pageByStartLimit(Integer start, Integer limit, Class<T> beanClz) {
        return PageQuery.page(this, beanClz, start, limit);
    }

    /**
     * Do the pagination by start/limit.
     *
     * @param start The start position
     * @param limit The limit of records, equals to page size
     * @return The page result in Map format.
     */
    public PageResult<Map<String, Object>> pageByStartLimit(Integer start, Integer limit) {
        return PageQuery.page(this, null, start, limit);
    }

    /**
     * Do the pagination by start/limit. The parameters are from the request automatically.
     *
     * @param req     The request object.
     * @param beanClz The type of result object, null for Map.
     * @return The page result.
     */
    public <T> PageResult<T> pageByStartLimit(HttpServletRequest req, Class<T> beanClz) {
        int start = PageQuery.getParameter(req, PageQuery.START, 0);
        int limit = PageQuery.getParameter(req, PageQuery.PAGE_SIZE, PageQuery.DEFAULT_PAGE_SIZE);

        return pageByStartLimit(start, limit, beanClz);
    }

    /**
     * Do the pagination by start/limit. The parameters are from the request automatically.
     *
     * @param req The request object.
     * @return The page result.
     */
    public PageResult<Map<String, Object>> pageByStartLimit(HttpServletRequest req) {
        return pageByStartLimit(req, null);
    }

    /**
     * Do the pagination by pageNo/pageSize.
     *
     * @param pageNo   The number of pages.
     * @param pageSize The size of every page.
     * @return The page result in Java Bean format.
     */
    public <T> PageResult<T> pageByPageNo(Integer pageNo, Integer pageSize, Class<T> beanClz) {
        return pageByStartLimit(PageQuery.pageNo2start(pageNo, pageSize), pageSize, beanClz);
    }

    /**
     * Do the pagination by pageNo/pageSize.
     *
     * @param pageNo   The number of pages.
     * @param pageSize The size of every page.
     * @return The page result in Map format.
     */
    public PageResult<Map<String, Object>> pageByPageNo(Integer pageNo, Integer pageSize) {
        return pageByStartLimit(PageQuery.pageNo2start(pageNo, pageSize), pageSize);
    }

    /**
     * Do the pagination by pageNo/pageSize. The parameters are from the request automatically.
     *
     * @param req     The request object.
     * @param beanClz The type of result object, null for Map.
     * @return The page result.
     */
    public <T> PageResult<T> pageByPageNo(HttpServletRequest req, Class<T> beanClz) {
        int pageNo = PageQuery.getParameter(req, PageQuery.PAGE_NO, 1);
        int pageSize = PageQuery.getParameter(req, PageQuery.PAGE_SIZE, PageQuery.DEFAULT_PAGE_SIZE);

        return pageByPageNo(pageNo, pageSize, beanClz);
    }

    /**
     * Do the pagination by pageNo/pageSize. The parameters are from the request automatically.
     *
     * @param req The request object.
     * @return The page result.
     */
    public PageResult<Map<String, Object>> pageByPageNo(HttpServletRequest req) {
        return pageByPageNo(req, null);
    }
}
