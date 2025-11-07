package com.ajaxjs.sqlman.model;

import lombok.Data;

/**
 * The abstract class of a result.
 */
@Data
public abstract class Result {
    /**
     * if the result is ok.
     */
    private boolean isOk;

    /**
     * The message of the result.
     */
    private String message;
}
