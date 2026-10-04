package com.ajaxjs.sqlman.sqltemplate.model;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A rendered SQL template and the corresponding parameter values.
 */
@Getter
public final class RenderedSql {
    /**
     * Rendered SQL text before named placeholders are converted to JDBC bindings.
     */
    private final String sql;

    /**
     * Immutable snapshot of values used while rendering.
     */
    private final Map<String, Object> params;

    /**
     * Creates a rendered SQL result and defensively snapshots its parameters.
     *
     * @param sql    rendered SQL text
     * @param params parameter values used by the template
     */
    public RenderedSql(String sql, Map<String, Object> params) {
        this.sql = sql;
        this.params = Collections.unmodifiableMap(new HashMap<>(params));
    }
}
