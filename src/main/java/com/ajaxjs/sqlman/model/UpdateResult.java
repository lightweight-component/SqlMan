package com.ajaxjs.sqlman.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Contains the outcome of an update or delete operation.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateResult extends Result {
    /**
     * 操作成功后 影响的行数
     */
    private int effectedRows;
}
