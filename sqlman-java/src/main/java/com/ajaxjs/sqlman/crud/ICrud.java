package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.model.Create;
import com.ajaxjs.sqlman.sql.DAO;

import java.io.Serializable;

/**
 * 通用实体快速的 CRUD。这个服务无须 DataService
 * 提供默认 CRUD 的逻辑，包含常见情况的 SQL。
 */
public interface ICrud extends DAO {
    /**
     * 查询单笔记录，以 Java Bean 格式返回
     *
     * @return 查询单笔记录，可以是 Bean 或者 Map，如果为 null 表示没数据
     */
    <T extends Serializable> Crud info(T id);

    DAO input(Object javaBean);

    <T extends Serializable> Create<T> create();
}
