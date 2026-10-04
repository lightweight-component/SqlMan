package com.ajaxjs.sqlman.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Compiled JDBC SQL and ordered bind values.
 */
@Getter
@RequiredArgsConstructor
public class PreparedSql {
    private final String sql;

    private final Object[] params;
}
