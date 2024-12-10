package com.ajaxjs.sqlman.model;

import com.ajaxjs.sqlman.sql.JdbcCommand;
import lombok.Data;

@Data
public class TableInfo {
    private String tableName;

    private JdbcCommand crud;

    private IdField idField;
}
