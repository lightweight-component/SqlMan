package com.ajaxjs.sqlman.crud.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 具体表里面的各个字段是什么名称，这里指定
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TableModel extends IdField {
    /**
     * 表名
     */
    private String tableName;

    /**
     * 名称 的字段名称
     */
    private String nameField = "name";

    /**
     * 创建者的字段名称
     */
    private String creatorIdField = "creator_id";

    /**
     * 修改者的字段名称
     */
    private String updaterIdField = "updater_id";

    /**
     * 创建日期的字段名称
     */
    private String createDateField = "create_date";

    /**
     * 修改日期的字段名称
     */
    private String updateDateField = "update_date";

    /**
     * 全局唯一 id 的字段名称
     */
    private String uidField = "uid";

    /**
     * 状态的字段名称
     */
    private String stateField = "stat";

    /**
     * 是否有逻辑删除标记
     */
    private boolean hasIsDeleted;

    /**
     * 删除字段名称
     */
    private String delField = "is_deleted";
}
