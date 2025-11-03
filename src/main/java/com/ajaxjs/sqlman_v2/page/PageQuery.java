package com.ajaxjs.sqlman_v2.page;

import com.ajaxjs.sqlman_v2.Action;
import com.ajaxjs.sqlman_v2.crud.Query;

import java.util.List;

public class PageQuery {
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
            List<T> list;
            action.setSql(pageControl.getPagedSql());

            if (beanClz == null) // 如果 beanCls 为 null，则将查询结果作为 Map 列表返回 否则将查询结果转换为指定实体类的列表
                list = (List<T>) query.list();
            else
                list = query.list(beanClz);

            if (list != null) {
                result.setTotalCount(total);
                result.setList(list);
                setParams(result, total, start);
            }
        }

        return result;
    }

    /**
     * 分页的逻辑运算
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
}
