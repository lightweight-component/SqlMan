package com.ajaxjs.sqlman.experiment;

import java.io.Serializable;

public abstract interface JpaStyle<T, ID extends Serializable> {
    /**
     * 查询单笔记录，以 Java Bean 格式返回
     *
     * @return 查询单笔记录，可以是 Bean 或者 Map，如果为 null 表示没数据
     */
    T info(ID id);
}
