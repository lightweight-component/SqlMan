package com.ajaxjs.sqlman.xml;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public final class RenderState {
    private final Map<String, Object> params;
    private int nextBinding;

    public RenderState(Map<String, Object> params) {
        this.params = new HashMap<>();

        if (params != null)
            this.params.putAll(params);
    }
}
