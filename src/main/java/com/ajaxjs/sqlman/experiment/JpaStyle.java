package com.ajaxjs.sqlman.experiment;

import java.io.Serializable;

/**
 * Experimental JPA-style contract for entity operations.
 *
 * @param <T>  the entity type.
 * @param <ID> the identifier type.
 */
public abstract interface JpaStyle<T, ID extends Serializable> {
    /**
     * 查询单笔记录，以 Java Bean 格式返回
     *
     * @param id 记录标识。
     * @return 查询单笔记录，可以是 Bean 或者 Map，如果为 null 表示没数据
     */
    T info(ID id);
}
