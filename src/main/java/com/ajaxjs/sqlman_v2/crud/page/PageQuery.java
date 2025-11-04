package com.ajaxjs.sqlman_v2.crud.page;

import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.crud.Query;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Pagination utilities.
 */
public class PageQuery {
    /**
     * Do the pagination.
     *
     * @param query   Query object
     * @param beanClz The type of result object, null for Map
     * @param start   The start position
     * @param limit   The limit of records, equals to page size
     * @param <T>     The type of result object
     * @return The result object
     */
    @SuppressWarnings({"unchecked"})
    public static <T> PageResult<T> page(Query query, Class<T> beanClz, Integer start, Integer limit) {
        Action action = query.getAction();
        PageControl pageControl = new PageControl(action.getDatabaseVendor(), action.getSql(), start, limit);
        pageControl.getCount();
        action.setSql(pageControl.getCountSql());
        Integer total = query.oneValue(Integer.class);

        PageResult<T> result = new PageResult<>();
        result.setStart(start);
        result.setPageSize(limit);

        if (total == null || total <= 0) {
            result.setTotalCount(0);
            result.setZero(true);
        } else {
            action.setSql(pageControl.getPagedSql());
            // 如果 beanCls 为 null，则将查询结果作为 Map 列表返回 否则将查询结果转换为指定实体类的列表
            List<T> list = beanClz == null ? (List<T>) query.list() : query.list(beanClz);

            if (list != null) {
                result.setTotalCount(total);
                result.setList(list);
                setParams(result, total, start);// might be not meaningful,
                // you can delete this line if higher performance is needed
            } else
                throw new UnsupportedOperationException("Impossible to right here.");
        }

        return result;
    }

    /**
     * Calculate the parameters of pagination related, like total page.
     *
     * @param result     The result object
     * @param totalCount The total count of records
     * @param start      The start position
     */
    public static void setParams(PageResult<?> result, int totalCount, int start) {
        int pageSize = result.getPageSize();
        int totalPage = totalCount / pageSize, remainder = totalCount % pageSize;// 余数

        totalPage = (remainder == 0 ? totalPage : totalPage + 1);
        result.setTotalPage(totalPage);

        int currentPage = (start / pageSize) + 1;
        result.setCurrentPage(currentPage);
    }

//    void fastPage() {
//        // 分页时高效的总页数计算 我们一般分页是这样来计算页码的：
//        int row = 200; //记录总数
//        int page = 5;//每页数量 int
//        int count = row % 5 == 0 ? row / page : row / page + 1;
//        //上面这种是用的最多的! 那么下面我们来一种最简单的，不用任何判断！ 看代码：
//
//        row = 21;
//        int pageCount = 5;
//        int sum = (row - 1) / pageCount + 1;//这样就计算好了页码数量，逢1进1
//    }

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

    public static final String[] START = new String[]{"start", "offset"};

    public static final String[] PAGE_NO = new String[]{"pageNo", "page"};

    public static final String[] PAGE_SIZE = new String[]{"pageSize", "rows", "limit"};

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 12;

    /**
     * 根据 HttpServletRequest 和字符串数组返回一个整数。
     *
     * @param req   请求对象
     * @param maybe 字符串数组，包含可能的参数名
     * @return 返回一个整数，如果参数存在且为整数，则返回对应的整数值；否则返回 null
     */
    public static Integer getParameter(HttpServletRequest req, String[] maybe, int defaultValue) {
        for (String m : maybe) {
            String parameter = req.getParameter(m);

            if (parameter != null)
                return Integer.parseInt(parameter);
        }

        return defaultValue;
    }

    /**
     * Detect which type to page by request parameters.
     *
     * @param req The request object
     * @return true = using start/limit ; false = using pageNo/pageSize
     */
    public static boolean autoDetectPageWay(HttpServletRequest req) {
        Integer start = getParameter(req, START, 0);

        if (start != null)
            return true;
        else {
            int pageNo = getParameter(req, PAGE_NO, 0);

            if (pageNo != 0)
                return false;
            else
                throw new UnsupportedOperationException("can't detect what type is this pagination");
        }
    }
}
