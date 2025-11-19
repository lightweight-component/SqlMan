package com.ajaxjs.sqlman.sqlgenerator;

import lombok.Data;

import java.util.List;

/**
 * Config for joining tables
 */
@Data
public class TableJoin {
    /**
     * 要 JOIN 的表名
     */
    String joinTableName;

    /**
     * 主表用于连接的列名 (不含别名)
     */
    String mainTableJoinColumn;

    /**
     * 被连接表用于连接的列名 (不含别名)
     */
    String joinedTableJoinColumn;

    /**
     * 要从被连接表选取的字段列表 (不含别名)
     */
    List<String> fieldsToSelectFromJoinedTable;
}
