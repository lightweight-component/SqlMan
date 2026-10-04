package com.ajaxjs.sqlman.sqltemplate.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutable state owned by one DOM template rendering operation.
 */
@Getter
@Setter
public final class RenderState {
    /**
     * Request-local values, including generated bindings for loop variables.
     */
    private final Map<String, Object> params;

    /**
     * Sequence used to give each generated loop binding a unique name.
     */
    private int nextBinding;

    /**
     * Creates a state object with a private copy of caller parameters.
     *
     * @param params caller values, which may be {@code null}
     */
    public RenderState(Map<String, Object> params) {
        this.params = new HashMap<>();

        if (params != null)
            this.params.putAll(params);
    }
}
