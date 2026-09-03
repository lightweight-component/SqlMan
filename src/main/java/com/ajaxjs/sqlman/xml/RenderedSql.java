package com.ajaxjs.sqlman.xml;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A rendered SQL template and the corresponding parameter values.
 */
@Getter
public final class RenderedSql {
    private final String sql;
    private final Map<String, Object> params;

    public RenderedSql(String sql, Map<String, Object> params) {
        this.sql = sql;
        this.params = Collections.unmodifiableMap(new HashMap<>(params));
    }
}