package com.ajaxjs.sqlman_v2.model;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    /**
     * 总记录数
     */
    private int totalCount;

    /**
     * 从第几笔记录开始
     */
    private int start;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 总页数
     */
    private int totalPage;

    /**
     * 当前第几页
     */
    private int currentPage;

    /**
     * 是否没有数据，就是查询了之后，一条记录符合都没有
     */
    private boolean isZero;

    /**
     * The paged data.
     */
    private List<T> list;
}
