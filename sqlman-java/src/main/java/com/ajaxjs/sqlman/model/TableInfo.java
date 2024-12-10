package com.ajaxjs.sqlman.model;

import com.ajaxjs.sqlman.JdbcCRUD;
import lombok.Data;

@Data
public class TableInfo {
    private String tableName;

    private JdbcCRUD crud;

    private IdField idField;
}
