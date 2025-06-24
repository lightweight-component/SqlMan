package com.ajaxjs.data.crud;

import lombok.Data;

/**
 * 数据服务的基础配置字段
 */
@Data
public abstract class FastCRUD_Config {
    /**
     * 命名空间，标识
     */
    private String namespace;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 具体表里面的各个字段是什么名称，这里指定
     */
    private TableModel tableModel;

    /**
     * 查询列表的时候，是否自动加上按照日期排序
     */
    private boolean listOrderByDate = true;

    /**
     * 实体类引用名称
     */
    private String clzName;

    /**
     * 单条 SQL 命令
     */
    private String sql;

    /**
     * 查询详情的 SQL（可选的）
     */
    private String infoSql;

    /**
     * 查询列表的 SQL（可选的）
     */
    private String listSql;

    /**
     * 创建实体的 SQL（可选的）
     */
    private String createSql;

    /**
     * 修改实体的 SQL（可选的）
     */
    private String updateSql;

    /**
     * 删除实体的 SQL（可选的）
     */
    private String deleteSql;

    /**
     * 是否加入租户数据隔离
     */
    private boolean isTenantIsolation;

    /**
     * 当前用户的约束
     */
    private boolean isCurrentUserOnly;

    /**
     * 1=自增；2=雪花；3=UUID
     */
    private Integer idType;
}